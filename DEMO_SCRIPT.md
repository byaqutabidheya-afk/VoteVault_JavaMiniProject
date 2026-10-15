# Vote Vault: Complete Feature-by-Feature Evaluation Demo Script

This script provides the complete, step-by-step walkthrough to start **Vote Vault** and demonstrate every single feature to teachers, evaluators, or viva examiners.

---

## Step 1: Clean Start & Compilation

Open **PowerShell** in `D:\VoteVault_JavaMiniProject` and run:

```powershell
# 1. Clean previous data and build folder for a fresh demonstration:
if (Test-Path data) { Remove-Item -Recurse -Force data }
if (Test-Path out)  { Remove-Item -Recurse -Force out }

# 2. Compile all files using plain javac:
javac -d out (Get-ChildItem -Recurse -Filter *.java src).FullName

# 3. Launch the application:
java -cp out voting.Main
```

---

## Step 2: Startup & Integrity Verification

1. The cyan **VOTE VAULT** banner will appear.
2. Notice the green verification message on launch:
   ```text
   ✔ Blockchain verified: 1 blocks, integrity OK
   ```
3. Press **Enter** to proceed to the **Main Menu**.
   - *What to say:* *"Vote Vault boots with an automated cryptographic integrity check validating the mined Genesis block."*

---

## Step 3: Complete Feature-by-Feature Walkthrough

### Feature 1: Admin Panel & Password Protection (Menu Option 5)
1. Type **`5`** and press **Enter**.
2. **Demonstrate Wrong Password Rejection**:
   - Type `wrongpass` ➔ Yellow warning: `⚠ Incorrect password. 2 attempt(s) remaining.`
   - Type `admin123` ➔ Access granted to the **Admin Panel**.
   - *What to say:* *"Administrative credentials are authenticated against a salted SHA-256 digest; the plaintext password is never stored."*
3. **Register Candidates**:
   - Type **`2`** (Add Candidate) ➔ Name: `Alice` ➔ Press Enter.
   - Type **`2`** (Add Candidate) ➔ Name: `Bob` ➔ Press Enter.
   - Type **`2`** (Add Candidate) ➔ Name: `Carol` ➔ Press Enter.
   - *(Optional duplicate check)*: Try typing `alice` again ➔ Red error shows case-insensitive duplicate rejection!
4. **Register Voters**:
   - Type **`1`** (Register Voter) ➔ Voter ID: `V001`, Name: `David` ➔ Press Enter.
   - Type **`1`** (Register Voter) ➔ Voter ID: `V002`, Name: `Elena` ➔ Press Enter.
   - Type **`1`** (Register Voter) ➔ Voter ID: `V003`, Name: `Farooq` ➔ Press Enter.
5. **View Voter Registry**:
   - Type **`3`** (List Voters) ➔ Displays a formatted table with IDs, Names, and `Voted? NO`.
6. Type **`0`** to return to the Main Menu.

---

### Feature 2: Casting Votes & Proof-of-Work Mining (Menu Option 1)
1. Type **`1`** (Cast Vote).
2. Enter Voter ID: **`V001`**.
3. It displays the official ballot candidates. Choose **`1`** (Alice).
4. **Observe the Multithreaded Animated Spinner**:
   ```text
   Mining block for your vote... / (done)
   ```
5. A green confirmation message shows:
   - Block **#1** index.
   - The mined SHA-256 hash starting with **`0000`** (difficulty 4 target satisfied).
6. Press **Enter** to return to the menu.
   - *What to say:* *"The vote is mined using proof-of-work consensus. Notice that the voter's raw ID is never stored on the blockchain ledger; only their SHA-256 hash is recorded to protect ballot secrecy."*

---

### Feature 3: Single Source of Truth & Double-Vote Prevention
1. Type **`1`** (Cast Vote).
2. Enter Voter ID: **`V001`** again.
3. The system scans the blockchain ledger and immediately blocks the attempt:
   ```text
   ✘ Voter 'V001' has already voted! Double voting is strictly prohibited.
   ```
   - *What to say:* *"Double voting is prevented by auditing the blockchain ledger as the single source of truth, not a separate flag that could be altered."*
4. Now cast the remaining votes:
   - Type **`1`** ➔ Voter ID: **`V002`** ➔ Vote for **`1`** (Alice) ➔ Press Enter.
   - Type **`1`** ➔ Voter ID: **`V003`** ➔ Vote for **`2`** (Bob) ➔ Press Enter.

---

### Feature 4: Live Election Results & Bar Charts (Menu Option 2)
1. Type **`2`** (View Live Results).
2. The ASCII graphical bar chart renders dynamically:
   - **Alice**: Green bar (leader), 2 votes (**66.7%**).
   - **Bob**: 1 vote (**33.3%**).
   - **Carol**: 0 votes (**0.0%**).
   - Displays statistics: Total Votes (3), Registered Voters (3), Voter Turnout (**100.0%**), Total Blocks (4), Difficulty (4), and **Average Mining Time (in ms)**.
3. Press **Enter** to return to the menu.

---

### Feature 5: Blockchain Explorer with Pagination (Menu Option 3)
1. Type **`3`** (Explore Blockchain).
2. Explores the sequential cryptographic blocks:
   - **Block #0** (Genesis block, `Prev Hash: 0`).
   - `↓ linked to next block`
   - **Block #1** (`Prev Hash` points to Block #0).
   - `↓ linked to next block`
   - **Block #2** (`Prev Hash` points to Block #1).
   - `↓ linked to next block`
   - **Block #3** (`Prev Hash` points to Block #2).
3. **Show Voter Privacy**:
   - Point out that nowhere in any block is `V001` or `David` visible; only the 64-character SHA-256 hash appears.
4. Press **Enter** to return to the menu.

---

### Feature 6: Hacker Simulation & Live Tamper Detection (Menu Option 6) — *The Star Feature!*
1. Type **`6`** (Hacker Simulation).
2. Admin password prompt: Enter **`admin123`**.
3. Choose which block to attack: Type **`2`** (Block #2, where Bob was voted for).
4. Enter the replacement candidate: Type **`Carol`**.
5. The console shows:
   ```text
   Simulating attacker editing chain.txt...
   ```
6. **Live Cryptographic Detection Triggers Instantly**:
   - `✔ Block #0: valid`
   - `✔ Block #1: valid`
   - `✘ Block #2: INVALID - Block data was modified (hash mismatch)`
   - `⚠ Block #3: UNTRUSTED (broken link)`
   - Big Red Boxed Alert:
     ```text
     +-----------------------------------------------------------+
     |  TAMPERING DETECTED!                                      |
     |  Compromised Block: Block #2                              |
     |  Failure Reason: Block data was modified (hash mismatch)  |
     +-----------------------------------------------------------+
     ```
   - **Side-by-Side Evidence**: Displays `STORED Hash` vs. `RECALCULATED Hash`.
   - Displays explanation: *"Changing one vote breaks this block's fingerprint and every link after it. Re-mining all following blocks would be required."*
7. Press **Enter**.
8. Notice the Main Menu status now displays in bold red:
   ```text
   [SYSTEM LOCKED: TAMPERING DETECTED]
   ```
9. **Show System Lockdown**:
   - Try typing **`1`** (Cast Vote) ➔ System refuses:
     `✘ Voting is currently locked due to blockchain integrity issues.`

---

### Feature 7: Disaster Recovery from Automated Backup (Menu Option 5 ➔ 5)
1. Type **`5`** (Admin Panel) ➔ Password: **`admin123`**.
2. Type **`5`** (Repair from Backup).
3. The system copies the verified `chain.backup.txt` over `chain.txt`:
   ```text
   ✔ Backup restored successfully from chain.backup.txt!
   ✔ Blockchain verified: 4 blocks, integrity OK
   ```
4. Type **`0`** to return to Main Menu.
5. The system status returns to **`Ready`** and voting is unlocked!

---

### Feature 8: Verify Integrity Anytime (Menu Option 4)
1. Type **`4`** (Verify Integrity).
2. Reloads directly from disk:
   - Green checkmarks appear next to each block.
   - Prints: `✔ Blockchain verified: 4 blocks, integrity OK.`

---

### Feature 9: Clean Exit & Persistence (Menu Option 0)
1. Type **`0`** ➔ Prints `Thank you for using VoteVault. Goodbye!` and exits cleanly.
2. **Demonstrate Persistence**:
   - Relaunch the app: `java -cp out voting.Main`
   - Notice: `✔ Blockchain verified: 4 blocks, integrity OK` appears immediately with all 4 blocks and 3 registered voters preserved on disk!
   - Exit with **`0`**.

---

## Step 4: Run the Automated Test Suite (To Prove Correctness)

To prove to your teachers that all 42 automated test cases pass without any errors:

```powershell
java -cp out voting.test.TestRunner
```

**Expected Result**:
```text
=== Running Phase 1 Tests === (5 passed)
=== Running Phase 2 Tests === (7 passed)
=== Running Phase 3 Tests === (7 passed)
=== Running Phase 4 Tests === (7 passed)
=== Running Phase 5 Tests === (12 passed)
=== Running Phase 7 Tests === (4 passed)

42 passed, 0 failed
```
