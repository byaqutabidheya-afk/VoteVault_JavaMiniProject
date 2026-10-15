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