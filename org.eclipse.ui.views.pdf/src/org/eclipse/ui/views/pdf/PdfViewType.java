package org.eclipse.ui.views.pdf;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.ui.part.PageBook;
import org.eclipse.ui.views.file.IFileViewType;

public class PdfViewType implements IFileViewType<PdfViewPage> {

	public static final String EXTENSION = "pdf"; //$NON-NLS-1$

	private final PdfViewToolbarManager toolbar = new PdfViewToolbarManager();

	@Override
	public PdfViewPage createPage(PageBook pageBook, IFile file) throws Exception {
		return new PdfViewPage(pageBook, file);
	}

	@Override
	public IContributionItem[] getToolbarContributions() {
		return toolbar.getToolbarContributions();
	}

	@Override
	public void pageShown(PdfViewPage page) {
		toolbar.setPage(page);
		page.setToolbar(toolbar);
	}

}
