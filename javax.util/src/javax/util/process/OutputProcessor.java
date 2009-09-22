package javax.util.process;

public interface OutputProcessor {

	/**
	 * Processes a line of the output of a running process.
	 */
	public void processOutput(String line);

}
