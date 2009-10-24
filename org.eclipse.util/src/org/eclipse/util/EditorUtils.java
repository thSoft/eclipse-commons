package org.eclipse.util;

import org.eclipse.core.resources.IFile;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

public class EditorUtils {

	private EditorUtils() {
	}

	/**
	 * Returns the topmost visible editor part, if there is any.
	 */
	public static IEditorPart getActiveEditor() {
		IWorkbenchWindow workbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (workbenchWindow == null) {
			return null;
		}
		IWorkbenchPage page = workbenchWindow.getActivePage();
		if (page == null) {
			return null;
		}
		return page.getActiveEditor();
	}

	/**
	 * Returns the file being edited in the active editor. Returns
	 * <code>null</code> if there is no open editor, or if it is not a file
	 * resource that is being edited.
	 */
	public static IFile getCurrentlyOpenFile() {
		IEditorPart activeEditor = getActiveEditor();
		if (activeEditor == null) {
			return null;
		}
		IFile result = (IFile)activeEditor.getEditorInput().getAdapter(IFile.class);
		return result;
	}

}
