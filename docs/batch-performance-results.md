# Batch vs Normal Operations — Performance Results

Performance comparison between individual DataStore operations and batch operations,
measured on Desktop/JVM (JUnit 5) using `kotlin.time.measureTime`.

> **Environment:** macOS (darwin, Apple Silicon), JVM desktop target, `StandardTestDispatcher`,
> single test run on 2026-09-06, inline `datastore.batchX { … }` API.
> Timings include DataStore I/O overhead (file reads/writes). Absolute timings are
> machine-dependent and speedup ratios vary run to run; treat them as orders of magnitude,
> not exact figures.

## Summary

| Operation            | Count | Normal (total) | Batch (total) | Speedup |
|----------------------|------:|---------------:|--------------:|--------:|
| **Write**            |     5 |        2.40 ms |     463.08 µs |  5.17x  |
| **Write**            |    10 |        5.29 ms |     473.92 µs | 11.15x  |
| **Write**            |    25 |       13.68 ms |     591.42 µs | 23.12x  |
| **Write**            |    50 |       65.80 ms |       1.37 ms | 48.16x  |
| **Write (mixed)**    |     4 |       44.35 ms |     915.75 µs | 48.43x  |
| **Read**             |     5 |      200.92 µs |      76.63 µs |  2.62x  |
| **Read**             |    10 |      582.21 µs |     900.17 µs |  0.64x  |
| **Read**             |    25 |      853.67 µs |     163.46 µs |  5.22x  |
| **Read**             |    50 |        1.50 ms |     314.08 µs |  4.78x  |
| **Read (mixed)**     |     4 |      147.38 µs |      74.08 µs |  1.98x  |
| **Update**           |     5 |        1.78 ms |     354.75 µs |  5.00x  |
| **Update**           |    10 |        5.48 ms |       1.19 ms |  4.59x  |
| **Update**           |    25 |       13.52 ms |     486.75 µs | 27.77x  |
| **Delete**           |    10 |        8.62 ms |     751.04 µs | 11.47x  |
| **ResetToDefault**   |    10 |        6.84 ms |     755.04 µs |  9.06x  |

## Key Findings

### Write operations scale dramatically

Batch writes become more impactful as the number of preferences grows. Each normal `set()` call
triggers a separate DataStore `edit` transaction (read file → modify → write file), while
`batchWrite` collapses all writes into **one transaction**.

| Preferences | Normal per-op | Batch per-op | Speedup |
|------------:|--------------:|-------------:|--------:|
|           5 |     479.56 µs |     92.62 µs |  5.17x  |
|          10 |     528.83 µs |     47.39 µs | 11.15x  |
|          25 |     547.06 µs |     23.66 µs | 23.12x  |
|          50 |       1.32 ms |     27.33 µs | 48.16x  |

At 50 preferences, batch is **~48x faster** because it performs 1 file write instead of 50.

### Read operations benefit from shared snapshots

Each normal `get()` call independently reads from the DataStore flow. `batchReadValues` takes a
**single snapshot** and reads all values from it in-memory.

| Preferences | Normal per-op | Batch per-op | Speedup |
|------------:|--------------:|-------------:|--------:|
|           5 |      40.18 µs |     15.33 µs |  2.62x  |
|          10 |      58.22 µs |     90.02 µs |  0.64x  |
|          25 |      34.15 µs |      6.54 µs |  5.22x  |
|          50 |      30.04 µs |      6.28 µs |  4.78x  |

At 50 preferences, batch reads are **~5x faster**. Small reads are noisy: at 10 preferences both
sides are sub-millisecond and a repeat run measured 0.46x–0.64x, i.e. a wash — the fixed cost of
building the `BatchValues` map dominates when there is almost no I/O to save. Reads win
decisively once the snapshot replaces many independent flow collections.

### Update operations: compounded savings

`batchUpdate` combines the read + write savings into a single atomic transaction:

| Preferences | Normal per-op | Batch per-op | Speedup |
|------------:|--------------:|-------------:|--------:|
|           5 |     355.41 µs |     70.95 µs |  5.00x  |
|          10 |     548.10 µs |    119.30 µs |  4.59x  |
|          25 |     540.73 µs |     19.47 µs | 27.77x  |

### Mixed-type operations

Writing 4 preferences of different types (String, Int, Boolean, Long) in a single batch is
**~48x faster** than writing them individually (44.35 ms vs 915.75 µs total). Reading the same
4 back in one snapshot is **~2x faster** (147.38 µs vs 74.08 µs total).

## When to use batch operations

Every batch operation takes an inline `datastore.batchX { … }` declaration (`add(pref)` reuse or
`string(…)`/`int(…)` from scratch); the measurements above apply equally to the inline API.

| Scenario                              | Recommendation                     |
|---------------------------------------|------------------------------------|
| Reading/writing 1–2 preferences       | Normal operations are fine         |
| Reading/writing 3+ preferences        | Use `batchReadValues`/`batchWrite` |
| Read-modify-write on multiple values  | Use `batchUpdate`                  |
| Resetting or deleting multiple prefs  | Use `batchDelete` or `batchWrite`  |
| UI settings screens saving all fields | Use `batchWrite`                   |

## How to reproduce

`DesktopBatchPerformanceTest` is `@Ignore`d at class level (benchmark timings are noisy and
machine-dependent), so temporarily remove the `@Ignore` first, then run the performance test
suite on Desktop/JVM:

```bash
./gradlew :generic-datastore-preferences:jvmTest \
  --tests "io.github.arthurkun.generic.datastore.preferences.batch.DesktopBatchPerformanceTest"
```

Test output is captured in the JUnit XML report:

```
generic-datastore-preferences/build/test-results/jvmTest/
  TEST-io.github.arthurkun.generic.datastore.preferences.batch.DesktopBatchPerformanceTest.xml
```
