package org.eclipse.ui.views.file;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.PageBook;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.views.file.source.IFileViewSource;

/**
 * A view that displays the file determined by its current source.
 */
public class FileView extends ViewPart {

	private static class ErrorPage extends Composite {

		public ErrorPage(Composite parent, String errorMessage) {
			super(parent, SWT.NONE);
			setLayout(new GridLayout());
			Label errorLabel = new Label(this, SWT.CENTER);
			errorLabel.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true));
			errorLabel.setText(errorMessage);
		}

	}

	private static final String ID = "id"; //$NON-NLS-1$

	private static final String SOURCE = "source"; //$NON-NLS-1$

	private IFileViewType<? super Composite> type;

	private final List<String> extensions = new ArrayList<String>();

	private final List<FileViewSourceDescriptor> sourceDescriptors = new ArrayList<FileViewSourceDescriptor>();

	private String errorMessage = "File not found";

	private PageBook pageBook;

	private final Map<IFile, Composite> pages = new HashMap<IFile, Composite>();

	private IFile file;

	private FileViewSourceDescriptor sourceDescriptor;

	private Control errorPage;

	private FileViewSourceMenu sourceMenu;

	private IToolBarManager toolbar;

	private boolean toolbarFilled = false;

	private IContributionItem[] toolbarContributions;

	@SuppressWarnings("unchecked")
	@Override
	public void init(IViewSite site, IMemento memento) throws PartInitException {
		super.init(site, memento);
		IConfigurationElement[] configurationElements = Platform.getExtensionRegistry().getConfigurationElementsFor(Activator.getId(), "bindings"); //$NON-NLS-1$
		for (IConfigurationElement configurationElement : configurationElements) {
			if (site.getId().equals(configurationElement.getAttribute("viewId"))) { //$NON-NLS-1$
				// Type
				try {
					Object type = configurationElement.createExecutableExtension("type"); //$NON-NLS-1$
					if (type instanceof IFileViewType) {
						this.setType((IFileViewType<? super Composite>)type);
					}
				} catch (CoreException e) {
					Activator.logError("Can't initialize file view type", e);
				}
				// Extensions
				for (IConfigurationElement extensionElement : configurationElement.getChildren("fileExtension")) { //$NON-NLS-1$
					extensions.add(extensionElement.getAttribute("extension")); //$NON-NLS-1$
				}
				// Sources
				for (IConfigurationElement sourceElement : configurationElement.getChildren(SOURCE)) {
					String sourceId = sourceElement.getAttribute(ID);
					IConfigurationElement[] sourceConfigurationElements = Platform.getExtensionRegistry().getConfigurationElementsFor(Activator.getId(), "sources"); //$NON-NLS-1$
					for (IConfigurationElement sourceConfigurationElement : sourceConfigurationElements) {
						if (sourceConfigurationElement.getAttribute(ID).equals(sourceId)) {
							try {
								// Class
								Object source = sourceConfigurationElement.createExecutableExtension("class"); //$NON-NLS-1$
								if (source instanceof IFileViewSource) {
									final FileViewSourceDescriptor sourceDescriptor = new FileViewSourceDescriptor();
									sourceDescriptors.add(sourceDescriptor);
									sourceDescriptor.source = (IFileViewSource)source;
									// Icon
									String pluginId = sourceConfigurationElement.getDeclaringExtension().getNamespaceIdentifier();
									String imageFilePath = sourceConfigurationElement.getAttribute("icon"); //$NON-NLS-1$
									sourceDescriptor.icon = AbstractUIPlugin.imageDescriptorFromPlugin(pluginId, imageFilePath);
									// Toolbar contributions
									toolbarContributions = getType().getToolbarContributions();
								}
							} catch (CoreException e) {
								Activator.logError("Can't instantiate file view source", e);
							}
						}
					}
				}
				// Error message
				String errorMessage = configurationElement.getAttribute("errorMessage"); //$NON-NLS-1$
				if (errorMessage != null) {
					this.errorMessage = errorMessage;
				}
			}
		}
		// Source menu
		sourceMenu = new FileViewSourceMenu(this, site.getId());
		toolbar = site.getActionBars().getToolBarManager();
		toolbar.add(sourceMenu);
		// Restore current source
		if (memento != null) {
			String sourceClassName = memento.getString(SOURCE);
			for (FileViewSourceDescriptor sourceDescriptor : sourceDescriptors) {
				if (sourceDescriptor.source.getClass().getName().equals(sourceClassName)) {
					setSourceDescriptor(sourceDescriptor);
				}
			}
		}
		if ((getSourceDescriptor() == null) && !sourceDescriptors.isEmpty()) {
			setSourceDescriptor(sourceDescriptors.get(0));
		}
	}

	@Override
	public void createPartControl(Composite parent) {
		// Page book
		pageBook = new PageBook(parent, SWT.NONE);
		// Error page
		this.errorPage = new ErrorPage(pageBook, errorMessage);
		// Activate source
		if (getSourceDescriptor() != null) {
			getSourceDescriptor().source.init(this, true);
		}
	}

	@Override
	public void setFocus() {
		pageBook.setFocus();
	}

	@Override
	public void saveState(IMemento memento) {
		if (getSourceDescriptor() != null) {
			memento.putString(SOURCE, getSourceDescriptor().source.getClass().getName());
		}
	}

	public String[] getExtensions() {
		return extensions.toArray(new String[0]);
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
			Composite page = getPage();
			if ((page == null) || page.isDisposed()) {
				load(file);
			}
			refresh();
		}
		// Fill toolbar for the first time
		if (!toolbarFilled) {
			toolbarFilled = true;
			String sourceMenuId = sourceMenu.getId();
			for (IContributionItem toolbarContribution : toolbarContributions) {
				toolbar.insertBefore(sourceMenuId, toolbarContribution);
			}
			toolbar.insertBefore(sourceMenuId, new Separator());
			toolbar.update(true);
		}
	}

	private void load(IFile file) {
		Composite page = null;
		try {
			page = getType().createPage(pageBook, file);
		} catch (Exception e) {
			Activator.logError("Can't create file view page", e);
		}
		pages.put(file, page);
	}

	private void refresh() {
		if (pageBook != null) {
			Composite page = getPage();
			if (page == null) {
				pageBook.showPage(errorPage);
			} else {
				pageBook.showPage(page);
				getType().pageShown(page);
			}
		}
		refreshToolbarContributions();
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
			try {
				getType().reload(oldPage);
			} catch (Exception e) {
				Activator.logError("Error while reloading file", e);
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

	public void setSourceDescriptor(FileViewSourceDescriptor sourceDescriptor) {
		if (this.sourceDescriptor != null) {
			this.sourceDescriptor.source.done();
		}
		this.sourceDescriptor = sourceDescriptor;
		if (pageBook != null) {
			sourceDescriptor.source.init(this, false);
		}
		refreshSourceMenu();
	}

	private void refreshSourceMenu() {
		if (sourceMenu != null) {
			sourceMenu.setToolTipText(MessageFormat.format("{0} ({1})", getSourceDescriptor().source.getLongName(), getFile() == null ? "none" : getFile().getFullPath()));
			sourceMenu.setImageDescriptor(getSourceDescriptor().icon);
		}
	}

	public FileViewSourceDescriptor getSourceDescriptor() {
		return sourceDescriptor;
	}

	private void setFile(IFile file) {
		this.file = file;
		refreshSourceMenu();
	}

	public IFile getFile() {
		return file;
	}

	public FileViewSourceDescriptor[] getSourceDescriptors() {
		return sourceDescriptors.toArray(new FileViewSourceDescriptor[0]);
	}

	private void setType(IFileViewType<? super Composite> type) {
		this.type = type;
	}

	public IFileViewType<? super Composite> getType() {
		return type;
	}

}
