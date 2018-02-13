package org.eclipse.ui.views.pdf;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.util.TextEditorUtils;

public class PdfAnnotationHyperlink extends Composite {

	public PdfAnnotationHyperlink(Composite parent, final PdfAnnotation annotation) {
		super(parent, SWT.NO_BACKGROUND);
		setCursor(new Cursor(Display.getDefault(), SWT.CURSOR_HAND));
		final FileEditorInput editorInput = new FileEditorInput(annotation.file);
		addMouseListener(new MouseAdapter() {

			@Override
			public void mouseDown(MouseEvent e) {
				TextEditorUtils.revealPosition(editorInput, annotation.lineNumber, annotation.columnNumber, 1);
			}

		});
	}

}
