package org.eclipse.ui.views.file;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.PageBook;

/**
 * Clients can plug in custom file types to the file view by implementing this
 * interface and binding it to the view.
 */
public interface IFileViewType<P extends Composite> {

	/**
	 * Creates the page corresponding to the given file.
	 */
	P createPage(PageBook pageBook, IFile file) throws Exception;

	/**
	 * Returns the contribution items to be added to the file view's toolbar.
	 */
	IContributionItem[] getToolbarContributions();

	/**
	 * Called when the given page is shown in the file view.
	 */
	void pageShown(P page);

	/**
	 * Called when the file corresponding to the given page is reloaded.
	 */
	void reload(P page) throws Exception;

	/**
	 * Called before the given page is closed.
	 */
	void pageClosed(P page);

}
