# Proto DataStore: Per-field Preferences

## Problem Statement

For Proto DataStore, the current API only exposes the entire proto object as a single
`Prefs<T>`. There is no way to create preference wrappers for individual fields of the proto.
This means each field read/write goes through the whole object, and there is no per-field
`asFlow()`, `get()`, or `set()`. Introducing per-field proto preferences (with `getter`/`updater`
lambdas) would give each field its own `get()`, `set()`, `asFlow()`, property delegation, etc.

## Architecture Overview

```kotlin
// Each field gets its own preference wrapper
val namePref = protoDatastore.field(
    defaultValue = "",
    getter = { it.name },
    updater = { proto, value -> proto.copy(name = value) },
)

// Nested fields work the same way
val cityPref = protoDatastore.field(
    defaultValue = "",
    getter = { it.userProfile.address.city },
    updater = { proto, value ->
        proto.copy(
            userProfile = proto.userProfile.copy(
                address = proto.userProfile.address.copy(city = value)
            )
        )
    },
)

```

## Phase 1: Refactor `ProtoPreference` into `ProtoFieldPreference`

The existing `ProtoPreference<T>` wraps the entire proto as `Prefs<T>`. The new
`ProtoFieldPreference<P, T>` wraps an individual field. These two share nearly identical
boilerplate — in fact `ProtoPreference<T>` is just `ProtoFieldPreference<T, T>` with identity
`getter = { it }` and `updater = { _, value -> value }`.

- [ ] **1.1** Create `ProtoFieldPreference<P, T>` internal class

  Location: `proto/ProtoFieldPreference.kt`

  `P` = proto/data class type, `T` = field type.

  ```kotlin
  internal class ProtoFieldPreference<P, T>(
      internal val datastore: DataStore<P>,
      private val key: String,
      override val defaultValue: T,
      internal val getter: (P) -> T,
      internal val updater: (P, T) -> P,
      private val defaultProtoValue: P,
      private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
  ) : Preference<T> {

      init {
          require(key.isNotBlank()) {
              "Proto key cannot be blank."
          }
      }

      override fun key(): String = key

      override suspend fun get(): T = withContext(ioDispatcher) {
          asFlow().first()
      }

      override suspend fun set(value: T) {
          withContext(ioDispatcher) {
              datastore.updateData { current -> updater(current, value) }
          }
      }

      override suspend fun update(transform: (T) -> T) {
          withContext(ioDispatcher) {
              datastore.updateData { current ->
                  val currentField = getter(current)
                  updater(current, transform(currentField))
              }
          }
      }

      override suspend fun delete() = resetToDefault()

      override suspend fun resetToDefault() = set(defaultValue)

      override fun asFlow(): Flow<T> = datastore.data
          .catch { e ->
              if (e is IOException) emit(defaultProtoValue) else throw e
          }
          .map { getter(it) }

      override fun stateIn(scope: CoroutineScope, started: SharingStarted): StateFlow<T> =
          asFlow().stateIn(scope, started, defaultValue)

      override fun getBlocking(): T = runBlocking { get() }

      override fun setBlocking(value: T) = runBlocking { set(value) }
  }
  ```

  Key design decisions:
  - `getter: (P) -> T` extracts the field from the proto snapshot. Works for any nesting depth.
  - `updater: (P, T) -> P` returns a new proto with the field updated. Uses `copy()` chains
    for nested data classes.
  - `delete()` delegates to `resetToDefault()` since proto fields can't be "removed" — they
    reset to their default value.
  - `defaultProtoValue` is needed so `asFlow()` can emit a fallback on `IOException`.
  - `getter` and `updater` are `internal` so batch scopes (in the same module) can access
    them directly without an extra interface.

- [ ] **1.2** Refactor existing `ProtoPreference<T>` to delegate to `ProtoFieldPreference<T, T>`

  Replace the current `ProtoPreference<T>` class body with delegation to a
  `ProtoFieldPreference<T, T>` using identity `getter`/`updater`:

  ```kotlin
  internal class ProtoPreference<T>(
      datastore: DataStore<T>,
      defaultValue: T,
      key: String = "proto_data",
  ) : Prefs<T>,
      Preference<T> by ProtoFieldPreference(
          datastore = datastore,
          key = key,
          defaultValue = defaultValue,
          getter = { it },
          updater = { _, value -> value },
          defaultProtoValue = defaultValue,
      ) {

      private val delegate = ProtoFieldPreference(
          datastore = datastore,
          key = key,
          defaultValue = defaultValue,
          getter = { it },
          updater = { _, value -> value },
          defaultProtoValue = defaultValue,
      )

      override fun resetToDefaultBlocking() = delegate.setBlocking(delegate.defaultValue)

      override fun getValue(thisRef: Any?, property: KProperty<*>): T = delegate.getBlocking()

      override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) =
          delegate.setBlocking(value)
  }
  ```

  Alternatively, simplify by making `ProtoPreference` just a thin factory that returns a
  `ProtoFieldPrefs<T, T>` — removing the class entirely. Either approach eliminates the
  duplicated boilerplate. Choose whichever keeps the diff smaller.

- [ ] **1.3** Create `ProtoFieldPrefs<P, T>` (wraps `ProtoFieldPreference` as `Prefs`)

  Similar to how `PrefsImpl` wraps `Preference`, this wraps `ProtoFieldPreference` and adds
  property delegation + `resetToDefaultBlocking()`.

  ```kotlin
  internal class ProtoFieldPrefs<P, T>(
      private val pref: ProtoFieldPreference<P, T>,
  ) : Prefs<T>, Preference<T> by pref {

      override fun getValue(thisRef: Any?, property: KProperty<*>): T = pref.getBlocking()

      override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) = pref.setBlocking(value)

      override fun resetToDefaultBlocking() = pref.setBlocking(pref.defaultValue)
  }
  ```

- [ ] **1.4** Add `field()` factory method to `ProtoDatastore<T>` interface

  ```kotlin
  public interface ProtoDatastore<T> {
      fun data(): Prefs<T>

      fun <F> field(
          key: String,
          defaultValue: F,
          getter: (T) -> F,
          updater: (T, F) -> T,
      ): Prefs<F>
  }
  ```

- [ ] **1.5** Implement `field()` in `GenericProtoDatastore<T>`

  ```kotlin
  override fun <F> field(
      key: String,
      defaultValue: F,
      getter: (T) -> F,
      updater: (T, F) -> T,
  ): Prefs<F> = ProtoFieldPrefs(
      ProtoFieldPreference(
          datastore = datastore,
          key = key,
          defaultValue = defaultValue,
          getter = getter,
          updater = updater,
          defaultProtoValue = this.defaultValue,
      ),
  )
  ```

- [ ] **1.6** Consider convenience extension for nested field access

  For deeply nested fields, users write verbose `copy()` chains in `updater`. This is optional
  / future work:

  ```kotlin
  val cityPref = protoDatastore.field(
      key = "user_city",
      defaultValue = "",
      getter = { it.userProfile.address.city },
      updater = { proto, value ->
          proto.copy(
              userProfile = proto.userProfile.copy(
                  address = proto.userProfile.address.copy(city = value)
              )
          )
      },
  )
  ```

  The `updater` verbosity is inherent to immutable data class nesting. Libraries like
  `arrow-optics` solve this with lenses, but adding that dependency would violate the library's
  "thin wrapper" philosophy. Document the pattern in README instead.

## Phase 2: File Structure

- [ ] **2.1** Create new files

  ```
  proto/
  ├── ProtoFieldPreference.kt                     (per-field proto preference implementation)
  └── ProtoFieldPrefs.kt                          (Prefs wrapper for ProtoFieldPreference)
  ```

- [ ] **2.2** Modify existing files

  | File | Change |
  |---|---|
  | `proto/ProtoPreference.kt` | Refactor to delegate to `ProtoFieldPreference<T, T>` with identity getter/updater |
  | `proto/ProtoDatastore.kt` | Add `field()` to interface |
  | `proto/GenericProtoDatastore.kt` | Implement `field()` |

## Phase 3: Testing

- [ ] **3.1** Create abstract test classes in `commonTest`

  Following the existing abstract test class pattern:

  - `AbstractProtoFieldPreferenceTest` — tests for per-field proto preferences

- [ ] **3.2** Create platform-specific test subclasses

  - `androidDeviceTest` — concrete subclasses using `AndroidTestHelper`
  - `desktopTest` — concrete subclasses using `DesktopTestHelper`
  - `iosSimulatorArm64Test` — concrete subclasses using `IosTestHelper`

- [ ] **3.3** Per-field preference test scenarios

  - [ ] `field()` creates a preference that reads a top-level field
  - [ ] `field()` creates a preference that reads a nested field
  - [ ] `field()` creates a preference that reads a deeply nested field
  - [ ] `field().get()` returns defaultValue when proto is at default state
  - [ ] `field().set()` updates only the targeted field via `updateData`
  - [ ] `field().asFlow()` emits when the targeted field changes
  - [ ] `field().asFlow()` re-emits when an unrelated field changes (expected: same proto snapshot)
  - [ ] `field().update()` atomically reads and writes the field
  - [ ] `field().getBlocking()` and `setBlocking()` work correctly
  - [ ] `field()` property delegation works (`by` syntax)

- [ ] **3.4** Refactor regression tests

  - [ ] Existing `ProtoPreference<T>` tests still pass after delegation refactor
  - [ ] `data()` still returns a working `Prefs<T>` backed by the refactored `ProtoPreference`

## Edge Cases & Design Decisions

### Code reuse: `ProtoPreference` as `ProtoFieldPreference<T, T>`

`ProtoPreference<T>` is the identity case of `ProtoFieldPreference<P, T>` where `P == T`,
`getter = { it }`, and `updater = { _, value -> value }`. Rather than maintaining two nearly
identical classes, `ProtoPreference` delegates to `ProtoFieldPreference` internally. This means:

- All the `withContext(ioDispatcher)`, `runBlocking`, IOException catch, `stateIn` boilerplate
  lives in one place (`ProtoFieldPreference`)
- `ProtoPreference` only adds the `Prefs<T>` interface methods (`resetToDefaultBlocking`,
  `getValue`, `setValue` for property delegation)
- Bug fixes and behavioral changes to DataStore access propagate to both whole-object and
  per-field preferences automatically

### Proto field preferences: `delete()` semantics

Proto DataStore fields cannot be "removed" the way Preferences DataStore keys can. A proto field
always exists in the data class. Therefore `delete()` on a `ProtoFieldPreference` delegates to
`resetToDefault()`, which sets the field back to its `defaultValue`. This is consistent with
protobuf semantics where clearing a field sets it to its default value.

### Naming: `ProtoPreference` class conflict

The existing `internal class ProtoPreference<T>` (wraps the entire proto as a `Prefs<T>`)
conflicts with the WIP `ProtoPreference<T>` interface name from the user's idea. Resolution:

- Keep the existing `ProtoPreference<T>` class as-is (it wraps the whole proto object).
- Name the new per-field class `ProtoFieldPreference<P, T>` to distinguish it.
- The user's WIP interface concept is absorbed into the existing `Preference<T>` interface —
  no need for a separate `ProtoPreference<T>` interface since `ProtoFieldPreference` implements
  `Preference<T>` directly.

### Why `ProtoFieldPreference` implements `Preference<T>` (not a new interface)

The user's WIP defined a separate `ProtoPreference<T>` interface with slightly different method
names (`getSuspend`, `setSuspend`, `getAndSetSuspend`). Instead, `ProtoFieldPreference` implements
the existing `Preference<T>` interface so that:

1. Proto field preferences have the exact same API as Preferences DataStore preferences
2. They can be wrapped with `PrefsImpl`/`ProtoFieldPrefs` for property delegation
3. `MappedPrefs.map()`/`mapIO()` works on them
