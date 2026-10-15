package voting.service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import voting.core.Block;
import voting.core.Blockchain;
import voting.model.Voter;

/**
 * Service responsible for persisting and loading blockchain data, voter registries,
 * candidate lists, and backup restoration to and from plain-text, pipe-delimited files.
 */
public class Storage {

    private final File dataDir;
    private final File chainFile;
    private final File chainBackupFile;
    private final File votersFile;
    private final File candidatesFile;

    /**
     * Constructs a Storage manager targeting the specified directory.
     * Creates the directory if it does not already exist.
     *
     * @param dataDir directory path where data files are stored
     */
    public Storage(String dataDir) {
        this.dataDir = new File(dataDir);
        if (!this.dataDir.exists()) {
            this.dataDir.mkdirs();
        }
        this.chainFile = new File(this.dataDir, "chain.txt");
        this.chainBackupFile = new File(this.dataDir, "chain.backup.txt");
        this.votersFile = new File(this.dataDir, "voters.txt");
        this.candidatesFile = new File(this.dataDir, "candidates.txt");
    }

    /**
     * Checks whether a given string is safe for pipe-delimited storage.
     * Rejects null, blank, or strings containing the pipe ('|') character.
     *
     * @param s the text string to validate
     * @return true if safe; false if null, blank, or containing '|'
     */
    public static boolean isSafeText(String s) {
        if (s == null || s.trim().isEmpty()) {
            return false;
        }
        return !s.contains("|");
    }

    /**
     * Persists the given blockchain to {@code chain.txt} in pipe-delimited format.
     * Also updates {@code chain.backup.txt} if the blockchain is valid.
     *
     * @param bc the blockchain to save
     */
    public void saveChain(Blockchain bc) {
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(chainFile), StandardCharsets.UTF_8))) {
            for (Block block : bc.getBlocks()) {
                writer.write(block.getIndex() + "|"
                        + block.getTimestamp() + "|"
                        + block.getVoterHash() + "|"
                        + block.getCandidate() + "|"
                        + block.getPreviousHash() + "|"
                        + block.getNonce() + "|"
                        + block.getHash());
                writer.newLine();
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to save chain to " + chainFile.getPath(), e);
        }

        if (bc.isValid()) {
            try {
                Files.copy(chainFile.toPath(), chainBackupFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                throw new UncheckedIOException("Failed to create chain backup", e);
            }
        }
    }

    /**
     * Restores the blockchain file by copying {@code chain.backup.txt} over {@code chain.txt}.
     *
     * @throws IllegalStateException if no backup file exists or backup is empty
     */
    public void restoreBackup() {
        if (!chainBackupFile.exists() || chainBackupFile.length() == 0) {
            throw new IllegalStateException("No valid backup file found to restore from.");
        }
        try {
            Files.copy(chainBackupFile.toPath(), chainFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to restore chain from backup", e);
        }
    }

    /**
     * Simulates an attacker modifying a block's candidate in {@code chain.txt}
     * while retaining the old hash, simulating unauthorized file alteration.
     *
     * @param index        the 0-based block index to modify (must be > 0)
     * @param newCandidate the unauthorized candidate name
     * @throws IllegalArgumentException if index is 0, negative, or exceeds existing blocks
     */
    public void tamperBlock(int index, String newCandidate) {
        if (index <= 0) {
            throw new IllegalArgumentException("Cannot tamper genesis block (index 0) or negative index.");
        }
        if (!chainFile.exists()) {
            throw new IllegalStateException("Chain file does not exist: " + chainFile.getPath());
        }

        List<String> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(chainFile), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read chain file: " + e.getMessage(), e);
        }

        if (index >= lines.size()) {
            throw new IllegalArgumentException("Block index " + index + " is out of range.");
        }

        String[] parts = lines.get(index).split("\\|", -1);
        if (parts.length != 7) {
            throw new IllegalArgumentException("Malformed block line at index " + index);
        }

        parts[3] = newCandidate;
        lines.set(index, String.join("|", parts));

        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(chainFile), StandardCharsets.UTF_8))) {
            for (String l : lines) {
                writer.write(l);
                writer.newLine();
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to write tampered chain: " + e.getMessage(), e);
        }
    }

    /**
     * Loads the blockchain from {@code chain.txt}.
     * If the file is missing or empty, initializes a fresh chain and saves it.
     *
     * @return loaded Blockchain instance
     * @throws CorruptDataException if any line violates formatting or number rules
     */
    public Blockchain loadChain() throws CorruptDataException {
        if (!chainFile.exists() || chainFile.length() == 0) {
            Blockchain freshChain = new Blockchain();
            saveChain(freshChain);
            return freshChain;
        }

        List<Block> loadedBlocks = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(chainFile), StandardCharsets.UTF_8))) {
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.trim().isEmpty()) {
                    continue;
                }
                loadedBlocks.add(parseBlockLine(line, lineNumber));
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read chain from " + chainFile.getPath(), e);
        }

        if (loadedBlocks.isEmpty()) {
            Blockchain freshChain = new Blockchain();
            saveChain(freshChain);
            return freshChain;
        }

        return new Blockchain(loadedBlocks);
    }

    /**
     * Parses a single pipe-delimited line into a Block object.
     *
     * @param line       the raw text line
     * @param lineNumber 1-based line number for error reporting
     * @return constructed Block
     * @throws CorruptDataException on bad field count or invalid numbers
     */
    private Block parseBlockLine(String line, int lineNumber) throws CorruptDataException {
        String[] parts = line.split("\\|", -1);
        if (parts.length != 7) {
            throw new CorruptDataException(lineNumber, "Expected 7 pipe-separated fields, found " + parts.length);
        }
        try {
            int index = Integer.parseInt(parts[0].trim());
            long timestamp = Long.parseLong(parts[1].trim());
            String voterHash = parts[2].trim();
            String candidate = parts[3].trim();
            String previousHash = parts[4].trim();
            int nonce = Integer.parseInt(parts[5].trim());
            String hash = parts[6].trim();
            return new Block(index, timestamp, voterHash, candidate, previousHash, nonce, hash);
        } catch (NumberFormatException nfe) {
            throw new CorruptDataException(lineNumber, "Invalid numeric format: " + nfe.getMessage());
        }
    }

    /**
     * Persists the voter registry to {@code voters.txt}.
     *
     * @param voters map of voter IDs to Voter records
     */
    public void saveVoters(Map<String, Voter> voters) {
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(votersFile), StandardCharsets.UTF_8))) {
            for (Voter voter : voters.values()) {
                writer.write(voter.getVoterId() + "|" + voter.getName());
                writer.newLine();
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to save voters to " + votersFile.getPath(), e);
        }
    }

    /**
     * Loads the registered voters from {@code voters.txt}.
     *
     * @return map of voter IDs to Voter records
     * @throws CorruptDataException if any record is malformed
     */
    public Map<String, Voter> loadVoters() throws CorruptDataException {
        Map<String, Voter> voters = new LinkedHashMap<>();
        if (!votersFile.exists() || votersFile.length() == 0) {
            return voters;
        }

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(votersFile), StandardCharsets.UTF_8))) {
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.trim().isEmpty()) {
                    continue;
                }
                String[] parts = line.split("\\|", -1);
                if (parts.length != 2) {
                    throw new CorruptDataException(lineNumber,
                            "Expected 2 pipe-separated fields for voter, found " + parts.length);
                }
                String voterId = parts[0].trim();
                String name = parts[1].trim();
                voters.put(voterId, new Voter(voterId, name));
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read voters from " + votersFile.getPath(), e);
        }
        return voters;
    }

    /**
     * Persists the candidate list to {@code candidates.txt}.
     *
     * @param candidates list of candidate names
     */
    public void saveCandidates(List<String> candidates) {
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(candidatesFile), StandardCharsets.UTF_8))) {
            for (String candidate : candidates) {
                writer.write(candidate);
                writer.newLine();
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to save candidates to " + candidatesFile.getPath(), e);
        }
    }

    /**
     * Loads candidate names from {@code candidates.txt}.
     *
     * @return list of candidate names
     */
    public List<String> loadCandidates() {
        List<String> candidates = new ArrayList<>();
        if (!candidatesFile.exists() || candidatesFile.length() == 0) {
            return candidates;
        }

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(candidatesFile), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }
                candidates.add(line.trim());
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read candidates from " + candidatesFile.getPath(), e);
        }
        return candidates;
    }
}
