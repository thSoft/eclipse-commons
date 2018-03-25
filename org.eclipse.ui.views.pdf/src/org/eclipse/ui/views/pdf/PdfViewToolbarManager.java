package org.eclipse.ui.views.pdf;

import java.text.MessageFormat;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.ControlContribution;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.views.file.ToolbarSubmenuAction;

public class PdfViewToolbarManager {

	private static final String COMMAND_ID_PREFIX="org.eclipse.ui.views.pdf.";

	private PdfViewPage page;

	public void setPage(PdfViewPage page) {
		this.page = page;
	}

	public PdfViewPage getPage() {
		return page;
	}

	private final FitToAction fitToPageAction = new FitToAction("Page", "Page", true, true); //$NON-NLS-2$

	private final FitToAction fitToWidthAction = new FitToAction("Width", "Width", true, false); //$NON-NLS-2$

	private final FitToAction fitToHeightAction = new FitToAction("Height", "Height", false, true); //$NON-NLS-2$

	private final IContributionItem[] contributions = new IContributionItem[] {
		new ActionContributionItem(new FirstPageAction()),
		new ActionContributionItem(new PreviousPageAction()),
		new CurrentPageContribution(), new PageCountContribution(),
		new ActionContributionItem(new NextPageAction()),
		new ActionContributionItem(new LastPageAction()), new Separator(),
		getZoomMenu()
	};

	public IContributionItem[] getToolbarContributions() {
		return contributions;
	}

	public void refresh() {
		for (IContributionItem contribution : contributions) {
			contribution.update();
		}
	}

	private static final String ICON_PATH = "icons/"; //$NON-NLS-1$

	private static final String ID_PREFIX = ".toolbars."; //$NON-NLS-1$

	// Navigation

	public abstract class NavigateAction extends Action {

		public NavigateAction(String tooltipTextFragment, String iconNameFragment) {
			setToolTipText(MessageFormat.format("Go To {0} Page", tooltipTextFragment));
			setImageDescriptor(Activator.getImageDescriptor(ICON_PATH + MessageFormat.format("{0}Page.png", iconNameFragment))); //$NON-NLS-1$
			setActionDefinitionId(COMMAND_ID_PREFIX+"Navigate"+iconNameFragment);
		}

		@Override
		public void run() {
			getPage().setPage(getNewPage());
		}

		protected abstract int getNewPage();

		@Override
		public boolean isEnabled() {
			int newPage=getNewPage();
			return getPage()!=null && getPage().isPageValid(newPage) && getPage().getPage()!=newPage;
		}

	}

	public class FirstPageAction extends NavigateAction {

		public FirstPageAction() {
			super("First", "First"); //$NON-NLS-2$
		}

		@Override
		protected int getNewPage() {
			return 1;
		}

	}

	public class PreviousPageAction extends NavigateAction {

		public PreviousPageAction() {
			super("Previous", "Previous"); //$NON-NLS-2$
		}

		@Override
		protected int getNewPage() {
			return getPage().getPage() - 1;
		}

	}

	public class NextPageAction extends NavigateAction {

		public NextPageAction() {
			super("Next", "Next"); //$NON-NLS-2$
		}

		@Override
		protected int getNewPage() {
			return getPage().getPage() + 1;
		}

	}

	public class LastPageAction extends NavigateAction {

		public LastPageAction() {
			super("Last", "Last"); //$NON-NLS-2$
		}

		@Override
		protected int getNewPage() {
			return getPage().getPageCount();
		}

	}

	public class CurrentPageContribution extends ControlContribution {

		private Text text;

		public CurrentPageContribution() {
			super(Activator.getId() + ID_PREFIX + "currentPage"); //$NON-NLS-1$
		}

		@Override
		protected int computeWidth(Control control) {
			return control.computeSize(20, SWT.DEFAULT).x;
		}

		@Override
		protected Control createControl(Composite parent) {
			text = new Text(parent, SWT.SINGLE | SWT.BORDER);
			text.setToolTipText("Current page");
			text.addVerifyListener(new VerifyListener() {

				@Override
				public void verifyText(VerifyEvent event) {
					for (int i = 0; i < event.text.length(); i++) {
						if (!Character.isDigit(event.text.charAt(i))) {
							event.doit = false;
							return;
						}
					}
				}

			});
			text.addKeyListener(new KeyAdapter() {

				@Override
				public void keyPressed(KeyEvent event) {
					if(getPage()==null){
						return;
					}
					if ((event.keyCode == SWT.CR || event.keyCode==SWT.KEYPAD_CR) && (text.getText().length() > 0)) {
						getPage().setPage(Integer.parseInt(text.getText()));
					}
				}

			});
			text.addMouseListener(new MouseAdapter() {

				@Override
				public void mouseDown(MouseEvent e) {
					text.setFocus();
				}

			});
			update();
			return text;
		}

		@Override
		public void update() {
			if ((text != null) && !text.isDisposed()) {
				text.setText(String.valueOf(getPage().getPage()));
			}
		}

	}

	public class PageCountContribution extends ControlContribution {

		private Label label;

		public PageCountContribution() {
			super(Activator.getId() + ID_PREFIX + "pageCount"); //$NON-NLS-1$
		}

		@Override
		protected Control createControl(Composite parent) {
			label = new Label(parent, SWT.CENTER);
			update();
			return label;
		}

		@Override
		public void update() {
			if ((label != null) && !label.isDisposed()) {
				label.setText(MessageFormat.format("/{0}", getPage().getPageCount()));
				label.setToolTipText("Page count");
				label.getParent().update();
			}
		}
		
		@Override
		public boolean isDynamic() {
			return true;
		}

	}

	// Zoom

	protected static final float ZOOM_STEP = 0.25f;

	public abstract class ZoomAction extends Action {

		public ZoomAction(String tooltipTextFragment, String iconNameFragment) {
			setToolTipText(MessageFormat.format("Zoom {0}", tooltipTextFragment));
			setText(getToolTipText());
			setImageDescriptor(Activator.getImageDescriptor(ICON_PATH + MessageFormat.format("Zoom{0}.png", iconNameFragment))); //$NON-NLS-1$
		}

		@Override
		public void run() {
			disableFit();
			getPage().setZoom(getNewZoom());
		}

		protected abstract float getNewZoom();

		@Override
		public boolean isEnabled() {
			return getPage()!=null && getPage().isZoomValid(getNewZoom());
		}

	}

	public class ZoomOutAction extends ZoomAction {

		public ZoomOutAction() {
			super("Out", "Out"); //$NON-NLS-2$
			setActionDefinitionId(COMMAND_ID_PREFIX+"ZoomOut");
		}

		@Override
		protected float getNewZoom() {
			return getPage().getZoom() * (1 - ZOOM_STEP);
		}

	}

	public class ZoomInAction extends ZoomAction {

		public ZoomInAction() {
			super("In", "In"); //$NON-NLS-2$
			setActionDefinitionId(COMMAND_ID_PREFIX+"ZoomIn");
		}

		@Override
		protected float getNewZoom() {
			return getPage().getZoom() * (1 + ZOOM_STEP);
		}

	}

	public class ZoomToActualSizeAction extends ZoomAction {

		public ZoomToActualSizeAction() {
			super("To Actual Size", "ToActualSize"); //$NON-NLS-2$
			setActionDefinitionId(COMMAND_ID_PREFIX+"ZoomNormal");
		}

		@Override
		protected float getNewZoom() {
			return 1;
		}

	}

	// Zoom to fit

	public void disableFit() {
		fitToPageAction.setChecked(false);
		fitToWidthAction.setChecked(false);
		fitToHeightAction.setChecked(false);
	}

	public class FitToAction extends Action {

		private final ControlListener resizeListener = new ControlAdapter() {
			private AtomicBoolean preventZoomingToOftenLock=new AtomicBoolean(false);

			@Override
			public void controlResized(ControlEvent e) {
				if(preventZoomingToOftenLock.get()){
					//workaround for for problem described in pull request 17
					return;
				}
				preventZoomingToOftenLock.set(true);
				PdfViewPage page = getPage();
				float widthRatio = Float.MAX_VALUE;
				if (fitToWidth) {
					float imageWidth = page.getClientArea().width - page.getVerticalBar().getSize().x;
					int pageWidth = page.getPageWidth();
					widthRatio = imageWidth / pageWidth;
				}
				float heightRatio = Float.MAX_VALUE;
				if (fitToHeight) {
					float imageHeight = page.getClientArea().height - page.getHorizontalBar().getSize().y;
					int pageHeight = page.getPageHeight();
					heightRatio = imageHeight / pageHeight;
				}
				page.setZoom(Math.min(widthRatio, heightRatio));
				preventZoomingToOftenLock.set(false);
			}

		};

		private final boolean fitToWidth;

		private final boolean fitToHeight;

		public FitToAction(String tooltipTextFragment, String iconNameFragment, boolean fitToWidth, boolean fitToHeight) {
			super(null, AS_RADIO_BUTTON);
			if (!fitToWidth && !fitToHeight) {
				throw new IllegalArgumentException("At least one dimension must be specified"); //$NON-NLS-1$
			}
			this.fitToWidth = fitToWidth;
			this.fitToHeight = fitToHeight;
			setToolTipText(MessageFormat.format("Fit To {0}", tooltipTextFragment));
			setText(getToolTipText());
			setImageDescriptor(Activator.getImageDescriptor(ICON_PATH + MessageFormat.format("FitTo{0}.png", iconNameFragment))); //$NON-NLS-1$
			setActionDefinitionId(COMMAND_ID_PREFIX+"Zoom"+iconNameFragment);
		}

		@Override
		public void setChecked(boolean checked) {
			if(checked){
				//ensure disabling other radio buttons when using short cuts
				disableFit();
			}
			if (checked != isChecked()) {
				super.setChecked(checked);
				PdfViewPage page = getPage();
				if (checked) {
					resizeListener.controlResized(null);
					page.addControlListener(resizeListener);
					page.setFitToAction(this); // Save special zoom setting
				} else {
					page.removeControlListener(resizeListener);
					if (page.getFitToAction() == this) { // Clear special zoom setting
						page.setFitToAction(null);
					}
				}
			}
		}

		@Override
		public boolean isEnabled() {
			return getPage()!=null &&super.isEnabled();
		}

	}

	private IContributionItem getZoomMenu() {
		ToolbarSubmenuAction zoomMenu = new ToolbarSubmenuAction("Zoom", Activator.getImageDescriptor(ICON_PATH + "Zoom.png")); //$NON-NLS-2$
		zoomMenu.addAction(new ZoomOutAction());
		zoomMenu.addAction(new ZoomInAction());
		zoomMenu.addAction(new ZoomToActualSizeAction());
		zoomMenu.addAction(fitToPageAction);
		zoomMenu.addAction(fitToWidthAction);
		zoomMenu.addAction(fitToHeightAction);
		return new ActionContributionItem(zoomMenu);
	}

}