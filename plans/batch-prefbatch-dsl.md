# Batch Operations Redesign — Declarative `prefBatch` DSL

- **Branch:** `feat/update-batch-read-and-write`
- **Status:** Approved design (2026-09-06)
- **Scope:** `:generic-datastore-preferences`, `:generic-datastore-compose`, `:samples:preferenceComposeApp`, README/docs

## 1. Overview

Reimplement batch operations around a declarative DSL. A `prefBatch { ... }` builder declares a
batch of preferences — one function per supported type, keyed by `String`, carrying a default — and
every batch operation consumes that same declaration:

- **Read** maps every declared pref to its stored value (or default) in a single `dataOrEmpty`
  emission (`Flow<BatchValues>` or one-shot).
- **Write / Update / Delete** apply to the declared prefs inside a single `datastore.edit { }`
  transaction.

Everything works directly against the raw `Preferences` / `MutablePreferences` API — one snapshot
read or one transaction per operation, never per-key datastore calls.

**Decision (user-confirmed):** the existing scope-based batch API is **removed**, not deprecated.
The API is alpha; breaking changes are acceptable.

## 2. Current state (what exists today)

Batch code lives in `generic-datastore-preferences/src/commonMain/.../preferences/batch/`:

- `BatchReadScope.kt` / `BatchWriteScope.kt` / `BatchUpdateScope.kt` — scopes wrapping a
  `Preferences` snapshot or `MutablePreferences`; their operators take `Preference<T>` and cast to
  the internal `PreferencesAccessor<T>` (`readFrom` / `writeInto` / `removeFrom`).
- `PreferencesAccessor.kt` — internal interface + `@PreferencesBatchDsl` annotation. Implemented by
  every internal preference item class (`GenericPreferenceItem`, `CustomGenericPreferenceItem`,
  `CustomSetGenericPreferenceItem`, nullable variants) and by `PreferenceImpl` / `MappedPrefs`.
- `preferences/utils/Extensions.kt` — internal `DataStore<Preferences>.dataOrEmpty` flow
  (`IOException` → `emptyPreferences()`).
- `PreferencesDatastore` interface + `GenericPreferencesDatastore` expose:
  `batchReadFlow(distinctUntilChanged, block)`, `batchRead(block)`, `batchWrite(block)`,
  `batchUpdate(block)`, `batchReadBlocking(block)`, `batchWriteBlocking(block)`,
  `batchUpdateBlocking(block)`.

The current implementation already operates on raw snapshots; the replacement changes the **API
shape** (declarative batch declaration consumed by every operation, whole-batch delete, key-first
declarations that don't require pre-built `Preference` objects) and adds the missing
`stringList` / `nullableStringList` types.

No batch references exist outside `:generic-datastore-preferences`, `:generic-datastore-compose`,
and `samples/preferenceComposeApp` (verified by grep).

## 3. Target API

All new batch code is common code in package
`io.github.arthurkun.generic.datastore.preferences.batch` (no expect/actual needed).

### 3.1 `BatchPref.kt` — typed declaration handles

```kotlin
public sealed class BatchPref<T>(
    public val key: String,
    public val defaultValue: T,
) {
    // Internal raw access — the only bridge to the DataStore snapshot/transaction.
    internal abstract fun readFrom(preferences: Preferences): T
    internal abstract fun writeTo(mutablePreferences: MutablePreferences, value: T)
    internal abstract fun removeFrom(mutablePreferences: MutablePreferences)
}
```

Internal data-class subclasses (equals/hashCode on key + default so `distinctUntilChanged` and
map lookups behave) mirror each storage strategy **exactly** as the corresponding single
preference class does:

| Subclass | Backing storage | Absent read | Decode failure | Null write |
|---|---|---|---|---|
| `BatchTypedPref<T>` | typed `Preferences.Key<T>` (`intPreferencesKey`, `longPreferencesKey`, `floatPreferencesKey`, `doublePreferencesKey`, `booleanPreferencesKey`, `stringPreferencesKey`, `stringSetPreferencesKey`) | default | n/a | n/a |
| `BatchNullableTypedPref<T : Any>` | same typed keys | `null` | n/a | removes key |
| `BatchCustomPref<T>` | `stringPreferencesKey` + `(T) -> String` / `(String) -> T` | default | default | n/a |
| `BatchNullableCustomPref<T : Any>` | `stringPreferencesKey` | `null` | `null` | removes key |
| `BatchSetPref<T>` | `stringSetPreferencesKey` + per-element (de)serializers | default | failing elements skipped | n/a |
| `PreferenceBatchAdapter<T>` | wraps an existing `Preference<T>`, delegates to internal `PreferencesAccessor`; throws `IllegalStateException("Batch operations only support preferences created by this library")` for foreign implementations | via wrapped pref | via wrapped pref | via wrapped pref |

List declarations (`stringList`, `serializedList`, `kserializedList` and their nullable
variants) reuse `BatchCustomPref` / `BatchNullableCustomPref`: the list storage format (JSON
array of string-wrapped elements) is implemented by the serializer/deserializer lambdas passed
at the `PrefBuilder` call site. There is intentionally no `BatchNullableSetPref` — the
single-preference API has no nullable custom-set type either, so batch parity stops at
`serializedSet` / `kserializedSet` plus the nullable list variants.

Equality is on concrete subclass + key + default (lambdas are behavior, not state, and are
excluded — see `BatchPref` KDoc); `hashCode` additionally folds in the concrete class.

`CancellationException` is always rethrown (matches existing behavior).

### 3.2 `PrefBuilder.kt` — the DSL

```kotlin
@PreferencesBatchDsl
public class PrefBuilder internal constructor() {
    // one function per entry in the Types table below; each creates, registers and RETURNS
    // the typed handle so callers can capture it for typed snapshot access
    public fun build(): PreferenceBatch
}

public fun prefBatch(block: PrefBuilder.() -> Unit): PreferenceBatch =
    PrefBuilder().apply(block).build()
```

Rules:

- Duplicate keys and blank keys throw `IllegalArgumentException` at declaration time.
- Builder functions return the created `BatchPref<T>`; the recommended typed-access pattern is
  capturing the returned handles (e.g. into properties of a holder class).
- `add(pref: BatchPref<T>)` re-registers an existing handle; `add(pref: Preference<T>)` wraps an
  existing library-created `Preference<T>` in `PreferenceBatchAdapter` — this keeps the compose
  `rememberPreferences(pref1..pref5)` API working unchanged.

#### Types table (complete builder surface)

| Category | Functions | Storage | Absent / failure semantics |
|---|---|---|---|
| Primitives | `int(key, default = 0)`, `long(key, default = 0L)`, `float(key, default = 0f)`, `double(key, default = 0.0)`, `bool(key, default = false)`, `string(key, default = "")`, `stringSet(key, default = emptySet())` | native typed keys | default |
| New primitives | `stringList(key, default = emptyList())`, `nullableStringList(key)` | `stringPreferencesKey`, JSON array of strings | default / null; bad elements skipped |
| Nullable primitives | `nullableInt`, `nullableLong`, `nullableFloat`, `nullableDouble`, `nullableBool`, `nullableString`, `nullableStringSet` (key-only, default is always `null`) | native typed keys | `null`; writing `null` removes |
| Custom | `serialized<T>(key, default, serializer, deserializer)`, `serializedSet<T>(key, default = emptySet(), elemSer, elemDeser)`, `serializedList<T>(key, default = emptyList(), elemSer, elemDeser)` | string key / string-set key / string key (JSON array) | default; set & list skip failing elements |
| Kotlinx serialization | `kserialized<T>(key, default, serializer: KSerializer<T>, json: Json? = null)` + `inline reified` overload; same for `kserializedSet`, `kserializedList` | string key (JSON) / string-set key / string key (JSON array) | default; sets & lists skip failing elements |
| Nullable custom | `nullableSerialized<T : Any>(key, serializer, deserializer)`, `nullableKserialized<T : Any>(key, serializer, json)` + reified, `nullableSerializedList<T>(key, elemSer, elemDeser)`, `nullableKserializedList<T>(key, serializer: KSerializer<T>, json)` + reified | string key / JSON array | `null`; writing `null` removes |
| Enums | `enum<E : Enum<E>>(key, default: E)`, `enumSet<E>(key, default: Set<E> = emptySet())`, `nullableEnum<E>(key)` | string key (`Enum.name`) / string-set key / string key | default on unknown name / skipped / `null` |

`kserialized*` with `json = null` resolves at call time the same way single preferences do — the
batch DSL has no datastore reference, so the default is `PreferenceDefaults.defaultJson` when the
caller omits `json` (documented on each function).

### 3.3 `PreferenceBatch.kt`

```kotlin
public class PreferenceBatch internal constructor(
    internal val entries: List<BatchPref<*>>,
) : List<BatchPref<*>> by entries
```

### 3.4 `BatchValues.kt` — read result (replaces `BatchReadScope.kt`)

```kotlin
public class BatchValues internal constructor(
    private val preferences: Preferences,
    private val batch: PreferenceBatch,
) {
    private val values: Map<BatchPref<*>, Any?> = batch.associateWith { it.readFrom(preferences) }

    /** Typed access. */
    public operator fun <T> get(pref: BatchPref<T>): T
    /** Whole-batch view (the `associateWith` mapping from the spec). */
    public fun toMap(): Map<BatchPref<*>, Any?>
    // equals/hashCode derived from `values` so distinctUntilChanged works on Flow<BatchValues>
}
```

Deliberately **not** a `Map` subtype: `Map.get(BatchPref<*>)` and a typed
`get(BatchPref<T>): T` would be a JVM signature clash. `toMap()` provides the map view.

### 3.5 `BatchWriteScope.kt` / `BatchUpdateScope.kt` — reworked parameters

Same class names and semantics as today, but operators take `BatchPref<T>` instead of
`Preference<T>`:

- `BatchWriteScope`: `set(pref, value)`, indexing operator `this[pref] = value`, `delete(pref)`,
  `resetToDefault(pref)`.
- `BatchUpdateScope`: everything above plus `get(pref)` and `update(pref) { transform }`; reads
  observe writes made earlier in the same transaction.

Batch membership is **not** enforced — the scopes operate on any `BatchPref` handles (documented).

### 3.6 Operations on `PreferencesDatastore` / `GenericPreferencesDatastore`

Replaces all seven existing batch members:

```kotlin
public suspend fun <R> batchRead(batch: PreferenceBatch, block: BatchValues.() -> R = { this }): R
public fun <R> batchReadFlow(
    batch: PreferenceBatch,
    distinctUntilChanged: Boolean = false,
    block: BatchValues.() -> R = { this },
): Flow<R>
public suspend fun batchWrite(batch: PreferenceBatch, block: BatchWriteScope.() -> Unit)
public suspend fun batchUpdate(batch: PreferenceBatch, block: BatchUpdateScope.() -> Unit)
public suspend fun batchDelete(batch: PreferenceBatch)
public fun <R> batchReadBlocking(batch: PreferenceBatch, block: BatchValues.() -> R = { this }): R
public fun batchWriteBlocking(batch: PreferenceBatch, block: BatchWriteScope.() -> Unit)
public fun batchUpdateBlocking(batch: PreferenceBatch, block: BatchUpdateScope.() -> Unit)
public fun batchDeleteBlocking(batch: PreferenceBatch)
```

Implementation mechanics (per the spec's "Backed by" column):

- `batchReadFlow` = `datastore.dataOrEmpty.map { prefs -> BatchValues(prefs, batch).block() }`,
  optionally `distinctUntilChanged()`; `batchRead` = `batchReadFlow(...).first()`.
- `batchWrite` / `batchUpdate` = one `datastore.edit { prefs -> Scope(prefs).block() }`.
- `batchDelete` = one `datastore.edit { prefs -> batch.forEach { it.removeFrom(prefs) } }`.
- Blocking variants wrap the suspend calls in `runBlocking` (as today).

### 3.7 New single-preference factories

Added to `PreferencesDatastore` + `GenericPreferencesDatastore` so single preferences and batches
stay storage-interoperable:

```kotlin
public fun stringList(key: String, defaultValue: List<String> = emptyList()): Preference<List<String>>
// = SerializedListPrimitive(datastore, key, default, serializer = { it }, deserializer = { it })

public fun nullableStringList(key: String): Preference<List<String>?>
// = NullableSerializedListPrimitive(datastore, key, { it }, { it })
```

## 4. Internal implementation notes

1. **Format parity is critical.** Batch reads/writes must produce byte-identical storage to the
   single-preference classes. Extract the shared logic into
   `preferences/utils/Serialization.kt` (internal top-level helpers): `safeDeserialize`
   (fallback-to-default and fallback-to-null variants), set-element safe mapping, and the JSON
   list encode/decode. Refactor `CustomGenericPreferenceItem`, `CustomSetGenericPreferenceItem`,
   `SerializedListPrimitive`, and the nullable custom bases to use them; the new `BatchPref`
   subclasses use the same helpers.
2. `PreferencesAccessor.kt` keeps the internal `PreferencesAccessor<T>` interface (used by item
   classes and the new adapter) and the `@PreferencesBatchDsl` annotation (now applied to
   `PrefBuilder` and the write/update scopes).
3. `utils/Extensions.kt` (`dataOrEmpty`) is unchanged and continues to back reads.
4. Style: no wildcard imports; `kotlinx.coroutines.IO` (not `Dispatchers.IO`) if a dispatcher is
   ever needed (batch code shouldn't need one).

## 5. Breaking changes (removals)

From `PreferencesDatastore` / `GenericPreferencesDatastore`:

- `batchReadFlow(distinctUntilChanged, block: BatchReadScope.() -> R)`
- `batchRead(block: BatchReadScope.() -> R)`
- `batchWrite(block: BatchWriteScope.() -> Unit)`
- `batchUpdate(block: BatchUpdateScope.() -> Unit)`
- `batchReadBlocking(block)`, `batchWriteBlocking(block)`, `batchUpdateBlocking(block)`

Files removed: `batch/BatchReadScope.kt`. Scope operator signatures on `BatchWriteScope` /
`BatchUpdateScope` change from `Preference<T>` to `BatchPref<T>`.

## 6. Compose module (`:generic-datastore-compose`)

- `RememberBatchRead.kt` (commonMain expect + android/ios/jvm actuals):

  ```kotlin
  @Composable
  public expect fun <R> PreferencesDatastore.rememberBatchRead(
      batch: PreferenceBatch,
      context: CoroutineContext = EmptyCoroutineContext,
      block: BatchValues.() -> R = { this },
  ): State<R?>
  ```

  Android actual collects via `collectAsStateWithLifecycle(initialValue = null)`, JVM/iOS via
  `collectAsState(initial = null)` (same split as today).

- `RememberPreferences.kt` / `RememberPreferencesLocal.kt`: **public signatures unchanged** (still
  take two-to-five `Preference<T>` objects). Internally: `remember(pref1, ..., policy) {
  prefBatch { add(pref1); add(pref2); ... } }`, then `rememberBatchRead(batch, context)`, then the
  existing `PreferencesState2..5` of per-preference states. `MainScreen.kt` in the sample keeps
  working without changes.

- `BatchPrefsComposeState.kt`: holds the `BatchPref<T>` handle resolved from the batch (the value
  returned by `add(preference)`), reads via `batchState.value?.get(handle) ?: preference.defaultValue`,
  writes via `scope.launch { datastore.batchWrite(batch) { set(handle, value) } }`. Optimistic
  override + `SnapshotMutationPolicy` logic unchanged.

- `PreferencesState.kt`, `LocalPreferencesDatastore.kt` (`ProvidePreferencesDatastore`): unchanged.

## 7. Samples (`:samples:preferenceComposeApp`)

- `domain/PreferenceStore.kt`: `batchWriteBlock(block)` helper and `runApiCoverageShowcase()`
  rewritten against the new API — declare a `prefBatch { ... }` (including `add(...)` of existing
  preferences), then exercise `batchRead`, `batchReadFlow`, `batchWrite`, `batchUpdate`,
  `batchDelete`, and the blocking variants.
- `ui/MainViewModel.kt`: `randomize()` uses `batchWrite(batch) { set(...) ... }`.
- `ui/MainScreen.kt`: unchanged (`rememberPreferences` signature preserved).

## 8. Tests

Rewrite the **contents** of the three abstract classes (names kept so the existing platform shims
in `androidDeviceTest` / `jvmTest` / `iosSimulatorArm64Test` continue to compile unchanged):

- `AbstractBatchOperationsTest.kt` (suspend, `runTest(testDispatcher)`):
  - Builder: every Types-table function; implicit primitive defaults; explicit defaults;
    duplicate-key and blank-key `IllegalArgumentException`; `add(BatchPref)` and
    `add(Preference)` aggregation; declaration order preserved.
  - Read: defaults when absent / stored values, per storage family; nullable semantics;
    decode-failure fallbacks (default for customs, `null` for nullable customs, element-skip for
    sets/lists, malformed array → default); single-emission consistency.
  - `batchReadFlow`: emits on change; `distinctUntilChanged` suppresses equal `BatchValues`.
  - Write: multiple `set`s, `delete`, `resetToDefault`, and null-write-removes-key inside one
    transaction; intra-transaction effects visible to `batchUpdate` reads.
  - `batchDelete`: every declared key removed; subsequent reads return defaults.
  - **Storage interop** (key correctness): values written via batch read back through the
    equivalent single `Preference` (`datastore.int(...)` etc.) and vice versa, for all storage
    families; raw-format assertion that `stringList` stores a JSON array of strings
    (via `dataStore.data.first()` / `dataStore.edit`).
  - Mapped preferences (`mapIO`) work through `add(pref)`.
- `AbstractBatchOperationsBlockingTest.kt`: blocking variants of the above.
- `AbstractBatchPerformanceTest.kt`: re-port the batch-vs-individual timing comparisons
  (sizes 5/10/25/50) to the new DSL; remains `@Ignore`d and desktop-only.

Compose tests:

- `AbstractBatchPrefsComposeStateTest.kt`: fakes switch from `mutableStateOf<BatchReadScope?>` to
  `mutableStateOf<BatchValues?>` (snapshots built via `batchRead(prefBatch { add(pref) }) { this }`);
  the counting/failing datastore delegates override `batchWrite(batch, block)`.
- `AbstractRememberPreferencesAndBatchReadTest.kt`: `rememberPreferences` tests unchanged in
  structure; `rememberBatchRead` tests use the new `batch` parameter (handles captured from
  `prefBatch { add(...) }` for typed assertions).

## 9. Docs

- README: rewrite the batch section (~lines 540–690) and the compose batch docs (~1189–1213);
  update the API table row (~line 185) to the new member list.
- `docs/batch-performance-results.md`: refresh terminology after re-running the perf test.
- This file is the implementation plan of record (`plans/batch-prefbatch-dsl.md`).

## 10. Implementation order

1. Write this plan (done — this file).
2. `preferences/utils/Serialization.kt` helpers + refactor item classes onto them (no behavior
   change; existing tests must stay green).
3. `batch/BatchPref.kt`, `batch/PreferenceBatch.kt`, `batch/PrefBuilder.kt`, `batch/BatchValues.kt`;
   rework `BatchWriteScope.kt` / `BatchUpdateScope.kt`; delete `BatchReadScope.kt`.
4. `PreferencesDatastore.kt` / `GenericPreferencesDatastore.kt`: swap batch members, add
   `stringList` / `nullableStringList` factories.
5. Rewrite the three abstract batch test classes; verify shims compile.
6. Compose module rework + compose test rework.
7. Samples update.
8. README + docs refresh.
9. Full verification (below).

## 11. Verification

```bash
# Preferences module
./gradlew :generic-datastore-preferences:compileAndroidMain
./gradlew :generic-datastore-preferences:compileAndroidDeviceTest
./gradlew :generic-datastore-preferences:compileKotlinJvm :generic-datastore-preferences:compileTestKotlinJvm
./gradlew :generic-datastore-preferences:jvmTest
./gradlew :generic-datastore-preferences:iosSimulatorArm64Test   # macOS host available

# Compose module
./gradlew :generic-datastore-compose:compileAndroidMain :generic-datastore-compose:compileAndroidDeviceTest
./gradlew :generic-datastore-compose:compileKotlinJvm :generic-datastore-compose:compileTestKotlinJvm
./gradlew :generic-datastore-compose:jvmTest

# Style
./gradlew spotlessApply
```

No Android emulator is assumed — instrumentation test source sets are compiled, not run. All
`jvmTest` and `iosSimulatorArm64Test` suites must pass before merge.

## 12. Implementation outcome (2026-09-06)

Implemented as specified, with two internal simplifications that preserve all semantics:

- The internal `BatchPref` subclasses were collapsed from nine to six:
  `BatchTypedPref`, `BatchNullableTypedPref`, `BatchCustomPref`, `BatchNullableCustomPref`,
  `BatchSetPref`, and `PreferenceBatchAdapter`. The list strategies from section 3.1
  (`BatchListPref` / `BatchNullableListPref`) are expressed as `BatchCustomPref<List<T>>` /
  `BatchNullableCustomPref<List<T>>` with `serializeList` / `deserializeList` codecs — the same
  lambda-based collapse the single-preference classes already use (`ObjectPrimitive` →
  `CustomGenericPreferenceItem`). `BatchNullableSetPref` was dropped because no nullable set
  custom exists in the single-preference API (`nullableStringSet` is a typed key).
- The read operations expose two overloads instead of a defaulted block parameter
  (`batchRead(batch)` / `batchRead(batch) { block }`), because a defaulted `block = { this }`
  cannot drive the generic type parameter's inference. The no-block forms are interface members
  with default implementations.

Verification on this machine: all main/test source sets compile (Android + JVM for both modules),
394 JVM tests pass with 0 failures (15 `@Ignore`d performance tests skipped), `spotlessCheck`
clean. `iosSimulatorArm64Test` could not run: this host has only the Xcode Command Line Tools
(`xcrun xcodebuild` unavailable), a pre-existing environment limitation; iOS main source sets
compile.
