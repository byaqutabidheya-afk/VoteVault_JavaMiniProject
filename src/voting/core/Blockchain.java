package voting.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents the VoteVault blockchain.
 * Manages an ordered list of mined blocks, allows adding new votes, and provides
 * cryptographic integrity validation against tampering and re-mining attacks.
 */
public class Blockchain {

    /**
     * The default proof-of-work difficulty (number of leading zeros required).
     */
    public static final int DIFFICULTY = 4;

    private final List<Block> chain;

    /**
     * Initializes a new Blockchain with the mined Genesis block.
     */
    public Blockchain() {
        this.chain = new ArrayList<>();
        this.chain.add(Block.genesis(DIFFICULTY));
    }

    /**
     * Initializes a Blockchain with an existing list of loaded blocks.
     * The blocks are used as-is without re-mining or modification.
     *
     * @param loadedBlocks the list of blocks to load into the chain
     */
    public Blockchain(List<Block> loadedBlocks) {
        this.chain = new ArrayList<>(loadedBlocks);
    }

    /**
     * Returns the most recent block in the blockchain, or null if the chain is empty.
     *
     * @return the latest block
     */
    public Block getLatestBlock() {
        if (chain.isEmpty()) {
            return null;
        }
        return chain.get(chain.size() - 1);
    }

    /**
     * Creates, mines, and appends a new vote block to the blockchain.
     *
     * @param voterHash the cryptographic hash of the voter
     * @param candidate the candidate receiving the vote
     * @return the newly mined and added block
     */
    public Block addVote(String voterHash, String candidate) {
        int index = chain.size();
        long timestamp = System.currentTimeMillis();
        String previousHash = (getLatestBlock() != null) ? getLatestBlock().getHash() : "0";

        Block newBlock = new Block(index, timestamp, voterHash, candidate, previousHash);
        newBlock.mineBlock(DIFFICULTY);
        chain.add(newBlock);
        return newBlock;
    }

    /**
     * Validates the integrity of the blockchain against four validation rules:
     * <ol>
     *   <li>Stored hash equals recalculated hash</li>
     *   <li>Block previousHash equals preceding block's hash (skipped for index 0)</li>
     *   <li>Block hash meets the proof-of-work difficulty target</li>
     *   <li>Block index matches its sequential position in the chain</li>
     * </ol>
     *
     * @return -1 if the entire chain is valid; otherwise the index of the first invalid block,
     *         or 0 if the chain is empty
     */
    public int findFirstInvalidBlock() {
        if (chain.isEmpty()) {
            return 0;
        }
        String target = "0".repeat(DIFFICULTY);
        for (int i = 0; i < chain.size(); i++) {
            Block current = chain.get(i);

            // Rule 1: stored hash != calculateHash()
            if (!current.getHash().equals(current.calculateHash())) {
                return i;
            }

            // Rule 2: block.previousHash != previous block's hash (skip for index 0)
            if (i > 0) {
                Block previous = chain.get(i - 1);
                if (!current.getPreviousHash().equals(previous.getHash())) {
                    return i;
                }
            }

            // Rule 3: hash does not start with DIFFICULTY zeros
            if (!current.getHash().startsWith(target)) {
                return i;
            }

            // Rule 4: block.getIndex() != its position in the list
            if (current.getIndex() != i) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Returns a human-readable explanation of why a block at a given position is invalid.
     *
     * @param position the 0-based index of the block to evaluate
     * @return a description of the integrity violation, or "Block is valid" if no rules are violated
     */
    public String getInvalidReason(int position) {
        if (position < 0 || position >= chain.size()) {
            return "Invalid block position";
        }
        Block current = chain.get(position);

        // Rule 1: stored hash != calculateHash()
        if (!current.getHash().equals(current.calculateHash())) {
            return "Block data was modified (hash mismatch)";
        }

        // Rule 2: block.previousHash != previous block's hash
        if (position > 0) {
            Block previous = chain.get(position - 1);
            if (!current.getPreviousHash().equals(previous.getHash())) {
                return "Broken link to previous block";
            }
        }

        // Rule 3: hash does not start with DIFFICULTY zeros
        String target = "0".repeat(DIFFICULTY);
        if (!current.getHash().startsWith(target)) {
            return "Proof-of-work not satisfied";
        }

        // Rule 4: block.getIndex() != its position in the list
        if (current.getIndex() != position) {
            return "Block missing or out of order";
        }

        return "Block is valid";
    }

    /**
     * Checks whether the entire blockchain is cryptographically valid.
     *
     * @return true if all blocks satisfy all integrity rules; false otherwise
     */
    public boolean isValid() {
        return findFirstInvalidBlock() == -1;
    }

    /**
     * Returns an unmodifiable view of the blocks in the chain.
     *
     * @return unmodifiable list of blocks
     */
    public List<Block> getBlocks() {
        return Collections.unmodifiableList(chain);
    }

    /**
     * Returns the total number of blocks in the chain.
     *
     * @return the chain size
     */
    public int size() {
        return chain.size();
    }
}
