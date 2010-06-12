package org.eclipse.ui.views.pdf;

import org.eclipse.core.resources.IFile;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.PageBook;
import org.eclipse.ui.views.file.AbstractFileView;

/**
 * A view that displays a PDF file.
 */
public abstract class PdfView extends AbstractFileView {

	public static final String[] EXTENSIONS = { "pdf" }; //$NON-NLS-1$

	@Override
	public String[] getExtensions() {
		return EXTENSIONS;
	}

	@Override
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);
		// TODO restore default zoom
	}

	@Override
	public Composite createPage(PageBook pageBook, IFile file) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void reload(IFile file) {
		// TODO Auto-generated method stub
	}

}
