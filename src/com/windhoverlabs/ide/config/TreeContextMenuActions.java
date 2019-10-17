package com.windhoverlabs.ide.config;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import com.google.gson.JsonElement;

public class TreeContextMenuActions {

	public static Action createPlaceholderAction(TreeViewer treeViewer, CfsConfig cfsConfig) {
		return new Action("PlaceHolder Action") {
			@Override
			public void run() {
				IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
				Shell shell = window.getShell();
				MessageDialog.openInformation(
						shell,
						"PlaceHolder",
						"This Action is to be implemented!");
			}
		};
	}
	
	public static Action createActionAddObject(TreeViewer treeViewer, CfsConfig cfsConfig ) {
		return new Action("Add Object") {
			@Override
			public void run() {
				IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
				Shell shell = window.getShell();
				
				if (treeViewer.getSelection().isEmpty()) {
					return;
				}
				if (treeViewer.getSelection() instanceof IStructuredSelection) {
					IStructuredSelection selection = (IStructuredSelection) treeViewer.getSelection();
					Object selectedNode = selection.getFirstElement();
					
					if (selectedNode != null) {
						NamedObject namedObject = (NamedObject) selectedNode; 
						JsonElement selectedElem = (JsonElement) namedObject.getObject();
						AddObjectDialog dialog = null;
						if (selectedElem.isJsonObject()) {
							dialog = new AddObjectDialog(shell);
							NamedObject addToConfig = new NamedObject();
							JsonElement toAdd = null;
							String name = null;
							if (dialog.open() == Window.OK) {
								toAdd = dialog.getJsonElement();
								name = dialog.getName();
								addToConfig.setName(name);
								addToConfig.setPath(namedObject.getPath().concat("."+name));
								addToConfig.setObject(toAdd);
								addToConfig.setOverridden(true);
								
								cfsConfig.update(addToConfig);
							}
						} 
					}
				}
			}
		};
	}

	public static Action createActionUnoverride(TreeViewer treeViewer, CfsConfig cfsConfig) {
		return new Action("Unoverride Action") {
			@Override
			public void run() {
				IStructuredSelection selection = (IStructuredSelection) treeViewer.getSelection();
				Object objSel = selection.getFirstElement();
				NamedObject namedObject = (NamedObject) objSel;
				cfsConfig.UnoverrideByPath(namedObject.getPath());
				
			}
		};
	}
	
	public static Action createActionDelete(TreeViewer treeViewer, CfsConfig cfsConfig) {
		return new Action("Delete Action") {
			@Override
			public void run() {
				IStructuredSelection selection = (IStructuredSelection) treeViewer.getSelection();
				Object objSel = selection.getFirstElement();
				NamedObject namedObject = (NamedObject) objSel;
				cfsConfig.UnoverrideByPath(namedObject.getPath());
			
				IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
				Shell shell = window.getShell();
				MessageDialog.openInformation(
						shell,
						"Delete Object",
						"Delete has same functionality as override right now.");
					
			}
		};
	}
	
	public static Action createActionAddArray(TreeViewer treeViewer, CfsConfig cfsConfig) {
		return new Action("Add Array Action") {
			@Override
			public void run() {
				IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
				Shell shell = window.getShell();
				MessageDialog.openInformation(
						shell,
						"Add Array",
						"This Action is to be implemented!");
			}
		};
	}	
	
	
}
