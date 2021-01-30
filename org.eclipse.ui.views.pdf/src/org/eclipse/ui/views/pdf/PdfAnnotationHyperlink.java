package org.eclipse.ui.views.pdf;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.util.TextEditorUtils;

public class PdfAnnotationHyperlink extends Composite {

	private static MouseListener LISTENER = new MouseAdapter() {
		@Override
		public void mouseDown(MouseEvent e) {
			Object source = e.getSource();
			if (source instanceof PdfAnnotationHyperlink) {
				PdfAnnotation annotation = ((PdfAnnotationHyperlink) source).annotation;
				if (annotation != null) {
					if (annotation.fileURI != null) {
						TextEditorUtils.revealPosition(annotation.fileURI, annotation.lineNumber,
								annotation.columnNumber, 1);
					} else if (annotation.file != null) {
						TextEditorUtils.revealPosition(annotation.file, annotation.lineNumber, annotation.columnNumber,
								1);
					}
				}
			}
		}
	};

	private PdfAnnotation annotation;

	public PdfAnnotationHyperlink(Composite parent, final PdfAnnotation annotation) {
		super(parent, SWT.TRANSPARENT | SWT.NO_BACKGROUND);
		this.annotation = annotation;
		setCursor(Display.getDefault().getSystemCursor(SWT.CURSOR_HAND));
		addMouseListener(LISTENER);
	}

}