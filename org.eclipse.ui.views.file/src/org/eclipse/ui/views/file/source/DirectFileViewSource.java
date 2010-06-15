package org.eclipse.ui.views.file.source;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.views.file.Activator;
import org.eclipse.ui.views.file.FileView;
import org.eclipse.util.FileSelectionDialog;

/**
 * A source for a file that the user can choose by browsing the workspace.
 */
public class DirectFileViewSource extends AbstractFileViewSource {

	private String getPathPreference() {
		return getFileView().getViewSite().getId() + ".Path"; //$NON-NLS-1$
	}

	@Override
	public void init(FileView fileView, boolean startup) {
		super.init(fileView, startup);
		IPreferenceStore preferenceStore = Activator.getInstance().getPreferenceStore();
		if (startup) {
			String path = preferenceStore.getString(getPathPreference());
			IResource resource = ResourcesPlugin.getWorkspace().getRoot().findMember(new Path(path));
			if ((resource != null) && (resource instanceof IFile)) {
				getFileView().show((IFile)resource);
			}
		} else {
			Shell shell = Display.getDefault().getActiveShell();
			FileSelectionDialog dialog = new FileSelectionDialog(shell, getFileView().getExtensions());
			IFile file = dialog.selectFile();
			if (file != null) {
				getFileView().show(file);
				preferenceStore.setValue(getPathPreference(), file.getFullPath().toString());
			} else {
				getFileView().hide();
				preferenceStore.setToDefault(getPathPreference());
			}
		}
	}

	@Override
	public String getName() {
		return "Browse...";
	}

	@Override
	public String getLongName() {
		return "Specified file from the workspace";
	}

}
