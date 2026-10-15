package voting.service;

/**
 * Unchecked exception thrown when a voting business rule or validation constraint is violated.
 */
public class VotingException extends RuntimeException {

    /**
     * Constructs a new VotingException with the specified error message.
     *
     * @param message the detail explanation of the rule violation
     */
    public VotingException(String message) {
        super(message);
    }
}
