package org.eclipse.ui.views.file.source;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.views.file.FileView;
import org.eclipse.util.EditorUtils;
import org.eclipse.util.UiUtils;

/**
 * A source for the currently edited file (or a file derived from it).
 */
public class CurrentFileViewSource extends AbstractFileViewSource implements IPartListener {

	@Override
	public void init(FileView fileView, boolean startup) {
		super.init(fileView, startup);
		if (startup) { // At startup, workbench page may not be created yet
			new Job("Initializing current file view source") {

				@Override
				protected IStatus run(IProgressMonitor monitor) {
					Display.getDefault().syncExec(new Runnable() { // Run in UI thread

						@Override
						public void run() {
							init();
						}

					});
					return Status.OK_STATUS;
				}

			}.schedule();
		} else {
			init();
		}
	}

	private void init() {
		UiUtils.getWorkbenchPage().addPartListener(this);
		currentEditorChanged();
	}

	private void currentEditorChanged() {
		IFile currentFile = EditorUtils.getCurrentlyOpenFile();
		if (currentFile != null) {
			getFileView().show(deriveFile(currentFile));
		} else {
			getFileView().hide();
		}
	}

	/**
	 * Override to return another file derived from the currently edited file.
	 */
	protected IFile deriveFile(IFile currentFile) {
		return currentFile;
	}

	@Override
	public void done() {
		UiUtils.getWorkbenchPage().removePartListener(this);
		super.done();
	}

	@Override
	public void partActivated(IWorkbenchPart part) {
		if (part instanceof IEditorPart) {
			currentEditorChanged();
		}
	}

	@Override
	public void partBroughtToTop(IWorkbenchPart part) {
	}

	@Override
	public void partClosed(IWorkbenchPart part) {
		if (part instanceof IEditorPart) {
			currentEditorChanged();
		}
	}

	@Override
	public void partDeactivated(IWorkbenchPart part) {
	}

	@Override
	public void partOpened(IWorkbenchPart part) {
	}

	@Override
	public String getName() {
		return "Current";
	}

	@Override
	public String getLongName() {
		return "Currently edited file";
	}

}
