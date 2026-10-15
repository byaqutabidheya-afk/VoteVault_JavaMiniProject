package voting.test;

import voting.core.Block;
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
     * Executes test cases for Phase 2: Block creation, hashing, and proof-of-work mining.
     */
    public static void testPhase2() {
        System.out.println("\n=== Running Phase 2 Tests ===");

        // a) new block's hash equals its calculateHash()
        Block b1 = new Block(1, 1000L, "voterHash123", "Alice", "0000abc");
        check("Phase 2a: new block's hash equals its calculateHash()",
                b1.getHash().equals(b1.calculateHash()));

        // b) after mineBlock(4), hash starts with "0000"
        long d4Start = System.nanoTime();
        b1.mineBlock(4);
        long d4DurationMs = (System.nanoTime() - d4Start) / 1_000_000;
        check("Phase 2b: after mineBlock(4), hash starts with \"0000\" (mined in " + d4DurationMs + " ms)",
                b1.getHash().startsWith("0000"));

        // c) after mining, hash still equals calculateHash()
        check("Phase 2c: after mining, hash still equals calculateHash()",
                b1.getHash().equals(b1.calculateHash()));

        // d) mining with difficulty 3 completes in under 5 seconds (measure with System.nanoTime)
        Block b2 = new Block(2, 2000L, "voterHash456", "Bob", b1.getHash());
        long start = System.nanoTime();
        b2.mineBlock(3);
        long elapsedNanos = System.nanoTime() - start;
        check("Phase 2d: mining with difficulty 3 completes in under 5 seconds",
                elapsedNanos < 5_000_000_000L);

        // e) Block.genesis(4) has index 0, previousHash "0", and a hash starting with "0000"
        Block genesis = Block.genesis(4);
        check("Phase 2e: Block.genesis(4) has index 0, previousHash \"0\", and a hash starting with \"0000\"",
                genesis.getIndex() == 0 && "0".equals(genesis.getPreviousHash()) && genesis.getHash().startsWith("0000"));

        // f) two blocks with different candidates have different hashes
        Block bCandidateA = new Block(3, 3000L, "voterHash789", "Alice", "prevHash");
        Block bCandidateB = new Block(3, 3000L, "voterHash789", "Bob", "prevHash");
        check("Phase 2f: two blocks with different candidates have different hashes",
                !bCandidateA.getHash().equals(bCandidateB.getHash()));

        // g) the full-field constructor keeps the exact hash it was given, even a fake one
        Block fullBlock = new Block(4, 4000L, "voterHashX", "Charlie", "prevHashY", 1234, "fake-hash-value");
        check("Phase 2g: the full-field constructor keeps the exact hash it was given, even a fake one",
                "fake-hash-value".equals(fullBlock.getHash()) && fullBlock.getNonce() == 1234);
    }

    /**
     * Runs all test suites, outputs the final pass/fail summary, and exits with code 1 on failure.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        testPhase1();
        testPhase2();

        System.out.println("\n" + passed + " passed, " + failed + " failed");
        if (failed > 0) {
            System.exit(1);
        }
    }
}
