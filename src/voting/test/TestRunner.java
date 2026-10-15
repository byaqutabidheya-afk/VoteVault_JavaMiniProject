package voting.test;

import voting.util.HashUtil;

/**
 * Lightweight test runner framework for the ChainVote project.
 * Executes phase-by-phase verification tests and reports results.
 */
public class TestRunner {

    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_GREEN = "\u001B[32m";
    private static final String ANSI_RED = "\u001B[31m";

    static int passed = 0;
    static int failed = 0;

    /**
     * Asserts that a condition is true, updates test counters, and prints the result.
     *
     * @param testName name or description of the test case
     * @param condition boolean condition that must be true for the test to pass
     */
    public static void check(String testName, boolean condition) {
        if (condition) {
            passed++;
            System.out.println(ANSI_GREEN + "[PASS] " + testName + ANSI_RESET);
        } else {
            failed++;
            System.out.println(ANSI_RED + "[FAIL] " + testName + ANSI_RESET);
        }
    }

    /**
     * Executes test cases for Phase 1: HashUtil and skeleton verification.
     */
    public static void testPhase1() {
        System.out.println("=== Running Phase 1 Tests ===");

        // a) sha256("hello") equals known hash
        check("Phase 1a: sha256(\"hello\") matches known hash",
                HashUtil.sha256("hello").equals("2cf24dba5fb0a30e26e83b2ac5b9e29e1b161e5c1fa7425e73043362938b9824"));

        // b) result length is 64
        check("Phase 1b: sha256(\"hello\") length is 64",
                HashUtil.sha256("hello").length() == 64);

        // c) sha256("hello") != sha256("Hello")
        check("Phase 1c: sha256(\"hello\") != sha256(\"Hello\")",
                !HashUtil.sha256("hello").equals(HashUtil.sha256("Hello")));

        // d) sha256("") equals known empty string hash
        check("Phase 1d: sha256(\"\") equals known empty string hash",
                HashUtil.sha256("").equals("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"));

        // e) calling sha256 twice on the same input gives the same output
        check("Phase 1e: sha256 is deterministic on repeated calls",
                HashUtil.sha256("hello").equals(HashUtil.sha256("hello")));
    }

    /**
     * Runs all test suites, outputs the final pass/fail summary, and exits with code 1 on failure.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        testPhase1();

        System.out.println("\n" + passed + " passed, " + failed + " failed");
        if (failed > 0) {
            System.exit(1);
        }
    }
}
