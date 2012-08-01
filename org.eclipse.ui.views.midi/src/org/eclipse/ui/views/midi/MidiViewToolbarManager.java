package org.eclipse.ui.views.midi;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.swt.SWT;

public class MidiViewToolbarManager {

	private MidiViewPage page;

	public void setPage(MidiViewPage page) {
		this.page = page;
	}

	public MidiViewPage getPage() {
		return page;
	}

	public class PlaybackAction extends Action {

		public PlaybackAction() {
			setPlaying(false);
			setAccelerator(SWT.SHIFT | SWT.CTRL | 'P');
		}

		@Override
		public void run() {
			if (getPage().isPlaying()) {
				getPage().pause();
			} else {
				getPage().play();
				setPlaying(true);
			}
		}

		public void setPlaying(boolean playing) {
			setImageDescriptor(Activator.getImageDescriptor(MidiViewPage.ICON_PATH + (playing ? "Pause.png" : "Play.png"))); //$NON-NLS-1$ //$NON-NLS-2$
			setToolTipText(playing ? "Pause" : "Play");
		}

	}

	private final PlaybackAction playbackAction = new PlaybackAction();

	public PlaybackAction getPlaybackAction() {
		return playbackAction;
	}

	private final IContributionItem[] contributions = new IContributionItem[] { new ActionContributionItem(getPlaybackAction()) };

	public IContributionItem[] getToolbarContributions() {
		return contributions;
	}

}
