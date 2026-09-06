# Inline `datastore.batchX { }` DSL — Implementation Plan

- **Branch:** `feat/update-batch-read-and-write` (follow-up to `plans/batch-prefbatch-dsl.md`, which stays as history)
- **Status:** Approved design (2026-09-06+)
- **Scope:** `:generic-datastore-preferences`, `:generic-datastore-compose`, `:samples:preferenceComposeApp`, README/docs

## 1. Goal

Replace the `prefBatch { … }` + `batchX(batch)` two-step API with inline builders directly on
`PreferencesDatastore`, reusing the `datastore.string(…)` / `datastore.int(…)` preference
factories (master-style reuse) plus from-scratch declarations — one unified syntax for
read, write, update, and delete:

```kotlin
val text = datastore.string("text", defaultValue = "Hello World!")
val num = datastore.int("num", defaultValue = 0)

// Reuse existing Preference objects
val flow = datastore.batchReadFlowValues {
    add(text)
    add(num)
}
flow.collect { (t: String, n: Int) ->
    state.update { it.copy(text = t, num = n) }
}

// Or declare from scratch — same effect, no pre-built variables
val flow2 = datastore.batchReadFlowValues {
    string("text", defaultValue = "Hello World!")
    int("num", defaultValue = 0)
}
```

**Locked decisions (user-confirmed):**
`batchRead` = `batchReadFlow(…).first()` (same for the `Values` pair); positional destructuring
via generic `componentN` on `BatchValues` (+ `values[handle]` stays the type-safe path);
**Replace** (not coexist) the `batch(batch: PreferenceBatch)` datastore signatures;
**unified declare+operate** for write/update (one receiver with declarations + ops);
full `PrefBuilder` type parity in every inline builder.

## 2. Current state

- HEAD implements `prefBatch { int/string/…/add(BatchPref)/add(Preference) }` → `PreferenceBatch`
  → `batchRead(batch)` / `batchReadFlow(batch)` / `batchWrite(batch)` / `batchUpdate(batch)` /
  `batchDelete(batch)` + blocking variants (`PreferencesDatastore.kt`,
  `GenericPreferencesDatastore.kt`, `preferences/batch/`).
- `master` took `Preference<T>` directly in scopes via `PreferencesAccessor` (`this[pref]`) —
  the reuse behavior this plan brings back in `add(Preference)` + inline-builder form.

## 3. Target API (`PreferencesDatastore` + `GenericPreferencesDatastore`)

`PrefBuilder` itself is the read/delete declare receiver (no new class). Write/update scopes
become `PrefBuilder` subclasses, so every declaration function (primitives, nullable,
`stringList`, `serialized*/kserialized*` + set/list, nullable custom/list, `enum/enumSet/nullableEnum`,
`add(BatchPref)`, `add(Preference)`) is available in every inline block for free.

```kotlin
// READS — declare-only `PrefBuilder` block builds an internal batch, then one snapshot/flow.
// `distinctUntilChanged` comes first so the declaration lambda stays trailing (Kotlin binds a
// bare trailing lambda to the last parameter).
public fun batchReadFlowValues(
    distinctUntilChanged: Boolean = false,
    declare: PrefBuilder.() -> Unit,
): Flow<BatchValues>
public fun <R> batchReadFlow(
    distinctUntilChanged: Boolean = false,
    declare: PrefBuilder.() -> Unit,
    block: BatchValues.() -> R,
): Flow<R>
public suspend fun batchReadValues(declare: PrefBuilder.() -> Unit): BatchValues =
    batchReadFlowValues(declare = declare).first()
public suspend fun <R> batchRead(declare: PrefBuilder.() -> Unit, block: BatchValues.() -> R): R =
    batchReadFlow(declare = declare, block = block).first()

// WRITE / UPDATE — unified single block: declarations + ops in one `edit` transaction
public suspend fun batchWrite(block: BatchWriteScope.() -> Unit)
public suspend fun batchUpdate(block: BatchUpdateScope.() -> Unit)

// DELETE — declare-only, removes every declared key in one `edit` transaction
public suspend fun batchDelete(declare: PrefBuilder.() -> Unit)

// BLOCKING — runBlocking wrappers, same shapes
public fun batchReadBlockingValues(declare: PrefBuilder.() -> Unit): BatchValues
public fun <R> batchReadBlocking(declare: PrefBuilder.() -> Unit, block: BatchValues.() -> R): R
public fun batchWriteBlocking(block: BatchWriteScope.() -> Unit)
public fun batchUpdateBlocking(block: BatchUpdateScope.() -> Unit)
public fun batchDeleteBlocking(declare: PrefBuilder.() -> Unit)
```

Usage:

```kotlin
datastore.batchWrite {
    val t = string("text", "Hello World!") // or: val t = add(textPref)
    set(t, "Hi"); resetToDefault(t)
}
datastore.batchUpdate {
    val n = add(numPref)
    update(n) { it + 1 }                   // get() observes earlier set()s, as today
}
datastore.batchDelete { add(textPref); add(numPref) }
```

- Read projection needs **two** lambdas (`declare` + `BatchValues` project) because the snapshot
  only exists after declarations run; handles cross the boundary via captured `var`s (same
  pattern as today's `prefBatch` handle capture). The `Values` single-lambda form covers the
  common case from the request.
- Write/update ops accept **any** `BatchPref` (membership not enforced, as today), so
  `batchWrite { set(existingHandle, v) }` works with zero re-declaration.

## 4. Core changes (`:generic-datastore-preferences`)

1. `batch/PrefBuilder.kt` — make `open class`, `protected` registry (`prefs`, `keys`,
   `register`); add `internal fun buildBatch(declare: PrefBuilder.() -> Unit): PreferenceBatch`.
   `prefBatch()`, `PreferenceBatch`, `BatchPref` subtypes: unchanged and public.
2. `batch/BatchWriteScope.kt` / `batch/BatchUpdateScope.kt` — extend `PrefBuilder`
   (keep class names, ops, `internal constructor(mutablePreferences)`); ops unchanged
   (`set/get/update/delete/resetToDefault` on `BatchPref<T>`).
3. `batch/BatchValues.kt` — add `toList()` (declaration order) + generic
   `component1()..component10()` (unchecked cast, declaration order) for
   `collect { (t: String, n: Int) -> … }` / `val (t: String, n: Int) = values`.
   Document `values[handle]` as the type-safe path; destructuring is order-sensitive.
4. `PreferencesDatastore.kt` / `GenericPreferencesDatastore.kt` — delete the 11
   `batchX(batch: PreferenceBatch, …)` members; add the 12 inline members above.
   Mechanics unchanged: reads via `dataOrEmpty.map { BatchValues(…) }` (+`distinctUntilChanged`),
   one-shot = `.first()`; write/update = one `edit { Scope(it).block() }`;
   delete = one `edit { batch.forEach { it.removeFrom(…) } }`; blocking = `runBlocking`.
5. Storage format parity: untouched (same `BatchPref` subclasses / serializers).

## 5. Compose (`:generic-datastore-compose`)

- `RememberBatchRead` (expect + android/jvm/ios actuals): `batch: PreferenceBatch` param →
  `declare: PrefBuilder.() -> Unit` (+ projection overload). Impl remembers the flow on the
  datastore (`remember(this) { batchReadFlowValues(declare = declare) }`) and collects; `declare` must be
  stable (static keys/defaults) — documented.
- `RememberPreferences.kt` / `RememberPreferencesLocal.kt` (2–5 prefs): signatures unchanged;
  handles via `remember(*prefs) { prefBatch { … } }` + positional cast (as today); observation via
  `key(prefs) { rememberBatchRead({ prefs.forEach { add(it) } }, context) }` so pref changes refresh.
- `BatchPrefsComposeState.kt`: drop `writeBatch` field; write via
  `datastore.batchWrite { set(handle, value) }`.

## 6. Samples

- `PreferenceStore.kt`: `mainBatch`/`textHandle/…` init via `prefBatch` stays for handle
  adaptation (or captured from inline declares); `batchWriteBlock` takes
  `BatchWriteScope.() -> Unit` (unified); `runApiCoverageShowcase()` rewritten to inline
  reads/writes/updates/deletes + blocking.
- `MainViewModel.kt`: `randomize()` → `batchWrite { set(…) … }`.

## 7. Tests

- `AbstractBatchOperationsTest` / `AbstractBatchOperationsBlockingTest`: mechanical migration —
  drop `batch` args; reads use `batchReadValues({ batch.forEach { add(it) } })` / two-lambda
  projections; writes/updates use ops directly (`set(handle, …)`, no re-declaration needed);
  deletes use `batchDelete { batch.forEach { add(it) } }`; duplicate/blank-key tests exercise
  inline builders too. Add: `componentN` order/type tests, `toList()` test.
- `AbstractBatchPerformanceTest`: re-port timing loops to inline DSL (still `@Ignore`d).
- `AbstractBatchPrefsComposeStateTest`: `handleFor`/`currentSnapshot` via inline reads;
  `CountingPreferencesDatastore` overrides `batchWrite(block: BatchWriteScope.() -> Unit)`.
- `AbstractRememberPreferencesAndBatchReadTest`: `rememberBatchRead` via declare lambdas.
- Platform shims (`androidDeviceTest` / `jvmTest` / `iosSimulatorArm64Test`): unchanged names.

## 8. Docs

- README batch + compose-batch sections and API table rows → inline signatures/examples.
- `docs/batch-performance-results.md`: terminology refresh.
- This file is the plan of record.

## 9. Verification

```bash
./gradlew :generic-datastore-preferences:compileAndroidMain
./gradlew :generic-datastore-preferences:compileAndroidDeviceTest
./gradlew :generic-datastore-preferences:compileKotlinJvm :generic-datastore-preferences:compileTestKotlinJvm
./gradlew :generic-datastore-preferences:jvmTest
./gradlew :generic-datastore-preferences:iosSimulatorArm64Test
./gradlew :generic-datastore-compose:compileAndroidMain :generic-datastore-compose:compileAndroidDeviceTest
./gradlew :generic-datastore-compose:compileKotlinJvm :generic-datastore-compose:compileTestKotlinJvm
./gradlew :generic-datastore-compose:jvmTest
./gradlew spotlessApply
```

No emulator assumed (Android device tests compile-only). All `jvmTest` suites green before merge.
