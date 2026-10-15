# ChainVote: Blockchain-Based Voting System

ChainVote is a lightweight, tamper-evident console voting platform written in pure Core Java 17+. It leverages blockchain architecture, cryptographic SHA-256 hashing, and proof-of-work consensus to guarantee election integrity, voter privacy, and auditable ballots without reliance on external frameworks, databases, or third-party dependencies.

---

## Features

- **Decentralized Cryptographic Ledger**: Every cast vote is stored as an immutable block cryptographically bound to the previous block via SHA-256 digests.
- **Proof-of-Work (PoW) Mining**: Includes proof-of-work consensus requiring nonces that produce hashes with 4 leading zeros, protecting the chain from arbitrary alterations.
- **Voter Privacy**: Stores anonymized SHA-256 voter ID hashes on the blockchain ledger rather than plaintext identities, making vote attribution impossible while mathematically preventing double-voting.
- **Instant Tamper Detection**: Audits the entire blockchain against four fundamental rules: recalculated block hash matching, cryptographic link chaining, proof-of-work difficulty adherence, and sequential ordering.
- **Live Hacker Simulation & Audience Demo**: An interactive simulation allowing authorized administrators to tamper with records in `data/chain.txt` and immediately observe side-by-side diagnostic alerts and chain lockdown.
- **Automated Backup & Disaster Recovery**: Automatically maintains a pristine `chain.backup.txt` on valid operations, enabling one-click emergency restoration from the admin console.
- **Human-Readable Persistence**: Uses transparent, pipe-delimited UTF-8 plain-text files (`chain.txt`, `voters.txt`, `candidates.txt`) for easy inspection and demonstration.
- **Interactive Console UI**: ANSI color-coded menus, ASCII art banner, background multithreaded mining spinner, and live election result bar charts.

---

## Core Java Concepts Used

- **Object-Oriented Programming (OOP) & Clean Architecture**: Strict separation of concerns across core domain models, persistence services, business rules, and presentation utilities.
- **Encapsulation**: Private fields with explicit immutability (`final`), defensive copies, and unmodifiable collection wrappers (`Collections.unmodifiableList`, `Collections.unmodifiableMap`).
- **Collections Framework**: Ordered `ArrayList` for blockchain sequences, `LinkedHashMap` for deterministic voter registry order and sorted election tallies, and entry comparator sorting.
- **Robust File I/O & Streams**: Plain-text persistence with `BufferedReader`, `BufferedWriter`, `FileInputStream`, `FileOutputStream`, and atomic file operations using `java.nio.file.Files`.
- **Exception Hierarchy & Handling**: Checked exception (`CorruptDataException`) tracking 1-based file line numbers and unchecked domain exception (`VotingException`) preventing invalid actions.
- **Multithreading & Concurrency**: Background daemon thread displaying an animated console spinner (`|/-\`) synchronized with mining completion via atomic flags (`AtomicBoolean`).
- **Cryptography & Security**: SHA-256 digest hashing with `java.security.MessageDigest` and constant-time password hash verification for administrative access.
- **Static Factory Methods**: Creation of the Genesis block via `Block.genesis(int difficulty)`.

---

## Project Structure

```text
VoteVault_JavaMiniProject/
├── data/                                 # Human-readable plain-text persistence files
│   ├── chain.txt                         # Blockchain ledger records
│   ├── chain.backup.txt                  # Automated verified backup
│   ├── voters.txt                        # Registered voter directory
│   └── candidates.txt                    # Election candidate ballot
├── src/
│   └── voting/
│       ├── Main.java                     # Application entry point and interactive CLI menu loop
│       ├── core/
│       │   ├── Block.java                # Immutable block entity with mining and hashing
│       │   └── Blockchain.java           # Chain manager with 4-rule cryptographic audit
│       ├── model/
│       │   └── Voter.java                # Registered voter entity
│       ├── service/
│       │   ├── Storage.java              # File persistence, backup, and tamper simulator
│       │   ├── VotingService.java        # Core business rules, ballot tallies, and security
│       │   ├── CorruptDataException.java # Checked exception for corrupt text records
│       │   └── VotingException.java      # Unchecked exception for election rule violations
│       ├── util/
│       │   ├── HashUtil.java             # SHA-256 cryptographic hashing helper
│       │   └── ConsoleUI.java            # ANSI colors, banner, input readers, and bar chart
│       └── test/
│           └── TestRunner.java           # Standalone automated testing framework (42 tests)
├── README.md                             # Project documentation
├── DEMO_SCRIPT.md                        # Step-by-step 2-minute live presentation guide
└── VIVA_QA.md                            # Comprehensive exam and viva defense questions
```

---

## How Blockchain Prevents Tampering

In ChainVote, each vote is permanently sealed inside a block that contains two crucial pieces of data:
1. **The block's own cryptographic fingerprint** (the SHA-256 hash computed over its index, timestamp, voter hash, candidate, previous block hash, and nonce).
2. **The previous block's hash**, forming an unbreakable digital chain.

```
+--------------------+        +--------------------+        +--------------------+
| Block #1           |        | Block #2           |        | Block #3           |
| Candidate: Alice   |        | Candidate: Bob     |        | Candidate: Alice   |
| PrevHash: 0000...  |◄───────| PrevHash: 0000874c |◄───────| PrevHash: 00008cd3 |
| Hash:     0000874c |        | Hash:     00008cd3 |        | Hash:     00001444 |
+--------------------+        +--------------------+        +--------------------+
```

If an attacker opens `data/chain.txt` and attempts to alter a vote in Block #2 (e.g., changing "Bob" to "Carol"):
- **Recalculation Catches It**: Because the SHA-256 hash depends on the candidate's name, the hash recalculated from the modified content no longer matches the stored hash originally mined for that block.
- **Broken Chain Link**: Even if the attacker re-mines Block #2 to find a new nonce starting with `0000`, Block #2's new hash will no longer match the `previousHash` pointer permanently recorded in Block #3.
- **Cascading Invalidation**: To make the alteration look authentic, the attacker would be forced to re-mine every single subsequent block across the entire chain—a computationally prohibitive task.

When ChainVote audits the chain, it flags the modified block with red diagnostic alerts, shows side-by-side cryptographic evidence, marks all downstream blocks as `UNTRUSTED`, and locks down the voting system.

---

## Compilation & Execution Guide

### Prerequisites
- Java Development Kit (JDK) 17 or higher.
- Terminal with ANSI color support (Windows Terminal, VS Code Terminal, PowerShell, or bash).

### Windows (PowerShell)
```powershell
# 1. Compile all source files into the out directory
javac -d out (Get-ChildItem -Recurse -Filter *.java src).FullName

# 2. Run automated test suite (all 42 unit tests)
java -cp out voting.test.TestRunner

# 3. Launch interactive application
java -cp out voting.Main
```

### Windows (Command Prompt)
```cmd
dir /s /b src\*.java > sources.txt
javac -d out @sources.txt
java -cp out voting.test.TestRunner
java -cp out voting.Main
```

### Linux / macOS
```bash
# 1. Compile
javac -d out $(find src -name "*.java")

# 2. Run automated tests
java -cp out voting.test.TestRunner

# 3. Launch interactive application
java -cp out voting.Main
```

---

## Future Scope

1. **Graphical User Interface (GUI)**: Implement a modern desktop interface using JavaFX or a lightweight Web dashboard visualizing live block nodes and graphs.
2. **Peer-to-Peer Network Nodes**: Expand beyond local file persistence to a distributed P2P network using Java Sockets where multiple polling stations synchronize blocks via consensus algorithms.
3. **Asymmetric Public-Key Cryptography (Digital Signatures)**: Incorporate RSA or ECDSA key pairs so voters cryptographically sign each ballot with their private key, providing non-repudiation.
4. **Smart Contracts**: Introduce automated election rules (e.g., automatic poll closure at predetermined timestamps, runoff voting logic).
