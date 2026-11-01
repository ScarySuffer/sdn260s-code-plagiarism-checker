# Code Plagiarism Checker

SDN260S Software Design II · Group 8 · Topic 17

This is a Java tool that compares student source-code files and shows how similar they are. Point it at a folder of submissions and it works through every pair, ranks them from most to least similar, marks the ones that look worth a closer read, and can write the results to a report.

> **Please read this first.** The tool measures similarity. It does not decide whether anyone plagiarised. Two students who follow the same assignment can honestly write very similar code. A flag means "a person should look at this pair", nothing more.

---

## 1. What it does

- Reads `.java` files, either one at a time or from a folder (sub-folders are searched too).
- Strips out comments, `package` and `import` lines and extra spacing, then breaks the code into tokens. Every variable, method and class name becomes `ID`, so renaming things does not hide copied logic.
- Compares every pair of files and gives each pair a similarity percentage.
- Flags pairs at or above the review threshold (70%).
- Shows the matching parts of the code so you can see why a pair scored high.
- Writes a text or CSV report, keeps a log file, and can save results and load them again later (GUI).
- Comes with a text menu and a Swing window. Both run the same comparison code.
- Compares pairs on several threads when the computer has more than one processor core.

## 2. Requirements

| You need | Notes |
| --- | --- |
| JDK 11 or newer | Developed against release 11 and also run on JDK 21. Swing is part of the JDK. |
| Apache Maven 3.x | Only needed to build with Maven and to run the unit tests (`mvn test`). |
| An internet connection the first time | Maven downloads its plugins and JUnit on the first build. |

The program itself uses no outside libraries. JUnit 5 is only used for the tests.

Check what you have:

```
java -version
javac -version
mvn -version
```

## 3. Project layout

```
Group8_CodePlagiarismChecker/
├── pom.xml                      Maven build file
├── README.md                    this file
├── src/
│   ├── main/java/plagiarismchecker/     the program (18 classes)
│   └── test/java/plagiarismchecker/     the unit tests (87 tests)
├── samples/
│   ├── StudentA.java, StudentB.java, StudentC.java    three tiny samples
│   └── mock/                            11 invented submissions + README.txt + expected.csv
├── test_datasets/               nine small test folders (T01 to T09) with their own README
├── docs/                        UML class diagram, Big O notes, demo notes
└── logs/                        created when you run the program
```

`target/` is created by the build.

## 4. Build and test

Open a terminal in the folder that contains `pom.xml`.

```
mvn clean test
```

A good run ends with:

```
Tests run: 87, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

To compile only (this puts the classes in `target/classes`):

```
mvn compile
```

No Maven? You can compile by hand.

Windows (PowerShell):

```powershell
mkdir target\classes
javac --release 11 -d target\classes (Get-ChildItem src\main\java -Recurse -Filter *.java).FullName
```

Linux / macOS:

```bash
mkdir -p target/classes
javac --release 11 -d target/classes $(find src/main/java -name "*.java")
```

## 5. Running the program

All commands are run from the project folder, after the classes have been compiled. On Windows use `target\classes`; on Linux and macOS use `target/classes`.

The main class is `plagiarismchecker.Main`. It has three modes. Prefer to let Maven start it? Run `mvn compile` once, then this works too (the first run downloads the exec plugin, so it needs internet):

```
mvn org.codehaus.mojo:exec-maven-plugin:3.1.0:java "-Dexec.mainClass=plagiarismchecker.Main" "-Dexec.args=--cli"
```

Change `--cli` to `--gui`, or to a file or folder path such as `samples`, for the other modes.

### 5.1 Compare files or folders straight from the command line

```
java -cp target/classes plagiarismchecker.Main samples/mock
```

You can give it any mix of files and folders:

```
java -cp target/classes plagiarismchecker.Main samples/StudentA.java samples/StudentB.java samples/StudentC.java
```

It loads everything, lists what it found, then prints a ranked similarity table. Real output for the three small samples:

```
========================================
       CODE PLAGIARISM CHECKER
========================================
Loaded 3 file(s):
  1. StudentA.java (10 lines)
  2. StudentB.java (9 lines)
  3. StudentC.java (12 lines)

Similarity (method: 3-token groups, flag at 70%)
  StudentA.java <-> StudentB.java: 100.0% (flagged for review)
  StudentA.java <-> StudentC.java: 27.8%
  StudentB.java <-> StudentC.java: 27.8%
A flag only marks a pair for a person to review. It is not a finding of plagiarism.
```

StudentA and StudentB do the same job with different names and spacing, which is why they reach 100%.

Exit codes: 0 finished normally · 1 no usable file could be loaded · 2 no arguments were given (the usage text is shown).

### 5.2 The text menu

```
java -cp target/classes plagiarismchecker.Main --cli
```

```
--- Code Plagiarism Checker ---
1. Add submissions (file or folder)
2. View submissions
3. Compare
4. View results
5. Generate report
6. Exit
Choose:
```

A typical session: choose 1 and type a path to a `.java` file or a folder (repeat to add more), 2 to check what is loaded, 3 to compare, 4 to see the ranked results, 5 to write a report, 6 to leave. Anything that is not 1 to 6 gets "Please choose a number from 1 to 6." and the menu comes back.

### 5.3 The window (GUI)

```
java -cp target/classes plagiarismchecker.Main --gui
```

The toolbar buttons, left to right:

| Button | What it does |
| --- | --- |
| Add files... | Pick one or more `.java` files. |
| Add folder... | Pick a folder. Sub-folders are searched. |
| Clear | Empty the file list and the results. |
| Compare | Compare every pair (needs at least two files). |
| Export report... | Save the results as `.txt` or `.csv`. |
| Save results... | Save the results as a `.g8` file. |
| Load results... | Open a `.g8` file saved earlier. |

The left panel lists the loaded files with their line counts. The table shows each pair with its similarity and a status ("Flagged for review" or "Not flagged"); flagged pairs are shaded, and you can click a column heading to sort. Click a pair to see its matching parts underneath. A status line at the bottom tells you what happened. Loading and comparing run in the background, so the window does not freeze.

The full step-by-step walk-through is in the User Guide (`docs/`). `--gui` and `--cli` must be the only argument. Anything else is treated as a file or folder path.

### 5.4 In NetBeans or another IDE

Open the project folder as a Maven project. To run it, run `plagiarismchecker.Main` and give it the arguments you want (`--gui`, `--cli`, or file and folder paths). In NetBeans the arguments go in Project Properties → Run → Arguments. Run it from the project folder so that `samples/` and `logs/` are found.

## 6. How the score is worked out

1. **Clean.** Comments are removed (comment marks inside quoted text are left alone). `package` and `import` lines are dropped because every file has them. Blank lines and extra spaces are removed.
2. **Tokenise.** Java keywords and `true`, `false`, `null` stay as written. Every other name becomes `ID`, numbers become `NUM`, text in quotes becomes `STR`, characters become `CHR`. Operators and punctuation are kept.
3. **Group.** The tokens are cut into overlapping groups of 3 in a row.
4. **Compare.** Each file becomes a set of unique groups. The score is the Jaccard index:

   ```
   similarity = (groups in both files) / (groups in either file) × 100
   ```

5. **Flag.** A pair at or above 70% is flagged for review.

The group size (3) and the threshold (70%) were approved by the group on 1 November 2026. They are set in `SimilarityChecker` and are not options in the menu or window.

**Reading the numbers.** On the mock data, renamed copies scored 80–100% and a partial copy scored 61%. Independent solutions to the same task scored about 29–51%, and unrelated programs about 16–45%. Short beginner programs share a lot of ordinary structure (for loops, `System.out.println`), so a middling score proves very little. Use the matching parts, not just the percentage.

## 7. Reports, saved results and the log

- **Text report** (`.txt`, or any name that does not end in `.csv`): a header, the number of pairs and flags, then every pair with up to 10 matching fragments shown.
- **CSV report** (`.csv`): one row per pair with the columns File A, File B, Similarity (%), Flagged for review, Matching fragments. It opens in Excel.
- **Saved results** (`.g8`, GUI only): a binary file made with Java serialization. Loading only accepts this program's own classes. A file from somewhere else is refused with a readable message.
- **Log** (`logs/plagiarism-checker.log` in the folder you ran from): one timestamped line per event, tagged INFO, WARN or ERROR. It records every file loaded, every file skipped and why, and every comparison. When you give files or folders on the command line, the same lines are also printed on screen. In the text menu and in the window they are only in the log file, so look there if a file you added did not load.

## 8. How it handles bad input

A bad file never stops a run. It is skipped, the reason is logged, and the others carry on.

| Situation | What happens |
| --- | --- |
| File or folder does not exist | ERROR logged, skipped |
| File does not end in `.java` | ERROR: Only .java files can be checked, skipped |
| Empty or blank file | ERROR: File is empty, skipped |
| A path that is a folder when a file was expected, or the reverse | Clear message, skipped |
| File that cannot be read | ERROR logged, skipped |
| Not valid UTF-8 (older editors) | Read as ISO-8859-1, WARN logged |
| Byte-order mark at the start | Removed silently |
| Same file added twice | Counted once |
| Symbolic link inside a folder | Skipped with a WARN (avoids loops) |
| Only one file loaded | "Add at least two files to compare them." |
| Only comments or imports in a file | Loads, but has nothing to compare, so scores 0.0% against everything |
| Report or results file cannot be written | Error message, program keeps running |

On the command line each of these messages shows on screen as `[ERROR]` or `[WARN]`. In the text menu you only see "0 file(s) loaded in total" and in the window "No usable .java files were found"; the reason is in `logs/plagiarism-checker.log`. When a folder is scanned, files that do not end in `.java` are skipped without any message.

## 9. The classes

| Class | Job |
| --- | --- |
| `Main` | Starts the program; reads the arguments |
| `CLI` | The text menu |
| `GUI`, `ResultsTableModel` | The Swing window and its results table |
| `ComparisonService` | Runs load → compare so the CLI and GUI share one code path |
| `FileReader` | Reads and checks `.java` files; scans folders recursively |
| `CodeFile` | One loaded file (path and text); immutable |
| `CodeProcessor` | Removes comments, imports and extra spacing |
| `CodeTokenizer` | Turns cleaned code into tokens |
| `SimilarityChecker` | Builds the groups, calculates the score, compares pairs on several threads |
| `SimilarityResult` | The result for one pair: files, score, matching parts, flag |
| `RankedList<T>`, `Pair<A,B>` | Small generic classes written for this project |
| `ReportGenerator` | Writes the TXT and CSV reports |
| `ResultStore` | Saves and loads results |
| `Logger` | Keeps and writes the log |
| `InvalidSourceFileException` | The error for a file that cannot be used |
| `Benchmark` | Times comparisons on generated files (not part of normal use) |

The UML class diagram is in `docs/`.

## 10. Testing

**Unit tests.** `mvn test` runs 87 tests with JUnit 5.

| Test class | Tests | Covers |
| --- | --- | --- |
| FileReaderTest | 14 | reading, checks, folder scan, bad files |
| SimilarityCheckerTest | 8 | scoring, flags, limits |
| CodeTokenizerTest | 7 | tokens |
| RankedListTest | 7 | the ranked list |
| CodeProcessorTest | 6 | comment and spacing removal |
| MainTest | 6 | command-line mode and exit codes |
| ResultStoreTest | 6 | saving and loading |
| MockSamplesTest | 5 | the mock submissions |
| CodeFileTest | 4 | the file object |
| ComparisonServiceTest | 4 | the service |
| ParallelComparisonTest | 4 | several threads give the same results as one |
| ReportGeneratorTest | 4 | TXT and CSV reports |
| CLITest, PairTest, SimilarityResultTest | 3 each | menu, pair, result |
| LoggerTest | 2 | the log |
| ResultsTableModelTest | 1 | the table model |

**Mock data.** `samples/mock/` holds eleven invented submissions with relationships built in on purpose (renamed copies, a reordered copy, a partial copy, independent solutions, unrelated programs). `expected.csv` lists what each pair was meant to be. It is the intention, not a measured result.

**Test datasets and the test log.** `test_datasets/` holds nine small folders (T01 to T09) with known relationships between the files, such as a renamed copy, identical files, an empty file and a wrong file type. Its own README gives the expected result for each. The 13 tests built on them (T-01 to T-13) are recorded in the Test Log workbook. On the final build 11 passed and 2 failed; the failures are the two problems listed in the limitations below.

**Speed check.** `java -cp target/classes plagiarismchecker.Benchmark` generates files and times the comparison on one thread and on several. The numbers depend on your computer, and extra threads only help if it has more than one processor core.

## 11. Known limitations

- Only `.java` files are supported.
- The score looks at structure, not behaviour. Two programs that work the same way but are built differently can score low.
- The flag threshold and group size cannot be changed from the menu or window.
- Short programs score each other higher than you might expect, because they share ordinary code.
- Saving and loading results is available in the window only, not in the text menu.
- It compares the files you give it. It does not search the internet or earlier years.
- In the text menu and the window, the reason a file was rejected is only written to the log (the screen shows a short summary).
- A path typed at the text menu that contains a character the system does not allow stops the menu with an error. On Windows this includes quotation marks, so type or paste paths without quotes.
- A file that contains only comments and imports loads but scores 0.0% against everything, without a message.
- The method (3-token groups, Jaccard similarity, 70% threshold) was approved by the group on 1 November 2026. So far it has only been tried on invented and generated files, not on real student work.

## 12. Troubleshooting

| Problem | Try this |
| --- | --- |
| `'javac' is not recognized` | Install a JDK (not just a JRE) and reopen the terminal. |
| Could not find or load main class `plagiarismchecker.Main` | Compile first, and run from the folder that contains `target`. Check the `-cp` path. |
| No usable source files were loaded | Check the path, quote it if it has spaces, and make sure the files end in `.java`. |
| The menu stops with a long Java error after you typed a path | Type the path without quotation marks and without unusual characters, then start the menu again. |
| A file does not show up in the list | Look in `logs/plagiarism-checker.log` for the reason. |
| Window does not open | `--gui` must be the only argument. On a remote or headless machine there is no screen to show it on. |
| `mvn` not found | Install Maven, or use the manual `javac` commands in section 4. |
| Tests fail on an old JDK | Use JDK 11 or newer. |

## Team (Group 8)

Dembe Kwinda, Murunwa Grace Khorommbi, Kagiso Lengolo, Liphelo Tayo, Ndugiselo Masase, Bandile Qwane.
