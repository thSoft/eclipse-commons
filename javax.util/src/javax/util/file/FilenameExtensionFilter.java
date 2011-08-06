package javax.util.file;

import java.io.File;
import java.io.FilenameFilter;

public class FilenameExtensionFilter implements FilenameFilter {

	private final String extension;

	public FilenameExtensionFilter(String extension) {
		this.extension = extension;
	}

	@Override
	public boolean accept(File dir, String name) {
		return name.endsWith("." + extension);
	}

}
