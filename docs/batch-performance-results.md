# Batch vs Normal Operations — Performance Results

Performance comparison between individual DataStore operations and batch operations,
measured on Desktop/JVM (JUnit 5) using `kotlin.time.measureTime`.

> **Environment:** Windows 10, JVM desktop target, `StandardTestDispatcher`, single test run.
> Timings include DataStore I/O overhead (file reads/writes). Results may vary across runs.

## Summary

| Operation            | Count | Normal (total) | Batch (total) | Speedup |
|----------------------|------:|---------------:|--------------:|--------:|
| **Write**            |     5 |      254.46 ms |      83.49 ms |  3.05x  |
| **Write**            |    10 |      257.88 ms |      21.32 ms | 12.10x  |
| **Write**            |    25 |      728.96 ms |      23.48 ms | 31.05x  |
| **Write**            |    50 |     1459.24 ms |      26.81 ms | 54.42x  |
| **Write (mixed)**    |     7 |      631.82 ms |      32.28 ms | 19.57x  |
| **Read**             |     5 |        5.64 ms |       0.46 ms | 12.33x  |
| **Read**             |    10 |       70.84 ms |       4.99 ms | 14.18x  |
| **Read**             |    25 |       15.68 ms |       0.47 ms | 33.08x  |
| **Read**             |    50 |      115.35 ms |       1.82 ms | 63.52x  |
| **Read (mixed)**     |     7 |        7.87 ms |       0.63 ms | 12.49x  |
| **Update**           |     5 |      209.53 ms |      45.91 ms |  4.56x  |
| **Update**           |    10 |      219.97 ms |      30.94 ms |  7.11x  |
| **Update**           |    25 |      815.90 ms |      22.69 ms | 35.95x  |
| **Delete**           |    10 |      230.95 ms |      17.28 ms | 13.36x  |
| **ResetToDefault**   |    10 |      196.04 ms |      24.18 ms |  8.11x  |

## Key Findings

### Write operations scale dramatically

Batch writes become more impactful as the number of preferences grows. Each normal `set()` call
triggers a separate DataStore `edit` transaction (read file → modify → write file), while
`batchWrite` collapses all writes into **one transaction**.

| Preferences | Normal per-op | Batch per-op | Speedup |
|------------:|--------------:|-------------:|--------:|
|           5 |      50.89 ms |     16.70 ms |  3.05x  |
|          10 |      25.79 ms |      2.13 ms | 12.10x  |
|          25 |      29.16 ms |      0.94 ms | 31.05x  |
|          50 |      29.18 ms |      0.54 ms | 54.42x  |

At 50 preferences, batch is **~54x faster** because it performs 1 file write instead of 50.

### Read operations benefit from shared snapshots

Each normal `get()` call independently reads from the DataStore flow. `batchGet` takes a
**single snapshot** and reads all values from it in-memory.

| Preferences | Normal per-op | Batch per-op | Speedup |
|------------:|--------------:|-------------:|--------:|
|           5 |       1.13 ms |     91.48 μs | 12.33x  |
|          10 |       7.08 ms |    499.46 μs | 14.18x  |
|          25 |     627.15 μs |     18.96 μs | 33.08x  |
|          50 |       2.31 ms |     36.32 μs | 63.52x  |

At 50 preferences, batch reads are **~64x faster**.

### Update operations: compounded savings

`batchUpdate` combines the read + write savings into a single atomic transaction:

| Preferences | Normal per-op | Batch per-op | Speedup |
|------------:|--------------:|-------------:|--------:|
|           5 |      41.91 ms |      9.18 ms |  4.56x  |
|          10 |      22.00 ms |      3.09 ms |  7.11x  |
|          25 |      32.64 ms |      0.91 ms | 35.95x  |

### Mixed-type operations

Writing 7 preferences of different types (String, Int, Boolean, Long, Float, Double, Set\<String\>)
in a single batch is **~20x faster** than writing them individually.

## When to use batch operations

| Scenario                              | Recommendation               |
|---------------------------------------|------------------------------|
| Reading/writing 1–2 preferences       | Normal operations are fine   |
| Reading/writing 3+ preferences        | Use `batchGet`/`batchWrite`  |
| Read-modify-write on multiple values  | Use `batchUpdate`            |
| Resetting or deleting multiple prefs  | Use `batchWrite`             |
| UI settings screens saving all fields | Use `batchWrite`             |

## How to reproduce

Run the performance test suite on Desktop/JVM:

```bash
./gradlew :generic-datastore:desktopTest \
  --tests "io.github.arthurkun.generic.datastore.preferences.batch.DesktopBatchPerformanceTest"
```

Test output is captured in the JUnit XML report:

```
generic-datastore/build/test-results/desktopTest/
  TEST-io.github.arthurkun.generic.datastore.preferences.batch.DesktopBatchPerformanceTest.xml
```
