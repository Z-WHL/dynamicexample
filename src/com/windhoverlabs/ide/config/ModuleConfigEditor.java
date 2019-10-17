package com.windhoverlabs.ide.config;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.forms.widgets.FormToolkit;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class ModuleConfigEditor extends SashForm implements ICfsConfigChangeListener {

	private final FormToolkit toolkit = new FormToolkit(Display.getCurrent());
	private ConfigTreeViewer treeViewer;
	private Composite editor;
	private CfsConfig cfsConfig;
	private NamedObject currentRightObject;
	private JsonElement currentRightElement;
	private Composite parent;
	private ModuleConfigEditor copy;
	
	/**
	 * Create the composite.
	 * @param parent
	 * @param style
	 */
	public ModuleConfigEditor(Composite parent, int style, String jsonPath, CfsConfig cfsConfig, String moduleName) {
		super(parent, style);
		this.cfsConfig = cfsConfig;
		this.parent = parent;
		this.copy = ModuleConfigEditor.this;
		
		addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				toolkit.dispose();
			}
		});
		toolkit.adapt(this);
		toolkit.paintBordersFor(this);
		setLayout(null);

		setTreeViewer(new ConfigTreeViewer(this, SWT.BORDER, jsonPath, cfsConfig, moduleName, ModuleConfigEditor.this));
		editor = null;
		// Register this SashForm as a Listener to the CfsConfig Subject. Upon listening to a change, it will rebuild the appropriate editor. 
		// We must implement the listener at this level because we must be able to reconstruct the editor when they key values are unoverrided.
		cfsConfig.addChangeListener(this);
	}
	

	@Override
	public void cfsConfigUpdated() {
	}
	
	public void goUpdateKeyValueTable(NamedObject selectedObject) {
		this.currentRightObject = selectedObject;
		this.currentRightElement = (JsonElement) selectedObject.getObject();
		
		if (editor != null) {
			if (editor instanceof KeyValueTable) {
				((KeyValueTable) editor).updatedObject(selectedObject);
				layout(true, true);
			} else {
				editor.dispose();
				editor = new KeyValueTable(this, SWT.FILL, currentRightObject, cfsConfig);
				layout(true, true);
			}
		} else {
			editor = new KeyValueTable(this, SWT.FILL, currentRightObject, cfsConfig);
			layout(true, true);
		}
	}

	public void goUpdateCustomEditor(NamedObject selectedObject) {
		JsonElement selectedElement = (JsonElement) selectedObject.getObject();
		JsonObject wizardObject = selectedElement.getAsJsonObject().get("_config_wizard").getAsJsonObject();
		String[] paths  = wizardObject.get("class").getAsString().split("\\.|\\[|\\]");
		String className = paths[paths.length - 1];
		
		if (editor != null) {
			if (editor instanceof KeyValueTable) {
				((KeyValueTable) editor).removeThisListener();
			}
			editor.dispose();
		}
		editor = createCustomClass(selectedObject, className);
		parent.layout(true, true);
	}

	public ConfigTreeViewer getTreeViewer() {
		return treeViewer;
	}

	public void setTreeViewer(ConfigTreeViewer treeViewer) {
		this.treeViewer = treeViewer;
	}


	public Composite createCustomClass(NamedObject currentNamedObject, String className){
		String path = "/home/vagrant/development/airliner/apps/sch/classes.jar";
		String currentSelection = "foo.".concat(className);
		Composite returned = null;

		try {
			URL jarFile = new URL("file://"+path);
			ClassLoader cl = URLClassLoader.newInstance(new URL[] {jarFile}, getClass().getClassLoader());

			Class<?> defaultClass = Class.forName("foo.Initiator", true, cl);
			Object[] arguments = new Object[1];
			Method mainMethod = defaultClass.getMethod("main", String[].class);
			mainMethod.invoke(null,  arguments);
			
			Class<?> theComposite  = Class.forName(currentSelection, true, cl);
			Class<? extends Composite> compositeClass = theComposite.asSubclass(Composite.class);
			Constructor<? extends Composite> constructor = compositeClass.getConstructor(Composite.class, int.class);
			
			returned = constructor.newInstance(copy, SWT.FILL);
			
		} catch (ClassNotFoundException | NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		
		return returned;
	}


	public void disposethis() {
		editor.dispose();
	}
}