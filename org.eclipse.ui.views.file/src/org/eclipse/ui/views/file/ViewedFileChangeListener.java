package org.eclipse.ui.views.file;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.util.UiUtils;

/**
 * Refreshes the file views that display files that were changed in the
 * workspace.
 */
public class ViewedFileChangeListener implements IResourceChangeListener {

	private static final IResourceDeltaVisitor VISITOR = new IResourceDeltaVisitor() {

		@Override
		public boolean visit(IResourceDelta delta) throws CoreException {
			final IResource resource = delta.getResource();
			final String extension = resource.getFileExtension();
			if ((resource instanceof IFile) && (extension != null)) {
				Display.getDefault().asyncExec(new Runnable() { // The view can be accessed only in UI thread

					@Override
					public void run() {
						IWorkbenchPage workbenchPage = UiUtils.getWorkbenchPage();
						if (workbenchPage != null) {
							for (IViewReference viewReference : workbenchPage.getViewReferences()) {
								IViewPart view = viewReference.getView(false);
								if (view instanceof FileView) {
									FileView fileView = (FileView)view;
									IFile file = (IFile)resource;
									if (fileView.getExtensions().contains(file.getFileExtension())) {
										fileView.reload(file);
									}
								}
							}
						}
					}

				});
			}
			return true;
		}

	};

	@Override
	public void resourceChanged(IResourceChangeEvent event) {
		try {
			event.getDelta().accept(VISITOR);
		} catch (CoreException e) {
			Activator.logError("Couldn't refresh the file view, try to reopen it", e);
		}
	}

}
