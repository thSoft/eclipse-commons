package org.eclipse.ui.views.file.source;

import org.eclipse.ui.views.file.FileView;

/**
 * Defines the file to be shown in a file view.
 */
public interface IFileViewSource {

	/**
	 * Initializes this source. Called when this source is selected as the source
	 * of the given file view.
	 * 
	 * @param startup whether the source is restored at startup
	 */
	void init(FileView fileView, boolean startup);

	/**
	 * Shuts down this source. Called when this source ceases to be the source of
	 * a file view.
	 */
	void done();

	/**
	 * Returns the user-friendly name of the file source to be displayed on the
	 * UI.
	 */
	String getName();

	String getLongName();

}
