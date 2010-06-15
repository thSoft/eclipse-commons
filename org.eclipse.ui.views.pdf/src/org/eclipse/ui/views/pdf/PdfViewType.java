package org.eclipse.ui.views.pdf;

import org.eclipse.core.resources.IFile;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.PageBook;
import org.eclipse.ui.views.file.IFileViewType;

public abstract class PdfViewType implements IFileViewType {

	public static final String EXTENSION = "pdf"; //$NON-NLS-1$

	@Override
	public Composite createPage(PageBook pageBook, IFile file) throws Exception {
		return new PdfViewPage(pageBook, file.getLocation().toOSString());
	}

}
