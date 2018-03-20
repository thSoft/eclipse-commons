package org.eclipse.util;

import java.io.File;
import java.net.URI;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.dialogs.PopupDialog;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditorPreferenceConstants;

public class TextEditorUtils {

	private TextEditorUtils() {
	}

	/**
	 * use revealPosition(IFile file, int lineNumber, int columnNumber, int
	 * tabWidth)
	 *
	 * Opens the file given by an editor input in a text editor and reveals the
	 * character at the given position. If the given tab width is 0 or less, the
	 * actual tab width of the text editor is used.
	 */
	@Deprecated
	public static void revealPosition(IFileEditorInput editorInput, int lineNumber, int columnNumber, int tabWidth) {
		revealPosition(editorInput.getFile(), lineNumber, columnNumber, tabWidth);
	}

	public static void revealPosition(URI fileURI, int lineNumber, int columnNumber, int tabWidth) {
		File file = new File(fileURI);
		if (!file.exists()) {
			openErrorPopup(fileNotFoundError(file.getName()));
		} else {
			//TODO try to obtain IFile from file and delegate when successful?
			IEditorDescriptor editorDescriptor = PlatformUI.getWorkbench().getEditorRegistry().getDefaultEditor(file.getName());
			if (editorDescriptor == null) {
				openErrorPopup(noEditorFoundError(file.getName()));
			} else {
				IWorkbenchPage page = UiUtils.getWorkbenchPage();
				try {
					IEditorPart editor = IDE.openEditor(page, fileURI.normalize(), editorDescriptor.getId(), true);
					revealPosition(editor, lineNumber, columnNumber, tabWidth);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static void revealPosition(IFile file, int lineNumber, int columnNumber, int tabWidth) {
		try {
			if (!file.exists()) {
				openErrorPopup(fileNotFoundError(file.getName()));
				return;
			}
			IWorkbenchPage page = UiUtils.getWorkbenchPage();
			IEditorPart editor = IDE.openEditor(page, file);
			revealPosition(editor, lineNumber, columnNumber, tabWidth);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void revealPosition(IEditorPart editor, int lineNumber, int columnNumber, int tabWidth) throws BadLocationException {
		if (editor instanceof TextEditor) {
			TextEditor textEditor = (TextEditor)editor;
			IDocument document = textEditor.getDocumentProvider().getDocument(editor.getEditorInput());

			int realTabWidth = tabWidth;
			if (realTabWidth <= 0) {
				realTabWidth = EditorsUI.getPreferenceStore().getInt(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_TAB_WIDTH);
			}

			int offset = DocumentUtils.getOffsetOfPosition(document, lineNumber, columnNumber, realTabWidth);
			textEditor.selectAndReveal(offset, 0);
		}
	}

	private static String fileNotFoundError(String fileName) {
		return "linked file " + fileName + " not found";
	}

	private static String noEditorFoundError(String fileName) {
		return "no editor found for " + fileName;
	}

	private static void openErrorPopup(final String errorMessage) {
		PopupDialog popup = new PopupDialog(Display.getDefault().getActiveShell(), PopupDialog.INFOPOPUPRESIZE_SHELLSTYLE, true, false, false, false, false, "Problem occurred", null) {

			@Override
			protected Control createDialogArea(Composite parent) {
				Text text = new Text(parent, SWT.MULTI | SWT.READ_ONLY | SWT.WRAP | SWT.NO_FOCUS);
				text.setText(errorMessage);
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
