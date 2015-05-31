package org.eclipse.util;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.dialogs.PopupDialog;
import org.eclipse.jface.text.IDocument;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditorPreferenceConstants;

public class TextEditorUtils {

	private TextEditorUtils() {
	}

	/**
	 * Opens the file given by an editor input in a text editor and reveals the
	 * character at the given position. If the given tab width is 0 or less, the
	 * actual tab width of the text editor is used.
	 */
	public static void revealPosition(IFileEditorInput editorInput, int lineNumber, int columnNumber, int tabWidth) {
		try {
			IFile file = editorInput.getFile();
			if (!file.exists()) {
				openFileNotFoundPopup(file);
				return;
			}
			IWorkbenchPage page = UiUtils.getWorkbenchPage();
			IEditorPart editor = IDE.openEditor(page, file);
			if (editor instanceof TextEditor) {
				TextEditor textEditor = (TextEditor)editor;
				IDocument document = textEditor.getDocumentProvider().getDocument(editorInput);

				int realTabWidth = tabWidth;
				if (realTabWidth <= 0) {
					realTabWidth = EditorsUI.getPreferenceStore().getInt(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_TAB_WIDTH);
				}

				int offset = DocumentUtils.getOffsetOfPosition(document, lineNumber, columnNumber, realTabWidth);
				textEditor.selectAndReveal(offset, 0);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void openFileNotFoundPopup(final IFile file) {
		PopupDialog popup = new PopupDialog(Display.getDefault().getActiveShell(), PopupDialog.INFOPOPUPRESIZE_SHELLSTYLE, true, false, false, false, false, "Problem occurred", null) {

			@Override
			protected Control createDialogArea(Composite parent) {
				Text text = new Text(parent, SWT.MULTI | SWT.READ_ONLY | SWT.WRAP | SWT.NO_FOCUS);
				text.setText("linked file " + file.getName() + " not found");
				return text;
			}

			@Override
			protected Point getInitialLocation(Point initialSize) {
				Point cursorLocation = Display.getDefault().getCursorLocation();
				return new Point(cursorLocation.x + 20, cursorLocation.y);
			}
		};
		popup.open();
	}
}
