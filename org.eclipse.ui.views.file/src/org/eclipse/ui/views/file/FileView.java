package org.eclipse.ui.views.file;

import java.util.List;
import org.eclipse.core.resources.IFile;
import org.eclipse.ui.IViewPart;

/**
 * A view that displays a file.
 */
public interface FileView extends IViewPart {

	/**
	 * Returns the extensions of the files supported by this file view.
	 */
	String[] getExtensions();

	/**
	 * Displays the given file.
	 */
	void show(IFile file);

	/**
	 * Reloads the given file if it is loaded.
	 */
	void reload(IFile file);

	/**
	 * Returns the unique identifier of this file view.
	 */
	String getId();

	/**
	 * Returns the file view sources that can be the sources if this file view.
	 */
	List<FileViewSource> getSources();

}
