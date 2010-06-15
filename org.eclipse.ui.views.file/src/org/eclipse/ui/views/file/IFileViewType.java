package org.eclipse.ui.views.file;

import org.eclipse.core.resources.IFile;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.PageBook;

public interface IFileViewType {

	/**
	 * Creates the page corresponding to the given file.
	 */
	Composite createPage(PageBook pageBook, IFile file) throws Exception;

}
