package org.eclipse.ui.views.pdf;

import java.util.Map;
import java.util.WeakHashMap;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.ui.part.PageBook;
import org.eclipse.ui.views.file.IFileViewType;
import org.eclipse.ui.views.pdf.PdfViewToolbarManager.FitToAction;

public class PdfViewType implements IFileViewType<PdfViewPage> {

	public static final String EXTENSION = "pdf"; //$NON-NLS-1$

	Map<IFile, PdfViewPage> pages=new WeakHashMap<IFile,PdfViewPage>();
	
	@Override
	public PdfViewPage createPage(PageBook pageBook, IFile file) throws Exception {
		PdfViewPage result = new PdfViewPage(pageBook, file);
		pages.put(file, result);
		return result;
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

	public void prepareDelete(IFile file){
		PdfViewPage view = pages.get(file);
		if(view!=null){
			view.closeFile();
			pages.remove(file);
		}
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

	@Override
	public IFile getFile(IFile sourceFile) {
		return sourceFile;
	}

}
