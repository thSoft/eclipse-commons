package org.eclipse.util;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;

public class DocumentUtils {

	private DocumentUtils() {
	}

	/**
	 * Calculates the offset of the position specified by a line and a column
	 * number. Line and column numbering starts from 0.
	 */
	public static int getOffsetOfPosition(IDocument document, int lineNumber, int columnNumber) throws BadLocationException {
		return document.getLineOffset(lineNumber) + columnNumber;
	}

}
