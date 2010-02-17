package org.eclipse.util;

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

}
