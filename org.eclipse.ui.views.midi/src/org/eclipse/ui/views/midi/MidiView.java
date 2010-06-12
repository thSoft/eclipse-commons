package org.eclipse.ui.views.midi;

import org.eclipse.core.resources.IFile;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.PageBook;
import org.eclipse.ui.views.file.AbstractFileView;

/**
 * A view that plays back a MIDI file.
 */
public abstract class MidiView extends AbstractFileView {

	public static final String[] EXTENSIONS = { "midi", "mid" }; //$NON-NLS-1$ //$NON-NLS-2$

	@Override
	public String[] getExtensions() {
		return EXTENSIONS;
	}

	@Override
	public Composite createPage(PageBook pageBook, IFile file) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void reload(IFile file) {
		// TODO Auto-generated method stub
	}

}
