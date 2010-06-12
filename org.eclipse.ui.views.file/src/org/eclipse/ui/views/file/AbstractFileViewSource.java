package org.eclipse.ui.views.file;

/**
 * The default implementation of {@link FileViewSource}.
 */
public abstract class AbstractFileViewSource implements FileViewSource {

	private FileView fileView;

	@Override
	public void init(FileView fileView) {
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
