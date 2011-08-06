package javax.util.file;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class FileUtils {

	private FileUtils() {
	}

	public static String readFileAsString(String filePath) throws IOException {
		byte[] buffer = new byte[(int)new File(filePath).length()];
		BufferedInputStream stream = null;
		try {
			stream = new BufferedInputStream(new FileInputStream(filePath));
			stream.read(buffer);
		} finally {
			if (stream != null) {
				stream.close();
			}
		}
		return new String(buffer);
	}

}
