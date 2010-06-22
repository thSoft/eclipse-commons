package org.eclipse.emf.util;

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
		} else {
			return null;
		}
	}

}
