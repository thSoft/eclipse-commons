package javax.util.process;

/**
 * Processes a line of the output of a running process.
 */
public interface OutputProcessor {

	void processOutput(String line);

}
