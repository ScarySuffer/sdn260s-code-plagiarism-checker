# Efficiency analysis (Big O)

Symbols: **n** = number of files, **t** = tokens in one file (about the same for every file),
**g** = number of 3-token groups in one file (g is about t), **P** = number of pairs = n(n-1)/2.

## Per stage

| Stage | Time | Space | Why |
|---|---|---|---|
| Read files (`FileReader`) | O(total characters) | O(total characters) | Each file is read once. Sub-folders are scanned recursively, each folder once. |
| Normalise and tokenise | O(total characters) | O(n·t) | One pass over each file's text. |
| Compare one pair (`SimilarityChecker.compare`) | O(g) expected | O(g) | Two hash sets are built, then intersected and merged. Hash look-ups are O(1) on average. |
| Compare all pairs | **O(n²·g)** | O(n·t) plus O(g) per pair in progress | P = n(n-1)/2 pairs, each O(g). |
| Order the results | O(P log P) with `List.sort`; O(P²) worst case with `RankedList` | O(P) | `RankedList` finds each position by binary search, O(log P), but shifting items is O(P). The shift is a fast `System.arraycopy`, and the pair comparisons dominate the run time. |
| Parallel comparison | O(n²·g / threads) at best | same | Pairs are independent. The tokenising and the final ordering stay single-threaded, so the speed-up is below the number of threads. |

The n² term is the important one: doubling the number of files makes the comparison about four times slower.

## Measured (real runs)

Measured on 8 Nov 2026 by running the project's code: OpenJDK 21, `--release 11`, a machine with
2 processors (not a student laptop), 2 warm-up runs, then the median of 5 runs. The full table,
including the real test datasets, is in `REAL_EXECUTION_TIME_BENCHMARK.md`.

| Files | Pairs | 1 thread (ms) | 2 threads (ms) | Speed-up |
|---|---|---|---|---|
| 10 | 45 | 22.8 | 20.5 | 1.11x |
| 20 | 190 | 61.3 | 38.1 | 1.61x |
| 40 | 780 | 242.4 | 122.4 | 1.98x |
| 80 | 3160 | 904.4 | 574.8 | 1.57x |

Reading the table: from 20 to 40 files the single-thread time grows about 4x (61.3 to 242.4 ms),
which matches O(n²). Two threads give roughly 1.6x to 2x on larger inputs. For tiny inputs (one
pair of files) two threads gain nothing, because starting the threads costs about as much as the
work. Timings depend on the machine, so a computer with more cores will give a different speed-up.

## Why it matters for the course (GA7)

Comparing every pair is the cost that limits how many submissions the tool can handle. Using
the available processors, and measuring the result, is the resource-efficiency evidence for the
report. A possible further improvement (not built) is to skip pairs that share too few
groups, using an index from group to files, which avoids comparing clearly unrelated pairs.
