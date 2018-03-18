package org.eclipse.ui.views.pdf;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.util.TextEditorUtils;

public class PdfAnnotationHyperlink extends Composite {

	public PdfAnnotationHyperlink(Composite parent, final PdfAnnotation annotation) {
		super(parent, SWT.TRANSPARENT | SWT.NO_BACKGROUND); // Both are needed for correct cross-platform behavior
		setCursor(new Cursor(Display.getDefault(), SWT.CURSOR_HAND));
		addMouseListener(new MouseAdapter() {

			@Override
			public void mouseDown(MouseEvent e) {
				if(annotation.fileURI!=null) {
					TextEditorUtils.revealPosition(annotation.fileURI, annotation.lineNumber, annotation.columnNumber, 1);
				}else if(annotation.file!=null) {
					TextEditorUtils.revealPosition(annotation.file, annotation.lineNumber, annotation.columnNumber, 1);
				}
			}
		});
	}

}
