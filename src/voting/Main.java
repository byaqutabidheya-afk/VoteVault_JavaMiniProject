package voting;

import java.util.List;
import java.util.Map;
import java.util.Scanner;
import voting.core.Block;
import voting.core.Blockchain;
import voting.model.Voter;
import voting.service.Storage;
import voting.service.VotingException;
import voting.service.VotingService;
import voting.util.ConsoleUI;

/**
 * Main application class running the interactive console user interface for VoteVault.
 */
public class Main {

    private static final Storage storage = new Storage("data");
    private static VotingService service;

    /**
     * Entry point for running the VoteVault console application.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        service = new VotingService(storage);

        clearScreen();
        ConsoleUI.banner();
        runStartupIntegrityCheck();
        ConsoleUI.pause(scanner);

        boolean running = true;
        while (running) {
            try {
                clearScreen();
                ConsoleUI.banner();
                displayStatusSummary();
                displayMainMenu();

                int choice = ConsoleUI.readInt(scanner, "\nEnter choice [0-6]: ", 0, 6);
                switch (choice) {
                    case 1 -> handleCastVote(scanner);
                    case 2 -> handleViewResults(scanner);
                    case 3 -> handleExploreBlockchain(scanner);
                    case 4 -> handleVerifyIntegrity(scanner);
                    case 5 -> handleAdminPanel(scanner);
                    case 6 -> handleHackerSimulation(scanner);
                    case 0 -> {
                        running = false;
                        System.out.println("\n" + ConsoleUI.CYAN + "Thank you for using VoteVault. Goodbye!" + ConsoleUI.RESET);
                    }
                    default -> ConsoleUI.error("Invalid option selected.");
                }
            } catch (VotingException ve) {
                ConsoleUI.error(ve.getMessage());
                ConsoleUI.pause(scanner);
            } catch (Exception e) {
                ConsoleUI.error("An unexpected error occurred: " + e.getMessage());
                ConsoleUI.pause(scanner);
            }
        }
    }

    /**
     * Prints the primary navigation menu.
     */
    private static void displayMainMenu() {
        System.out.println("\n" + ConsoleUI.BOLD + "=== MAIN MENU ===" + ConsoleUI.RESET);
        System.out.println("  1. Cast Vote");
        System.out.println("  2. View Live Results");
        System.out.println("  3. Explore Blockchain");
        System.out.println("  4. Verify Integrity");
        System.out.println("  5. Admin Panel");
        System.out.println("  6. Hacker Simulation");
        System.out.println("  0. Exit");
    }

    /**
     * Clears the console terminal screen.
     */
    private static void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    /**
     * Runs startup integrity verification and displays the status of the blockchain.
     */
    private static void runStartupIntegrityCheck() {
        if (service.isCorrupted()) {
            printBoxedRedWarning("CRITICAL: DATA CORRUPTION DETECTED!\n"
                    + service.getLoadError() + "\n"
                    + "Voting is locked. Contact administrator.");
        } else if (!service.getBlockchain().isValid()) {
            int badIndex = service.getBlockchain().findFirstInvalidBlock();
            String reason = service.getBlockchain().getInvalidReason(badIndex);
            printBoxedRedWarning("CRITICAL: BLOCKCHAIN INTEGRITY COMPROMISED!\n"
                    + "Tampered block detected at Block #" + badIndex + " (" + reason + ")\n"
                    + "Voting is locked. Contact administrator.");
        } else {
            ConsoleUI.success("Blockchain verified: " + service.getBlockchain().size()
                    + " blocks, integrity OK");
        }
    }

    /**
     * Displays a compact status line indicating registration and vote numbers.
     */
    private static void displayStatusSummary() {
        int votes = service.getTotalVotes();
        int voters = service.getTotalVoters();
        boolean locked = service.isCorrupted() || !service.getBlockchain().isValid();

        if (locked) {
            System.out.println(ConsoleUI.RED + ConsoleUI.BOLD + "[SYSTEM LOCKED: TAMPERING DETECTED]" + ConsoleUI.RESET);
        } else {
            System.out.println(ConsoleUI.CYAN + "Status: Ready | Total Votes: " + votes
                    + " | Registered Voters: " + voters
                    + " | Candidates: " + service.getCandidates().size() + ConsoleUI.RESET);
        }
    }

    /**
     * Handles the voting workflow: prompts for voter ID, candidate choice, and mines the block.
     *
     * @param sc input scanner
     */
    private static void handleCastVote(Scanner sc) {
        clearScreen();
        System.out.println(ConsoleUI.BOLD + "--- CAST YOUR VOTE ---" + ConsoleUI.RESET);

        if (service.isCorrupted() || !service.getBlockchain().isValid()) {
            ConsoleUI.error("Voting is currently locked due to blockchain integrity issues.");
            ConsoleUI.pause(sc);
            return;
        }

        List<String> candidates = service.getCandidates();
        if (candidates.isEmpty()) {
            ConsoleUI.error("No candidates registered in this election yet. An administrator must add candidates first.");
            ConsoleUI.pause(sc);
            return;
        }

        String voterId = ConsoleUI.readLine(sc, "Enter your Voter ID: ");
        if (voterId.isEmpty()) {
            ConsoleUI.error("Voter ID cannot be empty.");
            ConsoleUI.pause(sc);
            return;
        }

        String upperId = voterId.toUpperCase();
        if (!service.getVoters().containsKey(upperId)) {
            ConsoleUI.error("Voter ID '" + upperId + "' is not registered.");
            ConsoleUI.pause(sc);
            return;
        }

        if (service.hasVoted(upperId)) {
            ConsoleUI.error("Voter '" + upperId + "' has already voted! Double voting is strictly prohibited.");
            ConsoleUI.pause(sc);
            return;
        }

        System.out.println("\nOfficial Ballot Candidates:");
        for (int i = 0; i < candidates.size(); i++) {
            System.out.println("  " + (i + 1) + ". " + candidates.get(i));
        }

        int choice = ConsoleUI.readInt(sc, "\nSelect candidate number [1-" + candidates.size() + "]: ", 1, candidates.size());
        String selectedCandidate = candidates.get(choice - 1);

        final Block[] minedBlock = new Block[1];
        ConsoleUI.spinner("Mining block for your vote...", () -> {
            minedBlock[0] = service.castVote(upperId, selectedCandidate);
        });

        Block block = minedBlock[0];
        System.out.println();
        ConsoleUI.success("Vote recorded successfully in Block #" + block.getIndex() + "!");
        System.out.println("Candidate: " + block.getCandidate());
        System.out.println("Block Hash: " + block.getHash());
        System.out.println("Your vote is now cryptographically secured on the blockchain.");
        ConsoleUI.pause(sc);
    }

    /**
     * Displays the election results with graphical ASCII bar charts and mining statistics.
     *
     * @param sc input scanner
     */
    private static void handleViewResults(Scanner sc) {
        clearScreen();
        System.out.println(ConsoleUI.BOLD + "--- LIVE ELECTION RESULTS ---" + ConsoleUI.RESET + "\n");

        Map<String, Integer> results = service.getResults();
        int totalVotes = service.getTotalVotes();
        int totalVoters = service.getTotalVoters();
        double turnout = service.getTurnoutPercent();

        if (results.isEmpty()) {
            ConsoleUI.info("No candidates registered yet.");
        } else {
            ConsoleUI.barChart(results, totalVotes);
            System.out.println("\n" + "-".repeat(60));
            System.out.printf("Total Votes Cast : %d%n", totalVotes);
            System.out.printf("Registered Voters: %d%n", totalVoters);
            System.out.printf("Voter Turnout    : %.1f%%%n", turnout);
            System.out.printf("Total Blocks     : %d | Difficulty: %d | Avg Mining Time: %s%n",
                    service.getBlockchain().size(),
                    Blockchain.DIFFICULTY,
                    service.getAverageMiningTimeMs());
            System.out.println("-".repeat(60));
        }

        ConsoleUI.pause(sc);
    }

    /**
     * Explores and displays the sequential blocks in the blockchain with pagination.
     *
     * @param sc input scanner
     */
    private static void handleExploreBlockchain(Scanner sc) {
        clearScreen();
        System.out.println(ConsoleUI.BOLD + "--- BLOCKCHAIN EXPLORER ---" + ConsoleUI.RESET);

        List<Block> blocks = service.getBlockchain().getBlocks();
        int totalBlocks = blocks.size();
        int pageSize = 5;

        for (int i = 0; i < totalBlocks; i += pageSize) {
            int end = Math.min(i + pageSize, totalBlocks);
            System.out.println("\n" + ConsoleUI.CYAN + "=== Showing blocks " + (i + 1) + " to " + end
                    + " of " + totalBlocks + " ===" + ConsoleUI.RESET + "\n");

            for (int j = i; j < end; j++) {
                System.out.println(blocks.get(j));
                if (j < totalBlocks - 1) {
                    System.out.println("      " + ConsoleUI.CYAN + "↓ linked to next block" + ConsoleUI.RESET);
                }
            }

            if (end < totalBlocks) {
                String input = ConsoleUI.readLine(sc, "\nPress Enter for next page, or type 'q' to return to menu: ");
                if (input.equalsIgnoreCase("q")) {
                    return;
                }
            }
        }

        ConsoleUI.pause(sc);
    }

    /**
     * Reloads data from disk and verifies the cryptographic integrity of the entire blockchain.
     * Displays detailed side-by-side diagnostic evidence if tampering is discovered.
     *
     * @param sc input scanner
     */
    private static void handleVerifyIntegrity(Scanner sc) {
        clearScreen();
        System.out.println(ConsoleUI.BOLD + "--- BLOCKCHAIN INTEGRITY VERIFICATION ---" + ConsoleUI.RESET + "\n");

        service = new VotingService(storage);

        if (service.isCorrupted()) {
            printBoxedRedWarning("DATA FILE CORRUPTED!\n"
                    + service.getLoadError() + "\n"
                    + "Voting is locked.");
            ConsoleUI.pause(sc);
            return;
        }

        Blockchain chain = service.getBlockchain();
        int badIndex = chain.findFirstInvalidBlock();

        if (badIndex == -1) {
            for (Block b : chain.getBlocks()) {
                String shortHash = shortenHash(b.getHash());
                ConsoleUI.success("Block #" + b.getIndex() + " [" + shortHash + "]: valid");
            }
            System.out.println();
            ConsoleUI.success("Blockchain verified: " + chain.size() + " blocks, integrity OK.");
        } else {
            displayTamperingReport(chain, badIndex);
        }

        ConsoleUI.pause(sc);
    }

    /**
     * Displays a comprehensive tampering analysis with side-by-side hash comparison.
     *
     * @param chain    the compromised blockchain
     * @param badIndex index of the first invalid block
     */
    private static void displayTamperingReport(Blockchain chain, int badIndex) {
        for (int i = 0; i < chain.size(); i++) {
            Block b = chain.getBlocks().get(i);
            String shortHash = shortenHash(b.getHash());

            if (i < badIndex) {
                ConsoleUI.success("Block #" + b.getIndex() + " [" + shortHash + "]: valid");
            } else if (i == badIndex) {
                String reason = chain.getInvalidReason(i);
                ConsoleUI.error("Block #" + b.getIndex() + " [" + shortHash + "]: INVALID - " + reason);
            } else {
                ConsoleUI.warn("Block #" + b.getIndex() + " [" + shortHash + "]: UNTRUSTED (broken link)");
            }
        }

        System.out.println();
        printBoxedRedWarning("TAMPERING DETECTED!\n"
                + "Compromised Block: Block #" + badIndex + "\n"
                + "Failure Reason: " + chain.getInvalidReason(badIndex));

        displayCryptographicEvidence(chain, badIndex);

        System.out.println("\n" + ConsoleUI.YELLOW
                + "Changing one vote breaks this block's fingerprint and every link after it. "
                + "Re-mining all following blocks would be required." + ConsoleUI.RESET);
    }

    /**
     * Prints side-by-side cryptographic evidence showing hash or link mismatches.
     *
     * @param chain    the blockchain
     * @param badIndex index of the invalid block
     */
    private static void displayCryptographicEvidence(Blockchain chain, int badIndex) {
        Block badBlock = chain.getBlocks().get(badIndex);
        String reason = chain.getInvalidReason(badIndex);

        System.out.println("\n" + ConsoleUI.BOLD + "Side-by-Side Cryptographic Evidence:" + ConsoleUI.RESET);
        if (reason.contains("hash mismatch")) {
            String stored = badBlock.getHash();
            String recalced = badBlock.calculateHash();
            System.out.println("  STORED Hash       : " + stored.substring(0, Math.min(20, stored.length())) + "...");
            System.out.println("  RECALCULATED Hash : " + recalced.substring(0, Math.min(20, recalced.length())) + "...");
        } else if (reason.contains("Broken link") && badIndex > 0) {
            Block prevBlock = chain.getBlocks().get(badIndex - 1);
            String recorded = badBlock.getPreviousHash();
            String actual = prevBlock.getHash();
            System.out.println("  RECORDED PrevHash : " + recorded.substring(0, Math.min(20, recorded.length())) + "...");
            System.out.println("  ACTUAL PrevHash   : " + actual.substring(0, Math.min(20, actual.length())) + "...");
        } else {
            System.out.println("  Reason Details    : " + reason);
        }
    }

    /**
     * Executes the Hacker Simulation allowing an authorized admin to tamper with a block
     * and observe live detection.
     *
     * @param sc input scanner
     */
    private static void handleHackerSimulation(Scanner sc) {
        clearScreen();
        System.out.println(ConsoleUI.BOLD + "--- HACKER SIMULATION (AUDIENCE DEMO) ---" + ConsoleUI.RESET);

        String pwd = ConsoleUI.readLine(sc, "Enter Admin Password to proceed: ");
        if (!service.verifyPassword(pwd)) {
            ConsoleUI.error("Access denied: Admin credentials required.");
            ConsoleUI.pause(sc);
            return;
        }

        Blockchain chain = service.getBlockchain();
        if (chain.size() <= 1) {
            ConsoleUI.warn("No votes exist yet to tamper with. Cast some votes first!");
            ConsoleUI.pause(sc);
            return;
        }

        System.out.println("\nAvailable Blocks to Attack (1 to " + (chain.size() - 1) + "):");
        for (int i = 1; i < chain.size(); i++) {
            Block b = chain.getBlocks().get(i);
            System.out.println("  Block #" + b.getIndex() + " | Current Candidate: " + b.getCandidate());
        }

        int targetIndex = ConsoleUI.readInt(sc, "\nEnter block number to attack: ", 1, chain.size() - 1);
        Block targetBlock = chain.getBlocks().get(targetIndex);
        String currentCand = targetBlock.getCandidate();

        System.out.println("\nCurrent candidate in Block #" + targetIndex + " is: " + currentCand);
        String newCand = ConsoleUI.readLine(sc, "Enter replacement candidate name: ");
        if (!Storage.isSafeText(newCand) || newCand.equalsIgnoreCase(currentCand)) {
            ConsoleUI.error("Candidate must be valid safe text and different from current candidate ('" + currentCand + "').");
            ConsoleUI.pause(sc);
            return;
        }

        System.out.println("\n" + ConsoleUI.MAGENTA + "Simulating attacker editing chain.txt..." + ConsoleUI.RESET);
        try {
            storage.tamperBlock(targetIndex, newCand);
            Thread.sleep(600);
        } catch (Exception e) {
            ConsoleUI.error("Tamper failed: " + e.getMessage());
            ConsoleUI.pause(sc);
            return;
        }

        handleVerifyIntegrity(sc);
    }

    /**
     * Authenticates an administrator and provides election administration actions.
     *
     * @param sc input scanner
     */
    private static void handleAdminPanel(Scanner sc) {
        clearScreen();
        System.out.println(ConsoleUI.BOLD + "--- ADMIN AUTHENTICATION ---" + ConsoleUI.RESET);

        if (!authenticateAdmin(sc)) {
            ConsoleUI.pause(sc);
            return;
        }

        boolean inAdmin = true;
        while (inAdmin) {
            clearScreen();
            System.out.println(ConsoleUI.BOLD + "=== ADMIN PANEL ===" + ConsoleUI.RESET);
            System.out.println("  1. Register Voter");
            System.out.println("  2. Add Candidate");
            System.out.println("  3. List Voters");
            System.out.println("  4. Reset Election");
            System.out.println("  5. Repair from Backup");
            System.out.println("  0. Back to Main Menu");

            int adminChoice = ConsoleUI.readInt(sc, "\nEnter admin choice [0-5]: ", 0, 5);
            switch (adminChoice) {
                case 1 -> handleAdminRegisterVoter(sc);
                case 2 -> handleAdminAddCandidate(sc);
                case 3 -> handleAdminListVoters(sc);
                case 4 -> handleAdminResetElection(sc);
                case 5 -> handleAdminRepairBackup(sc);
                case 0 -> inAdmin = false;
                default -> ConsoleUI.error("Invalid choice.");
            }
        }
    }

    /**
     * Prompts for admin password with up to 3 attempts.
     *
     * @param sc input scanner
     * @return true if authenticated; false otherwise
     */
    private static boolean authenticateAdmin(Scanner sc) {
        for (int attempt = 1; attempt <= 3; attempt++) {
            String inputPwd = ConsoleUI.readLine(sc, "Enter Admin Password: ");
            if (service.verifyPassword(inputPwd)) {
                return true;
            }
            int remaining = 3 - attempt;
            if (remaining > 0) {
                ConsoleUI.warn("Incorrect password. " + remaining + " attempt(s) remaining.");
            } else {
                ConsoleUI.error("Access denied. Too many incorrect attempts.");
            }
        }
        return false;
    }

    /**
     * Admin action to register a new voter.
     */
    private static void handleAdminRegisterVoter(Scanner sc) {
        String vId = ConsoleUI.readLine(sc, "Enter Voter ID (3-12 alphanumeric characters): ");
        String name = ConsoleUI.readLine(sc, "Enter Voter Full Name: ");
        try {
            service.registerVoter(vId, name);
            ConsoleUI.success("Voter registered successfully: " + vId.toUpperCase());
        } catch (VotingException ve) {
            ConsoleUI.error(ve.getMessage());
        }
        ConsoleUI.pause(sc);
    }

    /**
     * Admin action to add a new ballot candidate.
     */
    private static void handleAdminAddCandidate(Scanner sc) {
        String cName = ConsoleUI.readLine(sc, "Enter Candidate Name: ");
        try {
            service.addCandidate(cName);
            ConsoleUI.success("Candidate added successfully: " + cName);
        } catch (VotingException ve) {
            ConsoleUI.error(ve.getMessage());
        }
        ConsoleUI.pause(sc);
    }

    /**
     * Admin action to list registered voters and their voting participation status.
     */
    private static void handleAdminListVoters(Scanner sc) {
        System.out.println("\nRegistered Voters Registry:");
        Map<String, Voter> voters = service.getVoters();
        if (voters.isEmpty()) {
            ConsoleUI.info("No voters registered yet.");
        } else {
            System.out.printf("%-15s %-30s %-10s%n", "Voter ID", "Name", "Voted?");
            System.out.println("-".repeat(58));
            for (Voter v : voters.values()) {
                boolean hasVoted = service.hasVoted(v.getVoterId());
                String votedStatus = hasVoted
                        ? (ConsoleUI.GREEN + "YES" + ConsoleUI.RESET)
                        : (ConsoleUI.YELLOW + "NO" + ConsoleUI.RESET);
                System.out.printf("%-15s %-30s %-10s%n", v.getVoterId(), v.getName(), votedStatus);
            }
        }
        ConsoleUI.pause(sc);
    }

    /**
     * Admin action to reset the blockchain election data.
     */
    private static void handleAdminResetElection(Scanner sc) {
        ConsoleUI.warn("WARNING: This will reset the entire blockchain to Genesis block!");
        ConsoleUI.warn("Registered voters and candidates will remain intact.");
        String conf = ConsoleUI.readLine(sc, "Type 'RESET' in uppercase to confirm: ");
        if ("RESET".equals(conf)) {
            service.resetElection();
            ConsoleUI.success("Election reset successfully. Blockchain re-initialized.");
        } else {
            ConsoleUI.info("Reset cancelled.");
        }
        ConsoleUI.pause(sc);
    }

    /**
     * Admin action to restore the blockchain from the secure backup copy.
     */
    private static void handleAdminRepairBackup(Scanner sc) {
        try {
            storage.restoreBackup();
            ConsoleUI.success("Backup restored successfully from chain.backup.txt!");
            service = new VotingService(storage);
            runStartupIntegrityCheck();
        } catch (Exception e) {
            ConsoleUI.error("Failed to restore backup: " + e.getMessage());
        }
        ConsoleUI.pause(sc);
    }

    /**
     * Returns a shortened 16-character hash representation.
     */
    private static String shortenHash(String hash) {
        if (hash == null) {
            return "null";
        }
        return (hash.length() > 16) ? hash.substring(0, 16) + "..." : hash;
    }

    /**
     * Prints a red boxed warning message to the console.
     *
     * @param text the message lines to format into a box
     */
    private static void printBoxedRedWarning(String text) {
        String[] lines = text.split("\n");
        int maxLen = 0;
        for (String l : lines) {
            if (l.length() > maxLen) {
                maxLen = l.length();
            }
        }
        int width = Math.max(maxLen + 4, 50);
        System.out.println(ConsoleUI.RED + ConsoleUI.BOLD);
        System.out.println("+" + "-".repeat(width) + "+");
        for (String l : lines) {
            int pad = width - l.length() - 2;
            System.out.println("|  " + l + " ".repeat(Math.max(0, pad)) + "|");
        }
        System.out.println("+" + "-".repeat(width) + "+");
        System.out.print(ConsoleUI.RESET);
    }
}
