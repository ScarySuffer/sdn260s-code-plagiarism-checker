# Code Plagiarism Checker

SDN260S Software Design II, Group 8 (CPUT)

A Java application that helps lecturers and tutors compare student source-code files and find significant similarities. It **assists** a person's review. It does not decide that plagiarism has happened: a high similarity score only flags a pair for a closer look.

## Status

Week 1 of the plan in Check-In 1 (planning and design).

| Part | State |
|---|---|
| `Main`, `CodeFile`, `FileReader`, `Logger`, `SimilarityResult` | Done and tested |
| `CodeProcessor`, `CodeTokenizer` | Implemented, with unit tests. Normalisation and token rules approved by the group (see class Javadoc) |
| `SimilarityChecker` | Implemented, with unit tests. Formula (Jaccard on 3-token groups) and 70% flag threshold approved by the group on 1 Nov 2026 |
| `CLI` | Implemented (text menu, `--cli`), with tests |
| `GUI` (Swing) | Implemented (`--gui`): add files/folder, compare, sortable results, matching parts, export. Needs a manual check on each machine |
| `ReportGenerator` (TXT/CSV) | Implemented, with tests |
| `Pair<A,B>`, `RankedList<T>` (own generic classes) | Implemented, with tests |
| `ResultStore`, GUI Save/Load results (serialization) | Implemented, with tests |
| Parallel comparison (`compareAll(..., threads)`) and `Benchmark` | Implemented, with tests. Timings on your own machine still to be recorded |

All planned classes are now implemented. The similarity method and threshold were approved by the group on 1 Nov 2026.

Run options: `--gui` opens the window, `--cli` opens the text menu, and a file or folder argument prints a similarity table.

## Requirements

- JDK 11 or newer
- Apache Maven 3.8 or newer (or open the folder as a Maven project in NetBeans, IntelliJ or VS Code)

## Build, test and run

```text
mvn test                       run the tests
mvn package                    build target/code-plagiarism-checker-0.1.0.jar
java -jar target/code-plagiarism-checker-0.1.0.jar samples
```

You can pass any mix of `.java` files and folders. Folders are searched recursively. Anything that cannot be used (empty, not `.java`, unreadable) is skipped and written to `logs/plagiarism-checker.log`.

## Project layout

```text
pom.xml
samples/                       three small example submissions
src/main/java/plagiarismchecker/
    Main                       starts the app and connects the components
    CLI, GUI                   user interfaces (text menu and Swing window)
    ComparisonService          shared logic used by Main, CLI and GUI
    CodeFile                   one submitted source file
    FileReader                 reads and validates files, scans folders recursively
    CodeProcessor              removes comments, normalises formatting
    CodeTokenizer              turns code into comparable tokens
    SimilarityChecker          compares tokens, calculates similarity
    SimilarityResult           files compared, percentage, matches, flag
    ReportGenerator            TXT/CSV report
    Logger                     events, warnings, errors
    InvalidSourceFileException a file or folder that cannot be used
src/test/java/plagiarismchecker/    JUnit 5 tests
```

`FileReader` is named to match the Check-In 1 UML diagram. It is not `java.io.FileReader`.

## Team and suggested ownership

Roles come from Check-In 1. The class owners below are suggestions. Agree them in your first meeting and change this table.

| Member | Role in Check-In 1 | Suggested classes |
|---|---|---|
| Dembe Kwinda | Project coordination and system design | `Main`, UML, README, design document |
| Murunwa Grace Khorommbi | Java implementation and core classes | `CodeFile`, `FileReader`, `Logger`, `CodeProcessor`, `CodeTokenizer` |
| Kagiso Lengolo | Similarity algorithm and data structures | `SimilarityChecker`, `SimilarityResult` |
| Liphelo Tayo | GUI and CLI development | `CLI`, `GUI` |
| Ndugiselo Masase | Testing and documentation | Tests, test datasets, `ReportGenerator` |

## Working with Git

1. Never commit straight to `main`.
2. One branch per task, for example `feature/tokenizer` or `fix/empty-file-message`.
3. Open a pull request. A teammate reviews it before it is merged.
4. Keep commits small and write what changed, for example `Add recursive folder scan`.

## Plan from Check-In 1

| Week | Planned work |
|---|---|
| 1 | Finalise requirements and UML, project structure, `Main`, `CodeFile`, file reading |
| 2 | Code processing, tokenisation, similarity comparison, collections and result storage, start the CLI |
| 3 | Complete the CLI, build the Swing GUI, reports and logging, test datasets, normal and edge-case tests |

## Testing

The tests cover normal cases (valid file, nested folders) and edge cases (missing, empty, whitespace-only, wrong type, folder given as file, null path, non-UTF-8 text, byte-order mark, very small and very large files, duplicate paths). As the later classes are written, add tests for different programs, similar programs, different formatting, and processing time, and record the results in the test log in the report.

## Syllabus coverage (SDN260S)

| Course topic | Where it is used |
|---|---|
| Object-oriented programming | `CodeFile`, `SimilarityResult`, `ComparisonService`, custom exception |
| Strings, characters, regular expressions | `CodeProcessor`, `CodeTokenizer` |
| Files, streams | `FileReader`, `ReportGenerator`, `Logger` |
| Object serialization | `ResultStore` (with `CodeFile` and `SimilarityResult` implementing `Serializable`; Save/Load in the GUI) |
| Recursion | `FileReader.scanRecursively` (sub-folders) |
| Searching, sorting, Big O | Binary search in `RankedList`, `SimilarityResult.HIGHEST_FIRST` ordering, analysis in `docs/BIG_O.md` |
| Generic collections | `List`, `Set`, `HashSet`, `LinkedHashSet` throughout |
| Generic classes and methods | `Pair<A,B>` and its generic `of` method |
| Custom generic data structure | `RankedList<T>` (array-backed, grows, ordered insert) |
| Multithreading | `SwingWorker` in the GUI; thread pool (`ExecutorService`) for comparing pairs |

## Performance

`java -cp target/classes plagiarismchecker.Benchmark` times the comparison on generated files, on one thread and on several. See `docs/BIG_O.md` for the analysis and for how to record your own results.
