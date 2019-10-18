package com.windhoverlabs.ide.config;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.forms.widgets.FormToolkit;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class ConfigTreeViewer extends TreeViewer implements ISelectionChangedListener, ICfsConfigChangeListener {

	private final FormToolkit toolkit = new FormToolkit(Display.getCurrent());
	private CfsConfig cfsConfig;
	NamedObject rootObject;
	String moduleName;
	String jsonPath;
	NamedObject currentObject;
	ModuleConfigEditor parentModuleConfigEditor;
	private ISelection selection;
	
	/**
	 * @wbp.parser.entryPoint
	 */
	public ConfigTreeViewer(Composite parent, int style, String jsonPath, CfsConfig cfsConfig, String moduleName, ModuleConfigEditor parentModuleConfigEditor) {
		super(parent, style);
		this.cfsConfig = cfsConfig;
		this.jsonPath = jsonPath;
		this.moduleName = moduleName;
		this.parentModuleConfigEditor = parentModuleConfigEditor;
		
		cfsConfig.addChangeListener(this);

		Tree tree = getTree();
		
		//tree.setBackground(SWTResourceManager.getColor(SWT.COLOR_INFO_FOREGROUND));
		toolkit.paintBordersFor(tree);
	
		FontData[] boldFontData = getModifiedFontData(tree.getFont().getFontData(), SWT.BOLD);
		Font boldFont = new Font(Display.getCurrent(), boldFontData);
		
		setLabelProvider(new JsonLabelProvider(boldFont));
		setContentProvider(new JsonContentProvider(cfsConfig));

		setUpRootObject();
		// Let's create columns for feauture extensibility. For now, just one column to display the names of the jsonObjects or primitives.
		createLabelColumn(tree, "Label");

		setInput(rootObject);
		
		addSelectionChangedListener(this);
		createMenu(ConfigTreeViewer.this);
		
		// Add this as a Listener to the CfsConfig Subject.
	
	}
	// Something has changed in the subject, this listener will do something upon receiving update
	@Override
	public void cfsConfigUpdated() {
		refreshTreeViewer(currentObject, cfsConfig);
	}
	
	
	private void setUpRootObject() {
		rootObject = new NamedObject();
		rootObject.setName("ROOT");
		JsonObject partial = new JsonObject();
		partial.add("modules", new JsonObject());
		partial = partial.get("modules").getAsJsonObject();
		JsonObject module = cfsConfig.getJsonElementByPath(jsonPath).getAsJsonObject();
		partial.add(moduleName, module);
		rootObject.setObject(partial);
		rootObject.setPath("modules");
	}
	
	private void createLabelColumn(Tree currentTree, String columnLabel) {
		TreeColumn column = new TreeColumn(currentTree, SWT.NONE);
		column.setWidth(150);
		column.setText(columnLabel);
		currentTree.setHeaderVisible(true);
		currentTree.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
	}
	
	@Override
	public void selectionChanged(SelectionChangedEvent event) {
		ISelection thisSelection = (ISelection) event.getSelection();
		IStructuredSelection thisStructuredSelection = (IStructuredSelection) thisSelection;
		Object selectedNode = thisStructuredSelection.getFirstElement();

		if (selectedNode != null) {
			NamedObject selectedNamedObject = (NamedObject) selectedNode; 
			JsonElement selectedElement = (JsonElement) selectedNamedObject.getObject();
			if (selectedElement.isJsonObject()) {
				parentModuleConfigEditor.goUpdate(selectedNamedObject, selectedElement);
			}
		}
	}
	
	public void refreshTreeViewer(NamedObject namedObject, CfsConfig cfsConfig) {
		this.cfsConfig = cfsConfig;

		final Object[] elements = getExpandedElements();
		final TreePath[] treePaths = getExpandedTreePaths();
		selection = getSelection();

		setUpRootObject();
		setInput(rootObject);
		refresh();
		setExpandedElements(elements);
		setExpandedTreePaths(treePaths);
		if (selection != null) {
			setSelection(selection);
		}
	}
	
	private static FontData[] getModifiedFontData(FontData[] originalData, int additionalStyle) {
		FontData[] styleData = new FontData[originalData.length];
		for (int i = 0; i < styleData.length; i++) {
			FontData base = originalData[i];
			styleData[i] = new FontData(base.getName(), base.getHeight(), base.getStyle() | additionalStyle);
		}
		return styleData;
	}
	
	public void createMenu(Viewer viewer) {
		final Action addObject = TreeContextMenuActions.createActionAddObject(ConfigTreeViewer.this, cfsConfig);
		final Action addArray = TreeContextMenuActions.createActionAddArray(ConfigTreeViewer.this, cfsConfig);
		final Action delete = TreeContextMenuActions.createActionDelete(ConfigTreeViewer.this, cfsConfig);
		final Action unoverride = TreeContextMenuActions.createActionUnoverride(ConfigTreeViewer.this, cfsConfig);

		
		MenuManager menumgr = new MenuManager();
		Menu men = menumgr.createContextMenu(viewer.getControl());
		
		menumgr.addMenuListener(new IMenuListener() {
			@Override
			public void menuAboutToShow(IMenuManager mgr) {
				if (getSelection() instanceof IStructuredSelection) {
					IStructuredSelection selection = (IStructuredSelection) getSelection();
					Object selectedNode = selection.getFirstElement();
					if (selectedNode != null) {
						NamedObject namedObject = (NamedObject) selectedNode; 
						JsonElement selectedElem = (JsonElement) namedObject.getObject();
						if (selectedElem.isJsonObject() || selectedElem.isJsonArray()) {
							addObject.setText("Add Object");
							menumgr.add(addObject);
							addArray.setText("Add Array");
							menumgr.add(addArray);
							delete.setText("Delete");
							menumgr.add(delete);
							if (namedObject.getOverridden()) {
								unoverride.setText("Unoverride");
								menumgr.add(unoverride);
							}
						}
				}
				ConfigTreeViewer.this.fillContextMenu(mgr);
			}
		}});
		menumgr.setRemoveAllWhenShown(true);

		viewer.getControl().setMenu(men);
		ConfigTreeViewer.this.refresh();
	}
	
	protected void fillContextMenu(IMenuManager contextMenu) {
		contextMenu.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
	}
}
