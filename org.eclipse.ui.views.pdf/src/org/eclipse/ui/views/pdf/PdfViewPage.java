package org.eclipse.ui.views.pdf;

import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.URIUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.util.ImageUtils;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.views.pdf.PdfViewToolbarManager.FitToAction;
import org.jpedal.PdfDecoder;
import org.jpedal.exception.PdfException;
import org.jpedal.objects.PdfPageData;
import org.jpedal.objects.acroforms.rendering.AcroRenderer;
import org.jpedal.objects.raw.FormObject;
import org.jpedal.objects.raw.PdfArrayIterator;
import org.jpedal.objects.raw.PdfDictionary;
import org.jpedal.objects.raw.PdfObject;

public class PdfViewPage extends ScrolledComposite {

	public PdfViewPage(Composite parent, IFile file) throws PdfException {
		super(parent, SWT.H_SCROLL | SWT.V_SCROLL);
		getHorizontalBar().setIncrement(getHorizontalBar().getIncrement() * 4);
		getVerticalBar().setIncrement(getVerticalBar().getIncrement() * 4);
		setShowFocusedControl(true);

		pdfDisplay = new Composite(this, SWT.NONE);
		pdfDisplay.setBackgroundMode(SWT.INHERIT_FORCE);
		pdfDisplay.addPaintListener(new HyperlinkHighlightPaintListener());
		setContent(pdfDisplay);

		this.file = file;
		reload();
	}

	// Rendering

	/**
	 * The control displaying the current page of the PDF file.
	 */
	private Composite pdfDisplay;

	/**
	 * The PDF engine which renders the pages.
	 */
	private final PdfDecoder pdfDecoder = new PdfDecoder();

	@Override
	public void redraw() {
		if (isFileOpen()) {
			pdfDecoder.setPageParameters(getZoom(), getPage());
			try {
				BufferedImage awtImage = pdfDecoder.getPageAsImage(getPage());
				Image oldImage = pdfDisplay.getBackgroundImage();
				if (oldImage != null) {
					oldImage.dispose();
				}
				Image swtImage = new Image(Display.getDefault(), ImageUtils.convertBufferedImageToImageData(awtImage));
				pdfDisplay.setBackgroundImage(swtImage);
				int width = awtImage.getWidth();
				int height = awtImage.getHeight();
				pdfDisplay.setSize(width, height);
				align();
			} catch (PdfException e) {
				Activator.logError("Can't redraw PDF page", e);
			}
			createHyperlinks();
			refreshToolbar();
		}
	}

	private void align() {
		Rectangle clientArea = getClientArea();
		Point size = pdfDisplay.getSize();
		Point location = pdfDisplay.getLocation();
		int left = location.x < 0 ? location.x : Math.max(0, clientArea.width / 2 - size.x / 2);
		int top = location.y < 0 ? location.y : Math.max(0, clientArea.height / 2 - size.y / 2);
		pdfDisplay.setLocation(left, top);
	}

	@Override
	public void setBounds(Rectangle rect) {
		super.setBounds(rect);
		align();
	}

	@Override
	public void setOrigin(Point origin) {
		super.setOrigin(origin);
		align();
	}

	// File handling

	/**
	 * The open PDF file.
	 */
	private IFile file;

	public IFile getFile() {
		return file;
	}

	public void setFile(IFile file) throws PdfException {
		pdfDecoder.openPdfFile(file.getLocation().toOSString());
		loadAnnotations();
		if (file.equals(this.file)) {
			setPage(getPage());
		} else {
			this.file = file;
			setPage(1);
		}
	}

	public void reload() throws PdfException {
		setFile(getFile());
	}

	public boolean isFileOpen() {
		return pdfDecoder.isOpen();
	}

	public void closeFile() {
		pdfDecoder.closePdfFile();
	}

	// Navigation

	/**
	 * The number of the currently viewed page, 1-based.
	 */
	private int page = 1;

	public int getPage() {
		return page;
	}

	public void setPage(int page) {
		if (page > getPageCount()) {
			this.page = getPageCount();
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
	 * Checks whether the page with the given number exists.
	 */
	public boolean isPageValid(int page) {
		return ((page >= 1) && (page <= getPageCount()));
	}

	// Page info

	/**
	 * Returns the real, zoom-independent width of the current page in PostScript
	 * points.
	 */
	public int getPageWidth() {
		return getPageDimension(false);
	}

	/**
	 * Returns the real, zoom-independent height of the current page in PostScript
	 * points.
	 */
	public int getPageHeight() {
		return getPageDimension(true);
	}

	private int getPageDimension(boolean height) {
		PdfPageData pageData = pdfDecoder.getPdfPageData();
		int page = getPage();
		int rotation = getPageRotation();
		if ((rotation == 90) || (rotation == 270)) {
			return height ? pageData.getMediaBoxWidth(page) : pageData.getMediaBoxHeight(page);
		} else {
			return height ? pageData.getMediaBoxHeight(page) : pageData.getMediaBoxWidth(page);
		}
	}

	/**
	 * Returns the rotation of the page in degrees.
	 */
	public int getPageRotation() {
		return pdfDecoder.getPdfPageData().getRotation(getPage());
	}

	// Zoom

	/**
	 * The current zoom factor.
	 */
	private float zoom = 1;

	public float getZoom() {
		return zoom;
	}

	public void setZoom(float zoom) {
		if (isZoomValid(zoom)) {
			this.zoom = zoom;
			redraw();
		}
	}

	public static final float MIN_SIZE = 16;

	/**
	 * Checks whether the given zoom factor is in a sensible range.
	 */
	public boolean isZoomValid(float zoom) {
		Rectangle screenSize = Display.getDefault().getBounds();
		float newWidth = getPageWidth() * zoom;
		float newHeight = getPageHeight() * zoom;
		boolean tooBig = newWidth > screenSize.width;
		boolean tooSmall = (newWidth < MIN_SIZE) || (newHeight < MIN_SIZE);
		return !(tooBig || tooSmall);
	}

	/**
	 * The currently selected special zoom setting.
	 */
	private FitToAction fitToAction;

	public FitToAction getFitToAction() {
		return fitToAction;
	}

	public void setFitToAction(FitToAction fitToAction) {
		this.fitToAction = fitToAction;
	}

	// Toolbar

	/**
	 * Manages the contributions to the toolbar.
	 */
	private PdfViewToolbarManager toolbar;

	public PdfViewToolbarManager getToolbar() {
		return toolbar;
	}

	public void setToolbar(PdfViewToolbarManager toolbar) {
		this.toolbar = toolbar;
	}

	private void refreshToolbar() {
		if (getToolbar() != null) {
			getToolbar().refresh();
		}
	}

	// Annotations

	/**
	 * The textedit annotations in the PDF file.
	 */
	private final List<PdfAnnotation> annotations = new ArrayList<PdfAnnotation>();

	public PdfAnnotation[] getAnnotations() {
		return annotations.toArray(new PdfAnnotation[0]);
	}

	private void loadAnnotations() {
		annotations.clear();
		AcroRenderer formRenderer = pdfDecoder.getFormRenderer();
		for (int page = 1; page <= getPageCount(); page++) {
			PdfArrayIterator pdfAnnotations = formRenderer.getAnnotsOnPage(page);
			if (pdfAnnotations != null) {
				while (pdfAnnotations.hasMoreTokens()) {
					String key = pdfAnnotations.getNextValueAsString(true);
					Object rawObject = formRenderer.getFormDataAsObject(key);
					if ((rawObject != null) && (rawObject instanceof FormObject)) {
						FormObject formObject = (FormObject)rawObject;
						int subtype = formObject.getParameterConstant(PdfDictionary.Subtype);
						if (subtype == PdfDictionary.Link) {
							PdfObject anchor = formObject.getDictionary(PdfDictionary.A);
							try {
								byte[] uriDecodedBytes = anchor.getTextStreamValue(PdfDictionary.URI).getBytes("ISO-8859-1"); //$NON-NLS-1$
								URI uri = new URI(new String(uriDecodedBytes));
								if (uri.getScheme().equals("textedit")) { //$NON-NLS-1$
									String[] sections = uri.getPath().split(":"); //$NON-NLS-1$
									String path = (uri.getAuthority() == null ? "" : uri.getAuthority()) + sections[0]; //$NON-NLS-1$
									URL url = new URL("file", null, path); //$NON-NLS-1$
									IFile[] files = ResourcesPlugin.getWorkspace().getRoot().findFilesForLocationURI(URIUtil.toURI(url));
									if (files.length > 0) {
										PdfAnnotation annotation = new PdfAnnotation();
										annotation.page = page;
										annotation.file = files[0];
										annotation.lineNumber = Integer.parseInt(sections[1]) - 1;
										annotation.columnNumber = Integer.parseInt(sections[2]); // This value is independent of tab width
										float[] rectangle = formObject.getFloatArray(PdfDictionary.Rect);
										annotation.left = rectangle[0];
										annotation.bottom = rectangle[1];
										annotation.right = rectangle[2];
										annotation.top = rectangle[3];
										annotations.add(annotation);
									}
								}
							} catch (URISyntaxException e) {
								Activator.logError("Invalid annotation URI", e);
							} catch (UnsupportedEncodingException e) {
								Activator.logError("Programming error", e);
							} catch (ArrayIndexOutOfBoundsException e) {
								Activator.logError("Error while parsing annotation URI", e);
							} catch (MalformedURLException e) {
								Activator.logError("Can't transform URI to URL", e);
							}
						}
					}
				}
			}
		}
	}

	// Hyperlinks

	/**
	 * The annotation-to-hyperlink mappings.
	 */
	private final Map<PdfAnnotation, PdfAnnotationHyperlink> annotationHyperlinkMap = new HashMap<PdfAnnotation, PdfAnnotationHyperlink>();

	/**
	 * Creates point-and-click hyperlinks from the form annotations on the current
	 * page.
	 */
	protected void createHyperlinks() {
		for (Control oldHyperlink : pdfDisplay.getChildren()) {
			oldHyperlink.dispose();
		}
		annotationHyperlinkMap.clear();
		for (PdfAnnotation annotation : annotations) {
			if (annotation.page == getPage()) {
				PdfAnnotationHyperlink hyperlink = new PdfAnnotationHyperlink(pdfDisplay, annotation);
				annotationHyperlinkMap.put(annotation, hyperlink);
				float zoom = getZoom();
				float left = annotation.left * zoom;
				float right = annotation.right * zoom;
				float width = Math.abs(right - left);
				float top = annotation.top * zoom;
				float bottom = annotation.bottom * zoom;
				float height = Math.abs(bottom - top);
				Rectangle2D.Float bounds = new Rectangle2D.Float(left, top, width, height);
				float pageWidth = getPageWidth() * zoom;
				float pageHeight = getPageHeight() * zoom;
				transform(bounds, getPageRotation(), pageWidth, pageHeight);
				hyperlink.setBounds((int)bounds.x, (int)bounds.y, (int)bounds.width, (int)bounds.height);
			}
		}
	}

	private void transform(Rectangle2D.Float rectangle, int rotation, float pageWidth, float pageHeight) {
		float x = rectangle.x;
		float y = rectangle.y;
		float width = rectangle.width;
		float height = rectangle.height;
		switch (rotation) {
		case 0:
			rectangle.y = pageHeight - y;
			break;
		case 90:
			rectangle.x = y - height;
			rectangle.y = x - width;
			rectangle.width = height;
			rectangle.height = width;
			break;
		case 180:
			rectangle.x = pageWidth - x - width;
			break;
		case 270:
			rectangle.x = pageHeight - y;
			rectangle.y = x - width;
			rectangle.width = height;
			rectangle.height = width;
			break;
		}
	}

	// Hyperlink highlighting
	// TODO extract

	/**
	 * The currently highlighted hyperlink.
	 */
	private PdfAnnotationHyperlink highlightedHyperlink;

	/**
	 * The space between the highlighted hyperlink and its outline.
	 */
	private static final float HYPERLINK_HIGHLIGHT_PADDING = 3;

	/**
	 * Reveals and highlights the hyperlink of the given annotation.
	 */
	public void highlightAnnotation(PdfAnnotation annotation) {
		setPage(annotation.page);
		PdfAnnotationHyperlink hyperlink = annotationHyperlinkMap.get(annotation);
		if (hyperlink != null) {
			highlightedHyperlink = hyperlink;
			hyperlink.setFocus();
			hyperlinkHighlightAnimator.start();
		}
	}

	private static int hyperlinkHighlightAlpha;

	private static float hyperlinkHighlightPaddingScale;

	private class HyperlinkHighlightPaintListener implements PaintListener {

		@Override
		public void paintControl(PaintEvent e) {
			if ((highlightedHyperlink != null) && !highlightedHyperlink.isDisposed()) {
				e.gc.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_BLUE));
				e.gc.setLineWidth(2);
				Rectangle bounds = highlightedHyperlink.getBounds();
				float padding = HYPERLINK_HIGHLIGHT_PADDING * getZoom() * hyperlinkHighlightPaddingScale;
				float x = bounds.x - padding;
				float y = bounds.y - padding;
				float width = bounds.width + 2 * padding;
				float height = bounds.height + 2 * padding;
				e.gc.setAlpha(hyperlinkHighlightAlpha);
				e.gc.drawRoundRectangle((int)x, (int)y, (int)width, (int)height, (int)padding, (int)padding);
			}
		}

	}

	private enum HyperlinkHighlightAnimatorState {
		FADE_IN {

			private final int MAX_ALPHA = 255;

			private final int ALPHA_STEP = 24;

			private final float INITIAL_PADDING_SCALE = 5;

			@Override
			public void init() {
				hyperlinkHighlightAlpha = 0;
				hyperlinkHighlightPaddingScale = INITIAL_PADDING_SCALE;
			}

			@Override
			public void step() {
				hyperlinkHighlightAlpha = Math.min(MAX_ALPHA, hyperlinkHighlightAlpha + ALPHA_STEP);
				hyperlinkHighlightPaddingScale -= (INITIAL_PADDING_SCALE - 1) / (MAX_ALPHA / ALPHA_STEP);
			}

			@Override
			public boolean isReady() {
				return hyperlinkHighlightAlpha >= MAX_ALPHA;
			}

		},
		WAIT {

			private int delay;

			@Override
			public void init() {
				delay = 192;
			}

			@Override
			public void step() {
				delay--;
			}

			@Override
			public boolean isReady() {
				return delay == 0;
			}
		},
		FADE_OUT {

			private final int ALPHA_STEP = 2;

			@Override
			public void init() {
			}

			@Override
			public void step() {
				hyperlinkHighlightAlpha = Math.max(0, hyperlinkHighlightAlpha - ALPHA_STEP);
			}

			@Override
			public boolean isReady() {
				return hyperlinkHighlightAlpha == 0;
			}

		};

		public abstract void init();

		public abstract void step();

		public abstract boolean isReady();

	}

	private final HyperlinkHighlightAnimator hyperlinkHighlightAnimator = new HyperlinkHighlightAnimator();

	private class HyperlinkHighlightAnimator implements Runnable {

		private static final int INTERVAL = 10;

		private int stateIndex;

		private final HyperlinkHighlightAnimatorState[] states = HyperlinkHighlightAnimatorState.values();

		public void start() {
			stateIndex = 0;
			initState();
			Display.getDefault().timerExec(0, this);
		}

		private void initState() {
			states[stateIndex].init();
		}

		@Override
		public void run() {
			HyperlinkHighlightAnimatorState state = states[stateIndex];
			if (!state.isReady()) {
				state.step();
				pdfDisplay.redraw();
			} else {
				if (stateIndex < states.length - 1) {
					stateIndex++;
					initState();
				} else {
					highlightedHyperlink = null;
					return;
				}
			}
			Display.getDefault().timerExec(INTERVAL, this);
		}

	}

}
