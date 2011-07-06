package org.eclipse.util;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

public class ResourceUtils {

	private ResourceUtils() {
	}

	/**
	 * Returns all files recursively in the given container.
	 */
	public static List<IFile> getAllFiles(IContainer container) {
		List<IFile> files = new ArrayList<IFile>();
		try {
			IResource[] members = container.members();
			for (IResource member : members) {
				if (member instanceof IFile) {
					files.add((IFile)member);
				} else {
					files.addAll(getAllFiles((IContainer)member));
				}
			}
		} catch (CoreException e) {
		}
		return files;
	}

	/**
	 * Returns a file which has the same name as the given file, but its extension
	 * is replaced with the given extension.
	 */
	public static IFile replaceExtension(IFile file, String extension) {
		IPath path = file.getFullPath();
		IPath newPath = path.removeFileExtension();
		final String dot = "."; //$NON-NLS-1$
		if (path.lastSegment().startsWith(dot)) {
			newPath = newPath.addTrailingSeparator().append(dot + extension); // addFileExtension() doesn't work with trailing separator
		} else {
			newPath = newPath.addFileExtension(extension);
		}
		return ResourcesPlugin.getWorkspace().getRoot().getFile(newPath);
	}

}
