package org.eclipse.ui.views.pdf;

import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.URIUtil;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.util.Util;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.util.ImageUtils;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.views.pdf.PdfViewToolbarManager.FitToAction;
import org.jpedal.PdfDecoderFX;
import org.jpedal.exception.PdfException;
import org.jpedal.objects.PdfPageData;
import org.jpedal.objects.acroforms.AcroRenderer;
import org.jpedal.objects.raw.FormObject;
import org.jpedal.objects.raw.PdfArrayIterator;
import org.jpedal.objects.raw.PdfDictionary;
import org.jpedal.objects.raw.PdfObject;

public class PdfViewPage extends ScrolledComposite {

	public PdfViewPage(Composite parent, IFile file) throws PdfException {
		super(parent, SWT.H_SCROLL | SWT.V_SCROLL);
		pdfDisplay = new Composite(this, SWT.NONE);
		pdfDisplay.setBackgroundMode(SWT.INHERIT_FORCE);
		if(pdfDecoder==null){
			pdfDisplay.setLayout(new GridLayout());
			Label errorLabel = new Label(pdfDisplay, SWT.CENTER);
			errorLabel.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true));
			errorLabel.setText(Activator.MISSING_JVM_ARGUMENT_ERROR);
			pdfDisplay.pack(true);
		}else{
			getHorizontalBar().setIncrement(getHorizontalBar().getIncrement() * 4);
			getVerticalBar().setIncrement(getVerticalBar().getIncrement() * 4);
			pdfDisplay.addPaintListener(new HyperlinkHighlightPaintListener());
			setFile(file);
		}
		setShowFocusedControl(true);
		setContent(pdfDisplay);
		PdfViewScrollHandler.fixNegativeOriginMouseScrollBug(this);
	}

	// Rendering

	/**
	 * The control displaying the current page of the PDF file.
	 */
	private final Composite pdfDisplay;

	/**
	 * The PDF engine which renders the pages.
	 */
	private final PdfDecoderFX pdfDecoder = createDecoder();

	private PdfDecoderFX createDecoder(){
		try {
			return new PdfDecoderFX();
		} catch (NoClassDefFoundError e) {
			return null;
		}
	}
	

	private final RenderJob renderJob=new RenderJob();

	private static final boolean IS_MAC=Util.isMac();
	private class RenderJob extends Job{

		private BufferedImage pageAsImage;
		public RenderJob() {
			super("Rendering PDF page");
		}

		public void obtainImage(){
			if(IS_MAC){
				Activator.initializeToolkit();
			}
			pdfDecoder.setPageParameters(getZoom(), getPage());
			try {
				pageAsImage=pdfDecoder.getPageAsImage(getPage());
			} catch (PdfException e) {
				Activator.logError("Can't render PDF page", e);
				pageAsImage=null;
			}
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			if(monitor.isCanceled()||pageAsImage==null){
				return Status.CANCEL_STATUS;
			}

			final BufferedImage awtImage=pageAsImage;
			final Image swtImage = new Image(Display.getDefault(), ImageUtils.convertBufferedImageToImageData(awtImage));
			Display.getDefault().syncExec(new Runnable() {

				@Override
				public void run() {
					if(pdfDisplay.isDisposed()){
						return;
					}
					Image oldImage = pdfDisplay.getBackgroundImage();
					if (oldImage != null) {
						oldImage.dispose();
					}
					pdfDisplay.setBackgroundImage(swtImage);
					int width = awtImage.getWidth();
					int height = awtImage.getHeight();
					pdfDisplay.setSize(width, height);
					align();
					refreshToolbar();
				}

			});
			if(!monitor.isCanceled()){
				loadAnnotationsJob.schedule();
			}
			return monitor.isCanceled()?Status.CANCEL_STATUS:Status.OK_STATUS;
		}
	}

	@Override
	public boolean setFocus() {
		//prevent setting focus to child element (pdf annotation) causing accidental scrolling
		//copied from Control#setFocus
		checkWidget ();
		if ((getStyle() & SWT.NO_FOCUS) != 0) return false;
		return forceFocus ();
	};

	@Override
	public void redraw() {
		if (isFileOpen()) {
			renderJob.cancel();
			loadAnnotationsJob.cancel();
			createHyperlinksJob.cancel();
			waitForJob(loadAnnotationsJob);
			//waiting for renderJob is not necessary - done by loadAnnotationsJob
			renderJob.obtainImage();
			renderJob.schedule();
			waitForJob(renderJob);
			createHyperlinks();
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

	private String getFileName(){
		return getFile().getFullPath().toOSString();
	}

	public void setFile(IFile file) throws PdfException {
		if(pdfDecoder==null){
			return;
		}
		pdfDecoder.openPdfFile(file.getLocation().toOSString());
		int pageToSet=1;
		if (file.equals(this.file)) {
			pageToSet=getPage();
		} else {
			this.file = file;
		}
		resetAnnotationsJob.schedule();
		waitForJob(resetAnnotationsJob);
		setPage(pageToSet);
	}

	public void reload() throws PdfException {
		setFile(getFile());
	}

	public boolean isFileOpen() {
		return pdfDecoder.isOpen();
	}

	public void closeFile() {
		if(pdfDecoder!=null){
			renderJob.cancel();
			waitForJob(renderJob);
			loadAnnotationsJob.cancel();
			waitForJob(loadAnnotationsJob);
			createHyperlinksJob.cancel();
			waitForJob(createHyperlinksJob);
			pdfDecoder.closePdfFile();
		}
		pdfDisplay.dispose();
		this.dispose();
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
		if (isZoomValid(zoom) && (zoom != getZoom())) {
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

	// The intended annotation loading and hyperlink creation lifecycle is as follows.
	// Load all annotations when the score is first opened or the pdf file changes.
	// Create hyperlinks for the currently visible page - as quickly as possible.

	// resetAnnotationsJob: clear the annotations on reload due to file change (noop on initally opening the file)
	// loadAnnotationsJob: loads annotations one page per run, reschedules itself for the next page
	// createHyperlinkJob: transforms annotations to hyperlinks for the current page; if annotations are not yet loaded
	// the page is marked as to be loaded by the next running loadAnnotationsJob

	/**
	 * Map of page numer to hyperlink annotations on that page in the PDF file.
	 */
	private final Map<Integer, List<PdfAnnotation>> annotations = new HashMap<Integer, List<PdfAnnotation>>();
	private Integer pageWithPriorityToLoad=null;

	public PdfAnnotation[] getAnnotationsOnPage(int page) {
		List<PdfAnnotation> loadedAnnotations=annotations.get(page);
		if(loadedAnnotations==null){
			return new PdfAnnotation[0];
		}else{
			return loadedAnnotations.toArray(new PdfAnnotation[0]);
		}
	}

	private final Job resetAnnotationsJob=new Job("Resetting point-and-click hyperlinks"){
		@Override
		public IStatus run(IProgressMonitor monitor) {
			renderJob.cancel();
			loadAnnotationsJob.cancel();
			waitForJob(loadAnnotationsJob);
			annotations.clear();
			return Status.OK_STATUS;
		}
	};

	private static final Charset ISOCHARSET=Charset.forName("ISO-8859-1");//$NON-NLS-1$
	private final Job loadAnnotationsJob = new Job("Loading annotations for point-and-click hyperlinks") {

		@Override
		public IStatus run(IProgressMonitor monitor) {
			waitForJob(renderJob);

			if(monitor.isCanceled()){
				return Status.CANCEL_STATUS;
			}

			Integer page=getNextPageToLoad();
			if(page==null){
				return Status.OK_STATUS;
			} else if(monitor.isCanceled()){
				return Status.CANCEL_STATUS;
			}

			List<PdfAnnotation> annotationsOnPage=getPossiblyIncompleteListOfAnnotationsForPage(page, monitor);

			if(monitor.isCanceled()){
				return Status.CANCEL_STATUS;
			}
			annotations.put(page, annotationsOnPage);
			this.schedule();
			return Status.OK_STATUS;
		}

		private Integer getNextPageToLoad(){
			final Integer currentPriorityPage=pageWithPriorityToLoad;
			int pageCount=getPageCount();
			if(currentPriorityPage!=null && !annotations.containsKey(currentPriorityPage) && currentPriorityPage<=pageCount){
				 return currentPriorityPage;
			}else{
				for(int i=1;i<=pageCount; i++){
					if(!annotations.containsKey(i)){
						return i;
					}
				}
			}
			return null;
		}

		private void addRawObjectToPdfAnnotationList(Integer page, FormObject formObject, List<PdfAnnotation> list, Map<String, IFile> fileCache){
				int subtype = formObject.getParameterConstant(PdfDictionary.Subtype);
				if (subtype == PdfDictionary.Link) {
					PdfObject anchor = formObject.getDictionary(PdfDictionary.A);
					try {
						byte[] uriDecodedBytes = anchor.getTextStreamValue(PdfDictionary.URI).getBytes(ISOCHARSET); 
						URI uri = new URI(new String(uriDecodedBytes));
						if (uri.getScheme().equals("textedit")) { //$NON-NLS-1$
							String[] sections = uri.getPath().split(":"); //$NON-NLS-1$
							String path = (uri.getAuthority() == null ? "" : uri.getAuthority()) + sections[0]; //$NON-NLS-1$
							IFile targetFile=null;
							if(fileCache.containsKey(path)){
								targetFile=fileCache.get(path);
							}else{
								URL url = new URL("file", null, path); //$NON-NLS-1$
								IFile[] files = ResourcesPlugin.getWorkspace().getRoot().findFilesForLocationURI(URIUtil.toURI(url));
								if(files.length>0){
									targetFile=files[0];
								}
								fileCache.put(path, targetFile);
							}
							if (targetFile!=null) {
								PdfAnnotation annotation = new PdfAnnotation();
								annotation.page = page;
								annotation.file = targetFile;
								annotation.lineNumber = Integer.parseInt(sections[1]) - 1;
								annotation.columnNumber = Integer.parseInt(sections[2]); // This value is independent of tab width
								float[] rectangle = formObject.getFloatArray(PdfDictionary.Rect);
								annotation.left = rectangle[0];
								annotation.bottom = rectangle[1];
								annotation.right = rectangle[2];
								annotation.top = rectangle[3];
								list.add(annotation);
							}
						}
					} catch (URISyntaxException e) {
						Activator.logError("Invalid annotation URI", e);
					} catch (ArrayIndexOutOfBoundsException e) {
						Activator.logError("Error while parsing annotation URI", e);
					} catch (MalformedURLException e) {
						Activator.logError("Can't transform URI to URL", e);
					}
				}
		}

		/**
		 * the list is incomplete if the job was cancelled
		 * */
		private List<PdfAnnotation> getPossiblyIncompleteListOfAnnotationsForPage(Integer page, IProgressMonitor monitor){
			AcroRenderer formRenderer = pdfDecoder.getFormRenderer();
			monitor.setTaskName(getFileName()+" page "+page);
			List<PdfAnnotation> annotationsOnPage = new ArrayList<PdfAnnotation>();

			//TODO This getter call accounts for 70-99% of the time spent in this method!!
			//Is there a later more performant jpedal version that can be used?
			//Can the currently used version be patched?
			PdfArrayIterator pdfAnnotations = formRenderer.getAnnotsOnPage(page);

			Map<String, IFile> fileCache=new HashMap<String, IFile>();
			while (!monitor.isCanceled() && pdfAnnotations.hasMoreTokens()) {
				String key = pdfAnnotations.getNextValueAsString(true);
				FormObject rawObject = formRenderer.getFormObject(key);
				addRawObjectToPdfAnnotationList(page, rawObject, annotationsOnPage, fileCache);
			}
			return annotationsOnPage;
		}
	};

	private static void waitForJob(Job job) {
		try {
			job.join();
		} catch (InterruptedException e) {
			Activator.logError("Interrupted while waiting for job", e);
		}
	}

	// Hyperlinks

	/**
	 * The annotation-to-hyperlink mappings.
	 */
	private final Map<PdfAnnotation, PdfAnnotationHyperlink> annotationHyperlinkMap = new HashMap<PdfAnnotation, PdfAnnotationHyperlink>();

	private final Job createHyperlinksJob = new Job("Creating point-and-click hyperlinks") {

		@Override
		public IStatus run(final IProgressMonitor monitor) {
			if(pdfDecoder==null){
				cancel();
			}
			waitForJob(renderJob);
			if(monitor.isCanceled()){
				return Status.CANCEL_STATUS;
			}
			disposeOldHyperlinks();
			annotationHyperlinkMap.clear();
			waitForPageAnnotationsToBeLoaded(monitor);
			if(monitor.isCanceled()){
				return Status.CANCEL_STATUS;
			}

			PdfAnnotation[] annotationsOnPage = getAnnotationsOnPage(page);
			monitor.setTaskName(getFileName() + " page "+page);
			fillAnnotationHyperlinkMap(annotationsOnPage, monitor);

			return monitor.isCanceled()?Status.CANCEL_STATUS:Status.OK_STATUS;
		}

		private void disposeOldHyperlinks(){
			Display.getDefault().syncExec(new Runnable() {

				@Override
				public void run() {
					if(!pdfDisplay.isDisposed()){
						Control[] oldHyperlinks = pdfDisplay.getChildren();
						for (Control oldHyperlink : oldHyperlinks) {
							oldHyperlink.dispose();
						}
					}
				}

			});
		}

		private void waitForPageAnnotationsToBeLoaded(IProgressMonitor monitor){
			while(!annotations.containsKey(page)){
				monitor.setTaskName("waiting for annotations to be loaded");
				if(monitor.isCanceled()){
					return;
				}
				pageWithPriorityToLoad=page;
				if(loadAnnotationsJob.getResult()==Status.CANCEL_STATUS){
					loadAnnotationsJob.schedule();
				}
				waitForJob(loadAnnotationsJob);
			}
		}

		private void fillAnnotationHyperlinkMap(final PdfAnnotation[] annotationsOnPage, final IProgressMonitor monitor){
			Display.getDefault().syncExec(new Runnable() {
				@Override
				public void run() {
					for (final PdfAnnotation annotation : annotationsOnPage) {
						if (monitor.isCanceled()) {
							return;
						}
						if(!pdfDisplay.isDisposed()){
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
			});
		}
	};

	/**
	 * Creates point-and-click hyperlinks from the hyperlink annotations on the
	 * current page.
	 */
	protected void createHyperlinks() {
		createHyperlinksJob.cancel();
		createHyperlinksJob.schedule();
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
		waitForJob(renderJob);
		waitForJob(createHyperlinksJob);
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
