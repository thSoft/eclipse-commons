package org.eclipse.ui.views.midi;

import static java.text.MessageFormat.format;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.core.commands.IParameterValues;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.views.IViewDescriptor;
import org.eclipse.ui.views.file.FileView;
import org.eclipse.ui.views.file.IFileViewType;

public class PlayPauseViewParameterValues implements IParameterValues {

	@Override
	public Map<String, String> getParameterValues() {
		final Map<String, String> result = new HashMap<String, String>();
		IConfigurationElement[] configurationElements = Platform.getExtensionRegistry().getConfigurationElementsFor(org.eclipse.ui.views.file.Activator.getId(), FileView.BINDINGS);
		for (IConfigurationElement configurationElement : configurationElements) {
			try {
				Object type = configurationElement.createExecutableExtension(FileView.TYPE);
				if (type instanceof IFileViewType) {
					IFileViewType<?> fileViewType = (IFileViewType<?>)type;
					if (fileViewType instanceof MidiViewType) {
						for (IViewDescriptor view : PlatformUI.getWorkbench().getViewRegistry().getViews()) {
							if (view.getId().equals(configurationElement.getAttribute(FileView.VIEW_ID))) {
								result.put(view.getLabel(), view.getId());
							}
						}
					}
				}
			} catch (CoreException e) {
				Activator.logError(format("Can''t determine type of file view {0}", configurationElement.getAttribute(FileView.VIEW_ID)), e);
			}
		}
		return result;
	}

}
