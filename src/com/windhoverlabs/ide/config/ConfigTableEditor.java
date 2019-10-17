package com.windhoverlabs.ide.config;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;

import com.google.gson.JsonElement;

public class ConfigTableEditor extends Composite {
	
	SashForm sashForm;
	KeyValueTable keyValueTable;
	CommonGroup scrollableGroups;

	CfsConfig cfsConfig;
	JsonElement jsonElement;
	NamedObject namedObject;
	NamedObject parentObject = null;
	
	public ConfigTableEditor(Composite parent, int style, JsonElement jsonElement, NamedObject nameObj, CfsConfig cfsConfig) {
		super(parent, style);
		setLayout(new FillLayout(SWT.VERTICAL));
		this.cfsConfig = cfsConfig;
		this.namedObject = nameObj;
		this.jsonElement = jsonElement;
		
		// Register this instance as a Listener to the CfsConfig Subject.
		keyValueTable = new KeyValueTable(this, SWT.FILL, nameObj, cfsConfig);
	}	
	
	public void updateTableContents(NamedObject currentRightObject, JsonElement currentRightElement) {
		keyValueTable.updatedObject(currentRightObject);
	}
}
