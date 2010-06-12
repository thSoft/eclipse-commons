package org.eclipse.util;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.FilteredResourcesSelectionDialog;

/**
 * A dialog for selecting files in the workspace with a specific extension.
 */
public class FileSelectionDialog extends FilteredResourcesSelectionDialog {

	private final List<String> extensions;

	public FileSelectionDialog(Shell shell, boolean multi, IContainer container, String[] extensions) {
		super(shell, multi, container, IResource.FILE);
		this.extensions = Arrays.asList(extensions);
	}

	public FileSelectionDialog(Shell shell, String[] extensions) {
		this(shell, false, ResourcesPlugin.getWorkspace().getRoot(), extensions);
	}

	@Override
	protected IStatus validateItem(Object item) {
		if ((item instanceof IFile) && extensions.contains(((IFile)item).getFileExtension())) {
			return Status.OK_STATUS;
		} else {
			String message = null;
			if (getSelectedItems().size() > 0) {
				if (extensions.size() == 1) {
					message = MessageFormat.format("Please select a file with the extension {0}", extensions.get(0));
				} else {
					message = MessageFormat.format("Please select a file with one of the following extensions: {0}", extensions.get(0));
				}
			}
			return new Status(IStatus.ERROR, ResourcesPlugin.PI_RESOURCES, message);
		}

	}

	public IFile selectFile() {
		if (open() == Window.OK) {
			Object result = getFirstResult();
			if (result instanceof IFile) {
				return (IFile)result;
			}
		}
		return null;
	}

}
