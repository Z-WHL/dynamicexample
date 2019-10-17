package com.windhoverlabs.ide.config;

import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.TextStyle;
import org.eclipse.swt.widgets.Event;

import com.google.gson.JsonElement;

public class KeyValueLabelProvider  extends StyledCellLabelProvider  {
		
	private final Styler fBoldStyler;
	
	public KeyValueLabelProvider(final Font boldFont) {
		fBoldStyler = new Styler() {
			@Override
			public void applyStyles(TextStyle textStyle) {
				textStyle.font = boldFont;
			}
		};
	}
	
	@Override
	public void update(ViewerCell cell) {
		NamedObject keyValue = (NamedObject) cell.getElement();
		JsonElement jsonElement = (JsonElement) keyValue.getObject();
		Styler style = null;
		
		if (keyValue.getOverridden()) {
			style = fBoldStyler;
		} 

    	if (cell.getColumnIndex() == 0) {
		    StyledString styledString = new StyledString((String)keyValue.getName(), style);
		    cell.setText(styledString.toString());
		    cell.setStyleRanges(styledString.getStyleRanges());
		    cell.setText((String)keyValue.getName());
    	} else {
		    StyledString styledString = new StyledString(jsonElement.getAsString(), style);
		    cell.setText(styledString.toString());
		    cell.setStyleRanges(styledString.getStyleRanges());
		    cell.setText(jsonElement.getAsString());
    	}
		
		super.update(cell);
	}
	
	@Override
	protected void measure(Event event, Object element) {
		super.measure(event,  element);
	}
}
	
	

