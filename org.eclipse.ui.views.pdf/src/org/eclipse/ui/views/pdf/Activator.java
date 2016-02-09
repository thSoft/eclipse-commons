package org.eclipse.ui.views.pdf;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.jpedal.eclipse.ToolkitUtil;
import org.osgi.framework.BundleContext;

public class Activator extends AbstractUIPlugin {

	private static Activator instance;
	private boolean javafxRuntimeAvailable=true;

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		instance = this;
		ensureToolkitInitialized();
	}

	private void ensureToolkitInitialized(){
		try {
			ToolkitUtil.initializeToolkit();
		} catch (NoClassDefFoundError e) {
			javafxRuntimeAvailable=false;
			logError("jfxrt.jar seems not to be on the class path; try adding -Dorg.osgi.framework.bundle.parent=ext as jvm start parameter to the eclipse.ini", e);
		}
	}

	public boolean isJavaFxRuntimeAvalilable(){
		return javafxRuntimeAvailable;
	}

	/**
	 * Returns the shared plug-in instance.
	 */
	public static Activator getInstance() {
		return instance;
	}

	/**
	 * Returns the plug-in's identifier.
	 */
	public static String getId() {
		return getInstance().getBundle().getSymbolicName();
	}

	/**
	 * Returns an image descriptor for the image file at the given plug-in
	 * relative path.
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(getId(), path);
	}

	/**
	 * Logs an exception with a message.
	 */
	public static void logError(String message, Throwable throwable) {
		getInstance().getLog().log(new Status(IStatus.ERROR, getId(), message, throwable));
	}

}