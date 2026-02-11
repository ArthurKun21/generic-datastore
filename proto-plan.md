# Proto DataStore: Per-field Preferences

## Problem Statement

For Proto DataStore, the current API only exposes the entire proto object as a single
`ProtoPreference<T>` (via `data()`). There is no way to create preference wrappers for individual
fields of the proto. This means each field read/write goes through the whole object, and there is
no per-field `asFlow()`, `get()`, or `set()`. Introducing per-field proto preferences (with
`getter`/`updater` lambdas) would give each field its own `get()`, `set()`, `asFlow()`, property
delegation, etc.

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

## Current Code Structure

The existing proto module (`proto/`) contains:

| File | Description |
|---|---|
| `ProtoDatastore.kt` | `ProtoDatastore<T>` interface with a single `data(): ProtoPreference<T>` method |
| `GenericProtoDatastore.kt` | Concrete implementation; holds `DataStore<T>`, `defaultValue`, and `key`; `data()` returns a `GenericProtoPreferenceItem` |
| `ProtoPreference.kt` | `ProtoPreference<T>` marker interface extending `DelegatedPreference<T>` |
| `GenericProtoPreferenceItem.kt` | `internal class` implementing `ProtoPreference<T>` with all the DataStore logic (`get`, `set`, `update`, `delete`, `asFlow`, `stateIn`, blocking variants, property delegation) |
| `CreateProtoDatastore.kt` | Factory functions for creating `GenericProtoDatastore` with various path types |

Key interfaces from `core/`:

- `BasePreference<T>` — core preference contract (`get`, `set`, `update`, `delete`,
  `resetToDefault`, `asFlow`, `stateIn`, `getBlocking`, `setBlocking`)
- `DelegatedPreference<T>` — extends `BasePreference<T>` + `ReadWriteProperty<Any?, T>`,
  adds `resetToDefaultBlocking()`
- `ProtoPreference<T>` — marker interface extending `DelegatedPreference<T>` for proto-backed
  preferences

## Phase 1: Refactor `GenericProtoPreferenceItem` and Create `ProtoFieldPreference`

The existing `GenericProtoPreferenceItem<T>` wraps the entire proto as `ProtoPreference<T>`. The
new `ProtoFieldPreference<P, T>` wraps an individual field. These two share nearly identical
boilerplate — in fact `GenericProtoPreferenceItem<T>` is just `ProtoFieldPreference<T, T>` with
identity `getter = { it }` and `updater = { _, value -> value }`.

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
  ) : BasePreference<T> {

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

- [ ] **1.2** Refactor existing `GenericProtoPreferenceItem<T>` to delegate to `ProtoFieldPreference<T, T>`

  Replace the current `GenericProtoPreferenceItem<T>` class body with delegation to a
  `ProtoFieldPreference<T, T>` using identity `getter`/`updater`:

  ```kotlin
  internal class GenericProtoPreferenceItem<T>(
      datastore: DataStore<T>,
      defaultValue: T,
      key: String = "proto_data",
  ) : ProtoPreference<T>,
      BasePreference<T> by ProtoFieldPreference(
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

  This eliminates the duplicated DataStore access boilerplate from `GenericProtoPreferenceItem`.
  The class retains its `ProtoPreference<T>` interface (which extends `DelegatedPreference<T>`)
  and only adds the property delegation + `resetToDefaultBlocking()` methods that
  `DelegatedPreference<T>` requires.

- [ ] **1.3** Create `ProtoFieldPrefs<P, T>` (wraps `ProtoFieldPreference` as `ProtoPreference`)

  Similar to how `GenericProtoPreferenceItem` wraps the whole proto, this wraps
  `ProtoFieldPreference` and adds property delegation + `resetToDefaultBlocking()` to satisfy
  the `ProtoPreference<T>` / `DelegatedPreference<T>` contract.

  Location: `proto/ProtoFieldPrefs.kt`

  ```kotlin
  internal class ProtoFieldPrefs<P, T>(
      private val pref: ProtoFieldPreference<P, T>,
  ) : ProtoPreference<T>, BasePreference<T> by pref {

      override fun getValue(thisRef: Any?, property: KProperty<*>): T = pref.getBlocking()

      override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) = pref.setBlocking(value)

      override fun resetToDefaultBlocking() = pref.setBlocking(pref.defaultValue)
  }
  ```

- [ ] **1.4** Add `field()` factory method to `ProtoDatastore<T>` interface

  ```kotlin
  public interface ProtoDatastore<T> {
      fun data(): ProtoPreference<T>

      fun <F> field(
          key: String,
          defaultValue: F,
          getter: (T) -> F,
          updater: (T, F) -> T,
      ): ProtoPreference<F>
  }
  ```

- [ ] **1.5** Implement `field()` in `GenericProtoDatastore<T>`

  ```kotlin
  override fun <F> field(
      key: String,
      defaultValue: F,
      getter: (T) -> F,
      updater: (T, F) -> T,
  ): ProtoPreference<F> = ProtoFieldPrefs(
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
  └── ProtoFieldPrefs.kt                          (ProtoPreference wrapper for ProtoFieldPreference)
  ```

- [ ] **2.2** Modify existing files

  | File | Change |
  |---|---|
  | `proto/GenericProtoPreferenceItem.kt` | Refactor to delegate to `ProtoFieldPreference<T, T>` with identity getter/updater |
  | `proto/ProtoDatastore.kt` | Add `field()` to interface |
  | `proto/GenericProtoDatastore.kt` | Implement `field()` |

## Phase 3: Testing

### Existing test structure

The proto module already has the following abstract test class pattern:

- `commonTest` — `AbstractProtoDatastoreTest` (suspending tests against `GenericProtoDatastore<TestProtoData>`),
  `AbstractProtoDatastoreBlockingTest` (blocking tests), and `TestProtoData` / `TestProtoDataSerializer`
  (a simple `data class TestProtoData(val id: Int = 0, val name: String = "")` with CSV-based serialization).
- `androidDeviceTest` — `AndroidProtoDatastoreTest`, `AndroidProtoDatastoreBlockingTest`
- `desktopTest` — `DesktopProtoDatastoreTest`, `DesktopProtoDatastoreBlockingTest`
- `iosSimulatorArm64Test` — `IosProtoDatastoreTest`, `IosProtoDatastoreBlockingTest`

### New tests

- [ ] **3.1** Create abstract test classes in `commonTest`

  Following the existing abstract test class pattern:

  - `AbstractProtoFieldPreferenceTest` — suspending tests for per-field proto preferences.
    Requires `protoDatastore`, `dataStore`, and `testDispatcher` abstract properties.
  - `AbstractProtoFieldPreferenceBlockingTest` — blocking tests for per-field proto preferences.
    Only requires `protoDatastore`.

- [ ] **3.2** Create platform-specific test subclasses

  - `androidDeviceTest` — concrete subclasses using `AndroidTestHelper`
  - `desktopTest` — concrete subclasses using `DesktopTestHelper`
  - `iosSimulatorArm64Test` — concrete subclasses using `IosTestHelper`

- [ ] **3.3** Per-field preference test scenarios (suspending — in `AbstractProtoFieldPreferenceTest`)

  - [ ] `field()` creates a preference that reads a top-level field (`name` from `TestProtoData`)
  - [ ] `field()` creates a preference that reads another top-level field (`id` from `TestProtoData`)
  - [ ] `field().get()` returns `defaultValue` when proto is at default state
  - [ ] `field().set()` updates only the targeted field via `updateData`
  - [ ] `field().asFlow()` emits when the targeted field changes
  - [ ] `field().asFlow()` re-emits when an unrelated field changes (expected: same proto snapshot)
  - [ ] `field().update()` atomically reads and writes the field
  - [ ] `field().delete()` resets field to `defaultValue`
  - [ ] `field().resetToDefault()` resets field to `defaultValue`
  - [ ] `field().key()` returns the configured key

- [ ] **3.4** Per-field preference test scenarios (blocking — in `AbstractProtoFieldPreferenceBlockingTest`)

  - [ ] `field().getBlocking()` returns default value
  - [ ] `field().setBlocking()` and `getBlocking()` work correctly
  - [ ] `field().resetToDefaultBlocking()` resets to default
  - [ ] `field()` property delegation works (`by` syntax)

- [ ] **3.5** Refactor regression tests

  - [ ] Existing `AbstractProtoDatastoreTest` and `AbstractProtoDatastoreBlockingTest` still
    pass after `GenericProtoPreferenceItem` delegation refactor
  - [ ] `data()` still returns a working `ProtoPreference<T>` backed by the refactored
    `GenericProtoPreferenceItem`

### Test data model

The existing `TestProtoData(val id: Int = 0, val name: String = "")` is sufficient for testing
top-level field access. If nested field tests are desired, a new test data class with nested
structure can be added later (consider this optional / future work since `TestProtoData` already
has two fields to validate per-field isolation).

## Edge Cases & Design Decisions

### Code reuse: `GenericProtoPreferenceItem` as `ProtoFieldPreference<T, T>`

`GenericProtoPreferenceItem<T>` is the identity case of `ProtoFieldPreference<P, T>` where
`P == T`, `getter = { it }`, and `updater = { _, value -> value }`. Rather than maintaining two
nearly identical classes, `GenericProtoPreferenceItem` delegates to `ProtoFieldPreference`
internally. This means:

- All the `withContext(ioDispatcher)`, `runBlocking`, IOException catch, `stateIn` boilerplate
  lives in one place (`ProtoFieldPreference`)
- `GenericProtoPreferenceItem` only adds the `DelegatedPreference<T>` interface methods
  (`resetToDefaultBlocking`, `getValue`, `setValue` for property delegation)
- Bug fixes and behavioral changes to DataStore access propagate to both whole-object and
  per-field preferences automatically

### Proto field preferences: `delete()` semantics

Proto DataStore fields cannot be "removed" the way Preferences DataStore keys can. A proto field
always exists in the data class. Therefore `delete()` on a `ProtoFieldPreference` delegates to
`resetToDefault()`, which sets the field back to its `defaultValue`. This is consistent with
protobuf semantics where clearing a field sets it to its default value.

### Return type: `ProtoPreference<F>` (not `Prefs<F>`)

The `field()` method returns `ProtoPreference<F>` (which extends `DelegatedPreference<F>`) to
maintain consistency with `data()`. This gives per-field preferences the same capabilities as
whole-object preferences: property delegation, `resetToDefaultBlocking()`, and the
`ProtoPreference` marker for type-safe extension functions.

### Naming: `GenericProtoPreferenceItem` vs `ProtoPreference`

The existing `internal class GenericProtoPreferenceItem<T>` (wraps the entire proto) and the
`ProtoPreference<T>` marker interface are separate types:

- `ProtoPreference<T>` — public marker interface extending `DelegatedPreference<T>`, used as
  the return type of `data()` and `field()`.
- `GenericProtoPreferenceItem<T>` — internal implementation class for whole-object access.
- `ProtoFieldPrefs<P, T>` — new internal implementation class for per-field access.

Both `GenericProtoPreferenceItem<T>` and `ProtoFieldPrefs<P, T>` implement `ProtoPreference<T>`
and delegate core logic to `ProtoFieldPreference`.

### Why `ProtoFieldPreference` implements `BasePreference<T>` (not `DelegatedPreference`)

`ProtoFieldPreference` implements `BasePreference<T>` (not `DelegatedPreference<T>`) because it
lacks property delegation and `resetToDefaultBlocking()`. Those methods are added by the wrapper
classes (`GenericProtoPreferenceItem` and `ProtoFieldPrefs`) that implement the full
`ProtoPreference<T>` / `DelegatedPreference<T>` contract.

### `asFlow()` emits `map { getter(it) }` — no `distinctUntilChanged()`

`ProtoFieldPreference.asFlow()` maps the entire proto flow through `getter` without applying
`distinctUntilChanged()`. This means if an unrelated field changes, the flow re-emits the same
field value. This is intentional: the user can add `.distinctUntilChanged()` themselves if needed,
and omitting it keeps the default behavior simple and predictable.
