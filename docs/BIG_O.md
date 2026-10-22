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

## Measured (generated files, about 120 statements each)

These numbers were measured in the assistant's 2-processor test environment, not on a group
member's computer. Run `Benchmark` on your own machine and replace this table with your
results before they go in the report.

| Files | Pairs | 1 thread (ms) | 2 threads (ms) | Speed-up |
|---|---|---|---|---|
| 10 | 45 | 36.0 | 29.9 | 1.21x |
| 20 | 190 | 93.2 | 50.4 | 1.85x |
| 40 | 780 | 334.7 | 190.7 | 1.75x |
| 80 | 3160 | 1397.2 | 790.0 | 1.77x |
| 160 | 12720 | 5518.0 | 3109.4 | 1.77x |

Reading the table: from 40 to 80 to 160 files the single-thread time grows 4.2x then 3.9x, which
matches O(n²). Two threads give roughly 1.8x. The small case (10 files) gains little because the
thread start-up time is comparable to the work.

## Why it matters for the course (GA7)

Comparing every pair is the cost that limits how many submissions the tool can handle. Using
the available processors, and measuring the result, is the resource-efficiency evidence for the
report. A possible further improvement (not built) is to skip pairs that share too few
groups, using an index from group to files, which avoids comparing clearly unrelated pairs.
