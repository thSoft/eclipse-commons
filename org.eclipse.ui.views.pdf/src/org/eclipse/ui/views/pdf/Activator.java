package org.eclipse.ui.views.pdf;

import javafx.embed.swing.JFXPanel;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

public class Activator extends AbstractUIPlugin {

	private static Activator instance;
	private boolean javafxRuntimeAvailable=true;
	public static final String MISSING_JVM_ARGUMENT_ERROR="jfxrt.jar seems not to be on the class path; try adding\n-Dorg.osgi.framework.bundle.parent=ext\nas JVM start parameter to the eclipse.ini";

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		instance = this;
		ensureToolkitInitialized();
	}

	private void ensureToolkitInitialized(){
		try {
			initializeToolkit();
		} catch (NoClassDefFoundError e) {
			javafxRuntimeAvailable=false;
			logError(MISSING_JVM_ARGUMENT_ERROR, e);
		}
	}

	//according to numerous StackOverflow questions and blog posts
	//this is a recommended way for initializing the toolkit
	/**
	 * @throws NoClassDefFoundError if jfxrt.jar is not on the class path
	 * */
	public static final void initializeToolkit(){
		new JFXPanel();
	}

	public boolean isJavaFxRuntimeAvailable(){
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