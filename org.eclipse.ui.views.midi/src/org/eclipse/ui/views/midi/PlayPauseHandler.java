package org.eclipse.ui.views.midi;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.views.file.FileView;
import org.eclipse.ui.views.file.IFileViewType;

public class PlayPauseHandler extends AbstractHandler {

	private static final String PARAMETER_VIEW = "org.eclipse.ui.views.midi.commands.PlayPause.view"; //$NON-NLS-1$ //XXX see plugin.xml

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		String viewId = event.getParameter(PARAMETER_VIEW);
		if (viewId != null) {
			IViewPart view = HandlerUtil.getActivePart(event).getSite().getPage().findView(viewId);
			if(view == null) {
				//TODO open view
			}
			if (view instanceof FileView) {
				//TODO set the correct file if not open
				FileView fileView = (FileView)view;
				IFileViewType<?> fileViewType = fileView.getType();
				if (fileViewType instanceof MidiViewType) {
					MidiViewType midiViewType = (MidiViewType)fileViewType;
					MidiViewPage midiViewPage = midiViewType.getPage();
					if (midiViewPage != null) {
						midiViewPage.togglePlayback();
					}
				}
			}
		}
		return null;
	}

}
