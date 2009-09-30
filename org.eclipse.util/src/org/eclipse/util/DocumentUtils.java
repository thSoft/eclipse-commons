package org.eclipse.util;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.filebuffers.ITextFileBufferManager;
import org.eclipse.core.filebuffers.LocationKind;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;

public class DocumentUtils {

	private DocumentUtils() {
	}

	/**
	 * Calculates the offset of the position specified by a line and a column
	 * number, taking the given tab width into account, if it is greater than 1.
	 * Line and column numbering starts from 0.
	 */
	public static int getOffsetOfPosition(IDocument document, int lineNumber, int columnNumber, int tabWidth) throws BadLocationException {
		int offset = document.getLineOffset(lineNumber) + columnNumber;
		if (tabWidth > 1) {
			// Correct by tab width
			int decrement = tabWidth - 1;
			int lineOffset = document.getLineOffset(lineNumber);
			int correctedColumnNumber = columnNumber;
			int spaceRegionSize = 0;
			for (int currentOffset = lineOffset; currentOffset < lineOffset + correctedColumnNumber; currentOffset++) {
				switch (document.getChar(currentOffset)) {
				case '\t':
					int correctedDecrement = decrement - spaceRegionSize;
					offset -= correctedDecrement;
					correctedColumnNumber -= correctedDecrement;
					spaceRegionSize = 0;
					break;
				case ' ':
					spaceRegionSize++;
					break;
				default:
					spaceRegionSize = 0;
				}
			}
		}
		return offset;
	}

	/**
	 * Calculates the offset of the position specified by a line and a column
	 * number. Line and column numbering starts from 0.
	 */
	public static int getOffsetOfPosition(IDocument document, int lineNumber, int columnNumber) throws BadLocationException {
		return getOffsetOfPosition(document, lineNumber, columnNumber, 1);
	}

	/**
	 * Returns a document with the initial contents of a file.
	 */
	public static IDocument getDocumentFromFile(IFile file) throws CoreException {
		ITextFileBufferManager bufferManager = FileBuffers.getTextFileBufferManager();
		bufferManager.connect(file.getFullPath(), LocationKind.IFILE, new NullProgressMonitor());
		ITextFileBuffer textFileBuffer = bufferManager.getTextFileBuffer(file.getFullPath(), LocationKind.IFILE);
		return textFileBuffer.getDocument();
	}

}
