package org.eclipse.util;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.IHyperlink;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditorPreferenceConstants;

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
	 * number. If it is 0 or less, the actual tab width of the text editor is
	 * used.
	 */
	private int tabWidth;

	public TextEditorHyperlink(IFileEditorInput editorInput, int lineNumber, int columnNumber) {
		this.editorInput = editorInput;
		this.lineNumber = lineNumber;
		this.columnNumber = columnNumber;
	}

	public void linkActivated() {
		try {
			IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
			IFile file = editorInput.getFile();
			IEditorPart editor = IDE.openEditor(page, file);
			if (editor instanceof TextEditor) {
				TextEditor textEditor = (TextEditor)editor;
				IDocument document = textEditor.getDocumentProvider().getDocument(editorInput);

				int tabWidth = getTabWidth();
				if (tabWidth <= 0) {
					tabWidth = EditorsUI.getPreferenceStore().getInt(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_TAB_WIDTH);
				}

				int offset = DocumentUtils.getOffsetOfPosition(document, lineNumber, columnNumber, tabWidth);
				textEditor.selectAndReveal(offset, 0);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
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
