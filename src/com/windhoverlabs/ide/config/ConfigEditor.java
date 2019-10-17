package com.windhoverlabs.ide.config;


import java.io.File;
import java.util.Map;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.*;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.part.MultiPageEditorPart;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.eclipse.ui.ide.IDE;

/**
 * An example showing how to create a multi-page editor.
 * This example has 3 pages:
 * <ul>
 * <li>page 0 contains a nested text editor.
 * <li>page 1 allows you to change the font used in page 2
 * <li>page 2 shows the words in page 0 in sorted order
 * </ul>
 */
public class ConfigEditor extends MultiPageEditorPart implements IResourceChangeListener, ICfsConfigChangeListener {

	/** The text editor used in page 0. */
	private TextEditor editor;
	
	private String pathName;

	private CfsConfig cfsConfig;
	/**
	 * Creates a multi-page editor example.
	 */
	public ConfigEditor() {
		super();
		ResourcesPlugin.getWorkspace().addResourceChangeListener(this);
	}

	


	/**
	 * Add the CFE page of the multi-page editor.
	 */
	private void addCFEPage() {
		final CfeConfigEditor composite = new CfeConfigEditor(getContainer(), SWT.FILL);
		
		int index = addPage(composite);
		setPageText(index, "Core");
	}

	/**
	 * Add the CFE page of the multi-page editor.
	 */
	private void addModulePage(String name, String jsonPath, CfsConfig cfsConfig) {
		final ModuleConfigEditor composite = new ModuleConfigEditor(getContainer(), SWT.FILL, jsonPath, cfsConfig, name);
		
		int index = addPage(composite);
		setPageText(index, name);
	}
	
	
	@Override
	public void cfsConfigUpdated() {
		firePropertyChange(IEditorPart.PROP_DIRTY);
		
	}
	

	/**
	 * Creates the pages of the multi-page editor.
	 */
	protected void createPages() {
		/* Collapse the configuration into a single object. */
		IEditorInput editorInput = getEditorInput();
		IPathEditorInput path = (IPathEditorInput) editorInput;
		File f = new File(path.getPath().toOSString());
		pathName = f.getAbsolutePath();
		cfsConfig = JsonObjectsUtil.goMerge(new File(pathName));
        cfsConfig.setPath(pathName);
        
        cfsConfig.addChangeListener(this);
        /* Add the CFE page. */
		addCFEPage();
        
		/* Iterate through the modules and add pages for them. */
        JsonObject modules = cfsConfig.getJsonObjectByPath("modules");        
        for (Map.Entry<String,JsonElement> entry : modules.entrySet()) {
        	if (entry.getValue().isJsonObject()) {
	        	String moduleKey = entry.getKey();
	        	String jsonPath = "modules." + moduleKey;
	        	addModulePage(moduleKey, jsonPath, cfsConfig);
        	}
        } 
	}
	
	
	/**
	 * The <code>MultiPageEditorPart</code> implementation of this 
	 * <code>IWorkbenchPart</code> method disposes all nested editors.
	 * Subclasses may extend.
	 */
	public void dispose() {
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
		super.dispose();
	}
	/**
	 * Saves the multi-page editor's document.
	 */
	public void doSave(IProgressMonitor monitor) {
		cfsConfig.saveFile();
	}
	/**
	 * Saves the multi-page editor's document as another file.
	 * Also updates the text for page 0's tab, and updates this multi-page editor's input
	 * to correspond to the nested editor's.
	 */
	public void doSaveAs() {
		IEditorPart editor = getEditor(0);
		editor.doSaveAs();
		setPageText(0, editor.getTitle());
		setInput(editor.getEditorInput());
	}
	/* (non-Javadoc)
	 * Method declared on IEditorPart
	 */
	public void gotoMarker(IMarker marker) {
		setActivePage(0);
		IDE.gotoMarker(getEditor(0), marker);
	}
	/**
	 * The <code>MultiPageEditorExample</code> implementation of this method
	 * checks that the input is an instance of <code>IFileEditorInput</code>.
	 */
	public void init(IEditorSite site, IEditorInput editorInput)
		throws PartInitException {
		if (!(editorInput instanceof IFileEditorInput))
			throw new PartInitException("Invalid Input: Must be IFileEditorInput");
		super.init(site, editorInput);
	}
	
	
	/* (non-Javadoc)
	 * Method declared on IEditorPart.
	 */
	public boolean isSaveAsAllowed() {
		return false;
	}
	
	
	public boolean isDirty() {
		return cfsConfig.isDirty();
	}

	
	/**
	 * Closes all project files on project close.
	 */
	public void resourceChanged(final IResourceChangeEvent event){
		if(event.getType() == IResourceChangeEvent.PRE_CLOSE){
			Display.getDefault().asyncExec(new Runnable(){
				public void run(){
					IWorkbenchPage[] pages = getSite().getWorkbenchWindow().getPages();
					for (int i = 0; i<pages.length; i++){
						if(((FileEditorInput)editor.getEditorInput()).getFile().getProject().equals(event.getResource())){
							IEditorPart editorPart = pages[i].findEditor(editor.getEditorInput());
							pages[i].closeEditor(editorPart,true);
						}
					}
				}            
			});
		}
	}

	
	
}
