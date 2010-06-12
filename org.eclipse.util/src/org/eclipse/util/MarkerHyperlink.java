package org.eclipse.util;

import org.eclipse.core.resources.IMarker;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.console.IHyperlink;
import org.eclipse.ui.ide.IDE;

/**
 * A hyperlink that jumps to a marker.
 */
public class MarkerHyperlink implements IHyperlink {

	private final IMarker marker;

	public MarkerHyperlink(IMarker marker) {
		this.marker = marker;
	}

	@Override
	public void linkEntered() {
	}

	@Override
	public void linkExited() {
	}

	@Override
	public void linkActivated() {
		try {
			IDE.openEditor(UiUtils.getWorkbenchPage(), marker);
		} catch (PartInitException e) {
			e.printStackTrace();
		}
	}

}
