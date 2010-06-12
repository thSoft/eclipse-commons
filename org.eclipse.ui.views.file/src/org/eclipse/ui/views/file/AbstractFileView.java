package org.eclipse.ui.views.file;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.core.resources.IFile;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.PageBook;
import org.eclipse.ui.part.ViewPart;

/**
 * The default implementation of {@link FileView}.
 */
public abstract class AbstractFileView extends ViewPart implements FileView {

	private FileViewSource source;

	private PageBook pageBook;

	private final Map<IFile, Composite> pages = new HashMap<IFile, Composite>();

	private static final String SOURCE = ".source"; //$NON-NLS-1$

	private IFile file;

	@Override
	public void show(IFile file) {
		this.setFile(file);
		if (!pages.containsKey(file)) {
			Composite page = createPage(pageBook, file);
			pages.put(file, page);
		}
		pageBook.showPage(getPage(file));
	}

	protected Composite getPage(IFile file) {
		return pages.get(file);
	}

	protected Composite getPage() {
		return pages.get(getFile());
	}

	public abstract Composite createPage(PageBook pageBook, IFile file);

	@Override
	public void init(IViewSite site, IMemento memento) throws PartInitException {
		super.init(site, memento);
		if (memento != null) {
			String sourceClassName = memento.getString(getSourceKey());
			if (sourceClassName != null) {
				Class<?> sourceClass;
				try {
					sourceClass = Activator.getInstance().getBundle().loadClass(sourceClassName);
					FileViewSource source = (FileViewSource)sourceClass.newInstance();
					setSource(source);
				} catch (Exception e) {
					Activator.logError("Cannot instantiate file view source", e);
				}
			}
		}
	}

	@Override
	public void createPartControl(Composite parent) {
		pageBook = new PageBook(parent, SWT.NONE);
	}

	@Override
	public void setFocus() {
		pageBook.setFocus();
	}

	/**
	 * Returns the message to be displayed when the file to be displayed is
	 * missing.
	 */
	protected String getErrorMessage() {
		return "File not found";
	}

	@Override
	public void saveState(IMemento memento) {
		if (getSource() != null) {
			memento.putString(getSourceKey(), getSource().getClass().getName());
		}
	}

	private String getSourceKey() {
		return getId() + SOURCE;
	}

	public void setSource(FileViewSource source) {
		this.source = source;
		source.init(this);
	}

	public FileViewSource getSource() {
		return source;
	}

	@Override
	public List<FileViewSource> getSources() {
		return Arrays.asList(new FileViewSource[] { new DirectFileViewSource(),
			new SelectedFileViewSource() });
	}

	private void setFile(IFile file) {
		this.file = file;
	}

	public IFile getFile() {
		return file;
	}

}
