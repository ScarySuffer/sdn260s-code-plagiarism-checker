# Check-In 2: Prototype (18 Oct 2026) - Group 8 demo pack

What the brief asks for: a short **console** demo (no GUI) showing the source code in line with the UML class
diagram; a working prototype; 40-50% of the requirements; organised, commented code; and the challenges faced.

## 1. UML and code (about 1 minute)

Show `docs/uml_class_diagram.png`. It was generated from the current source code, so every class, field and
method on it exists in `src/main/java/plagiarismchecker/`.

| Layer | Classes |
|---|---|
| Entry and interface | `Main`, `CLI` |
| Coordination | `ComparisonService` |
| Input | `FileReader` (recursive folder scan), `CodeFile`, `InvalidSourceFileException` |
| Processing | `CodeProcessor` (comments, imports, whitespace), `CodeTokenizer` |
| Comparison | `SimilarityChecker`, `SimilarityResult` |
| Output | `ReportGenerator` (TXT, CSV), `ResultStore` (save and load) |
| Support | `Logger`, `Pair<A,B>`, `RankedList<T>` |

**Check this against the Check-In 1 UML before presenting.** Classes that are probably newer than that diagram
(compare and confirm): `CodeTokenizer`, `ComparisonService`, `ResultStore`, `RankedList`, `Pair`. If the
Check-In 1 diagram differs, say so in the demo and show this one as the updated version.
The Swing `GUI` and `ResultsTableModel` exist but are left out of this diagram, since this stage is console only.

## 2. Live console demo (about 3 minutes)

Build first in NetBeans (Clean and Build). From PowerShell in the project folder:

```powershell
java -cp target\classes plagiarismchecker.Main samples
java -cp target\classes plagiarismchecker.Main --cli
```

Expected results with the three files in `samples/`: StudentA and StudentB 100.0% (flagged for review), StudentA
and StudentC 27.8%.

Text menu steps (`--cli`):

| Step | You type | What it shows |
|---|---|---|
| 1 | `1`, then `samples\mock` | Folder scan loads 11 files |
| 2 | `2` | List of submissions with line counts |
| 3 | `3` | "Compared 55 pair(s)" |
| 4 | `4` | Ranked table. Top: Alice/Bob 100.0%, Gina/Hank 100.0%, Dan/Eve 80.4% (flagged); next Gina/Kim 61.0% (not flagged) |
| 5 | `5`, then `report.txt` | Report file written; show it opens |
| 6 | `6` | Exit |

Edge cases to show (30 seconds): `java -cp target\classes plagiarismchecker.Main` with no arguments prints the
usage text; a wrong path (`Main nothing-here`) prints "File does not exist" and does not crash. Then show
`logs\plagiarism-checker.log`.

Always say: **a flag only marks a pair for a person to review. It is not a finding of plagiarism.** The
similarity method (3-token groups, Jaccard, 70% flag) was approved by the group on 1 Nov 2026.

## 3. Core features checklist (the 40-50% target)

| Requirement area | Status |
|---|---|
| Read a single file, with validation (missing, empty, wrong type, odd encodings) | Done |
| Scan a folder and its sub-folders (recursion) | Done |
| Logging to console and file | Done |
| Custom exception for bad input | Done |
| Normalise code (strip comments, imports, whitespace) | Done |
| Tokenise code (identifiers, numbers and strings generalised) | Done |
| Similarity score for a pair, with matching fragments | Done (method approved) |
| Compare every pair, rank, flag at a threshold | Done (70% approved) |
| Console menu (add, view, compare, results, report, exit) | Done |
| Report to TXT or CSV | Done |
| Save and load results (serialization) | Done |
| Unit tests | 87 passing (confirm in NetBeans) |
| Swing GUI | Built, beyond what this check-in needs |
| Approve the method and threshold as a group | Not done |
| Tune on real student samples | Not done |
| Processing-time measurement on your own machine | Not done (`Benchmark` is ready) |
| Final test plan and test evidence | Not done (due Check-In 3) |

All console core features are in place, so the project is past the 40-50% mark. What remains is group approval,
tuning, timing and documentation.

## 4. Code review points (about 1 minute)

- Every class has a class comment; public methods and non-obvious logic are commented.
- Logic lives in `ComparisonService`, so `Main`, `CLI` and `GUI` run the same code, with no duplicate logic.
- Collections used on purpose: `HashSet` for fast group look-up, `LinkedHashSet` to drop duplicates and keep order.
- Open `SimilarityChecker.java` and show the method comment, then the tests in `SimilarityCheckerTest`.

## 5. Challenges faced (say these in your own words)

Technical, all of which happened:
- **Number format:** on a Windows computer set to a comma decimal, "100.0" printed as "100,0" and a test failed.
  Fixed by formatting with a fixed locale.
- **Windows file names:** a test used a quote character in a file name, which Windows does not allow. Changed the test.
- **Boilerplate scores:** unrelated programs score about 20-30% because they share common code such as `main`
  and loops. This is why the flag threshold is a proposal, not a final number.
- **Partial copies:** a file with only part copied scores 61%, which is below the 70% flag. Needs tuning on real data.
- **Tooling:** Maven was not installed on the development computer, so the project was built with NetBeans.

Teamwork (confirm these with the group before saying them):
- The Java project is local only; it is not on GitHub yet, so teammates cannot see it.
- One member has limited laptop access, so their work is document and test-case preparation on a phone.
- Task ownership is suggested from Check-In 1 and still needs each person's confirmation.
