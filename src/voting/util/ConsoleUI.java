package voting.util;

import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Utility class providing ANSI color formatting, console banners, robust input readers,
 * bar charts, and loading spinners for the VoteVault user interface.
 */
public final class ConsoleUI {

    public static final String RESET = "\u001B[0m";
    public static final String RED = "\u001B[31m";
    public static final String GREEN = "\u001B[32m";
    public static final String YELLOW = "\u001B[33m";
    public static final String BLUE = "\u001B[34m";
    public static final String CYAN = "\u001B[36m";
    public static final String MAGENTA = "\u001B[35m";
    public static final String BOLD = "\u001B[1m";

    private ConsoleUI() {
    }

    /**
     * Displays the application header banner with ASCII art and subtitle.
     */
    public static void banner() {
        System.out.println(CYAN + BOLD +
                " __     __    _         __     __            _ _   \n" +
                " \\ \\   / /__ | |_ ___   \\ \\   / /_ _ _   _ _| | |_ \n" +
                "  \\ \\ / / _ \\| __/ _ \\   \\ \\ / / _` | | | | | __|\n" +
                "   \\ V / (_) | ||  __/    \\ V / (_| | |_| |_| | |_ \n" +
                "    \\_/ \\___/ \\__\\___|     \\_/ \\__,_|\\__,_|_|_|\\__|\n" +
                RESET);
        System.out.println(CYAN + "           Tamper-Proof Blockchain Voting System\n" + RESET);
    }

    /**
     * Prints a green success message prefixed with a checkmark symbol.
     *
     * @param msg the message to display
     */
    public static void success(String msg) {
        System.out.println(GREEN + "✔ " + msg + RESET);
    }

    /**
     * Prints a red error message prefixed with a cross symbol.
     *
     * @param msg the message to display
     */
    public static void error(String msg) {
        System.out.println(RED + "✘ " + msg + RESET);
    }

    /**
     * Prints a yellow warning message prefixed with a warning symbol.
     *
     * @param msg the message to display
     */
    public static void warn(String msg) {
        System.out.println(YELLOW + "⚠ " + msg + RESET);
    }

    /**
     * Prints an informational message in cyan.
     *
     * @param msg the message to display
     */
    public static void info(String msg) {
        System.out.println(CYAN + msg + RESET);
    }

    /**
     * Prompts the user and reads a line of input.
     * Never returns null and trims leading/trailing whitespace.
     * If input stream terminates (e.g. EOF via Ctrl+D or Ctrl+Z), exits cleanly with code 0.
     *
     * @param sc     scanner instance reading user input
     * @param prompt prompt message to display
     * @return trimmed non-null input string
     */
    public static String readLine(Scanner sc, String prompt) {
        System.out.print(prompt);
        System.out.flush();
        if (!sc.hasNextLine()) {
            System.out.println("\n" + CYAN + "Exiting VoteVault. Goodbye!" + RESET);
            System.exit(0);
        }
        String line = sc.nextLine();
        return (line != null) ? line.trim() : "";
    }

    /**
     * Reads an integer within the specified inclusive range [min, max].
     * Loops continuously on invalid input without throwing exceptions.
     *
     * @param sc     scanner instance
     * @param prompt prompt message
     * @param min    minimum allowable value (inclusive)
     * @param max    maximum allowable value (inclusive)
     * @return valid integer in the range [min, max]
     */
    public static int readInt(Scanner sc, String prompt, int min, int max) {
        while (true) {
            String input = readLine(sc, prompt);
            try {
                int val = Integer.parseInt(input);
                if (val >= min && val <= max) {
                    return val;
                }
                error("Input must be between " + min + " and " + max + ".");
            } catch (NumberFormatException e) {
                error("Invalid number. Please enter a valid integer.");
            }
        }
    }

    /**
     * Prints an ASCII bar chart representation of the candidate voting results.
     * Displays candidate names padded to 15 characters, graphical bars up to 40 characters,
     * vote counts, and percentages. Highlights the current leader in green.
     *
     * @param data  map of candidate names to vote counts
     * @param total total non-genesis votes cast
     */
    public static void barChart(Map<String, Integer> data, int total) {
        int maxVotes = 0;
        for (int count : data.values()) {
            if (count > maxVotes) {
                maxVotes = count;
            }
        }

        for (Map.Entry<String, Integer> entry : data.entrySet()) {
            String name = entry.getKey();
            int count = entry.getValue();
            double pct = (total > 0) ? ((double) count / total) * 100.0 : 0.0;
            int barLen = (total > 0) ? (int) Math.round(((double) count / total) * 40.0) : 0;
            String bar = "█".repeat(barLen);
            boolean isLeader = (count > 0 && count == maxVotes);
            String color = isLeader ? GREEN : RESET;
            String formattedName = String.format("%-15s", name.length() > 15 ? name.substring(0, 15) : name);
            System.out.printf("%s%s | %-40s (%d votes, %5.1f%%)%s%n",
                    color, formattedName, bar, count, pct, RESET);
        }
    }

    /**
     * Pauses console execution until the user presses Enter.
     * Exits cleanly on EOF.
     *
     * @param sc scanner reading console input
     */
    public static void pause(Scanner sc) {
        System.out.print("\nPress Enter to continue...");
        System.out.flush();
        if (!sc.hasNextLine()) {
            System.out.println("\n" + CYAN + "Exiting VoteVault. Goodbye!" + RESET);
            System.exit(0);
        }
        sc.nextLine();
    }

    /**
     * Displays an animated console spinner while executing the given task on a background thread.
     *
     * @param msg  message describing the operation being performed
     * @param task the runnable task to execute
     */
    public static void spinner(String msg, Runnable task) {
        char[] frames = new char[]{'|', '/', '-', '\\'};
        AtomicBoolean running = new AtomicBoolean(true);
        Thread spinnerThread = new Thread(() -> {
            int i = 0;
            while (running.get()) {
                System.out.print("\r" + msg + " " + frames[i % frames.length]);
                System.out.flush();
                i++;
                try {
                    Thread.sleep(80);
                } catch (InterruptedException e) {
                    break;
                }
            }
        });
        spinnerThread.start();
        try {
            task.run();
        } finally {
            running.set(false);
            try {
                spinnerThread.join();
            } catch (InterruptedException ignored) {
            }
            System.out.print("\r" + msg + " " + GREEN + "done" + RESET + "\n");
            System.out.flush();
        }
    }
}
