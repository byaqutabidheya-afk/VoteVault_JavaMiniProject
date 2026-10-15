package voting.service;

/**
 * Checked exception thrown when persisted data files are malformed, corrupted,
 * or violate expected data schemas.
 */
public class CorruptDataException extends Exception {

    private final int lineNumber;

    /**
     * Constructs a new CorruptDataException with the line number and detail message.
     *
     * @param lineNumber the 1-based line number where corruption was detected
     * @param message    the detail message describing the error
     */
    public CorruptDataException(int lineNumber, String message) {
        super("Line " + lineNumber + ": " + message);
        this.lineNumber = lineNumber;
    }

    /**
     * Returns the line number where the corrupt data was encountered.
     *
     * @return 1-based line number
     */
    public int getLineNumber() {
        return lineNumber;
    }
}
