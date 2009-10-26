package org.eclipse.util;

import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;

public class ConsoleUtils {

	private ConsoleUtils() {
	}

	/**
	 * Returns the console with the specified name. If it does not exist, it will
	 * be created using the given factory.
	 */
	@SuppressWarnings("unchecked")
	public static <T extends IConsole> T getConsole(String name, ConsoleFactory<T> factory) {
		// Find the console
		IConsoleManager consoleManager = ConsolePlugin.getDefault().getConsoleManager();
		for (IConsole console : consoleManager.getConsoles()) {
			if (console.getName().equals(name)) {
				return (T)console;
			}
		}
		// If not found, create it
		T console = factory.create(name);
		consoleManager.addConsoles(new IConsole[] { console });
		return console;
	}

}
