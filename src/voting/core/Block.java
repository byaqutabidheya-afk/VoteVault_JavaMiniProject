package voting.core;

import voting.util.HashUtil;

/**
 * Represents a single block in the VoteVault blockchain.
 * Each block records a vote and is linked to the previous block via cryptographic hashing.
 */
public class Block {

    private final int index;
    private final long timestamp;
    private final String voterHash;
    private final String candidate;
    private final String previousHash;
    private int nonce;
    private String hash;

    /**
     * Constructs a new Block with initial nonce 0 and calculates its hash.
     *
     * @param index        the sequential position of the block in the blockchain
     * @param timestamp    the creation timestamp in milliseconds
     * @param voterHash    the anonymized cryptographic hash of the voter
     * @param candidate    the candidate voted for
     * @param previousHash the hash of the previous block in the chain
     */
    public Block(int index, long timestamp, String voterHash, String candidate, String previousHash) {
        this.index = index;
        this.timestamp = timestamp;
        this.voterHash = voterHash;
        this.candidate = candidate;
        this.previousHash = previousHash;
        this.nonce = 0;
        this.hash = calculateHash();
    }

    /**
     * Constructs a Block with all fields explicitly set.
     * Used for loading existing blocks from storage without recalculating the hash.
     *
     * @param index        the sequential position of the block in the blockchain
     * @param timestamp    the creation timestamp in milliseconds
     * @param voterHash    the anonymized cryptographic hash of the voter
     * @param candidate    the candidate voted for
     * @param previousHash the hash of the previous block in the chain
     * @param nonce        the mined nonce value
     * @param hash         the stored hash of the block
     */
    public Block(int index, long timestamp, String voterHash, String candidate,
                 String previousHash, int nonce, String hash) {
        this.index = index;
        this.timestamp = timestamp;
        this.voterHash = voterHash;
        this.candidate = candidate;
        this.previousHash = previousHash;
        this.nonce = nonce;
        this.hash = hash;
    }

    /**
     * Calculates the SHA-256 hash of this block's contents.
     *
     * @return the computed 64-character SHA-256 hex string
     */
    public String calculateHash() {
        return HashUtil.sha256(index + Long.toString(timestamp) + voterHash + candidate + previousHash + nonce);
    }

    /**
     * Mines the block with proof-of-work by finding a nonce that produces a hash
     * starting with the specified number of zero characters.
     *
     * @param difficulty the number of leading zeros required
     */
    public void mineBlock(int difficulty) {
        String target = "0".repeat(difficulty);
        do {
            this.nonce++;
            this.hash = calculateHash();
        } while (!this.hash.startsWith(target));
    }

    /**
     * Returns the index of this block.
     *
     * @return the block index
     */
    public int getIndex() {
        return index;
    }

    /**
     * Returns the creation timestamp of this block.
     *
     * @return the timestamp in milliseconds
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * Returns the voter hash.
     *
     * @return the voter hash
     */
    public String getVoterHash() {
        return voterHash;
    }

    /**
     * Returns the candidate for whom the vote was cast.
     *
     * @return the candidate name
     */
    public String getCandidate() {
        return candidate;
    }

    /**
     * Returns the hash of the previous block.
     *
     * @return the previous block hash
     */
    public String getPreviousHash() {
        return previousHash;
    }

    /**
     * Returns the proof-of-work nonce.
     *
     * @return the nonce
     */
    public int getNonce() {
        return nonce;
    }

    /**
     * Returns the stored hash of this block.
     *
     * @return the block hash
     */
    public String getHash() {
        return hash;
    }

    /**
     * Returns a multi-line string representation of this block, shortening hashes for readability.
     *
     * @return a formatted multi-line summary of the block
     */
    @Override
    public String toString() {
        String shortVoterHash = shorten(voterHash);
        String shortPrevHash = shorten(previousHash);
        String shortHash = shorten(hash);

        return "Block #" + index + "\n"
                + "  Timestamp    : " + timestamp + "\n"
                + "  Voter Hash   : " + shortVoterHash + "\n"
                + "  Candidate    : " + candidate + "\n"
                + "  Prev Hash    : " + shortPrevHash + "\n"
                + "  Nonce        : " + nonce + "\n"
                + "  Hash         : " + shortHash;
    }

    /**
     * Helper method to shorten hash strings to the first 16 characters plus ellipsis.
     *
     * @param str the string to shorten
     * @return shortened string or the original if shorter than or equal to 16 characters
     */
    private String shorten(String str) {
        if (str == null) {
            return "null";
        }
        if (str.length() > 16) {
            return str.substring(0, 16) + "...";
        }
        return str;
    }

    /**
     * Factory method that creates and mines the Genesis block (the initial block in the chain).
     *
     * @param difficulty the proof-of-work difficulty for mining the Genesis block
     * @return the mined Genesis block
     */
    public static Block genesis(int difficulty) {
        Block genesisBlock = new Block(0, 0L, "GENESIS", "GENESIS", "0");
        genesisBlock.mineBlock(difficulty);
        return genesisBlock;
    }
}
