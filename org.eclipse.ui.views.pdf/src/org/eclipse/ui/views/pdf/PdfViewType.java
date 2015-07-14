package org.eclipse.ui.views.pdf;

import java.util.Map;
import java.util.Map.Entry;
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
		if(this.page!=null && this.page.getFile().equals(page.getFile())){
			if(!this.page.isDisposed()){
				this.page.closeFile();
			}
			setPage(null);
		}
		if(toolbar.getPage()!=null && toolbar.getPage().getFile().equals(page.getFile())){
			if(!toolbar.getPage().isDisposed()){
				toolbar.getPage().closeFile();
			}
			toolbar.setPage(null);
		}
		for (Entry<IFile, PdfViewPage> entry : pages.entrySet()) {
			if(page.equals(entry.getValue())){
				pages.remove(entry.getKey());
				break;
			}
		}
	}

	@Override
	public IFile getFile(IFile sourceFile) {
		return sourceFile;
	}

}
