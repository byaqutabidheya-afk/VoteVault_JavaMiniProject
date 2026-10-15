package voting.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import voting.core.Block;
import voting.core.Blockchain;
import voting.model.Voter;
import voting.util.HashUtil;

/**
 * Service managing all voting business rules, voter registrations, candidate nominations,
 * vote casting, and election analytics for ChainVote.
 */
public class VotingService {

    /**
     * SHA-256 hash of the default administrator password ("admin123").
     */
    public static final String ADMIN_PASSWORD_HASH = HashUtil.sha256("admin123");

    private final Storage storage;
    private Blockchain blockchain;
    private final Map<String, Voter> voters;
    private final List<String> candidates;
    private boolean corrupted;
    private String loadError;

    /**
     * Constructs a VotingService by loading existing data from storage.
     * If storage loading encounters corrupt data, marks the system as corrupted without crashing.
     *
     * @param storage the persistence storage provider
     */
    public VotingService(Storage storage) {
        this.storage = storage;
        this.voters = new LinkedHashMap<>();
        this.candidates = new ArrayList<>();
        this.corrupted = false;
        this.loadError = null;

        try {
            this.blockchain = storage.loadChain();
            Map<String, Voter> loadedVoters = storage.loadVoters();
            for (Map.Entry<String, Voter> entry : loadedVoters.entrySet()) {
                String upperId = entry.getKey().toUpperCase();
                this.voters.put(upperId, new Voter(upperId, entry.getValue().getName()));
            }
            this.candidates.addAll(storage.loadCandidates());
        } catch (CorruptDataException e) {
            this.corrupted = true;
            this.loadError = e.getMessage();
            this.blockchain = new Blockchain();
        }
    }

    /**
     * Registers a new voter into the system.
     * Voter ID must match regex {@code [A-Za-z0-9]{3,12}} and name must pass safe-text validation.
     *
     * @param voterId the unique voter ID
     * @param name    the voter's full name
     * @throws VotingException if validation fails or voter ID already exists
     */
    public void registerVoter(String voterId, String name) {
        if (voterId == null || !voterId.matches("^[A-Za-z0-9]{3,12}$")) {
            throw new VotingException("Invalid voter ID: must match [A-Za-z0-9]{3,12}");
        }
        if (!Storage.isSafeText(name)) {
            throw new VotingException("Invalid voter name: cannot be null, blank, or contain '|'");
        }
        String upperId = voterId.toUpperCase();
        if (voters.containsKey(upperId)) {
            throw new VotingException("Voter ID already registered: " + upperId);
        }

        voters.put(upperId, new Voter(upperId, name.trim()));
        storage.saveVoters(voters);
    }

    /**
     * Adds a candidate to the election ballot.
     * Candidates cannot be added after voting has started (chain size > 1).
     *
     * @param name the candidate name
     * @throws VotingException if name is invalid, duplicate, or votes already exist
     */
    public void addCandidate(String name) {
        if (!Storage.isSafeText(name)) {
            throw new VotingException("Invalid candidate name: cannot be null, blank, or contain '|'");
        }
        if (blockchain.size() > 1) {
            throw new VotingException("Cannot add candidates once voting has started");
        }
        for (String existing : candidates) {
            if (existing.equalsIgnoreCase(name.trim())) {
                throw new VotingException("Candidate already exists: " + name);
            }
        }

        candidates.add(name.trim());
        storage.saveCandidates(candidates);
    }

    /**
     * Checks whether the voter with the given ID has already cast a vote in the blockchain.
     *
     * @param voterId the voter ID
     * @return true if a block in the chain matches the hashed voter ID; false otherwise
     */
    public boolean hasVoted(String voterId) {
        if (voterId == null) {
            return false;
        }
        String voterHash = HashUtil.sha256(voterId.toUpperCase());
        for (Block block : blockchain.getBlocks()) {
            if (voterHash.equals(block.getVoterHash())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Casts an anonymized vote into the blockchain for the specified candidate.
     *
     * @param voterId   the ID of the registered voter casting the ballot
     * @param candidate the chosen candidate
     * @return the newly mined Block recording the vote
     * @throws VotingException if system is corrupted, chain tampered, voter ineligible,
     *                         or candidate unknown
     */
    public Block castVote(String voterId, String candidate) {
        if (corrupted || !blockchain.isValid()) {
            throw new VotingException("Voting locked: chain tampered");
        }
        if (voterId == null) {
            throw new VotingException("Voter ID cannot be null");
        }
        String upperId = voterId.toUpperCase();
        if (!voters.containsKey(upperId)) {
            throw new VotingException("Unregistered voter: " + upperId);
        }
        if (hasVoted(upperId)) {
            throw new VotingException("Voter has already cast a vote: " + upperId);
        }
        if (candidates.isEmpty()) {
            throw new VotingException("No candidates registered in election");
        }
        boolean validCandidate = false;
        for (String c : candidates) {
            if (c.equals(candidate)) {
                validCandidate = true;
                break;
            }
        }
        if (!validCandidate) {
            throw new VotingException("Unknown candidate: " + candidate);
        }

        String voterHash = HashUtil.sha256(upperId);
        Block block = blockchain.addVote(voterHash, candidate);
        if (!corrupted) {
            storage.saveChain(blockchain);
        }
        return block;
    }

    /**
     * Tallies election results, mapping every registered candidate (even with 0 votes)
     * to their vote count. Results are sorted in descending order of votes, with ties
     * maintaining the order candidates were added.
     *
     * @return sorted map of candidate names to total vote counts
     */
    public Map<String, Integer> getResults() {
        Map<String, Integer> counts = new LinkedHashMap<>();
        for (String candidate : candidates) {
            counts.put(candidate, 0);
        }

        for (int i = 1; i < blockchain.size(); i++) {
            Block block = blockchain.getBlocks().get(i);
            String candidate = block.getCandidate();
            if (counts.containsKey(candidate)) {
                counts.put(candidate, counts.get(candidate) + 1);
            }
        }

        List<Map.Entry<String, Integer>> entryList = new ArrayList<>(counts.entrySet());
        entryList.sort((e1, e2) -> Integer.compare(e2.getValue(), e1.getValue()));

        Map<String, Integer> sortedResults = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> entry : entryList) {
            sortedResults.put(entry.getKey(), entry.getValue());
        }
        return sortedResults;
    }

    /**
     * Returns the total count of non-genesis votes cast in the blockchain.
     *
     * @return total votes cast
     */
    public int getTotalVotes() {
        return Math.max(0, blockchain.size() - 1);
    }

    /**
     * Returns the total number of registered voters.
     *
     * @return registered voter count
     */
    public int getTotalVoters() {
        return voters.size();
    }

    /**
     * Calculates the voter turnout percentage.
     *
     * @return turnout percentage from 0.0 to 100.0, or 0.0 if no voters are registered
     */
    public double getTurnoutPercent() {
        if (voters.isEmpty()) {
            return 0.0;
        }
        return ((double) getTotalVotes() / voters.size()) * 100.0;
    }

    /**
     * Verifies an input administrator password against the stored SHA-256 hash.
     *
     * @param input the plain password string to check
     * @return true if the hash of input matches {@link #ADMIN_PASSWORD_HASH}; false otherwise
     */
    public boolean verifyPassword(String input) {
        if (input == null) {
            return false;
        }
        return ADMIN_PASSWORD_HASH.equals(HashUtil.sha256(input));
    }

    /**
     * Resets the election blockchain to a fresh Genesis block and clears corruption flags.
     * Existing registered voters and candidates are preserved.
     */
    public void resetElection() {
        this.blockchain = new Blockchain();
        this.corrupted = false;
        this.loadError = null;
        storage.saveChain(this.blockchain);
    }

    /**
     * Returns the underlying Blockchain instance.
     *
     * @return the blockchain
     */
    public Blockchain getBlockchain() {
        return blockchain;
    }

    /**
     * Returns an unmodifiable list of registered candidates.
     *
     * @return list of candidates
     */
    public List<String> getCandidates() {
        return Collections.unmodifiableList(candidates);
    }

    /**
     * Returns an unmodifiable map of registered voters keyed by uppercase voter ID.
     *
     * @return map of voters
     */
    public Map<String, Voter> getVoters() {
        return Collections.unmodifiableMap(voters);
    }

    /**
     * Indicates whether the underlying data files were corrupted on load.
     *
     * @return true if corrupted; false otherwise
     */
    public boolean isCorrupted() {
        return corrupted;
    }

    /**
     * Returns the error message encountered during data loading if corrupted.
     *
     * @return corruption error message or null
     */
    public String getLoadError() {
        return loadError;
    }
}
