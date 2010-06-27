package org.eclipse.ui.views.midi;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.ui.part.PageBook;
import org.eclipse.ui.views.file.IFileViewType;

public class MidiViewType implements IFileViewType<MidiViewPage> {

	@Override
	public MidiViewPage createPage(PageBook pageBook, IFile file) {
		return new MidiViewPage(pageBook, file);
	}

	@Override
	public IContributionItem[] getToolbarContributions() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void pageShown(MidiViewPage page) {
		// TODO Auto-generated method stub
	}

	@Override
	public void reload(MidiViewPage page) throws Exception {
		// TODO Auto-generated method stub
	}

	@Override
	public void pageClosed(MidiViewPage page) {
		// TODO Auto-generated method stub
		
	}

}
