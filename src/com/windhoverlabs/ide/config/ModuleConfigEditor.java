package com.windhoverlabs.ide.config;

import java.util.ArrayList;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
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
	private Composite copy;
	private boolean loaded;
	private String jarPath;
	private String classPath;
	private ArrayList<String> classPaths = new ArrayList<String>();
	
	
	/**
	 * Create the composite.
	 * @param parent
	 * @param style
	 */
	public ModuleConfigEditor(Composite parent, int style, String jsonPath, CfsConfig cfsConfig, String moduleName) {
		super(parent, style);
		this.cfsConfig = cfsConfig;
		this.copy = ModuleConfigEditor.this;
		
		addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				toolkit.dispose();
			}
		});
		toolkit.adapt(this);
		toolkit.paintBordersFor(this);
		setLayout(null);

		setUpPreferences();	
		setTreeViewer(new ConfigTreeViewer(this, SWT.BORDER, jsonPath, cfsConfig, moduleName, ModuleConfigEditor.this));
		editor = null;
		// Register this SashForm as a Listener to the CfsConfig Subject. Upon listening to a change, it will rebuild the appropriate editor. 
		// We must implement the listener at this level because we must be able to reconstruct the editor when they key values are unoverrided.
		cfsConfig.addChangeListener(this);
	}
	

	@Override
	public void cfsConfigUpdated() {
	}
	
	/**
	 * Sets up fields related to SWT jar and Custom class Jar preferneces and registers listeners for changes.
	 */
	private void setUpPreferences() {
		IPreferenceStore store = ContextManager.getDefault().getPreferenceStore();
		jarPath = store.getString(PreferenceConstants.SWT_PATH);
		classPath = store.getString(PreferenceConstants.CUSTOM_CLASS_PATH);
		String fileList = store.getString(PreferenceConstants.CUSTOM_CLASS_PATHS);
		if (fileList.length() > 0) {
			String[] files = fileList.split(":");
			for (int i = 0; i < files.length; i++) {
				classPaths.add(files[i]);
				System.out.println(classPaths.indexOf(i));
			}
		}
		
		loaded = DynamicClassLoader.setUp(jarPath, classPath, classPaths, copy);
		if (!loaded) {
			System.out.println("The SWT Jars or Class Jar paths are incorrect and were not loaded");
		}
		store.addPropertyChangeListener(new IPropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent event) {
				if (event.getProperty() == PreferenceConstants.SWT_PATH) {
					jarPath = event.getNewValue().toString();
				} else if (event.getProperty() == PreferenceConstants.CUSTOM_CLASS_PATH) {
					classPath = event.getNewValue().toString();
				} else if (event.getProperty() == PreferenceConstants.CUSTOM_CLASS_PATHS) {
					String[] files = ((String)event.getNewValue()).split(":");
					ArrayList<String> classJars = new ArrayList<>();
					for (int i = 0; i < files.length; i++) {
						classJars.add(files[i]);
					}
					classPaths = classJars;
				}
				loaded = DynamicClassLoader.setUp(jarPath, classPath, classPaths, copy);
				if (!loaded) {
					// Do error checking
					System.out.println("The SWT Jars or Class Jar paths are incorrect and were not loaded");
				}
			}
		});
	}
	
	
	/**
	 * Decides whether to load the Custom Composite if it has a specified Class and exists or the default Key Value Composite otherwise.
	 * @param selectedNamedObject
	 * @param selectedElement
	 */
	public void goUpdate(NamedObject selectedNamedObject, JsonElement selectedElement) {
		if (selectedElement.getAsJsonObject().has("_config_wizard")) {
			JsonObject wizardObject = selectedElement.getAsJsonObject().get("_config_wizard").getAsJsonObject();
			String className = wizardObject.get("class").getAsString();
			
			// Check whether the Custom class has been loaded
			boolean verified = DynamicClassLoader.verifyClassExistence(className, this);
			if (verified && loaded) {
				// The custom class exists, so load it.
				goUpdateCustomEditor(selectedNamedObject);
			} else {
				// It doesn't exists, load the default Key Value Composite.
				goUpdateKeyValueTable(selectedNamedObject);
			}
		} else {
			// There is no Custom Class specified, so load the default Key Value Table;
			goUpdateKeyValueTable(selectedNamedObject);
		}
	}
	
	/**
	 * If a key value table has been created, then update it's content
	 * Else create a new key value table.
	 * @param selectedObject
	 */
	public void goUpdateKeyValueTable(NamedObject selectedObject) {
		// Update the objects associated with the selection.
		this.currentRightObject = selectedObject;
		this.currentRightElement = (JsonElement) selectedObject.getObject();	
		
		if (editor != null) {
			if (editor instanceof KeyValueTable) {
				// Current editor composite is also a key value table, so update the contents with the new selected object.
				((KeyValueTable) editor).updatedObject(selectedObject);
				layout(true, true);
			} else {
				// Current editor is a custom composite, so dispose and create a new key value table.
				editor.dispose();
				editor = new KeyValueTable(this, SWT.FILL, currentRightObject, cfsConfig);
				layout(true, true);
			}
		} else {
			// Editor is null so create a new key value table.
			editor = new KeyValueTable(this, SWT.FILL, currentRightObject, cfsConfig);
			layout(true, true);
		}
	}

	/**
	 * Replace the editor with the custom class specified by the selected object.
	 * @param selectedObject
	 */
	public void goUpdateCustomEditor(NamedObject selectedObject) {
		// Retrieve the string representation of the class to load from the configurations.
		JsonElement selectedElement = (JsonElement) selectedObject.getObject();
		JsonObject wizardObject = selectedElement.getAsJsonObject().get("_config_wizard").getAsJsonObject();
		String className = wizardObject.get("class").getAsString();
		
		if (editor != null) {
			if (editor instanceof KeyValueTable) {
				// The editor is a key value table, so remove listeners.
				((KeyValueTable) editor).removeThisListener();
			}
			// Dispose the editor.
			editor.dispose();
		}
		// Create the specified custom class from a jar and set it to the editor.
		editor = DynamicClassLoader.createCustomClass(selectedObject, className, this);
		getParent().layout(true, true);
	}

	public ConfigTreeViewer getTreeViewer() {
		return treeViewer;
	}

	public void setTreeViewer(ConfigTreeViewer treeViewer) {
		this.treeViewer = treeViewer;
	}

}