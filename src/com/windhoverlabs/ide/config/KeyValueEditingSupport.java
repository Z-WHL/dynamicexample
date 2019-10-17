package com.windhoverlabs.ide.config;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.widgets.Composite;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public class KeyValueEditingSupport extends EditingSupport {
	
	private final CellEditor editor;
	private final int index;
	private CfsConfig cfsConfig;
	
	public KeyValueEditingSupport(TableViewer viewer, int index, CfsConfig cfsConfig) {
		super(viewer);
		this.index = index;
		this.cfsConfig = cfsConfig;
		this.editor = new TextCellEditor((Composite)getViewer().getControl());
	}
	
	@Override
	protected CellEditor getCellEditor(Object element) {
		return editor;
	}
	
	@Override
	protected boolean canEdit(Object element) {
		return true;
	}
	
	@Override
	protected Object getValue(Object element) {
		NamedObject namedObj = (NamedObject) element;
		JsonElement jsonElement = (JsonElement) namedObj.getObject();
		return index == 0 ? namedObj.getName() : jsonElement.getAsString();
	}
	
	@Override
	protected void setValue(Object element, Object userInputValue) {
		NamedObject selectedEntry = (NamedObject) element;
		JsonElement selectedElement = (JsonElement) selectedEntry.getObject();
		String key = selectedEntry.getName();
		String oldValue = selectedElement.getAsString();
		String newValue = String.valueOf(userInputValue);
		
		if (!oldValue.equalsIgnoreCase(newValue)) {
			JsonElement newPrimitive = new JsonPrimitive(newValue);
			selectedEntry.setObject(newPrimitive);
			cfsConfig.setKeyValue(cfsConfig.getPathOfParentElement(selectedEntry.getPath()), key, newValue);
		}
	}
}

