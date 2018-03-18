package org.eclipse.ui.views.pdf;

import java.net.URI;

import org.eclipse.core.resources.IFile;

public class PdfAnnotation {

	@Deprecated
	/**
	 * this field will be removed in an upcoming version
	 * */
	public IFile file;

	public URI fileURI;

	public int lineNumber;

	public int columnNumber;

	public int page;

	public float left;

	public float top;

	public float right;

	public float bottom;

}
