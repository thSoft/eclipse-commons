package org.eclipse.ui.views.file;

import org.eclipse.core.resources.IFile;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.util.FileSelectionDialog;

/**
 * A source for a file that the user can choose by browsing the workspace.
 */
public class DirectFileViewSource extends AbstractFileViewSource {

	@Override
	public void init(FileView fileView) {
		super.init(fileView);
		Shell shell = Display.getDefault().getActiveShell();
		FileSelectionDialog dialog = new FileSelectionDialog(shell, getFileView().getExtensions());
		IFile file = dialog.selectFile();
		if (file != null) {
			getFileView().show(file);
		}
	}

	@Override
	public String getName() {
		return "Browse...";
	}

}
