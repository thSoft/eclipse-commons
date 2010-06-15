package org.eclipse.ui.views.pdf;

import java.awt.image.BufferedImage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.util.ImageUtils;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.jpedal.PdfDecoder;
import org.jpedal.exception.PdfException;

public class PdfViewPage extends ScrolledComposite {

	/**
	 * The composite which is responsible for the center alignment.
	 */
	private Composite outerContainer;

	/**
	 * The composite which contains the PDF display.
	 */
	private Composite innerContainer;

	/**
	 * The label displaying the current page of the PDF file or an error message
	 * if the PDF is missing.
	 */
	private Label pdfDisplay;

	public PdfViewPage(Composite parent, String filename) throws PdfException {
		super(parent, SWT.V_SCROLL | SWT.H_SCROLL);
		setExpandHorizontal(true);
		setExpandVertical(true);
		getVerticalBar().setIncrement(getVerticalBar().getIncrement() * 4);
		getHorizontalBar().setIncrement(getHorizontalBar().getIncrement() * 4);

		outerContainer = new Composite(this, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		outerContainer.setLayout(layout);
		setContent(outerContainer);

		innerContainer = new Composite(outerContainer, SWT.NONE);
		innerContainer.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true));

		pdfDisplay = new Label(innerContainer, SWT.CENTER);

		this.filename = filename;

		reload();
	}

	/**
	 * The PDF engine which renders the pages.
	 */
	protected final PdfDecoder pdfDecoder = new PdfDecoder();

	@Override
	public void redraw() {
		if (isFileOpen()) {
			pdfDecoder.setPageParameters(zoom, getPage());
			try {
				BufferedImage awtImage = pdfDecoder.getPageAsImage(getPage());
				Image swtImage = new Image(Display.getDefault(), ImageUtils.convertBufferedImageToImageData(awtImage));
				pdfDisplay.setImage(swtImage);
				refreshLayout();
			} catch (PdfException e) {
				Activator.logError("Can't redraw PDF page", e);
			}
		}
	}

	/**
	 * Whenever the page size changes, this method has to be called to achieve the
	 * correct layout.
	 */
	protected void refreshLayout() {
		Point size = pdfDisplay.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		pdfDisplay.setBounds(0, 0, size.x, size.y);
		outerContainer.layout();
		setMinSize(size);
	}

	/**
	 * The full path of the open PDF file in the local file system.
	 */
	private String filename;

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) throws PdfException {
		pdfDecoder.openPdfFile(filename);
		if (filename.equals(this.filename)) {
			setPage(getPage());
		} else {
			this.filename = filename;
			setPage(1);
		}
	}

	public void reload() throws PdfException {
		setFilename(getFilename());
	}

	public void closeFile() {
		pdfDecoder.closePdfFile();
	}

	public boolean isFileOpen() {
		return pdfDecoder.isOpen();
	}

	/**
	 * The number of the currently viewed page, 1-based.
	 */
	private int page = 1;

	public int getPage() {
		return page;
	}

	public void setPage(int page) {
		if (page > pdfDecoder.getPageCount()) {
			this.page = pdfDecoder.getPageCount();
		} else if (page < 1) {
			this.page = 1;
		} else {
			this.page = page;
		}
		redraw();
	}

	/**
	 * Returns the number of pages in the PDF file.
	 */
	public int getPageCount() {
		return pdfDecoder.getPageCount();
	}

	/**
	 * Returns the real, zoom-independent height of the current page in PostScript
	 * points.
	 */
	public int getPageHeight() {
		return pdfDecoder.getPdfPageData().getMediaBoxHeight(getPage());
	}

	/**
	 * Returns the real, zoom-independent width of the current page in PostScript
	 * points.
	 */
	public int getPageWidth() {
		return pdfDecoder.getPdfPageData().getMediaBoxWidth(getPage());
	}

	/**
	 * The current zoom factor.
	 */
	private float zoom = 1;

	public float getZoom() {
		return zoom;
	}

	/**
	 * Checks whether the given zoom factor is in a sensible range.
	 */
	public boolean isZoomValid(float zoom) {
		Rectangle screenSize = Display.getDefault().getBounds();
		float newWidth = getPageWidth() * zoom;
		float newHeight = getPageHeight() * zoom;
		boolean tooBig = newWidth > screenSize.width;
		boolean tooSmall = (newWidth < 1) || (newHeight < 1);
		return !(tooBig || tooSmall);
	}

	public void setZoom(float zoom) {
		if (isZoomValid(zoom)) {
			this.zoom = zoom;
			redraw();
		}
	}

}
