package org.eclipse.ui.views.midi;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.ui.part.PageBook;
import org.eclipse.ui.views.file.IFileViewType;

public class MidiViewType implements IFileViewType<MidiViewPage> {

	public static final String EXTENSION = "midi"; //$NON-NLS-1$

	@Override
	public MidiViewPage createPage(PageBook pageBook, IFile file) throws Exception {
		return new MidiViewPage(pageBook, file);
	}

	private final MidiViewToolbarManager toolbar = new MidiViewToolbarManager();

	@Override
	public IContributionItem[] getToolbarContributions() {
		return toolbar.getToolbarContributions();
	}

	private MidiViewPage page;

	private void setPage(MidiViewPage page) {
		this.page = page;
	}

	public MidiViewPage getPage() {
		return page;
	}

	@Override
	public void pageShown(MidiViewPage page) {
		if ((getPage() != null) && (page != getPage())) {
			getPage().pause();
		}
		setPage(page);
		toolbar.setPage(page);
		page.setPlaybackAction(toolbar.getPlaybackAction());
	}

	@Override
	public void reload(MidiViewPage page) throws Exception {
		page.reload();
	}

	@Override
	public void pageClosed(MidiViewPage page) {
		page.closeFile();
	}

	@Override
	public IFile getFile(IFile sourceFile) {
		return sourceFile;
	}

}
