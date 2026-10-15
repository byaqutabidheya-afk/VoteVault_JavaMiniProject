# Vote Vault: Blockchain-Based Voting System
### Core Java Console Mini Project: Phase-by-Phase Vibe-Coding Build Guide

**Target time:** 12 hours  |  **Language:** Core Java 17+ (no external libraries, no Maven/Gradle)  |  **Interface:** Console (ANSI colors)

---

## How to Use This Guide

1. Do the phases **in order**. Don't start a phase until every test in the previous phase passes.
2. At the start of every new AI session, paste the **Master Context Prompt** (below) first, then the phase prompt.
3. After the AI writes code, **compile and run the phase's tests yourself**. If a test fails, paste the error and the failing test back to the AI with: *"This test failed. Fix only what is needed, don't rewrite working code."*
4. Commit or copy-backup your `src/` folder after each phase passes, so you can always roll back.

### Time Budget

| Phase | Topic | Time |
|---|---|---|
| 0 | Environment setup | 0:30 |
| 1 | Project skeleton + hashing | 1:00 |
| 2 | Block + proof-of-work mining | 1:00 |
| 3 | Blockchain + integrity validation | 1:30 |
| 4 | File persistence (human-editable) | 1:30 |
| 5 | Voters, candidates, voting rules | 1:30 |
| 6 | Console UI, menus, results chart | 2:00 |
| 7 | Tamper detection demo + polish | 1:00 |
| 8 | Final testing, README, demo rehearsal | 2:00 |
| | **Total** | **12:00** |

---

## Master Context Prompt (paste at the start of EVERY AI session)

```
You are helping me build a console-based Core Java mini project called "ChainVote",
a blockchain-based voting system. Follow these rules strictly:

TECH RULES
- Pure Core Java 17+. NO external libraries, NO Maven/Gradle, NO JUnit, NO frameworks.
- Compile with plain javac. Main class: voting.Main
- Use java.security.MessageDigest for SHA-256.
- Data is stored in human-readable pipe-delimited text files in a "data/" folder
  (NOT Java binary serialization), so a human can open and edit them.

PACKAGE STRUCTURE (create files only in these locations)
src/voting/Main.java                  -> entry point + menu loop
src/voting/util/HashUtil.java         -> SHA-256 helpers
src/voting/util/ConsoleUI.java        -> ANSI colors, banner, input helpers, bar charts
src/voting/core/Block.java            -> one block (one vote)
src/voting/core/Blockchain.java       -> chain list, mining, validation
src/voting/model/Voter.java           -> voter record
src/voting/service/Storage.java       -> load/save chain, voters, candidates
src/voting/service/VotingService.java -> business rules (one person one vote, etc.)
src/voting/service/CorruptDataException.java -> checked exception for unreadable files
src/voting/service/VotingException.java      -> unchecked exception for rule violations
src/voting/test/TestRunner.java       -> my own tiny test framework + all tests

CODE STYLE
- Clean OOP, private fields with getters, meaningful names, short methods.
- Javadoc comment on every class and public method (my teachers will read the code).
- Handle all exceptions gracefully; the program must NEVER crash on bad user input.
- Do not rewrite or rename existing working code unless I ask. Only add/modify what
  the current phase needs.
- After writing code, tell me the exact compile and run commands.

TEST RULES
- Each lettered test item (a, b, c...) in a phase is exactly ONE check(...) call,
  even if it verifies several things (combine them with &&). This keeps the
  pass counts I expect correct.
- Tests that use files must use their own temp folder and delete it afterwards.

I will now give you one phase at a time.
```

### Compile & Run Commands (used in every phase)

**Linux / macOS / Git Bash:**
```bash
javac -d out $(find src -name "*.java")
java -cp out voting.Main          # run the app
java -cp out voting.test.TestRunner  # run the tests
```

**Windows (PowerShell, the default in Windows Terminal and VS Code):**
```powershell
javac -d out (Get-ChildItem -Recurse -Filter *.java src).FullName
java -cp out voting.Main
java -cp out voting.test.TestRunner
```

**Windows (Command Prompt):**
```bat
dir /s /b src\*.java > sources.txt
javac -d out @sources.txt
java -cp out voting.Main
java -cp out voting.test.TestRunner
```
> ⚠️ Keep the project in a folder path **without spaces** (e.g. `C:\Projects\ChainVote`). The `@sources.txt` method breaks if the path contains spaces.

---

## Phase 0: Environment Setup (no AI needed)

### Prerequisites
- [ ] JDK 17 or newer installed → `java -version` and `javac -version` both print 17+
- [ ] An editor: VS Code (with "Extension Pack for Java") or IntelliJ IDEA Community
- [ ] A terminal that shows ANSI colors: **Windows Terminal**, the VS Code terminal, or any Linux/macOS terminal. (Old `cmd.exe` may show garbage like `[32m`; use Windows Terminal instead.)
- [ ] Your AI coding agent ready (Claude, Cursor, Copilot, etc.)

### Steps
1. Create a folder `ChainVote/`.
2. Inside it create `src/` and `data/` folders.
3. Open the folder in your editor.

### Test Cases
| # | Test | Expected |
|---|---|---|
| 0.1 | `java -version` | Prints version 17 or higher |
| 0.2 | `javac -version` | Prints version 17 or higher |
| 0.3 | Linux/macOS/Git Bash: run `echo -e "\e[32mGREEN\e[0m"`. Windows: just use Windows Terminal or the VS Code terminal (both support colors) | The word shows in green |

✅ **Done when:** all three pass.

---

## Phase 1: Project Skeleton + Hashing Utility

### Prerequisites
- [ ] Phase 0 complete
- [ ] You understand: a **hash** is a fixed-length fingerprint of data; change one character and the hash changes completely.

### AI Prompt
```
PHASE 1: Project skeleton + HashUtil + test framework.

1. Create src/voting/util/HashUtil.java with:
   - public static String sha256(String input): returns the lowercase 64-char hex
     SHA-256 hash using MessageDigest and StandardCharsets.UTF_8. Wrap
     NoSuchAlgorithmException in a RuntimeException.

2. Create src/voting/Main.java with a main method that just prints
   "ChainVote starting..." for now.

3. Create src/voting/test/TestRunner.java, a tiny self-made test framework:
   - static int passed, failed counters
   - static void check(String testName, boolean condition): prints
     "[PASS] name" in green or "[FAIL] name" in red using ANSI codes, updates counters
   - main() calls a method per phase (start with testPhase1()), then prints a summary
     "X passed, Y failed" and exits with System.exit(1) if any failed.
   - testPhase1() must check:
     a) sha256("hello") equals
        "2cf24dba5fb0a30e26e83b2ac5b9e29e1b161e5c1fa7425e73043362938b9824"
     b) result length is 64
     c) sha256("hello") != sha256("Hello")
     d) sha256("") equals
        "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"
     e) calling sha256 twice on the same input gives the same output

Give me the compile and run commands.
```

### Test Cases
| # | Test | Expected |
|---|---|---|
| 1.1 | Compile | No errors |
| 1.2 | `java -cp out voting.Main` | Prints `ChainVote starting...` |
| 1.3 | `java -cp out voting.test.TestRunner` | 5 `[PASS]` lines, `5 passed, 0 failed` |

✅ **Done when:** 1.1–1.3 pass. **Back up `src/`.**

---

## Phase 2: Block + Proof-of-Work Mining

### Prerequisites
- [ ] Phase 1 tests all pass
- [ ] You understand: each block stores the **previous block's hash**, which is what links the chain. **Mining** means trying nonce values until the hash starts with N zeros (the "difficulty").

### AI Prompt
```
PHASE 2: Block class with proof-of-work.

Create src/voting/core/Block.java:
- Fields (private): int index, long timestamp, String voterHash, String candidate,
  String previousHash, int nonce, String hash
- Constructor Block(int index, long timestamp, String voterHash, String candidate,
  String previousHash): sets fields, nonce = 0, hash = calculateHash()
- A second constructor that takes ALL fields including nonce and hash (needed later
  for loading from file). It must NOT recalculate the hash; store it exactly as given.
- public String calculateHash(): sha256 of
  index + timestamp + voterHash + candidate + previousHash + nonce (concatenated strings)
- public void mineBlock(int difficulty): increment nonce and recompute hash until the
  hash starts with "difficulty" zeros (build the target with "0".repeat(difficulty)).
- Getters for all fields.
- toString() that prints a readable multi-line summary (index, time, candidate,
  short hashes = first 16 chars + "...").
- A static factory Block.genesis(int difficulty): index 0, voterHash "GENESIS",
  candidate "GENESIS", previousHash "0", timestamp 0, then mined at the given difficulty.

Add testPhase2() to TestRunner (call it from main) checking:
 a) new block's hash equals its calculateHash()
 b) after mineBlock(4), hash starts with "0000"
 c) after mining, hash still equals calculateHash()
 d) mining with difficulty 3 completes in under 5 seconds (measure with System.nanoTime)
 e) Block.genesis(4) has index 0, previousHash "0", and a hash starting with "0000"
 f) two blocks with different candidates have different hashes
 g) the full-field constructor keeps the exact hash it was given, even a fake one
```

### Test Cases
| # | Test | Expected |
|---|---|---|
| 2.1 | Compile | No errors |
| 2.2 | TestRunner | Phase 1 + Phase 2 all `[PASS]` (12 total) |
| 2.3 | Mining time | Difficulty 4 mines in well under 2 seconds on a normal laptop |

⚠️ **If mining takes forever:** the AI probably forgot to recompute the hash inside the loop. Tell it so.

✅ **Done when:** all pass. **Back up `src/`.**

---

## Phase 3: Blockchain + Integrity Validation

### Prerequisites
- [ ] Phase 2 tests all pass
- [ ] You understand the four validation rules: (1) each block's stored hash must equal its recalculated hash, (2) each block's `previousHash` must equal the hash of the block before it, (3) each hash must meet the difficulty, (4) each block's index must match its position, so a deleted block is caught.

### AI Prompt
```
PHASE 3: Blockchain class with validation.

Create src/voting/core/Blockchain.java:
- public static final int DIFFICULTY = 4
- private final List<Block> chain (ArrayList)
- Constructor Blockchain(): starts with Block.genesis(DIFFICULTY)
- Constructor Blockchain(List<Block> loadedBlocks): uses the given list as-is
  (for loading from file; do NOT re-mine or modify)
- public Block getLatestBlock()
- public Block addVote(String voterHash, String candidate): create a block with
  index = size, timestamp = System.currentTimeMillis(), previousHash = latest hash,
  mine it at DIFFICULTY, add it, return it
- public int findFirstInvalidBlock(): returns -1 if valid, otherwise the index of the
  FIRST block that breaks any rule:
    rule 1: stored hash != calculateHash()
    rule 2: block.previousHash != previous block's hash (skip for index 0)
    rule 3: hash does not start with DIFFICULTY zeros
    rule 4: block.getIndex() != its position in the list
  Check the rules in that order for each block. Also check the genesis block
  (position 0) for rules 1, 3 and 4. Also return 0 if the list is empty.
- public String getInvalidReason(int position): returns a human-readable reason for
  the block at that position, e.g. "Block data was modified (hash mismatch)" or
  "Broken link to previous block" or "Proof-of-work not satisfied" or
  "Block missing or out of order"
- public boolean isValid(): findFirstInvalidBlock() == -1
- public List<Block> getBlocks(): returns an unmodifiable view
- public int size()

Add testPhase3() to TestRunner checking:
 a) new chain has size 1 and is valid
 b) after adding 3 votes, size is 4 and chain is valid
 c) each block's previousHash equals the prior block's hash
 d) TAMPER TEST: build a valid 4-block chain, then create a copy list where block 2 is
    replaced by a new Block using the full-field constructor with the same fields
    EXCEPT candidate changed and the OLD hash kept. Load into new Blockchain(list).
    findFirstInvalidBlock() must return 2, and isValid() false.
 e) RE-MINE ATTACK TEST: same as (d), but the attacker then calls
    mineBlock(Blockchain.DIFFICULTY) on the fake block 2 so its hash and proof-of-work
    are valid. findFirstInvalidBlock() must return 3 (block 3's link breaks).
 f) getInvalidReason returns a non-empty string for a tampered block
 g) getBlocks() is unmodifiable (adding to it throws UnsupportedOperationException)
```

### Test Cases
| # | Test | Expected |
|---|---|---|
| 3.1 | Compile | No errors |
| 3.2 | TestRunner | Phases 1–3 all pass (19 total) |
| 3.3 | Tests (d) and (e) | Tamper detected at the **correct** block index. This is your demo's core! |

✅ **Done when:** all pass. **Back up `src/`.**

---

## Phase 4: File Persistence (Human-Editable)

### Prerequisites
- [ ] Phase 3 tests all pass
- [ ] Understand why we use **text files** instead of Java serialization: so you can open `chain.txt` in Notepad during the demo, edit a vote, and show the system catching it.

### AI Prompt
```
PHASE 4: Storage service with plain-text, pipe-delimited files.

Create src/voting/model/Voter.java:
- Fields: String voterId, String name. Constructor, getters, toString.

Create src/voting/service/Storage.java:
- Constructor Storage(String dataDir): creates the directory if missing.
  Files: dataDir/chain.txt, dataDir/voters.txt, dataDir/candidates.txt
- File formats (one record per line, "|" delimiter):
    chain.txt:      index|timestamp|voterHash|candidate|previousHash|nonce|hash
    voters.txt:     voterId|name
    candidates.txt: candidateName
- public void saveChain(Blockchain bc)
- public Blockchain loadChain(): if chain.txt missing or empty, return new Blockchain()
  (fresh genesis) AND save it. Otherwise parse each line with the full-field Block
  constructor (never recalculate hashes on load!). Malformed lines (wrong field count,
  bad numbers) must throw a custom checked exception CorruptDataException(lineNumber,
  message). Create that class in the same package.
- public void saveVoters(Map<String, Voter> voters) / Map<String, Voter> loadVoters()
  (use LinkedHashMap to keep order; missing file => empty map)
- public void saveCandidates(List<String>) / List<String> loadCandidates()
  (missing file => empty list)
- Skip blank lines when loading. Use try-with-resources and UTF-8.
- Names/candidates containing "|" must be rejected elsewhere; add a static helper
  Storage.isSafeText(String s) returning false if s is null, blank, or contains "|".

Add testPhase4() to TestRunner. Use a temp folder "data_test" and delete it at the end:
 a) save a 3-vote chain, load it back: same size, isValid() true, hashes identical
 b) voters round-trip: save 2 voters, load, same IDs and names
 c) candidates round-trip
 d) loading when no files exist returns valid fresh chain of size 1 and empty maps/lists
 e) FILE TAMPER TEST: save a valid chain, then programmatically read chain.txt, replace
    the candidate in line 3 (block index 2) with another name, write it back, load:
    isValid() false and findFirstInvalidBlock() == 2
 f) a line with only 3 fields causes CorruptDataException with the right line number
 g) isSafeText("Alice") true, isSafeText("Al|ice") false, isSafeText("  ") false
```

### Test Cases
| # | Test | Expected |
|---|---|---|
| 4.1 | Compile | No errors |
| 4.2 | TestRunner | Phases 1–4 all pass (26 total) |
| 4.3 | Manual | Temporarily comment out the cleanup line, run tests, open `data_test/chain.txt` and confirm lines are readable text. Then restore the cleanup line |
| 4.4 | Cleanup | `data_test/` folder no longer exists after tests finish |

✅ **Done when:** all pass. **Back up `src/`.**

---

## Phase 5: Voters, Candidates & Voting Rules

### Prerequisites
- [ ] Phase 4 tests all pass
- [ ] Understand **privacy**: the chain stores `sha256(voterId)`, not the raw ID, so nobody can read who voted from the file, but the system can still block double voting.
- [ ] Understand **single source of truth**: "has this voter voted?" is answered by scanning the chain, not by a separate flag that could be edited.

### AI Prompt
```
PHASE 5: VotingService with all business rules.

Create src/voting/service/VotingService.java:
- Constructor VotingService(Storage storage): loads chain, voters, candidates.
  If loading throws CorruptDataException, store the error message, mark the system
  as "corrupted" (don't crash), and use an in-memory new Blockchain() so getters
  never return null. While corrupted, NEVER save the chain (the bad file must stay
  on disk as evidence).
- Custom unchecked exception VotingException(String message) in the same package.
- Voter IDs are always converted to UPPERCASE before storing, hashing or comparing.
- public void registerVoter(String voterId, String name):
    voterId must match regex "[A-Za-z0-9]{3,12}", name must pass Storage.isSafeText,
    duplicate voterId not allowed (case-insensitive: store IDs uppercase).
    Save voters after success. Throw VotingException with a clear message otherwise.
- public void addCandidate(String name): isSafeText, no duplicates (case-insensitive),
    cannot add candidates once any vote exists (chain size > 1). Save after.
- public boolean hasVoted(String voterId): true if any block's voterHash equals
  HashUtil.sha256(voterId uppercase)
- public Block castVote(String voterId, String candidate):
    reject if system is corrupted or chain invalid ("Voting locked: chain tampered"),
    reject unregistered voter, reject if hasVoted, reject unknown candidate,
    reject if no candidates exist.
    Otherwise chain.addVote(sha256(UPPERCASE ID), candidate), saveChain, return block.
- public Map<String, Integer> getResults(): every candidate (even with 0 votes) mapped
  to their vote count, counting only non-genesis blocks. LinkedHashMap sorted by votes
  descending; ties keep the order in which candidates were added.
- public int getTotalVotes(), getTotalVoters(), double getTurnoutPercent()
  (turnout = votes / registered voters * 100; return 0 if there are no voters)
- public boolean verifyPassword(String input): compares sha256(input) to a constant
  ADMIN_PASSWORD_HASH (hash of "admin123"). Never store the plain password.
- public void resetElection(): new Blockchain(), save it, clear the corrupted flag
  (voters and candidates stay)
- Getters: getBlockchain(), getCandidates(), getVoters(), isCorrupted(), getLoadError()

Add testPhase5() to TestRunner (temp folder "data_test5", deleted afterwards):
 a) register voter OK; registering same ID again (different case) throws
 b) invalid IDs ("ab", "has space", "a|b") throw
 c) add 3 candidates; duplicate candidate throws
 d) cast vote succeeds, chain size becomes 2, hasVoted true
 e) same voter voting again throws
 f) unregistered voter throws; unknown candidate throws
 g) adding a candidate after a vote exists throws
 h) results: cast 3 votes (2 for A, 1 for B) -> A=2, B=1, C=0, A listed first
 i) chain file does NOT contain any raw voter ID (read file text, check)
 j) verifyPassword("admin123") true, verifyPassword("wrong") false
 k) after tampering chain.txt and creating a new VotingService, castVote throws with
    a message containing "tamper"
 l) resetElection: chain size 1, voters still present
```

### Test Cases
| # | Test | Expected |
|---|---|---|
| 5.1 | Compile | No errors |
| 5.2 | TestRunner | Phases 1–5 all pass (38 total) |
| 5.3 | Test (i) | Raw voter IDs never appear in `chain.txt` (privacy proven) |

✅ **Done when:** all pass. **Back up `src/`.**

---

## Phase 6: Console UI, Menus & Results Chart

### Prerequisites
- [ ] Phase 5 tests all pass
- [ ] Your terminal shows ANSI colors (Phase 0, test 0.3)

### AI Prompt
```
PHASE 6: Full console user interface.

Create src/voting/util/ConsoleUI.java:
- ANSI constants: RESET, RED, GREEN, YELLOW, BLUE, CYAN, MAGENTA, BOLD
- static void banner(): a big ASCII-art "CHAINVOTE" title in cyan, plus the subtitle
  "Tamper-Proof Blockchain Voting System"
- static void success(msg) green "✔ msg", error(msg) red "✘ msg",
  warn(msg) yellow "⚠ msg", info(msg) cyan
- static String readLine(Scanner sc, String prompt): never returns null, trims input.
  If input has ended (sc.hasNextLine() is false, e.g. Ctrl+D / Ctrl+Z), print a
  goodbye message and exit cleanly with System.exit(0) instead of looping or throwing.
- static int readInt(Scanner sc, String prompt, int min, int max): loops until a valid
  number in range, showing an error for bad input (never throws)
- static void barChart(Map<String,Integer> data, int total): for each entry print
  name (padded to 15), a bar of "█" scaled to max 40 chars, count and percent.
  Leader in green. Handle total == 0 without dividing by zero.
- static void pause(Scanner sc): "Press Enter to continue..."
- static void spinner(String msg, Runnable task): runs the task while showing a
  simple animated "Mining block... |/-\" on one line, then "done" (use a thread).

Rewrite src/voting/Main.java:
- Show banner. Create Storage("data") and keep ONE VotingService in a field `service`.
- On startup run integrity check:
    if corrupted -> big red boxed warning showing getLoadError() (it includes the line
                    number) and say voting is locked
    if invalid   -> big red boxed warning naming the first bad block and its reason,
                    and say voting is locked
    otherwise    -> green "Blockchain verified: N blocks, integrity OK"
- Main menu loop (readInt):
   1. Cast Vote          -> ask voter ID, show numbered candidates, pick one,
                            mine with spinner, print block number + short hash
   2. View Live Results  -> barChart + total votes + turnout %
   3. Explore Blockchain -> print each block (toString) with a "  ↓ linked" arrow
                            between blocks; paginate 5 blocks at a time
   4. Verify Integrity   -> re-load from disk by REPLACING `service` with a new
                            VotingService (so later actions see the file as it is
                            now), run check, show ✔ for blocks before the first bad
                            one, ✘ on the bad one, "untrusted" after it, then the
                            final verdict
   5. Admin Panel        -> password prompt (3 attempts), then submenu:
                            1 Register Voter, 2 Add Candidate, 3 List Voters
                            (show voted yes/no), 4 Reset Election (confirm with
                            typing "RESET"), 0 Back
   0. Exit               -> goodbye message
- Catch VotingException everywhere and show it with ConsoleUI.error. The program
  must never crash; wrap the loop body in try/catch(Exception) as a last resort.
- Clear screen between menus using "\033[H\033[2J" + flush.

Do not change VotingService behaviour. Existing tests must still pass.
```

### Test Cases (manual: run `java -cp out voting.Main`, delete `data/` first for a clean start)
| # | Action | Expected |
|---|---|---|
| 6.1 | Start app first time | Banner shows; "Blockchain verified: 1 blocks, integrity OK" |
| 6.2 | Type `abc` at menu | Red error, menu reappears (no crash) |
| 6.3 | Type `99` at menu | Out-of-range error, no crash |
| 6.4 | Admin with wrong password 3 times | Access denied, back to menu |
| 6.5 | Admin → add candidates Alice, Bob, Carol | Green success each time |
| 6.6 | Admin → register voters V001, V002, V003 | Success |
| 6.7 | Cast vote as V001 for Alice | Spinner, then block #1 and hash shown |
| 6.8 | Cast vote as V001 again | Red "already voted" |
| 6.9 | Cast vote as XYZ999 | Red "not registered" |
| 6.10 | V002 → Alice, V003 → Bob; View Results | Alice 2 (green, longest bar), Bob 1, Carol 0, turnout 100% |
| 6.11 | Explore Blockchain | 4 blocks with link arrows |
| 6.12 | Press empty Enter at prompts | Error or re-prompt, no crash |
| 6.13a | Press Ctrl+D (Linux/macOS) or Ctrl+Z then Enter (Windows) | Clean goodbye and exit, no stack trace |
| 6.13 | Restart app | Votes still there, integrity OK |
| 6.14 | TestRunner | Still 38 passed |

✅ **Done when:** all pass. **Back up `src/`.**

---

## Phase 7: Tamper-Detection Demo + Polish

### Prerequisites
- [ ] Phase 6 tests all pass
- [ ] A working `data/` with at least 3 votes

### AI Prompt
```
PHASE 7: Make the tamper-detection demo dramatic and add polish.

1. In Verify Integrity (menu 4), when tampering is found, show:
   - a red ASCII "TAMPERING DETECTED" box
   - which block and the reason
   - side-by-side evidence (first 20 chars each) so the difference is visible:
       hash mismatch  -> STORED hash vs RECALCULATED hash
       broken link    -> this block's previousHash vs the previous block's real hash
       other reasons  -> just the reason text
   - an explanation line: "Changing one vote breaks this block's fingerprint and
     every link after it. Re-mining all following blocks would be required."
2. Add main menu option 6 "Hacker Simulation" (admin password required). Update the
   main menu readInt range to 0-6.
   - asks which block number to attack and which candidate to change it to
     (must be a different candidate from the one already in that block)
   - calls storage.tamperBlock(index, newCandidate), which rewrites ONLY that line in
     data/chain.txt and keeps the old hash, to simulate a hacker editing the file.
     Print in magenta "Simulating attacker editing chain.txt..."
   - then immediately runs Verify Integrity (which replaces `service`, so voting is
     now locked) so the audience sees it caught live
   - tamperBlock must reject block 0 and out-of-range indexes (throw
     IllegalArgumentException; Main shows it as an error)
3. Backup and repair:
   - Storage.saveChain(bc) also writes data/chain.backup.txt, but ONLY when
     bc.isValid() is true.
   - Add Storage.restoreBackup(): copies chain.backup.txt over chain.txt; throws a
     clear error if no backup exists.
   - Add Admin option 5 "Repair from backup": calls restoreBackup(), then replaces
     `service` with a new VotingService and shows the integrity result.
4. Add a stats line under results: total blocks, difficulty, average mining time per
   block (track in VotingService during this session; show "N/A" if no blocks were
   mined this session).
5. Review all classes: add missing Javadoc, remove unused imports, make sure no
   method is longer than ~40 lines.

Add testPhase7() to TestRunner (temp folder "data_test7", deleted afterwards):
 a) chain.backup.txt exists after casting a vote
 b) after 3 votes, tamperBlock(2, <a different candidate>) then loadChain():
    findFirstInvalidBlock() == 2
 c) restoreBackup() then loadChain() gives a valid chain with the same size as
    before tampering
 d) tamperBlock(0, ...) throws IllegalArgumentException
```

### Test Cases
| # | Action | Expected |
|---|---|---|
| 7.1 | TestRunner | All phases pass (42 total) |
| 7.2 | Menu 6 → attack block 2 → change to Carol | Red TAMPERING DETECTED at block 2, hashes differ visibly |
| 7.3 | Try to cast a vote after tampering | "Voting locked: chain tampered" |
| 7.4 | Manually edit `data/chain.txt` in Notepad (change a candidate name), restart app | Red warning on startup naming the right block |
| 7.5 | Admin → Repair from backup | Integrity OK, voting unlocked |
| 7.6 | Delete one **middle** line (not the last) from `chain.txt`, restart | Detected at that position, no crash |
| 7.7 | Put garbage text in `chain.txt`, restart | "Corrupted data at line N", no crash |

✅ **Done when:** all pass. **Back up `src/`.**

---

## Phase 8: Final Testing, README & Demo Rehearsal

### Prerequisites
- [ ] Phase 7 complete
- [ ] All 42 automated tests passing

### AI Prompt
```
PHASE 8: Final review and documentation.

1. Review the entire project for bugs, unhandled exceptions, and inconsistent
   messages. List any issues first, then fix them without changing behaviour that
   tests depend on.
2. Write README.md containing: project title, one-paragraph description, features
   list, Java concepts used (OOP, encapsulation, collections, file I/O, exceptions,
   custom exceptions, multithreading for spinner, hashing/cryptography, static
   factory), folder structure, compile/run commands for Windows and Linux, a "How
   blockchain prevents tampering" section in simple words, and future scope
   (GUI, network nodes, digital signatures).
3. Write a 2-minute DEMO_SCRIPT.md: exact menu steps to show registration, voting,
   double-vote rejection, results, blockchain explorer, hacker simulation, detection,
   repair.
4. Write VIVA_QA.md with 15 likely viva questions and short answers (e.g. why
   SHA-256, what is a nonce, what is proof-of-work, why store hashed voter IDs, how
   is double voting prevented, what happens if the attacker re-mines everything,
   why deleting the LAST block can't be caught by a single local copy and how
   real blockchains solve it with many nodes, why text files instead of a database).
```

### Final Acceptance Test (do this as a full run-through)
| # | Check | Expected |
|---|---|---|
| 8.1 | Delete `out/` and `data/`, recompile from scratch | Compiles with zero errors and zero warnings |
| 8.2 | TestRunner | `42 passed, 0 failed` |
| 8.3 | Run full `DEMO_SCRIPT.md` start to finish | Every step works first try |
| 8.4 | Give the keyboard to a friend to break it with random input | No crash, no stack trace ever printed |
| 8.5 | Copy the project folder to another PC/lab machine and run | Works with only a JDK installed |
| 8.6 | Explain each class in one sentence without looking | You can (teachers will ask!) |

---

## 🎤 The Wow Demo (memorize this flow)

1. **Start app**: banner + "integrity OK".
2. **Admin**: add 3 candidates, register 4 voters.
3. **Vote** as 3 voters: point out the mining spinner ("the computer is solving a puzzle to seal this vote").
4. **Try double voting**: rejected.
5. **Live Results**: bar chart.
6. **Explore Blockchain**: show the hash links ("each block holds the previous block's fingerprint").
7. **Open `chain.txt` in Notepad**: show voter IDs are hashed (privacy).
8. **Hacker Simulation** (admin password): change one vote → **TAMPERING DETECTED** in red, with the exact block and mismatched hashes.
9. **Try voting** with the 4th voter: locked.
10. **Repair from backup**: integrity restored.

**Closing line:** *"Even if someone edits the stored data directly, the cryptographic chain exposes it instantly. That's the same principle real blockchains use."*

---

## 🛠 Troubleshooting

| Problem | Fix |
|---|---|
| `[32m` garbage instead of colors | Use Windows Terminal or the VS Code terminal |
| `Could not find or load main class voting.Main` | Run from the project root; use `-cp out`; check `package voting;` at top of Main |
| `package voting.util does not exist` | You compiled only one file; compile all with the commands above |
| Mining never finishes | Hash isn't recalculated inside the mining loop |
| Tamper not detected after loading | The load code is recalculating hashes; it must keep the stored hash |
| Unicode `█ ✔ ✘` show as `?` | Run `chcp 65001` first, then run with `java -Dstdout.encoding=UTF-8 -cp out voting.Main` (JDK 19+). Last resort: ask the AI to swap them for `#`, `[OK]`, `[X]` |
| Vote cast after tampering silently "fixed" the file | Main is still using the old in-memory service. Verify Integrity and Repair must replace `service` with a freshly loaded one |
| AI rewrote working code and broke tests | Restore your phase backup, re-prompt: "Only modify X, don't touch other files" |
| Tests pass but app crashes on input | Wrap the menu loop body in try/catch; use `readInt` everywhere |

---

## 📚 Java Concepts Checklist (for your report)

- [ ] Classes, objects, encapsulation (private fields + getters)
- [ ] Constructor overloading (`Block` has 2 constructors)
- [ ] Static methods & static factory (`Block.genesis()`, `HashUtil`)
- [ ] Collections: `ArrayList`, `LinkedHashMap`, `Collections.unmodifiableList`
- [ ] File I/O with try-with-resources (readers/writers or `java.nio.file.Files`)
- [ ] Custom checked exception (`CorruptDataException`) & unchecked (`VotingException`)
- [ ] Multithreading (mining spinner)
- [ ] Cryptography (`MessageDigest`, SHA-256)
- [ ] Regex validation
- [ ] Packages & layered architecture (core / model / service / util)
- [ ] Self-written test framework

**Good luck, you've got this! 🚀**
