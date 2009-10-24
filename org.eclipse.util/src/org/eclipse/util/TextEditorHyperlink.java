package org.eclipse.util;

import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.console.IHyperlink;

/**
 * A hyperlink that links to a specified position in a text editor.
 */
public class TextEditorHyperlink implements IHyperlink {

	/**
	 * The input of the text editor to be opened.
	 */
	private final IFileEditorInput editorInput;

	/**
	 * The number of the line to link to (0-based).
	 */
	private final int lineNumber;

	/**
	 * The number of the column to link to (0-based).
	 */
	private final int columnNumber;

	/**
	 * The tab width which is taken into account when interpreting the column
	 * number.
	 */
	private int tabWidth;

	public TextEditorHyperlink(IFileEditorInput editorInput, int lineNumber, int columnNumber) {
		this.editorInput = editorInput;
		this.lineNumber = lineNumber;
		this.columnNumber = columnNumber;
	}

	public void linkActivated() {
		TextEditorUtils.revealPosition(editorInput, lineNumber, columnNumber, tabWidth);
	}

	public void linkEntered() {
	}

	public void linkExited() {
	}

	public void setTabWidth(int tabWidth) {
		this.tabWidth = tabWidth;
	}

	public int getTabWidth() {
		return tabWidth;
	}

}
