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

**No implementation work has started.** All phases below are pending. The codebase is at baseline
(latest commit: `87586ac feat: add nullable APIs and some updates to other APIs (#108)`).

## Phase 1: Internal interface

- [ ] **1.1** Create `PreferencesAccessor<T>` internal interface

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

- [ ] **1.2** Implement `PreferencesAccessor` on all five sealed base classes

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

- [ ] **1.3** Refactor existing `asFlow()`, `set()`, `update()`, `delete()` methods to
  delegate to `readFrom`/`writeInto`/`removeFrom` internally

  This avoids duplicating the read/write logic. The existing public behavior stays identical.

## Phase 2: Batch scope classes

- [ ] **2.1** Create `BatchReadScope`

  Location: `preferences/batch/BatchReadScope.kt`

  ```kotlin
  class BatchReadScope internal constructor(
      private val preferences: Preferences,
  ) {
      fun <T> Preference<T>.value(): T {
          val accessible = this as? PreferencesAccessor<T>
              ?: error("Batch operations only support preferences created by this library")
          return accessible.readFrom(preferences)
      }
  }
  ```

  The scope captures a single `Preferences` snapshot. Each `Preference<T>.value()` call reads
  from that same snapshot — no additional DataStore transactions.

- [ ] **2.2** Create `BatchWriteScope`

  Location: `preferences/batch/BatchWriteScope.kt`

  ```kotlin
  class BatchWriteScope internal constructor(
      private val mutablePreferences: MutablePreferences,
  ) {
      fun <T> Preference<T>.set(value: T) {
          val accessible = this as? PreferencesAccessor<T>
              ?: error("Batch operations only support preferences created by this library")
          accessible.writeInto(mutablePreferences, value)
      }

      fun <T> Preference<T>.delete() {
          val accessible = this as? PreferencesAccessor<T>
              ?: error("Batch operations only support preferences created by this library")
          accessible.removeFrom(mutablePreferences)
      }

      fun <T> Preference<T>.resetToDefault() {
          set(this.defaultValue)
      }
  }
  ```

  The scope captures a single `MutablePreferences` from `datastore.edit`. All writes happen
  within that one transaction.

- [ ] **2.3** Create `BatchUpdateScope` (atomic read-then-write)

  Location: `preferences/batch/BatchUpdateScope.kt`

  ```kotlin
  class BatchUpdateScope internal constructor(
      private val preferences: Preferences,
      private val mutablePreferences: MutablePreferences,
  ) {
      fun <T> Preference<T>.value(): T {
          val accessible = this as? PreferencesAccessor<T>
              ?: error("Batch operations only support preferences created by this library")
          return accessible.readFrom(preferences)
      }

      fun <T> Preference<T>.set(value: T) {
          val accessible = this as? PreferencesAccessor<T>
              ?: error("Batch operations only support preferences created by this library")
          accessible.writeInto(mutablePreferences, value)
      }

      fun <T> Preference<T>.update(transform: (T) -> T) {
          set(transform(value()))
      }

      fun <T> Preference<T>.delete() {
          val accessible = this as? PreferencesAccessor<T>
              ?: error("Batch operations only support preferences created by this library")
          accessible.removeFrom(mutablePreferences)
      }

      fun <T> Preference<T>.resetToDefault() {
          set(this.defaultValue)
      }
  }
  ```

  Uses `datastore.edit { mp -> BatchUpdateScope(mp.toPreferences(), mp).block() }` so that
  the current snapshot and the mutable preferences are from the same atomic transaction.

## Phase 3: Public API on PreferencesDatastore

- [ ] **3.1** Add batch methods to `PreferencesDatastore` interface

  ```kotlin
  fun batchReadFlow(): Flow<BatchReadScope>

  suspend fun <R> batchGet(block: BatchReadScope.() -> R): R

  suspend fun batchWrite(block: BatchWriteScope.() -> Unit)

  suspend fun batchUpdate(block: BatchUpdateScope.() -> Unit)
  ```

- [ ] **3.2** Implement in `GenericPreferencesDatastore`

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

- [ ] **3.3** Add blocking variants

  ```kotlin
  fun <R> batchGetBlocking(block: BatchReadScope.() -> R): R
  fun batchWriteBlocking(block: BatchWriteScope.() -> Unit)
  fun batchUpdateBlocking(block: BatchUpdateScope.() -> Unit)
  ```

  Implemented with `runBlocking { ... }`, matching the existing pattern for single preferences.

## Phase 4: MappedPrefs support

- [ ] **4.1** Implement `PreferencesAccessor<R>` on `MappedPrefs`

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

- [ ] **5.1** Create new files

  ```
  preferences/batch/
  ├── PreferencesAccessor.kt                      (internal interface)
  ├── BatchReadScope.kt                           (batch read scope)
  ├── BatchWriteScope.kt                          (batch write scope)
  └── BatchUpdateScope.kt                         (atomic read+write scope)
  ```

- [ ] **5.2** Modify existing files

  | File | Change |
  |---|---|
  | `preferences/utils/MappedPreference.kt` | Add `PreferencesAccessor<R>` implementation |
  | `preferences/core/GenericPreferenceItem.kt` | Add `: PreferencesAccessor<T>`, implement methods, refactor existing logic |
  | `preferences/core/custom/CustomGenericPreferenceItem.kt` | Same as above |
  | `preferences/core/customSet/CustomSetGenericPreferenceItem.kt` | Same as above |
  | `preferences/optional/NullableGenericPreferenceItem.kt` | Same as above |
  | `preferences/optional/custom/NullableCustomGenericPreferenceItem.kt` | Same as above |
  | `preferences/PreferencesDatastore.kt` | Add `batchReadFlow`, `batchGet`, `batchWrite`, `batchUpdate` to interface |
  | `preferences/GenericPreferencesDatastore.kt` | Implement batch methods |

## Phase 6: Testing

- [ ] **6.1** Create abstract test classes in `commonTest`

  Following the existing abstract test class pattern:

  - `AbstractBatchReadTest` — tests for `batchReadFlow()` flow and `batchGet()` one-shot
  - `AbstractBatchWriteTest` — tests for `batchWrite()` single-transaction writes
  - `AbstractBatchUpdateTest` — tests for atomic `batchUpdate()` read-modify-write
  - `AbstractBatchBlockingTest` — tests for blocking variants

- [ ] **6.2** Create platform-specific test subclasses

  - `androidDeviceTest` — concrete subclasses using `AndroidTestHelper`
  - `desktopTest` — concrete subclasses using `DesktopTestHelper`
  - `iosSimulatorArm64Test` — concrete subclasses using `IosTestHelper`

- [ ] **6.3** Test scenarios to cover

  - [ ] Batch read: multiple primitive preferences from a single snapshot
  - [ ] Batch read: mixed types (string, int, boolean, custom serialized)
  - [ ] Batch read: nullable preferences (absent keys return null)
  - [ ] Batch read: flow re-emits when any included preference changes
  - [ ] Batch read: mapped preferences work in batch scope
  - [ ] Batch write: multiple preferences in a single transaction
  - [ ] Batch write: delete and set in the same transaction
  - [ ] Batch write: resetToDefault in batch scope
  - [ ] Batch update: read current values and write new ones atomically
  - [ ] Batch update: transform function receives consistent snapshot
  - [ ] Blocking variants: batchGetBlocking, batchWriteBlocking, batchUpdateBlocking

## Edge Cases & Design Decisions

### Type safety of `PreferencesAccessor` cast

The batch scope extension functions cast `Preference<T>` to `PreferencesAccessor<T>`. This is safe
because all concrete `Preference` implementations in this library implement `PreferencesAccessor`.
However, if a user creates a custom `Preference` implementation outside the library and passes it
to a batch scope, the cast will fail at runtime.

**Decision**: Use `as?` with a descriptive error message rather than a raw `ClassCastException`.

### `batchReadFlow()` flow granularity

`batchReadFlow()` returns `Flow<BatchReadScope>` which maps from `datastore.dataOrEmpty`. This means
it re-emits whenever *any* preference in the datastore changes, not just the ones read in the
scope. This is consistent with how individual `Preference.asFlow()` works (it maps from the same
`datastore.data` flow). Consumers should use `distinctUntilChanged()` on their derived values if
they want to filter out irrelevant changes.

### `DelegatedPreferenceImpl` wrapper

`GenericPreferencesDatastore` wraps every sealed-class instance in `DelegatedPreferenceImpl` before
returning it. The batch scope casts `Preference<T>` to `PreferencesAccessor<T>`, so
`DelegatedPreferenceImpl` must either:
- Also implement `PreferencesAccessor<T>` by delegating to the wrapped `BasePreference`, or
- The batch scope must unwrap it first (access the inner `BasePreference` via the `DelegatedPreference` interface).

This needs to be addressed during Phase 1/2 implementation. The simplest approach is to have
`DelegatedPreferenceImpl` implement `PreferencesAccessor<T>` by delegating to the wrapped instance.
