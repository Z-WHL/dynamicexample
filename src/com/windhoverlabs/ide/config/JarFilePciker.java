package com.windhoverlabs.ide.config;

import java.io.File;
import java.util.ArrayList;
import java.util.StringTokenizer;

import org.eclipse.jface.preference.PathEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;

public class JarFilePciker extends PathEditor {
	
	private String lastPath;
	
	protected JarFilePciker() {
	}
	
	public JarFilePciker(String name, String labelText, String dirChooserLabelText, Composite parent) {
		init(name, labelText);
		createControl(parent);
	}
	
	@Override
	protected String getNewInputObject() {
		FileDialog dialog = new FileDialog(getShell(), SWT.SHEET);
		if (lastPath != null) {
			if (new File(lastPath).exists()) {
				dialog.setFilterPath(lastPath);
			}
		}
		
		File files = null;
		String file = dialog.open();
		String returned = "";
		
		if (file != null) {
			file = file.trim();
			File f = new File(file);
			lastPath = f.getParent();
			
			if (file.length() > 0) {
				files = new File(file);
				returned = files.getAbsolutePath();
			} 
			
		}
		return returned;
	}
}
