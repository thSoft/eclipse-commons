package org.eclipse.ui.views.file;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;

// from https://www.eclipse.org/forums/index.php/t/144833/
public class ToolbarSubmenuAction extends Action implements IMenuCreator {

	private Menu fMenu;
	private List<IAction> actions=new ArrayList<IAction>();

	public ToolbarSubmenuAction(String name, ImageDescriptor imageDescriptor) {
		setText(name);
		setImageDescriptor(imageDescriptor);
		setMenuCreator(this);
	}

	public void addAction(IAction action){
		actions.add(action);
	}

	List<IAction> getActions() {
		return actions;
	}

	@Override
	public void dispose() {
		if (fMenu != null) {
			fMenu.dispose();
			fMenu = null;
		}
	}

	@Override
	public Menu getMenu(Control parent) {
		if (fMenu != null){
			fMenu.dispose();
		}

		fMenu = new Menu(parent);
		for (IAction action : getActions()) {
			addActionToMenu(fMenu, action);
		}
		return fMenu;
	}

	@Override
	public Menu getMenu(Menu parent) {
		return null;
	}

	private void addActionToMenu(Menu parent, IAction action) {
		ActionContributionItem item = new ActionContributionItem(action);
		item.fill(parent, -1);
	}
}
