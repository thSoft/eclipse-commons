package org.eclipse.ui.views.file.source;

import org.eclipse.ui.views.file.FileView;

/**
 * The default implementation of {@link IFileViewSource}.
 */
public abstract class AbstractFileViewSource implements IFileViewSource {

	private FileView fileView;

	@Override
	public void init(FileView fileView, boolean startup) {
		this.setFileView(fileView);
	}

	@Override
	public void done() {
	}

	protected void setFileView(FileView fileView) {
		this.fileView = fileView;
	}

	protected FileView getFileView() {
		return fileView;
	}

}
