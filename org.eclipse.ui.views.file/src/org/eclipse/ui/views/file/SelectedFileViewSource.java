package org.eclipse.ui.views.file;

import java.util.Arrays;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;

/**
 * A source for the currently selected file (or a file derived from it).
 */
public class SelectedFileViewSource extends AbstractFileViewSource implements ISelectionListener {

	@Override
	public void init(FileView fileView) {
		super.init(fileView);
		ISelectionService selectionService = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getSelectionService();
		selectionService.addPostSelectionListener(this);
		selectionChanged(null, selectionService.getSelection());
	}

	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection structuredSelection = (IStructuredSelection)selection;
			Object selectedElement = structuredSelection.getFirstElement();
			if (selectedElement instanceof IFile) {
				IFile selectedFile = (IFile)selectedElement;
				if (Arrays.asList(getFileView().getExtensions()).contains(selectedFile.getFileExtension())) {
					getFileView().show(getFile(selectedFile));
				}
			}
		}
	}

	@Override
	public void done() {
		PlatformUI.getWorkbench().getActiveWorkbenchWindow().getSelectionService().removePostSelectionListener(this);
		super.done();
	}

	/**
	 * Override to return another file derived from the currently selected file.
	 */
	protected IFile getFile(IFile selectedFile) {
		return selectedFile;
	}

	@Override
	public String getName() {
		return "Selected";
	}

}
