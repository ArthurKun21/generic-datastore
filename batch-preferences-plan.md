# Preferences DataStore Batch Operations Plan

## Problem Statement

Currently every `Preference<T>` performs its own independent DataStore transaction:

- **Read**: each preference calls `datastore.dataOrEmpty.map { prefs -> prefs[key] }` separately
- **Write**: each preference calls `datastore.edit { ds -> ds[key] = value }` separately

Reading or writing N preferences means N independent DataStore transactions. Batch operations
should collapse these into a single `data.map` snapshot (read) and a single `edit` transaction
(write).

## Architecture Overview

```
datastore.dataOrEmpty
    .map { preferences ->
        // single snapshot → read multiple keys at once
    }

datastore.edit { mutablePrefs ->
    // single transaction → write multiple keys at once
}
```

## Status

**All phases complete.** Batch operations are fully implemented and tested. All 358 desktop tests
pass (including 30 batch-specific tests: 23 suspending + 7 blocking).

**Note on API deviation from plan:** The plan originally proposed extension functions on
`Preferences<T>` (e.g., `fun <T> Preferences<T>.value(): T`). During implementation, Kotlin's
member-vs-extension resolution caused `pref.set(value)` inside batch scope lambdas to resolve to
the suspend member `BasePreference.set()` rather than the scope's non-suspend extension. The final
API uses regular/operator functions taking the preference as a parameter:

- `get(pref)` / `this[pref]` for reads
- `set(pref, value)` / `this[pref] = value` for writes
- `delete(pref)`, `resetToDefault(pref)`, `update(pref) { ... }` for mutations

## Phase 1: Internal interface

- [x] **1.1** Create `PreferencesAccessor<T>` internal interface

  Location: `preferences/batch/PreferencesAccessor.kt`

  ```kotlin
  internal interface PreferencesAccessor<T> {
      fun readFrom(preferences: Preferences): T
      fun writeInto(mutablePreferences: MutablePreferences, value: T)
      fun removeFrom(mutablePreferences: MutablePreferences)
  }
  ```

  - `readFrom` — extracts this preference's value from a `Preferences` snapshot (returns
    `defaultValue` if absent, or uses safe deserialization for custom types)
  - `writeInto` — writes this preference's value into `MutablePreferences`
  - `removeFrom` — removes this preference's key from `MutablePreferences`

- [x] **1.2** Implement `PreferencesAccessor` on all five sealed base classes

  Each class already has the read/write logic inline. Extract it into the interface methods:

  | Sealed class | Location | `readFrom` | `writeInto` | `removeFrom` |
  |---|---|---|---|---|
  | `GenericPreferenceItem<T>` | `preferences/core/GenericPreferenceItem.kt` | `prefs[preferences] ?: defaultValue` | `mp[preferences] = value` | `mp.remove(preferences)` |
  | `CustomGenericPreferenceItem<T>` | `preferences/core/custom/CustomGenericPreferenceItem.kt` | `prefs[stringPrefKey]?.let { deserialize(it) } ?: defaultValue` | `mp[stringPrefKey] = serializer(value)` | `mp.remove(stringPrefKey)` |
  | `CustomSetGenericPreferenceItem<T>` | `preferences/core/customSet/CustomSetGenericPreferenceItem.kt` | `prefs[stringSetPrefKey]?.let { deserializeSet(it) } ?: defaultValue` | `mp[stringSetPrefKey] = value.map { serializer(it) }.toSet()` | `mp.remove(stringSetPrefKey)` |
  | `NullableGenericPreferenceItem<T>` | `preferences/optional/NullableGenericPreferenceItem.kt` | `prefs[preferences]` (nullable) | if null → `remove`, else `mp[preferences] = value` | `mp.remove(preferences)` |
  | `NullableCustomGenericPreferenceItem<T>` | `preferences/optional/custom/NullableCustomGenericPreferenceItem.kt` | `prefs[stringPrefKey]?.let { deserialize(it) }` | if null → `remove`, else `mp[stringPrefKey] = serializer(value)` | `mp.remove(stringPrefKey)` |

  **Note:** The plan originally referenced `preferences/default/` paths, but the actual codebase
  uses `preferences/core/` for non-nullable types and `preferences/optional/` for nullable types.
  The table above reflects the correct paths.

  Also implemented on `DelegatedPreferenceImpl` (delegates to wrapped `BasePreference`) and
  `MappedPrefs` (applies convert/reverse through wrapped accessor).

- [ ] **1.3** Refactor existing `asFlow()`, `set()`, `update()`, `delete()` methods to
  delegate to `readFrom`/`writeInto`/`removeFrom` internally

  **Skipped:** Not required for batch operations to work. The existing methods continue to function
  correctly alongside the new batch API. This refactoring can be done separately to reduce
  duplication without changing behavior.

## Phase 2: Batch scope classes

- [x] **2.1** Create `BatchReadScope`

  Location: `preferences/batch/BatchReadScope.kt`

  ```kotlin
  class BatchReadScope internal constructor(
      private val snapshot: Preferences,
  ) {
      operator fun <T> get(preference: Preferences<T>): T { ... }
  }
  ```

  The scope captures a single `Preferences` snapshot. Each `get(pref)` call reads from that same
  snapshot — no additional DataStore transactions. Supports `this[pref]` operator syntax.

- [x] **2.2** Create `BatchWriteScope`

  Location: `preferences/batch/BatchWriteScope.kt`

  ```kotlin
  class BatchWriteScope internal constructor(
      private val mutablePreferences: MutablePreferences,
  ) {
      operator fun <T> set(preference: Preferences<T>, value: T) { ... }
      fun <T> delete(preference: Preferences<T>) { ... }
      fun <T> resetToDefault(preference: Preferences<T>) { ... }
  }
  ```

  The scope captures a single `MutablePreferences` from `datastore.edit`. All writes happen
  within that one transaction. Supports `this[pref] = value` operator syntax.

- [x] **2.3** Create `BatchUpdateScope` (atomic read-then-write)

  Location: `preferences/batch/BatchUpdateScope.kt`

  ```kotlin
  class BatchUpdateScope internal constructor(
      private val snapshot: Preferences,
      private val mutablePreferences: MutablePreferences,
  ) {
      operator fun <T> get(preference: Preferences<T>): T { ... }
      operator fun <T> set(preference: Preferences<T>, value: T) { ... }
      fun <T> update(preference: Preferences<T>, transform: (T) -> T) { ... }
      fun <T> delete(preference: Preferences<T>) { ... }
      fun <T> resetToDefault(preference: Preferences<T>) { ... }
  }
  ```

  Uses `datastore.edit { mp -> BatchUpdateScope(mp.toPreferences(), mp).block() }` so that
  the current snapshot and the mutable preferences are from the same atomic transaction.

## Phase 3: Public API on PreferencesDatastore

- [x] **3.1** Add batch methods to `PreferencesDatastore` interface

  ```kotlin
  fun batchReadFlow(): Flow<BatchReadScope>

  suspend fun <R> batchGet(block: BatchReadScope.() -> R): R

  suspend fun batchWrite(block: BatchWriteScope.() -> Unit)

  suspend fun batchUpdate(block: BatchUpdateScope.() -> Unit)
  ```

- [x] **3.2** Implement in `GenericPreferencesDatastore`

  ```kotlin
  override fun batchReadFlow(): Flow<BatchReadScope> =
      datastore.dataOrEmpty.map { BatchReadScope(it) }

  override suspend fun <R> batchGet(block: BatchReadScope.() -> R): R =
      batchReadFlow().first().block()

  override suspend fun batchWrite(block: BatchWriteScope.() -> Unit) {
      datastore.edit { mutablePrefs ->
          BatchWriteScope(mutablePrefs).block()
      }
  }

  override suspend fun batchUpdate(block: BatchUpdateScope.() -> Unit) {
      datastore.edit { mutablePrefs ->
          BatchUpdateScope(mutablePrefs.toPreferences(), mutablePrefs).block()
      }
  }
  ```

- [x] **3.3** Add blocking variants

  ```kotlin
  fun <R> batchGetBlocking(block: BatchReadScope.() -> R): R
  fun batchWriteBlocking(block: BatchWriteScope.() -> Unit): Unit
  fun batchUpdateBlocking(block: BatchUpdateScope.() -> Unit): Unit
  ```

  Implemented with `runBlocking { ... }`, matching the existing pattern for single preferences.

## Phase 4: MappedPrefs support

- [x] **4.1** Implement `PreferencesAccessor<R>` on `MappedPrefs`

  Location: `preferences/utils/MappedPreference.kt`

  `MappedPrefs<T, R>` wraps another `Prefs<T>` and applies `convert`/`reverse`. Delegate to
  the inner pref's `PreferencesAccessor<T>`, then apply `convert`/`reverse`:

  ```kotlin
  internal class MappedPrefs<T, R>(...) : Prefs<R>, PreferencesAccessor<R> {
      override fun readFrom(preferences: Preferences): R {
          val raw = (prefs as PreferencesAccessor<T>).readFrom(preferences)
          return convertFallback(raw)
      }

      override fun writeInto(mutablePreferences: MutablePreferences, value: R) {
          (prefs as PreferencesAccessor<T>).writeInto(mutablePreferences, reverseFallback(value))
      }

      override fun removeFrom(mutablePreferences: MutablePreferences) {
          (prefs as PreferencesAccessor<T>).removeFrom(mutablePreferences)
      }
  }
  ```

  **Note:** The plan originally referenced `core/MappedPreference.kt`, but the actual location
  is `preferences/utils/MappedPreference.kt`.

## Phase 5: File structure

- [x] **5.1** Create new files

  ```
  preferences/batch/
  ├── PreferencesAccessor.kt                      (internal interface)
  ├── BatchReadScope.kt                           (batch read scope)
  ├── BatchWriteScope.kt                          (batch write scope)
  └── BatchUpdateScope.kt                         (atomic read+write scope)
  ```

- [x] **5.2** Modify existing files

  | File | Change |
  |---|---|
  | `preferences/utils/MappedPreference.kt` | Add `PreferencesAccessor<R>` implementation |
  | `core/DelegatedPreference.kt` | Add `PreferencesAccessor<T>` implementation (delegates to wrapped pref) |
  | `preferences/core/GenericPreferenceItem.kt` | Add `: PreferencesAccessor<T>`, implement methods |
  | `preferences/core/custom/CustomGenericPreferenceItem.kt` | Same as above |
  | `preferences/core/customSet/CustomSetGenericPreferenceItem.kt` | Same as above |
  | `preferences/optional/NullableGenericPreferenceItem.kt` | Same as above |
  | `preferences/optional/custom/NullableCustomGenericPreferenceItem.kt` | Same as above |
  | `preferences/PreferencesDatastore.kt` | Add `batchReadFlow`, `batchGet`, `batchWrite`, `batchUpdate` to interface |
  | `preferences/GenericPreferencesDatastore.kt` | Implement batch methods |

## Phase 6: Testing

- [x] **6.1** Create abstract test classes in `commonTest`

  Consolidated into two abstract classes following the project's pattern of separating suspending
  and blocking tests:

    - `AbstractBatchOperationsTest` — 23 suspending tests covering batchGet, batchReadFlow,
      batchWrite, batchUpdate, mapped preferences, and stringSet in batch
    - `AbstractBatchOperationsBlockingTest` — 7 blocking tests covering batchGetBlocking,
      batchWriteBlocking, batchUpdateBlocking

- [x] **6.2** Create platform-specific test subclasses

    - `desktopTest` — `DesktopBatchOperationsTest` and `DesktopBatchOperationsBlockingTest`
      using `DesktopTestHelper`

  Android and iOS subclasses can be added following the same pattern when those test environments
  are available.

- [x] **6.3** Test scenarios covered

    - [x] Batch read: multiple primitive preferences from a single snapshot
    - [x] Batch read: mixed types (string, long, float, double)
    - [x] Batch read: nullable preferences (absent keys return null)
    - [x] Batch read: nullable preferences with values set
    - [x] Batch read: custom serialized preferences
    - [x] Batch read: serialized set preferences
    - [x] Batch read: index operator syntax (`this[pref]`)
    - [x] Batch read: flow re-emits when any included preference changes
    - [x] Batch read: mapped preferences work in batch scope
    - [x] Batch write: multiple preferences in a single transaction
    - [x] Batch write: index operator syntax (`this[pref] = value`)
    - [x] Batch write: delete and set in the same transaction
    - [x] Batch write: resetToDefault in batch scope
    - [x] Batch write: nullable set and delete in transaction
    - [x] Batch write: custom serialized in batch
    - [x] Batch write: stringSet preference
    - [x] Batch write: mapped preference
    - [x] Batch update: read current values and write new ones atomically
    - [x] Batch update: transform function
    - [x] Batch update: index operator syntax
    - [x] Batch update: delete in update scope
    - [x] Batch update: resetToDefault in update scope
    - [x] Batch update: consistent snapshot values (read multiple, write derived)
    - [x] Blocking variants: batchGetBlocking, batchWriteBlocking, batchUpdateBlocking

## Edge Cases & Design Decisions

### Type safety of `PreferencesAccessor` cast

The batch scope functions cast `Preference<T>` to `PreferencesAccessor<T>`. This is safe
because all concrete `Preference` implementations in this library implement `PreferencesAccessor`.
However, if a user creates a custom `Preference` implementation outside the library and passes it
to a batch scope, the cast will fail at runtime.

**Decision**: Use `as?` with a descriptive error message rather than a raw `ClassCastException`.

### Member vs extension function resolution (API design change)

The plan originally proposed extension functions (e.g., `fun <T> Preferences<T>.set(value: T)`).
However, Kotlin's resolution rules always prefer member functions over extension functions. Inside
batch scope lambdas, `pref.set(value)` resolved to the suspend member `BasePreference.set()` rather
than the scope's non-suspend extension — causing compilation errors in non-coroutine contexts.

**Decision**: Use regular functions and operator overloading (`get(pref)`, `set(pref, value)`,
`this[pref]`, `this[pref] = value`) instead of extension functions. This avoids the shadowing issue
entirely and provides clean DSL syntax.

### `batchReadFlow()` flow granularity

`batchReadFlow()` returns `Flow<BatchReadScope>` which maps from `datastore.dataOrEmpty`. This means
it re-emits whenever *any* preference in the datastore changes, not just the ones read in the
scope. This is consistent with how individual `Preference.asFlow()` works (it maps from the same
`datastore.data` flow). Consumers should use `distinctUntilChanged()` on their derived values if
they want to filter out irrelevant changes.

### `DelegatedPreferenceImpl` wrapper

`GenericPreferencesDatastore` wraps every sealed-class instance in `DelegatedPreferenceImpl` before
returning it. The batch scope casts `Preference<T>` to `PreferencesAccessor<T>`, so
`DelegatedPreferenceImpl` must also implement `PreferencesAccessor<T>`.

**Decision**: `DelegatedPreferenceImpl` implements `PreferencesAccessor<T>` by delegating to the
wrapped `BasePreference` instance (cast to `PreferencesAccessor<T>`).
