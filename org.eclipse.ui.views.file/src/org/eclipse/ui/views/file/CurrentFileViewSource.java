package org.eclipse.ui.views.file;

import org.eclipse.core.resources.IFile;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.util.EditorUtils;
import org.eclipse.util.UiUtils;

/**
 * A source for the currently edited file (or a file derived from it).
 */
public class CurrentFileViewSource extends AbstractFileViewSource implements IPartListener {

	@Override
	public void init(FileView fileView) {
		super.init(fileView);
		UiUtils.getWorkbenchPage().addPartListener(this);
		currentEditorChanged();
	}

	private void currentEditorChanged() {
		IFile currentFile = EditorUtils.getCurrentlyOpenFile();
		if (currentFile != null) {
			getFileView().show(getFile(currentFile));
		}
	}

	/**
	 * Override to return another file derived from the currently edited file.
	 */
	protected IFile getFile(IFile currentFile) {
		return currentFile;
	}

	@Override
	public void done() {
		UiUtils.getWorkbenchPage().removePartListener(this);
		super.done();
	}

	@Override
	public void partActivated(IWorkbenchPart part) {
		currentEditorChanged();
	}

	@Override
	public void partBroughtToTop(IWorkbenchPart part) {
	}

	@Override
	public void partClosed(IWorkbenchPart part) {
		currentEditorChanged();
	}

	@Override
	public void partDeactivated(IWorkbenchPart part) {
	}

	@Override
	public void partOpened(IWorkbenchPart part) {
	}

	@Override
	public String getName() {
		return "Currently edited";
	}

}
