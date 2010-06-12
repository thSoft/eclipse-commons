package org.eclipse.emf.util;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;

public class ResourceUtils {

	private ResourceUtils() {
	}

	public static IResource convertEResourceToPlatformResource(Resource eResource) {
		URI uri = eResource.getURI();
		if (uri.isPlatformResource()) {
			return ResourcesPlugin.getWorkspace().getRoot().findMember(uri.toPlatformString(true));
		} else {
			return null;
		}
	}

}
