package org.eclipse.ui.views.pdf;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.util.TextEditorUtils;

public class PdfAnnotationHyperlink extends Composite {

	private static int STYLE= getHyperlinkStyle();

	private static int getHyperlinkStyle() {
		String system = System.getProperty("os.name", "unknown").toLowerCase();
		if(system.contains("win")) {
			return SWT.NONE; //prevent potential scrolling problems caused by #32
		}else {
			return SWT.TRANSPARENT | SWT.NO_BACKGROUND;
		}
	}

	public PdfAnnotationHyperlink(Composite parent, final PdfAnnotation annotation) {
		super(parent, STYLE);
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
