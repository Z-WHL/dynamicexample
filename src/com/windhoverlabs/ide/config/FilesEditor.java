package com.windhoverlabs.ide.config;

import java.io.File;

import org.eclipse.jface.preference.PathEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;

public class FilesEditor extends PathEditor {
	
	private String lastPath;
	
	protected FilesEditor() {
	}
	
	public FilesEditor(String name, String labelText, String dirChooserLabelText, Composite parent) {
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
		String returned = null;
		
		if (file != null) {
			file = file.trim();
			if (file.length() > 0) {
				files = new File(file);
				returned = files.getAbsolutePath();
			}
			lastPath = files.getParent();
		}
		return returned;
	}

}
