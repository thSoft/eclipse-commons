package org.eclipse.ui.views.pdf;

import java.text.MessageFormat;
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
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Scrollable;
import org.eclipse.swt.widgets.Text;

public class PdfViewToolbarManager {

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
		new ActionContributionItem(new ZoomOutAction()),
		new ActionContributionItem(new ZoomInAction()),
		new ActionContributionItem(new ZoomToActualSizeAction()),
		new ActionContributionItem(fitToPageAction),
		new ActionContributionItem(fitToWidthAction),
		new ActionContributionItem(fitToHeightAction) };

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

	public class FirstPageAction extends Action {

		public FirstPageAction() {
			setToolTipText("Go To First Page");
			setImageDescriptor(Activator.getImageDescriptor(ICON_PATH + "FirstPage.png")); //$NON-NLS-1$
		}

		@Override
		public void run() {
			getPage().setPage(1);
		}

	}

	public class PreviousPageAction extends Action {

		public PreviousPageAction() {
			setToolTipText("Go To Previous Page");
			setImageDescriptor(Activator.getImageDescriptor(ICON_PATH + "PreviousPage.png")); //$NON-NLS-1$
		}

		@Override
		public void run() {
			getPage().setPage(getPage().getPage() - 1);
		}

		@Override
		public boolean isEnabled() {
			return getPage().getPage() > 1;
		}

	}

	public class NextPageAction extends Action {

		public NextPageAction() {
			setToolTipText("Go To Next Page");
			setImageDescriptor(Activator.getImageDescriptor(ICON_PATH + "NextPage.png")); //$NON-NLS-1$
		}

		@Override
		public void run() {
			getPage().setPage(getPage().getPage() + 1);
		}

		@Override
		public boolean isEnabled() {
			return getPage().getPage() < getPage().getPageCount();
		}

	}

	public class LastPageAction extends Action {

		public LastPageAction() {
			setToolTipText("Go To Last Page");
			setImageDescriptor(Activator.getImageDescriptor(ICON_PATH + "LastPage.png")); //$NON-NLS-1$
		}

		@Override
		public void run() {
			getPage().setPage(getPage().getPageCount());
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
					if ((event.keyCode == SWT.CR) && (text.getText().length() > 0)) {
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
			Composite container = new Composite(parent, SWT.NONE);
			GridLayout layout = new GridLayout();
			layout.marginHeight = 0;
			layout.marginWidth = 1;
			container.setLayout(layout);
			label = new Label(container, SWT.CENTER);
			GridData layoutData = new GridData();
			layoutData.verticalAlignment = SWT.CENTER;
			layoutData.grabExcessVerticalSpace = true;
			label.setLayoutData(layoutData);
			update();
			return container;
		}

		@Override
		public void update() {
			if ((label != null) && !label.isDisposed()) {
				label.setText(MessageFormat.format("/{0}", getPage().getPageCount()));
				label.setToolTipText("Page count");
			}
		}

	}

	protected static final float ZOOM_STEP = 0.25f;

	protected static final float ZOOM_IN_FACTOR = 1 + ZOOM_STEP;

	protected static final float ZOOM_OUT_FACTOR = 1 - ZOOM_STEP;

	public class ZoomOutAction extends Action {

		public ZoomOutAction() {
			setToolTipText("Zoom Out");
			setImageDescriptor(Activator.getImageDescriptor(ICON_PATH + "ZoomOut.png")); //$NON-NLS-1$
		}

		@Override
		public void run() {
			disableFit();
			getPage().setZoom(getNewZoom());
		}

		private float getNewZoom() {
			return getPage().getZoom() * ZOOM_OUT_FACTOR;
		}

		@Override
		public boolean isEnabled() {
			return getPage().isZoomValid(getNewZoom());
		}

	}

	public class ZoomInAction extends Action {

		public ZoomInAction() {
			setToolTipText("Zoom In");
			setImageDescriptor(Activator.getImageDescriptor(ICON_PATH + "ZoomIn.png")); //$NON-NLS-1$
		}

		@Override
		public void run() {
			disableFit();
			getPage().setZoom(getNewZoom());
		}

		private float getNewZoom() {
			return getPage().getZoom() * ZOOM_IN_FACTOR;
		}

		@Override
		public boolean isEnabled() {
			return getPage().isZoomValid(getNewZoom());
		}

	}

	public class ZoomToActualSizeAction extends Action {

		public ZoomToActualSizeAction() {
			setToolTipText("Zoom To Actual Size");
			setImageDescriptor(Activator.getImageDescriptor(ICON_PATH + "ZoomToActualSize.png")); //$NON-NLS-1$
		}

		@Override
		public void run() {
			disableFit();
			getPage().setZoom(1);
		}

	}

	public void disableFit() {
		fitToPageAction.setChecked(false);
		fitToWidthAction.setChecked(false);
		fitToHeightAction.setChecked(false);
	}

	public class FitToAction extends Action {

		private final ControlListener resizeListener = new ControlAdapter() {

			@Override
			public void controlResized(ControlEvent e) {
				Scrollable scrollable = getPage();
				float widthRatio = 1;
				if (fitToWidth) {
					float imageWidth = scrollable.getClientArea().width - scrollable.getVerticalBar().getSize().x;
					int pageWidth = getPage().getPageWidth();
					widthRatio = imageWidth / pageWidth;
				}
				float heightRatio = 1;
				if (fitToHeight) {
					float imageHeight = scrollable.getClientArea().height - scrollable.getHorizontalBar().getSize().y;
					int pageHeight = getPage().getPageHeight();
					heightRatio = imageHeight / pageHeight;
				}
				getPage().setZoom(Math.min(widthRatio, heightRatio));
			}

		};

		private final boolean fitToWidth;

		private final boolean fitToHeight;

		public FitToAction(String tooltipTextFragment, String iconNameFragment, boolean fitToWidth, boolean fitToHeight) {
			super(null, AS_RADIO_BUTTON);
			this.fitToWidth = fitToWidth;
			this.fitToHeight = fitToHeight;
			setToolTipText(MessageFormat.format("Fit To {0}", tooltipTextFragment));
			setImageDescriptor(Activator.getImageDescriptor(ICON_PATH + MessageFormat.format("FitTo{0}.png", iconNameFragment))); //$NON-NLS-1$
		}

		@Override
		public void setChecked(boolean checked) {
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

}
