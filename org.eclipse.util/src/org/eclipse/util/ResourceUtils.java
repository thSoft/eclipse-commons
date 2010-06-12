package org.eclipse.util;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

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
	 * is one of the given extensions. The extensions are tried in the specified
	 * order; if none of them works, null is returned.
	 */
	public static IFile replaceExtension(IFile file, String[] extensions) {
		for (String extension : extensions) {
			String newName = MessageFormat.format("{0}.{1}", file.getName(), extension); //$NON-NLS-1$
			IResource newResource = file.getParent().findMember(newName);
			if (newResource instanceof IFile) {
				return (IFile)newResource;
			}
		}
		return null;
	}

}
