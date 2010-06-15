package org.eclipse.ui.views.file;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;

public class FileViewSourceMenu extends Action implements IMenuCreator {

	private Menu menu;

	private final FileView fileView;

	public FileViewSourceMenu(FileView fileView) {
		super("Source", IAction.AS_DROP_DOWN_MENU);
		this.fileView = fileView;
		setMenuCreator(this);
	}

	@Override
	public void dispose() {
		if (menu != null) {
			menu.dispose();
		}
	}

	@Override
	public Menu getMenu(Control parent) {
		if (menu == null) {
			menu = new Menu(parent);
			for (final FileViewSourceDescriptor sourceDescriptor : fileView.getSourceDescriptors()) {
				ActionContributionItem item = new ActionContributionItem(new Action(sourceDescriptor.source.getName(), IAction.AS_RADIO_BUTTON) {

					{
						setImageDescriptor(sourceDescriptor.icon);
						if (fileView.getSourceDescriptor() == sourceDescriptor) {
							setChecked(true);
						}
					}

					@Override
					public void run() {
						if (isChecked()) {
							fileView.setSourceDescriptor(sourceDescriptor);
						}
					}

				});
				item.fill(menu, -1);
			}
		}
		return menu;
	}

	@Override
	public Menu getMenu(Menu parent) {
		return null;
	}

}
