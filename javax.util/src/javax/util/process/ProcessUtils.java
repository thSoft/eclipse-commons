package javax.util.process;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ProcessUtils {

	private ProcessUtils() {
	}

	/**
	 * Runs a process synchronously described by the given process builder and
	 * processes the lines of its output with the given output processor.
	 */
	public static void runProcess(ProcessBuilder processBuilder, OutputProcessor outputProcessor) throws IOException, InterruptedException {
		Process process = processBuilder.start();
		BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
		String line;
		while ((line = reader.readLine()) != null) {
			if (outputProcessor != null) {
				outputProcessor.processOutput(line);
			}
		}
		process.waitFor();
	}

}
