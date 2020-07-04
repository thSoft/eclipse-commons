package org.eclipse.ui.views.midi;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.views.file.FileView;
import org.eclipse.ui.views.file.IFileViewType;
import org.eclipse.util.ResourceUtils;

public class PlayPauseHandler extends AbstractHandler {

	private static final String PARAMETER_VIEW = "org.eclipse.ui.views.midi.commands.PlayPause.view"; //$NON-NLS-1$ //XXX see plugin.xml

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchPart activePart = HandlerUtil.getActivePart(event);
		FileView midiView = getMidiFileView(activePart);
		String viewId = event.getParameter(PARAMETER_VIEW);
		IFile file = null;
		if (midiView == null && viewId != null) {
			file = getMidiFileForSelection(event);
			//if midi view not already active find it
			IViewPart view = HandlerUtil.getActivePart(event).getSite().getPage().findView(viewId);
			//if it does not exist, but there is a matching midi file - create the view
			if (file != null && view == null) {
				IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
				try {
					view = page.showView(viewId);
				} catch (PartInitException e) {
					Activator.logError("error creating midi view", e);
				}
			}
			midiView = getMidiFileView(view);
		}
		togglePlayForGivenFile(midiView, file);
		return null;
	}

	private void togglePlayForGivenFile(FileView midiView, IFile fileToPlay) {
		if (midiView != null) {
			boolean waitForViewToOpen = false;
			if (fileToPlay != null) {
				midiView.show(fileToPlay);
				waitForViewToOpen = true;
			}
			IFileViewType<?> type = midiView.getType();
			for (int i = 0; i < 3; i++) {
				MidiViewPage page = ((MidiViewType) type).getPage();
				if (page != null) {
					page.togglePlayback();
					return;
				} else if (waitForViewToOpen) {
					//page may be null if view was closed and link toggle is disabled
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
					}
				}
			}
		}
	}

	private FileView getMidiFileView(IWorkbenchPart part) {
		if (part instanceof FileView) {
			FileView fileView = (FileView) part;
			IFileViewType<?> fileViewType = fileView.getType();
			if (fileViewType instanceof MidiViewType) {
				return fileView;
			}
		}
		return null;
	}

	private IFile getMidiFileForSelection(ExecutionEvent event) {
		IFile file = null;
		IWorkbenchPart part = HandlerUtil.getActivePart(event);
		if (part instanceof EditorPart) {
			IEditorInput input = ((EditorPart) part).getEditorInput();
			file = input.getAdapter(IFile.class);
		} else if (part instanceof FileView) {
			file = ((FileView) part).getFile();
		} else {
			ISelection selection = part.getSite().getSelectionProvider().getSelection();
			if (selection instanceof IStructuredSelection) {
				IStructuredSelection structuredSelection = (IStructuredSelection) selection;
				if (structuredSelection.size() == 1) {
					Object firstElement = structuredSelection.getFirstElement();
					if (firstElement instanceof IFile) {
						file = (IFile) firstElement;
					}
				}
			}
		}
		if (file != null) {
			if (!MidiViewType.EXTENSION.equals(file.getFileExtension())) {
				file = ResourceUtils.replaceExtension(file, MidiViewType.EXTENSION);
			}
			if (!file.exists()) {
				file = null;
			}
		}
		return file;
	}
}