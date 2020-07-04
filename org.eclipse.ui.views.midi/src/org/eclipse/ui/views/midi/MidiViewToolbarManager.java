package org.eclipse.ui.views.midi;

import org.eclipse.jface.action.IContributionItem;

public class MidiViewToolbarManager {

	private MidiViewPage page;

	public void setPage(MidiViewPage page) {
		this.page = page;
	}

	public MidiViewPage getPage() {
		return page;
	}

	private final IContributionItem[] contributions = new IContributionItem[] {};

	public IContributionItem[] getToolbarContributions() {
		return contributions;
	}

}
