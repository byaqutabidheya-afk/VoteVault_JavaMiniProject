package voting.model;

/**
 * Represents a registered voter in the ChainVote system.
 */
public class Voter {

    private final String voterId;
    private final String name;

    /**
     * Constructs a new Voter record.
     *
     * @param voterId the unique identifier for the voter
     * @param name    the full name of the voter
     */
    public Voter(String voterId, String name) {
        this.voterId = voterId;
        this.name = name;
    }

    /**
     * Returns the voter identifier.
     *
     * @return the voter ID
     */
    public String getVoterId() {
        return voterId;
    }

    /**
     * Returns the full name of the voter.
     *
     * @return the voter's name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns a string representation of the voter.
     *
     * @return formatted voter string
     */
    @Override
    public String toString() {
        return voterId + " (" + name + ")";
    }
}
