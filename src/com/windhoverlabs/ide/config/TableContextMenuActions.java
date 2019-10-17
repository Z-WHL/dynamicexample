package com.windhoverlabs.ide.config;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import com.windhoverlabs.ide.config.KeyValueTable.KeyValueContentProvider;

public class TableContextMenuActions {
	
	public static Action createActionAddItem(NamedObject namedObj, CfsConfig cfsConfig, KeyValueContentProvider keyValueContentProvider) {
		return new Action("Add Item") {
			@Override
			public void run() {
				IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
				Shell shell = window.getShell();
				
				AddItemDialog dialog = new AddItemDialog(shell);
				if (dialog.open() == Window.OK) {
					String parentObjectPath = namedObj.getPath();
					String key = dialog.getName();
					cfsConfig.addKey(parentObjectPath, key);
				}
			
			}
		};
	}

	public static Action createActionDeleteItem(TableViewer tableViewer, CfsConfig cfsConfig) {
		return new Action("DeleteItem Action") {
			@Override
			public void run() {
				IStructuredSelection selection = (IStructuredSelection) tableViewer.getSelection();
				Object objSel = selection.getFirstElement();
				NamedObject entry = (NamedObject) objSel;

				cfsConfig.UnoverrideByPath(entry.getPath());
				
				IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
				Shell shell = window.getShell();
				MessageDialog.openInformation(
						shell,
						"Delete Item",
						"Delete has same functionality as override right now.");
			}
		};
	}
	
	public static Action createActionUnoverrideItem(TableViewer tableViewer, CfsConfig cfsConfig) {
		return new Action("UnoverrideItem Action") {
			@Override
			public void run() {
				IStructuredSelection selection = (IStructuredSelection) tableViewer.getSelection();
				Object objSel = selection.getFirstElement();
				NamedObject entry = (NamedObject) objSel;

				cfsConfig.UnoverrideByPath(entry.getPath());
			}
		};
	}
}
