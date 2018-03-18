package org.eclipse.emf.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;

public class ResourceUtils {

	private ResourceUtils() {
	}

	/**
	 * Returns the platform resource corresponding to the given EMF resource.
	 */
	public static IResource convertEResourceToPlatformResource(Resource eResource) {
		return findPlatformResource(eResource.getURI());
	}

	/**
	 * Returns the platform resource with the given EMF URI.
	 */
	public static IResource findPlatformResource(URI uri) {
		if (uri.isPlatformResource()) {
			return ResourcesPlugin.getWorkspace().getRoot().findMember(uri.toPlatformString(true));
		} else if (uri.isFile()) {
			java.net.URI fileURI = java.net.URI.create(uri.toString());
			List<IResource> candidates = findPlatformResources(fileURI);
			if (!candidates.isEmpty()) {
				return candidates.get(0);
			}
		}
		return null;
	}

	/**
	 * returns a list of (existing) platform resource for the given file uri
	 * 
	 * returns an empty list if no such resource exists or if the given URI is not a
	 * file uri
	 */
	public static List<IResource> findPlatformResources(java.net.URI fileURI) {
		List<IResource> result = new ArrayList<>();
		File file = new File(fileURI.getRawPath());// remove potential fragment
		if (file.exists()) {
			IResource[] candidates = null;
			if (file.isDirectory()) {
				candidates = ResourcesPlugin.getWorkspace().getRoot().findContainersForLocationURI(fileURI);
			} else if (file.isFile()) {
				candidates = ResourcesPlugin.getWorkspace().getRoot().findFilesForLocationURI(fileURI);
			}
			for (IResource candidate : candidates) {
				if (candidate.exists()) {
					result.add(candidate);
				}
			}
		}
		return result;
	}
}