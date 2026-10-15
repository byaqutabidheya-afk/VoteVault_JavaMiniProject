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
 * Main application class running the interactive console user interface for ChainVote.
 */
public class Main {

    private static final Storage storage = new Storage("data");
    private static VotingService service;

    /**
     * Entry point for running the ChainVote console application.
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

                System.out.println("\n" + ConsoleUI.BOLD + "=== MAIN MENU ===" + ConsoleUI.RESET);
                System.out.println("  1. Cast Vote");
                System.out.println("  2. View Live Results");
                System.out.println("  3. Explore Blockchain");
                System.out.println("  4. Verify Integrity");
                System.out.println("  5. Admin Panel");
                System.out.println("  0. Exit");

                int choice = ConsoleUI.readInt(scanner, "\nEnter choice [0-5]: ", 0, 5);
                switch (choice) {
                    case 1 -> handleCastVote(scanner);
                    case 2 -> handleViewResults(scanner);
                    case 3 -> handleExploreBlockchain(scanner);
                    case 4 -> handleVerifyIntegrity(scanner);
                    case 5 -> handleAdminPanel(scanner);
                    case 0 -> {
                        running = false;
                        System.out.println("\n" + ConsoleUI.CYAN + "Thank you for using ChainVote. Goodbye!" + ConsoleUI.RESET);
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
     * Displays the election results with graphical ASCII bar charts.
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
                String shortHash = (b.getHash() != null && b.getHash().length() > 16)
                        ? b.getHash().substring(0, 16) + "..." : b.getHash();
                ConsoleUI.success("Block #" + b.getIndex() + " [" + shortHash + "]: valid");
            }
            System.out.println();
            ConsoleUI.success("Blockchain verified: " + chain.size() + " blocks, integrity OK.");
        } else {
            for (int i = 0; i < chain.size(); i++) {
                Block b = chain.getBlocks().get(i);
                String shortHash = (b.getHash() != null && b.getHash().length() > 16)
                        ? b.getHash().substring(0, 16) + "..." : b.getHash();

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
                    + "Blockchain compromised at Block #" + badIndex + " (" + chain.getInvalidReason(badIndex) + ")\n"
                    + "Voting is locked. The chain has been modified externally!");
        }

        ConsoleUI.pause(sc);
    }

    /**
     * Authenticates an administrator and provides election administration actions.
     *
     * @param sc input scanner
     */
    private static void handleAdminPanel(Scanner sc) {
        clearScreen();
        System.out.println(ConsoleUI.BOLD + "--- ADMIN AUTHENTICATION ---" + ConsoleUI.RESET);

        boolean authenticated = false;
        for (int attempt = 1; attempt <= 3; attempt++) {
            String inputPwd = ConsoleUI.readLine(sc, "Enter Admin Password: ");
            if (service.verifyPassword(inputPwd)) {
                authenticated = true;
                break;
            }
            int remaining = 3 - attempt;
            if (remaining > 0) {
                ConsoleUI.warn("Incorrect password. " + remaining + " attempt(s) remaining.");
            } else {
                ConsoleUI.error("Access denied. Too many incorrect attempts.");
            }
        }

        if (!authenticated) {
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
            System.out.println("  0. Back to Main Menu");

            int adminChoice = ConsoleUI.readInt(sc, "\nEnter admin choice [0-4]: ", 0, 4);
            switch (adminChoice) {
                case 1 -> {
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
                case 2 -> {
                    String cName = ConsoleUI.readLine(sc, "Enter Candidate Name: ");
                    try {
                        service.addCandidate(cName);
                        ConsoleUI.success("Candidate added successfully: " + cName);
                    } catch (VotingException ve) {
                        ConsoleUI.error(ve.getMessage());
                    }
                    ConsoleUI.pause(sc);
                }
                case 3 -> {
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
                case 4 -> {
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
                case 0 -> inAdmin = false;
                default -> ConsoleUI.error("Invalid choice.");
            }
        }
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
