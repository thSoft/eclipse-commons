package org.eclipse.util;

import java.util.HashSet;
import java.util.Set;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.IPage;
import org.eclipse.ui.part.IPageBookViewPage;
import org.eclipse.ui.part.MessagePage;
import org.eclipse.ui.part.PageBook;
import org.eclipse.ui.part.PageBookView;

/**
 * A default PageBookView implementation. Subclasses only have to implement
 * application-specific methods, such as <code>isImportant</code> and
 * <code>doCreatePage</code>.
 */
public abstract class PageBookViewBase extends PageBookView {

	private final Set<IPage> pages = new HashSet<IPage>();

	@Override
	protected IPage createDefaultPage(PageBook book) {
		MessagePage page = new MessagePage();
		initPage(page);
		page.createControl(book);
		return page;
	}

	@Override
	protected void doDestroyPage(IWorkbenchPart part, PageRec pageRecord) {
		getPages().remove(pageRecord.page);
		pageRecord.dispose();
	}

	@Override
	protected IWorkbenchPart getBootstrapPart() {
		return getSite().getPage().getActiveEditor();
	}

	/**
	 * Subclasses should call this and return the result in
	 * <code>doCreatePage</code>.
	 */
	protected PageRec createPage(IWorkbenchPart part, IPageBookViewPage page) {
		initPage(page);
		page.createControl(getPageBook());
		getPages().add(page);
		return new PageRec(part, page);
	}

	/**
	 * Returns the page corresponding to a given part.
	 */
	public IPage getPageForPart(IWorkbenchPart part) {
		PageRec pageRec = getPageRec(part);
		if (pageRec != null) {
			return pageRec.page;
		} else {
			return null;
		}
	}

	/**
	 * Returns the currently maintained pages.
	 */
	public Set<IPage> getPages() {
		return pages;
	}

}
