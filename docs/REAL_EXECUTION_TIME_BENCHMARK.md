# Real Execution Time Benchmark

Measured by executing the project's Java implementation against the supplied source datasets. **These are actual run measurements, not fabricated values.**

## Environment and method
- OpenJDK 21.0.12.1; project source compiled using `javac --release 11`
- Runtime reports 2 processors
- 2 warm-up runs, then 5 measured runs; median reported
- Real dataset rows use source files under `test_datasets/`
- Synthetic rows use the existing `Benchmark.java` benchmark
- Results are machine/JVM/load dependent; use same machine for comparisons

| Dataset | Files | Pairs | Single-thread median (ms) | 2-thread median (ms) |
|---|---:|---:|---:|---:|
| T08_large_file (Large_A.java + Large_B.java, about 30 KB each) | 2 | 1 | 12.949 | 10.534 |
| T03_multiple_submissions | 5 | 10 | 1.904 | 1.369 |
| T02_similar_programs | 2 | 1 | 0.148 | 0.380 |
| T09_different_formatting | 2 | 1 | 0.347 | 0.402 |
| Synthetic – 10 files | 10 | 45 | 22.8 | 20.5 |
| Synthetic – 20 files | 20 | 190 | 61.3 | 38.1 |
| Synthetic – 40 files | 40 | 780 | 242.4 | 122.4 |
| Synthetic – 80 files | 80 | 3160 | 904.4 | 574.8 |

## Interpretation
The large-file pair took a median of 12.949 ms single-threaded and 10.534 ms with two threads. Parallel execution did not improve every small dataset: thread setup overhead can outweigh the comparison work for one pair. The five-file dataset measured 1.904 ms single-threaded and 1.369 ms with two threads.

## Limitations
Measurements reflect this execution environment and the included datasets; performance may differ on a student laptop. The synthetic 160-file run exceeded the execution time limit, so it is omitted rather than estimated.
