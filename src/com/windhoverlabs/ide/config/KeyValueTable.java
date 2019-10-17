package com.windhoverlabs.ide.config;

import java.util.ArrayList;
import java.util.Map;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IWorkbenchActionConstants;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class KeyValueTable extends Composite implements ICfsConfigChangeListener {

	public TableViewer tableViewer;
	private NamedObject namedObj;
	private CfsConfig cfsConfig;
	KeyValueContentProvider keyValueContentProvider;
	
	public KeyValueTable(Composite parent, int style, NamedObject namedObj, CfsConfig cfsConfig) {
		super(parent, style);
		this.namedObj = namedObj;
		this.cfsConfig = cfsConfig;
		
		// Register this Table as a Listener to the CfsConfig Subject.
		cfsConfig.addChangeListener(this);
		
		createTable(namedObj, parent);
		setLayout(new FillLayout(SWT.HORIZONTAL));
		createMenu(tableViewer);		

	}
	@Override
	public void cfsConfigUpdated() {
		if (cfsConfig.getJsonElementByPath(namedObj.getPath()) == null) {
			namedObj = cfsConfig.getNamedObjectByPath(cfsConfig.getPathOfParentElement(namedObj.getPath()));
		} 
		updatedObject(namedObj);
		
	}
	
	public void removeThisListener() {
		cfsConfig.removeChangeListener(this);
	}
	
	public void createMenu(Viewer viewer) {
		Action addItem = TableContextMenuActions.createActionAddItem(namedObj, cfsConfig, keyValueContentProvider);
		Action deleteItem = TableContextMenuActions.createActionDeleteItem(tableViewer, cfsConfig);
		Action unOverride = TableContextMenuActions.createActionUnoverrideItem(tableViewer, cfsConfig);
		
		MenuManager menumgr = new MenuManager();
		Menu men = menumgr.createContextMenu(viewer.getControl());
		menumgr.addMenuListener(new IMenuListener() {
			@Override
			public void menuAboutToShow(IMenuManager mgr) {			
				addItem.setText("Add Item");
				menumgr.add(addItem);
				if (viewer.getSelection() instanceof IStructuredSelection) {
					IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
					Object selectedObject = selection.getFirstElement();
					
					if (selectedObject != null) {
						NamedObject entry = (NamedObject) selectedObject;
						deleteItem.setText("Delete Item");
						menumgr.add(deleteItem);
						if (entry.getOverridden()) {
							unOverride.setText("Unoverride");
							menumgr.add(unOverride);
							
						}
					}
				} 
				KeyValueTable.this.fillContextMenu(mgr);
			}
		});
		menumgr.setRemoveAllWhenShown(true);
		viewer.getControl().setMenu(men);
	}
	
	protected void fillContextMenu(IMenuManager contextMenu) {
		contextMenu.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
	}
	
	public void createTable(NamedObject namedObject, Composite parent) {
		
		tableViewer = new TableViewer(this, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);
		keyValueContentProvider = new KeyValueContentProvider();
		tableViewer.setContentProvider(keyValueContentProvider);
		
		createColumns(this, tableViewer);
		Table table = tableViewer.getTable();

		FontData[] boldFontData = getModifiedFontData(tableViewer.getTable().getFont().getFontData(), SWT.BOLD);
		Font boldFont = new Font(Display.getCurrent(), boldFontData);
		tableViewer.setLabelProvider(new KeyValueLabelProvider(boldFont));
		tableViewer.setInput(namedObject);

		table.setHeaderVisible(true);
		table.setLinesVisible(true);
	}
	

	
	public TableViewer getViewer() {
		return this.tableViewer;
	}
	
	private void createColumns(final Composite parent, final TableViewer viewer) {
		String[] properties = { "Key", "Value" };
		TableViewerColumn col = createTableViewerColumn(properties[0], 100, 0);
		col.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				NamedObject entry = (NamedObject) element;
				return entry.getName();
			}
		});
		col = createTableViewerColumn(properties[1], 100, 1);
		col.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				NamedObject entry = (NamedObject) element;
				JsonElement asElement = (JsonElement) entry.getObject();
				return asElement.getAsString();
			}
		});
		col.setEditingSupport(new KeyValueEditingSupport(viewer, 1, cfsConfig));
	}
	
	private TableViewerColumn createTableViewerColumn(String title, int bound, final int colNum) {
		final TableViewerColumn viewerColumn = new TableViewerColumn(tableViewer, SWT.NONE);
		final TableColumn column = viewerColumn.getColumn();
		column.setText(title);;
		column.setWidth(bound);
		column.setResizable(true);
		column.setMoveable(false);
		return viewerColumn;
	}
	
	
	public class KeyValueContentProvider implements IStructuredContentProvider {
		
		@Override
		public Object[] getElements(Object inputElement) {
			
			ArrayList<NamedObject> keyValueEntries = new ArrayList<NamedObject>();
			NamedObject namedObject = (NamedObject) inputElement;
			JsonElement jsonEle = (JsonElement) namedObject.getObject();
			JsonObject currentObject = jsonEle.getAsJsonObject();
			for (Map.Entry<String, JsonElement> entry : currentObject.entrySet()) {
				if (!entry.getValue().isJsonObject()) {
					NamedObject toAddJsonPrimitive = new NamedObject();
					String key = entry.getKey();
					toAddJsonPrimitive.setName(key);
					JsonElement element = entry.getValue();
					toAddJsonPrimitive.setObject(element);
					String path = namedObject.getPath().concat("." + key);
					toAddJsonPrimitive.setPath(path);
					if(cfsConfig.isOverridden(path)) {
						toAddJsonPrimitive.setOverridden(true);
			    	} else {
			    		toAddJsonPrimitive.setOverridden(false);
			    	}
					keyValueEntries.add(toAddJsonPrimitive);
				}
			}
			return keyValueEntries.toArray();
		}
		
		public void add(NamedObject entry) {
			tableViewer.add(entry);
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
	public void updatedObject(NamedObject namedObject) {
		namedObj = namedObject;
		tableViewer.setInput(namedObject);	
		createMenu(tableViewer);

	}
}
