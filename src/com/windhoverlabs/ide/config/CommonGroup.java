package com.windhoverlabs.ide.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;


// This class has been removed from the main implementation, but it can be used as a
// composite which contains spreadsheet-like editing and saving capabilities for similar
// grouped items. Currently, no labels are provided so rows cannot be identified.
// TODO: Add column for key labels for ease of editing.

public class CommonGroup extends Composite {
	
	HashMap<String, ArrayList<String>> groupLabels = new HashMap<String, ArrayList<String>>();
	HashMap<String, JsonObject> commonGroups = new HashMap<String, JsonObject>();
	HashMap<String, TableViewer> tableViewerMaps = new HashMap<String, TableViewer>();
	HashMap<String, Table> tableMaps = new HashMap<String, Table>();
	String currentGroup;
	Table jsonTable;
	TableViewer viewer;
	List<String> currentGroupLabels = new ArrayList<String>();
	CfsConfig cfsConfigPointer;
	NamedObject namedObject;
	
	public CommonGroup(Composite scrollableHolder, int style, JsonElement current , String currentConfigName, NamedObject namedObject) {
		super(scrollableHolder, style);
		//this.cfsConfigPointer = cfsConfig;
		
		if (current.isJsonObject()) {
			this.namedObject = namedObject;
			doLabeling(currentConfigName, current.getAsJsonObject());
			this.currentGroup = currentConfigName;
			if (currentGroupLabels.size() > 0) {
				createViewer(scrollableHolder, current, currentConfigName);
			}
			setLayout(new FillLayout(SWT.HORIZONTAL));	
		}
	}
	
	private void createViewer(Composite parent, JsonElement current, String currentConfigName) {
		viewer = new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);
		createColumns(parent, viewer);
		final Table table = viewer.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		viewer.setContentProvider(new ArrayContentProvider());
		viewer.setInput(new ConfigModelProvider(current.getAsJsonObject(), namedObject).getJsons());
	}
	
	public TableViewer getViewer() {
		return viewer;
	}
	
	private void createColumns(final Composite parent, final TableViewer viewer) {
		TableViewerColumn col = null;
		if (currentGroupLabels.size() > 0) {
			for (int j = 0; j < currentGroupLabels.size(); j++) {
				addColumn(col, currentGroupLabels.get(j), 150, j);
				System.out.println("added a column");
			}
		}
	}
	
	private void addColumn(TableViewerColumn col, String title, int bound, final int colNum) {
		col = createTableViewerColumn(title, bound, colNum);
		col.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				int tempCounter = 0;
				NamedObject namedObj = (NamedObject) element;
				JsonObject singleJsonObject = (JsonObject) namedObj.getObject();
				for (Map.Entry<String, JsonElement> entry : singleJsonObject.entrySet()) {
					if (!entry.getValue().isJsonObject()) {
						if (tempCounter != (colNum)) {
							tempCounter++;
						} else {
							return entry.getValue().getAsString();
						}
					}
				}
				return null;
			}
		});
	}
	
	private TableViewerColumn createTableViewerColumn(String title, int bound, final int colNum) {
		final TableViewerColumn viewerColumn = new TableViewerColumn(viewer, SWT.NONE);
		final TableColumn column = viewerColumn.getColumn();
		column.setText(title);
		column.setWidth(bound);
		column.setResizable(true);
		column.setMoveable(false);
		return viewerColumn;
	}

	/**
	 * Function will take the input Json Object and group them based on their common labels.
	 * Stores in a HashMap.
	 * @param inputJson
	 */
	private void doLabeling(String currentConfigName, JsonObject inputJson) {
		for (Map.Entry<String, JsonElement> entry : inputJson.entrySet()) {
			if (entry.getValue().isJsonObject()) {
				for (Map.Entry<String, JsonElement> innerElement : entry.getValue().getAsJsonObject().entrySet()) {
					if (!innerElement.getValue().isJsonObject()) {
						if (canAdd(currentConfigName, innerElement.getKey())) {
							currentGroupLabels.add(innerElement.getKey());
						}
					}
				}
			}
		}
	}

	private boolean canAdd(String currentGroup, String keyElement) {
		if (!currentGroupLabels.contains(keyElement)) {
			return true;
		} else {
			return false;
		}
	}
}
