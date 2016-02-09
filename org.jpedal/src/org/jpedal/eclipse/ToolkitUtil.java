package org.jpedal.eclipse;
import javafx.embed.swing.JFXPanel;

public class ToolkitUtil {

	//according to numerous StackOverflow questions and blog posts
	//this is a recommended way for initializing the toolkit
	/**
	 * @throws NoClassDefFoundError if jfxrt.jar is not on the class path
	 * */
	public static final void initializeToolkit(){
		new JFXPanel();
	}
}
