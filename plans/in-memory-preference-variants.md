# In-Memory Preference Variants — Implementation Plan

- **Branch:** `feat/create-in-memory-preference-variants`
- **Scope:** `:generic-datastore-preferences` (commonMain `core/mem/`, `PreferenceImpl`, `PreferencesDatastore`, `GenericPreferencesDatastore`, `PreferenceApi`), tests, README/AGENTS.md, API dump files

## 1. Goal

Add in-memory variants of every preference factory so callers can keep some preferences
ephemeral (process-local) while others stay persisted on disk. The public naming scheme is
`<original-name>InMemory` — e.g. `string` → `stringInMemory`, `nullableEnumSet` →
`nullableEnumSetInMemory` — declared as members of the existing `PreferencesDatastore`
interface, so a single datastore instance can mix disk-backed and memory-backed preferences.

**Decided behavior (user-confirmed):**

- **Reuse, don't duplicate.** One internal `InMemoryPreferencesDataStore` (a
  `MutableStateFlow<Preferences>` + `Mutex` implementation of `DataStore<Preferences>`)
  backs the existing internal primitive classes — no per-type `*InMemory` classes.
  Every existing primitive in `core/data/` and `optional/data/` only touches
  `DataStore<Preferences>` through `dataOrEmpty` / `edit {}`, so they work unchanged against
  the in-memory store and inherit identical serialization, fallback, and null semantics.
- **No `createInMemoryPreferencesDatastore()` factory** in this change (declined; possible
  future follow-up).

## 2. Design

### 2.1 The in-memory store

New internal class in the reserved `preferences/core/mem/` directory:

```kotlin
// core/mem/InMemoryPreferencesDataStore.kt
internal class InMemoryPreferencesDataStore(
    initial: Preferences = emptyPreferences(),
) : DataStore<Preferences> {
    private val state = MutableStateFlow(initial)
    private val mutex = Mutex()

    override val data: Flow<Preferences> = state.asStateFlow()

    override suspend fun updateData(
        transform: suspend (Preferences) -> Preferences,
    ): Preferences = mutex.withLock {
        val updated = transform(state.value)
        state.value = updated
        updated
    }
}
```

Contract notes:

- `Mutex.withLock` serializes `updateData` transforms exactly like androidx DataStore's
  actor, so concurrent updates apply sequentially and each transform sees the previous
  result.
- A throwing transform propagates the exception and leaves the state unchanged (same as
  `edit {}` rollback).
- `StateFlow` conflates and deduplicates by `Preferences` equality — equivalent to the
  replay+distinct behavior consumers rely on for the disk-backed `data` flow.
- `dataOrEmpty`'s `IOException` catch is a no-op safety net here (the store never throws).

### 2.2 Per-datastore scope

`GenericPreferencesDatastore` holds `internal val inMemoryDatastore = InMemoryPreferencesDataStore()`
(one per wrapper instance, mirroring the `internal val datastore` exposure so module tests can
seed/inspect raw values). Two `GenericPreferencesDatastore` instances have **independent**
memory stores.

### 2.3 Batch guard

Batch operations (`batchRead*` / `batchWrite` / `batchUpdate` / `batchDelete`) operate on
disk `Preferences` snapshots. In-memory values are not in those snapshots, so letting memory
preferences participate would silently read/write the wrong store.

`PreferenceImpl` gains `internal val inMemoryStorage: Boolean = false` (set `true` only by the
`*InMemory` factories). When `true`, its `PreferencesAccessor` methods (`readFrom`,
`writeInto`, `removeFrom`) throw `IllegalStateException("In-memory preferences do not
participate in batch operations.")`. This catches every batch path, including `map`/`mapIO`
wrappers that delegate accessor calls to the underlying `PreferenceImpl`.

`PrefBuilder` gets **no** `*InMemory` declaration functions: inside batch blocks the only way
to reference a memory preference is `add(preference)`, which routes through the guarded
accessors.

### 2.4 Excluded operations (by design)

In-memory preferences are invisible to everything that reads or writes the disk store:

- `clearAll()` clears the disk store only; memory preferences keep their values.
- `exportAsData` / `exportAsString` never contain memory keys.
- `importData` / `importDataAsString` never write memory preferences.
- Batch operations throw (see 2.3) instead of misbehaving silently.

Everything else — `get`/`set`/`update`/`delete`/`resetToDefault`, `asFlow`/`stateIn`/
`stateInCurrent`, `getBlocking`/`setBlocking`, property delegation, `map`/`mapIO`,
`toggle` — works unchanged.

## 3. Public API surface

32 new interface members on `PreferencesDatastore` (same signatures, default values, and
KDoc structure as their disk counterparts; each doc notes the value is memory-backed and not
persisted):

| Family | Members |
|---|---|
| Primitives | `boolInMemory`, `doubleInMemory`, `floatInMemory`, `intInMemory`, `longInMemory`, `stringInMemory`, `stringSetInMemory` |
| Lists | `stringListInMemory` |
| Nullable primitives | `nullableBoolInMemory`, `nullableDoubleInMemory`, `nullableFloatInMemory`, `nullableIntInMemory`, `nullableLongInMemory`, `nullableStringInMemory`, `nullableStringSetInMemory`, `nullableStringListInMemory` |
| Serialized | `serializedInMemory`, `serializedSetInMemory`, `serializedListInMemory` |
| Kserialized | `kserializedInMemory`, `kserializedSetInMemory`, `kserializedListInMemory` |
| Nullable serialized | `nullableSerializedInMemory`, `nullableSerializedSetInMemory`, `nullableSerializedListInMemory` |
| Nullable kserialized | `nullableKserializedInMemory`, `nullableKserializedSetInMemory`, `nullableKserializedListInMemory` |
| Enum | `enumInMemory`, `enumSetInMemory`, `nullableEnumInMemory`, `nullableEnumSetInMemory` |

Plus 10 reified extensions in `PreferenceApi.kt` mirroring the existing sugar (delegating to
the members with `serializer<T>()` / `enumValues()`): `kserializedInMemory`,
`kserializedSetInMemory`, `kserializedListInMemory`, `nullableKserializedInMemory`,
`nullableKserializedListInMemory`, `nullableKserializedSetInMemory`, `enumInMemory`,
`enumSetInMemory`, `nullableEnumInMemory`, `nullableEnumSetInMemory`.

The enum members keep the deliberate member/extension arity split documented in AGENTS.md
(non-reified member with explicit `enumValues: Array<T>`; reified 2-arg extension that
shadows by arity).

Example usage:

```kotlin
val datastore = createPreferencesDatastore(producePath = { ... })
val token = datastore.string("token", "")                  // disk-backed
val scrollPos = datastore.intInMemory("scroll_pos", 0)     // process-local
```

## 4. Semantics unchanged by construction

Because the `*InMemory` factories construct the same internal classes with a different
`DataStore<Preferences>`, every documented rule carries over for free:

- Missing keys read back as `defaultValue` (non-null) or `null` (nullable).
- Nullable `set(null)` removes the key; `resetToDefault()` is `delete()` for nullable
  preferences and `set(defaultValue)` for non-nullable ones.
- Per-element decode failures in set/list preferences are skipped (all-fail set reads back
  as an empty set).
- Enum decode of an unknown name falls back per type (default / `null` / element skipping).
- Same key name families share the in-memory store exactly like on disk (`stringInMemory("k")`
  and `nullableStringInMemory("k")` hit the same slot with different key types).

## 5. Tests

Follow the abstract-test-class pattern (AGENTS.md) — suspending and blocking suites stay
separate:

- `commonTest/.../preferences/core/mem/InMemoryPreferencesDataStoreTest.kt` — plain
  commonTest class for the store itself (no platform helpers needed): initial value,
  `data` emissions, sequential `updateData`, 100 concurrent `updateData` calls applying
  exactly (Mutex serialization), throwing transform leaves state unchanged.
- `commonTest/.../preferences/core/mem/AbstractInMemoryPreferencesTest.kt` (suspending;
  requires `preferenceDatastore` + `testDispatcher`): all 32 factories (get/set/update/
  delete/resetToDefault/asFlow/stateIn), nullable semantics, bad-element skipping (seeding
  garbage via the exposed `inMemoryDatastore`), enum unknown-name fallback, disk/mem key
  isolation, two handles sharing one mem key, `clearAll()`/backup export/import exclusion,
  and `IllegalStateException` on batch use.
- `commonTest/.../preferences/core/mem/AbstractInMemoryPreferencesBlockingTest.kt`
  (requires only `preferenceDatastore`): `getBlocking`/`setBlocking`/
  `resetToDefaultBlocking`, property delegation.
- Platform subclasses with the existing helpers (still file-backed underneath — proving mem
  preferences never touch disk): `JvmInMemoryPreferences(Blocking)Test` (`@TempDir` +
  `JvmTestHelper`), `AndroidInMemory…` (`AndroidTestHelper`, companion-object setup for the
  blocking suite), `IosInMemory…` (`IosTestHelper`).

## 6. Docs & housekeeping

- README.md — new "In-memory preferences" subsection: usage example, naming rule,
  per-datastore scope, and the exclusion list (persistence, batch, backup, `clearAll`).
- AGENTS.md — update the `mem/` module descriptions (`core/mem/` now holds
  `InMemoryPreferencesDataStore`; `optional/mem/` gets a `.gitkeep` and stays reserved) and
  add an "In-memory preference types" section documenting the reuse decision, batch guard,
  and exclusion semantics.
- Regenerate `generic-datastore-preferences/api/*.api` via
  `:generic-datastore-preferences:updateKotlinAbi` (this repo's binary-compat task names;
  verified with `checkKotlinAbi`).

## 7. Verification

1. `./gradlew :generic-datastore-preferences:compileKotlinJvm :generic-datastore-preferences:compileTestKotlinJvm`
2. `./gradlew :generic-datastore-preferences:jvmTest`
3. `./gradlew :generic-datastore-preferences:compileAndroidMain :generic-datastore-preferences:compileAndroidDeviceTest`
4. `./gradlew :generic-datastore-preferences:compileKotlinIosSimulatorArm64` (+
   `iosSimulatorArm64Test` when a simulator is available)
5. `./gradlew :generic-datastore-preferences:checkKotlinAbi`

## 8. Non-goals

- Proto module in-memory variants.
- Batch DSL support for memory preferences (guarded with a clear exception instead).
- `createInMemoryPreferencesDatastore()` fully-in-memory datastore factory (declined for now).
