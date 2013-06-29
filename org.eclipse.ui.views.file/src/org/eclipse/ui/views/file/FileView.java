package org.eclipse.ui.views.file;

import static java.text.MessageFormat.format;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPathEditorInput;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.part.PageBook;
import org.eclipse.ui.part.ViewPart;

/**
 * A view that displays the file determined by its current source.
 */
public class FileView extends ViewPart {

	public static final String BINDINGS = "bindings"; //$NON-NLS-1$

	public static final String VIEW_ID = "viewId";

	public static final String TYPE = "type"; //$NON-NLS-1$

	private static class ErrorPage extends Composite {

		public ErrorPage(Composite parent, String errorMessage) {
			super(parent, SWT.NONE);
			setLayout(new GridLayout());
			Label errorLabel = new Label(this, SWT.CENTER);
			errorLabel.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true));
			errorLabel.setText(errorMessage);
		}

	}

	private static final String PATH = "path"; //$NON-NLS-1$

	private static final String LINKED = "linked"; //$NON-NLS-1$

	private IFileViewType<? super Composite> type;

	private final List<String> extensions = new ArrayList<String>();

	private String errorMessage = "File not found";

	private PageBook pageBook;

	private final Map<IFile, Composite> pages = new HashMap<IFile, Composite>();

	private IFile file;

	private Control errorPage;
	
	private Label fileNameDisplay;

	private IToolBarManager toolbar;

	private boolean toolbarFilled = false;

	private IContributionItem[] toolbarContributions;

	private boolean linked = true;

	@Override
	public void init(IViewSite site, IMemento memento) throws PartInitException {
		super.init(site, memento);
		IConfigurationElement[] configurationElements = Platform.getExtensionRegistry().getConfigurationElementsFor(Activator.getId(), BINDINGS);
		for (IConfigurationElement configurationElement : configurationElements) {
			if (site.getId().equals(configurationElement.getAttribute(VIEW_ID))) {
				// Type
				try {
					Object type = configurationElement.createExecutableExtension(TYPE);
					if (type instanceof IFileViewType) {
						@SuppressWarnings("unchecked")
						IFileViewType<? super Composite> fileViewType = (IFileViewType<? super Composite>)type;
						this.setType(fileViewType);
					}
				} catch (CoreException e) {
					Activator.logError(format("Can''t initialize type of file view {0}", site.getId()), e);
				}
				// Extensions
				for (IConfigurationElement extensionElement : configurationElement.getChildren("fileExtension")) { //$NON-NLS-1$
					extensions.add(extensionElement.getAttribute("extension")); //$NON-NLS-1$
				}
				// Error message
				String errorMessage = configurationElement.getAttribute("errorMessage"); //$NON-NLS-1$
				if (errorMessage != null) {
					this.errorMessage = errorMessage;
				}
			}
		}
		// Toolbar
		toolbar = site.getActionBars().getToolBarManager();
		toolbarContributions = getType().getToolbarContributions();
		// Restore settings
		if (memento != null) {
			// Path
			String pathString = memento.getString(PATH);
			if (pathString != null) {
				IPath path = Path.fromPortableString(pathString);
				IFile file = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(path);
				setFile(file);
			}
			// Linked
			Boolean linked = memento.getBoolean(LINKED);
			if (linked != null) {
				this.linked = linked;
			}
		}
		toggleLinkedAction.setChecked(this.linked);
	}

	@Override
	public void createPartControl(Composite parent) {
		GridLayout layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		layout.horizontalSpacing = 0;
		layout.verticalSpacing = 0;
		parent.setLayout(layout);
		fileNameDisplay = new Label(parent, SWT.NONE);
		GridData fileNameDisplayLayoutData = new GridData();
		fileNameDisplayLayoutData.horizontalAlignment = GridData.FILL;
		fileNameDisplay.setLayoutData(fileNameDisplayLayoutData);
		pageBook = new PageBook(parent, SWT.NONE);
		GridData pageBookLayoutData = new GridData();
		pageBookLayoutData.horizontalAlignment = GridData.FILL;
		pageBookLayoutData.verticalAlignment = GridData.FILL;
		pageBookLayoutData.grabExcessHorizontalSpace = true;
		pageBookLayoutData.grabExcessVerticalSpace = true;
		pageBook.setLayoutData(pageBookLayoutData);
		errorPage = new ErrorPage(pageBook, errorMessage);
		toolbar.add(toggleLinkedAction);
		if (getFile() != null) {
			show(getFile());
		}
		// Selection listener
		ISelectionService selectionService = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getSelectionService();
		selectionService.addPostSelectionListener(selectionListener);
	}

	@Override
	public void setFocus() {
		pageBook.setFocus();
	}

	@Override
	public void saveState(IMemento memento) {
		memento.putString(PATH, file == null ? null : file.getLocation().toPortableString());
		memento.putBoolean(LINKED, linked);
	}

	@Override
	public void dispose() {
		for (Composite page : pages.values()) {
			if (page != null) {
				getType().pageClosed(page);
			}
		}
		super.dispose();
	}

	public List<String> getExtensions() {
		return extensions;
	}

	public void hide() {
		if (!pageBook.isDisposed()) {
			pageBook.setVisible(false);
			setFile(null);
		}
		refreshToolbarContributions();
	}

	public void show(IFile file) {
		if (!pageBook.isDisposed()) {
			pageBook.setVisible(true);
			setFile(file);
			setTitleToolTip(file.getFullPath().toString());
			Composite page = getPage();
			if ((page == null) || page.isDisposed()) {
				load(file);
			}
			refresh();
			// Fill toolbar for the first time
			if (!toolbarFilled) {
				toolbarFilled = true;
				for (IContributionItem toolbarContribution : toolbarContributions) {
					toolbar.add(toolbarContribution);
				}
				toolbar.update(true);
			}
		}
		if (!fileNameDisplay.isDisposed()) {
			fileNameDisplay.setText(file.getFullPath().toOSString());
		}
	}

	private void load(IFile file) {
		Composite page = null;
		if (file.exists()) {
			try {
				page = getType().createPage(pageBook, file);
			} catch (Exception e) {
				Activator.logError("Can't create file view page", e);
			}
			pages.put(file, page);
		} else {
			showErrorPage();
		}
	}

	private void refresh() {
		if (pageBook != null) {
			Composite page = getPage();
			if (page == null) {
				showErrorPage();
			} else {
				pageBook.showPage(page);
				getType().pageShown(page);
				for (IContributionItem contributionItem : toolbarContributions) {
					contributionItem.setVisible(true);
				}
			}
		}
		refreshToolbarContributions();
	}

	private void showErrorPage() {
		pageBook.showPage(errorPage);
		for (IContributionItem contributionItem : toolbarContributions) {
			contributionItem.setVisible(false);
		}
	}

	private void refreshToolbarContributions() {
		for (IContributionItem toolbarContribution : toolbarContributions) {
			boolean visible = (getFile() != null) && (getPage() != null);
			if (visible) {
				toolbarContribution.update();
			}
			toolbarContribution.setVisible(visible);
		}
		toolbar.update(true);
	}

	public void reload(IFile file) {
		Composite oldPage = pages.get(file);
		if (oldPage == null) {
			load(file);
		} else {
			if (file.exists()) {
				try {
					getType().reload(oldPage);
				} catch (Exception e) {
					Activator.logError("Error while reloading file", e);
					pages.put(file, null);
				}
			} else {
				pages.put(file, null);
			}
		}
		if (file.equals(getFile())) {
			refresh();
		}
	}

	private Composite getPage() {
		return pages.get(getFile());
	}

	private void setFile(IFile file) {
		this.file = file;
	}

	public IFile getFile() {
		return file;
	}

	private void setType(IFileViewType<? super Composite> type) {
		this.type = type;
	}

	public IFileViewType<? super Composite> getType() {
		return type;
	}

	private final ISelectionListener selectionListener = new ISelectionListener() {

		@Override
		public void selectionChanged(IWorkbenchPart part, ISelection selection) {
			if (linked) {
				IFile selectedFile = null;
				if (selection instanceof IStructuredSelection) {
					IStructuredSelection structuredSelection = (IStructuredSelection)selection;
					Object selectedElement = structuredSelection.getFirstElement();
					if (selectedElement instanceof IFile) {
						selectedFile = (IFile)selectedElement;
					}
				} else if (part instanceof EditorPart) {
					EditorPart editorPart = (EditorPart)part;
					IEditorInput editorInput = editorPart.getEditorInput();
					if (editorInput instanceof IPathEditorInput) {
						IPathEditorInput pathEditorInput = (IPathEditorInput)editorInput;
						IPath locationPath = pathEditorInput.getPath();
						selectedFile = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(locationPath);
					}
				}
				if (selectedFile != null) {
					IFile fileToView = getType().getFile(selectedFile);
					if (getExtensions().contains(fileToView.getFileExtension())) {
						show(fileToView);
					}
				}
			}
		}

	};

	private final IAction toggleLinkedAction = new Action("Link with Editor and Selection", IAction.AS_CHECK_BOX) {

		{
			setImageDescriptor(Activator.getImageDescriptor("icons/Link.png"));
		}

		@Override
		public void run() {
			linked = !linked;
		}

	};

}
