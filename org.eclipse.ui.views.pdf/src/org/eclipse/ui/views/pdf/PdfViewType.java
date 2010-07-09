package org.eclipse.ui.views.pdf;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.ui.part.PageBook;
import org.eclipse.ui.views.file.IFileViewType;
import org.eclipse.ui.views.pdf.PdfViewToolbarManager.FitToAction;

public class PdfViewType implements IFileViewType<PdfViewPage> {

	public static final String EXTENSION = "pdf"; //$NON-NLS-1$

	@Override
	public PdfViewPage createPage(PageBook pageBook, IFile file) throws Exception {
		return new PdfViewPage(pageBook, file);
	}

	private final PdfViewToolbarManager toolbar = new PdfViewToolbarManager();

	@Override
	public IContributionItem[] getToolbarContributions() {
		return toolbar.getToolbarContributions();
	}

	private PdfViewPage page;

	private void setPage(PdfViewPage page) {
		this.page = page;
	}

	public PdfViewPage getPage() {
		return page;
	}

	@Override
	public void pageShown(PdfViewPage page) {
		setPage(page);
		toolbar.setPage(page);
		page.setToolbar(toolbar);
		// Restore special zoom setting
		FitToAction fitToAction = page.getFitToAction();
		toolbar.disableFit();
		if (fitToAction != null) {
			fitToAction.setChecked(true);
		}
	}

	@Override
	public void reload(PdfViewPage page) throws Exception {
		page.reload();
	}

	@Override
	public void pageClosed(PdfViewPage page) {
		page.closeFile();
	}

}
