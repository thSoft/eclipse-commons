package org.eclipse.util;

import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;

public class ConsoleUtils {

	private ConsoleUtils() {
	}

	/**
	 * Returns the message console with the specified name. If it does not exist,
	 * it will be created.
	 */
	public static MessageConsole getConsole(String name) {
		// Find the console
		IConsoleManager consoleManager = ConsolePlugin.getDefault().getConsoleManager();
		for (IConsole console : consoleManager.getConsoles()) {
			if (console.getName().equals(name)) {
				return (MessageConsole)console;
			}
		}
		// If not found, create it
		MessageConsole messageConsole = new MessageConsole(name, null);
		consoleManager.addConsoles(new IConsole[] { messageConsole });
		return messageConsole;
	}

}
