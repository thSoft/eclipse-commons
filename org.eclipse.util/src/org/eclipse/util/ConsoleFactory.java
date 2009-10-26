package org.eclipse.util;

import org.eclipse.ui.console.IConsole;

public interface ConsoleFactory<T extends IConsole> {

	T create(String name);

}
