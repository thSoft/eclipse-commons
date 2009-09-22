package org.eclipse.util;

import org.eclipse.core.resources.IFile;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;

public class EditorUtils {

	private EditorUtils() {
	}

	/**
	 * Returns the topmost visible editor part.
	 */
	public static IEditorPart getActiveEditor() {
		return PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
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
		IFile file = (IFile)activeEditor.getEditorInput().getAdapter(IFile.class);
		return file;
	}

}
