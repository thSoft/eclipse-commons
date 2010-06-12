package org.eclipse.util;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;

public class ProjectUtils {

	private ProjectUtils() {
	}

	public static void addNatures(IProject project, String... natureIds) throws CoreException {
		IProjectDescription projectDescription = project.getDescription();
		String[] oldNatureIds = projectDescription.getNatureIds();
		Set<String> newNatureIds = new HashSet<String>(Arrays.asList(oldNatureIds));
		newNatureIds.addAll(Arrays.asList(natureIds));
		projectDescription.setNatureIds(newNatureIds.toArray(new String[0]));
		project.setDescription(projectDescription, new NullProgressMonitor());
	}

	public static void removeNatures(IProject project, String... natureIds) throws CoreException {
		IProjectDescription projectDescription = project.getDescription();
		String[] oldNatureIds = projectDescription.getNatureIds();
		Set<String> newNatureIds = new HashSet<String>(Arrays.asList(oldNatureIds));
		newNatureIds.removeAll(Arrays.asList(natureIds));
		projectDescription.setNatureIds(newNatureIds.toArray(new String[0]));
		project.setDescription(projectDescription, new NullProgressMonitor());
	}

	public static void addBuilder(IProject project, String builderId) throws CoreException {
		IProjectDescription description = project.getDescription();
		ICommand[] buildCommands = description.getBuildSpec();
		for (ICommand buildCommand : buildCommands) {
			if (buildCommand.getBuilderName().equals(builderId)) {
				return;
			}
		}
		ICommand[] newCommands = new ICommand[buildCommands.length + 1];
		System.arraycopy(buildCommands, 0, newCommands, 0, buildCommands.length);
		ICommand command = description.newCommand();
		command.setBuilderName(builderId);
		newCommands[newCommands.length - 1] = command;
		description.setBuildSpec(newCommands);
		project.setDescription(description, new NullProgressMonitor());
	}

	public static void removeBuilder(IProject project, String builderId) throws CoreException {
		IProjectDescription description = project.getDescription();
		ICommand[] commands = description.getBuildSpec();
		for (int i = 0; i < commands.length; ++i) {
			if (commands[i].getBuilderName().equals(builderId)) {
				ICommand[] newCommands = new ICommand[commands.length - 1];
				System.arraycopy(commands, 0, newCommands, 0, i);
				System.arraycopy(commands, i + 1, newCommands, i, commands.length - i - 1);
				description.setBuildSpec(newCommands);
				project.setDescription(description, null);
				return;
			}
		}
	}

}
