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

**Problem:** The existing proto test subclasses duplicate all setup/teardown boilerplate inline
(dispatcher creation, scope management, datastore creation via `createProtoDatastore`, file
cleanup). The existing `AndroidTestHelper` / `DesktopTestHelper` / `IosTestHelper` only handle
Preferences DataStore setup and cannot be reused for proto tests. Adding per-field preference
tests would triple this duplication.

### Platform-specific proto test helpers

- [ ] **3.1** Create `AndroidProtoTestHelper`

  Location: `androidDeviceTest/.../proto/AndroidProtoTestHelper.kt`

  Follows the same `standard()` / `blocking()` factory pattern as `AndroidTestHelper`, but
  creates a `GenericProtoDatastore<TestProtoData>` via `createProtoDatastore()`.

  ```kotlin
  class AndroidProtoTestHelper private constructor(
      private val datastoreName: String,
      private val useStandardDispatcher: Boolean,
  ) {
      private lateinit var _protoDatastore: GenericProtoDatastore<TestProtoData>
      private lateinit var _testDispatcher: TestDispatcher
      private lateinit var testContext: Context
      private lateinit var testScope: CoroutineScope

      val protoDatastore: GenericProtoDatastore<TestProtoData> get() = _protoDatastore
      val testDispatcher: TestDispatcher get() = _testDispatcher

      fun setup() {
          _testDispatcher = if (useStandardDispatcher) {
              StandardTestDispatcher()
          } else {
              UnconfinedTestDispatcher()
          }
          Dispatchers.setMain(_testDispatcher)
          testContext = ApplicationProvider.getApplicationContext()
          testScope = CoroutineScope(Job() + _testDispatcher)
          _protoDatastore = createProtoDatastore(
              serializer = TestProtoDataSerializer,
              defaultValue = TestProtoData(),
              scope = testScope,
              producePath = {
                  testContext.filesDir.resolve("datastore/$datastoreName.pb").absolutePath
              },
          )
      }

      fun tearDown() {
          try {
              if (::testScope.isInitialized) {
                  testScope.cancel()
              }
          } finally {
              try {
                  if (::testContext.isInitialized) {
                      val dataStoreFile =
                          File(testContext.filesDir, "datastore/$datastoreName.pb")
                      if (dataStoreFile.exists()) {
                          dataStoreFile.delete()
                      }
                  }
              } finally {
                  Dispatchers.resetMain()
              }
          }
      }

      companion object {
          fun standard(datastoreName: String): AndroidProtoTestHelper {
              return AndroidProtoTestHelper(datastoreName, useStandardDispatcher = true)
          }

          fun blocking(datastoreName: String): AndroidProtoTestHelper {
              return AndroidProtoTestHelper(datastoreName, useStandardDispatcher = false)
          }
      }
  }
  ```

- [ ] **3.2** Create `DesktopProtoTestHelper`

  Location: `desktopTest/.../proto/DesktopProtoTestHelper.kt`

  Same pattern, but `setup(tempFolderPath: String)` takes the `@TempDir` path (consistent with
  `DesktopTestHelper`). JUnit 5 handles file cleanup via `@TempDir`.

  ```kotlin
  class DesktopProtoTestHelper private constructor(
      private val datastoreName: String,
      private val useStandardDispatcher: Boolean,
  ) {
      private lateinit var _protoDatastore: GenericProtoDatastore<TestProtoData>
      private lateinit var _testDispatcher: TestDispatcher
      private lateinit var testScope: CoroutineScope

      val protoDatastore: GenericProtoDatastore<TestProtoData> get() = _protoDatastore
      val testDispatcher: TestDispatcher get() = _testDispatcher

      fun setup(tempFolderPath: String) {
          _testDispatcher = if (useStandardDispatcher) {
              StandardTestDispatcher()
          } else {
              UnconfinedTestDispatcher()
          }
          Dispatchers.setMain(_testDispatcher)
          testScope = CoroutineScope(Job() + _testDispatcher)
          _protoDatastore = createProtoDatastore(
              serializer = TestProtoDataSerializer,
              defaultValue = TestProtoData(),
              scope = testScope,
              producePath = {
                  "$tempFolderPath/$datastoreName.pb"
              },
          )
      }

      fun tearDown() {
          try {
              if (::testScope.isInitialized) {
                  testScope.cancel()
              }
          } finally {
              Dispatchers.resetMain()
          }
      }

      companion object {
          fun standard(datastoreName: String): DesktopProtoTestHelper {
              return DesktopProtoTestHelper(datastoreName, useStandardDispatcher = true)
          }

          fun blocking(datastoreName: String): DesktopProtoTestHelper {
              return DesktopProtoTestHelper(datastoreName, useStandardDispatcher = false)
          }
      }
  }
  ```

- [ ] **3.3** Create `IosProtoTestHelper`

  Location: `iosSimulatorArm64Test/.../proto/IosProtoTestHelper.kt`

  Same pattern, manages its own `NSTemporaryDirectory` + UUID temp dir (consistent with
  `IosTestHelper`).

  ```kotlin
  class IosProtoTestHelper private constructor(
      private val datastoreName: String,
      private val useStandardDispatcher: Boolean,
  ) {
      private lateinit var tempDir: String
      private lateinit var _protoDatastore: GenericProtoDatastore<TestProtoData>
      private lateinit var _testDispatcher: TestDispatcher
      private lateinit var testScope: CoroutineScope

      val protoDatastore: GenericProtoDatastore<TestProtoData> get() = _protoDatastore
      val testDispatcher: TestDispatcher get() = _testDispatcher

      fun setup() {
          tempDir = NSTemporaryDirectory() + NSUUID().UUIDString
          _testDispatcher = if (useStandardDispatcher) {
              StandardTestDispatcher()
          } else {
              UnconfinedTestDispatcher()
          }
          Dispatchers.setMain(_testDispatcher)
          testScope = CoroutineScope(Job() + _testDispatcher)
          _protoDatastore = createProtoDatastore(
              serializer = TestProtoDataSerializer,
              defaultValue = TestProtoData(),
              scope = testScope,
              producePath = {
                  "$tempDir/$datastoreName.pb"
              },
          )
      }

      fun tearDown() {
          try {
              if (::testScope.isInitialized) {
                  testScope.cancel()
              }
          } finally {
              try {
                  if (::tempDir.isInitialized) {
                      NSFileManager.defaultManager.removeItemAtPath(tempDir, null)
                  }
              } finally {
                  Dispatchers.resetMain()
              }
          }
      }

      companion object {
          fun standard(datastoreName: String): IosProtoTestHelper {
              return IosProtoTestHelper(datastoreName, useStandardDispatcher = true)
          }

          fun blocking(datastoreName: String): IosProtoTestHelper {
              return IosProtoTestHelper(datastoreName, useStandardDispatcher = false)
          }
      }
  }
  ```

### Proto test helper summary

  | Platform | Helper Class            | Location                                                |
  |----------|-------------------------|---------------------------------------------------------|
  | Android  | `AndroidProtoTestHelper` | `androidDeviceTest/.../proto/AndroidProtoTestHelper.kt` |
  | Desktop  | `DesktopProtoTestHelper` | `desktopTest/.../proto/DesktopProtoTestHelper.kt`       |
  | iOS      | `IosProtoTestHelper`     | `iosSimulatorArm64Test/.../proto/IosProtoTestHelper.kt` |

  Each helper provides:
  - `standard(datastoreName)` — `StandardTestDispatcher` + custom `CoroutineScope`. Exposes
    `protoDatastore` and `testDispatcher`.
  - `blocking(datastoreName)` — `UnconfinedTestDispatcher` without custom scope. Only exposes
    `protoDatastore`.

### Refactor existing proto tests to use helpers

- [ ] **3.4** Refactor existing platform-specific proto test subclasses

  Replace the inline setup/teardown boilerplate in the six existing test files with the new
  proto test helpers.

  **Android standard test (before → after):**

  ```kotlin
  // Before: ~40 lines of inline setup/teardown
  // After:
  @RunWith(AndroidJUnit4::class)
  class AndroidProtoDatastoreTest : AbstractProtoDatastoreTest() {
      private val helper = AndroidProtoTestHelper.standard("test_proto")

      override val protoDatastore get() = helper.protoDatastore
      override val testDispatcher get() = helper.testDispatcher

      @Before
      fun setup() = helper.setup()

      @After
      fun tearDown() = helper.tearDown()
  }
  ```

  **Android blocking test (before → after):**

  ```kotlin
  @RunWith(AndroidJUnit4::class)
  class AndroidProtoDatastoreBlockingTest : AbstractProtoDatastoreBlockingTest() {
      private val helper = AndroidProtoTestHelper.blocking("test_proto_blocking")

      override val protoDatastore get() = helper.protoDatastore

      @Before
      fun setup() = helper.setup()

      @After
      fun tearDown() = helper.tearDown()
  }
  ```

  **Desktop standard test (before → after):**

  ```kotlin
  class DesktopProtoDatastoreTest : AbstractProtoDatastoreTest() {
      @TempDir
      lateinit var tempFolder: File

      private val helper = DesktopProtoTestHelper.standard("test_proto")

      override val protoDatastore get() = helper.protoDatastore
      override val testDispatcher get() = helper.testDispatcher

      @BeforeTest
      fun setup() = helper.setup(tempFolder.absolutePath)

      @AfterTest
      fun tearDown() = helper.tearDown()
  }
  ```

  **Desktop blocking test (before → after):**

  ```kotlin
  class DesktopProtoDatastoreBlockingTest : AbstractProtoDatastoreBlockingTest() {
      @TempDir
      lateinit var tempFolder: File

      private val helper = DesktopProtoTestHelper.blocking("test_proto_blocking")

      override val protoDatastore get() = helper.protoDatastore

      @BeforeTest
      fun setup() = helper.setup(tempFolder.absolutePath)

      @AfterTest
      fun tearDown() = helper.tearDown()
  }
  ```

  **iOS standard test (before → after):**

  ```kotlin
  class IosProtoDatastoreTest : AbstractProtoDatastoreTest() {
      private val helper = IosProtoTestHelper.standard("test_proto")

      override val protoDatastore get() = helper.protoDatastore
      override val testDispatcher get() = helper.testDispatcher

      @BeforeTest
      fun setup() = helper.setup()

      @AfterTest
      fun tearDown() = helper.tearDown()
  }
  ```

  **iOS blocking test (before → after):**

  ```kotlin
  class IosProtoDatastoreBlockingTest : AbstractProtoDatastoreBlockingTest() {
      private val helper = IosProtoTestHelper.blocking("test_proto_blocking")

      override val protoDatastore get() = helper.protoDatastore

      @BeforeTest
      fun setup() = helper.setup()

      @AfterTest
      fun tearDown() = helper.tearDown()
  }
  ```

### New tests

- [ ] **3.5** Create abstract test classes in `commonTest`

  Following the existing abstract test class pattern:

  - `AbstractProtoFieldPreferenceTest` — suspending tests for per-field proto preferences.
    Requires `protoDatastore` and `testDispatcher` abstract properties.
  - `AbstractProtoFieldPreferenceBlockingTest` — blocking tests for per-field proto preferences.
    Only requires `protoDatastore`.

- [ ] **3.6** Create platform-specific test subclasses using proto test helpers

  Each subclass uses the corresponding `<Platform>ProtoTestHelper` with a unique datastore name.

  **Android:**

  ```kotlin
  @RunWith(AndroidJUnit4::class)
  class AndroidProtoFieldPreferenceTest : AbstractProtoFieldPreferenceTest() {
      private val helper = AndroidProtoTestHelper.standard("test_proto_field")

      override val protoDatastore get() = helper.protoDatastore
      override val testDispatcher get() = helper.testDispatcher

      @Before
      fun setup() = helper.setup()

      @After
      fun tearDown() = helper.tearDown()
  }

  @RunWith(AndroidJUnit4::class)
  class AndroidProtoFieldPreferenceBlockingTest : AbstractProtoFieldPreferenceBlockingTest() {
      private val helper = AndroidProtoTestHelper.blocking("test_proto_field_blocking")

      override val protoDatastore get() = helper.protoDatastore

      @Before
      fun setup() = helper.setup()

      @After
      fun tearDown() = helper.tearDown()
  }
  ```

  **Desktop:**

  ```kotlin
  class DesktopProtoFieldPreferenceTest : AbstractProtoFieldPreferenceTest() {
      @TempDir
      lateinit var tempFolder: File

      private val helper = DesktopProtoTestHelper.standard("test_proto_field")

      override val protoDatastore get() = helper.protoDatastore
      override val testDispatcher get() = helper.testDispatcher

      @BeforeTest
      fun setup() = helper.setup(tempFolder.absolutePath)

      @AfterTest
      fun tearDown() = helper.tearDown()
  }

  class DesktopProtoFieldPreferenceBlockingTest : AbstractProtoFieldPreferenceBlockingTest() {
      @TempDir
      lateinit var tempFolder: File

      private val helper = DesktopProtoTestHelper.blocking("test_proto_field_blocking")

      override val protoDatastore get() = helper.protoDatastore

      @BeforeTest
      fun setup() = helper.setup(tempFolder.absolutePath)

      @AfterTest
      fun tearDown() = helper.tearDown()
  }
  ```

  **iOS:**

  ```kotlin
  class IosProtoFieldPreferenceTest : AbstractProtoFieldPreferenceTest() {
      private val helper = IosProtoTestHelper.standard("test_proto_field")

      override val protoDatastore get() = helper.protoDatastore
      override val testDispatcher get() = helper.testDispatcher

      @BeforeTest
      fun setup() = helper.setup()

      @AfterTest
      fun tearDown() = helper.tearDown()
  }

  class IosProtoFieldPreferenceBlockingTest : AbstractProtoFieldPreferenceBlockingTest() {
      private val helper = IosProtoTestHelper.blocking("test_proto_field_blocking")

      override val protoDatastore get() = helper.protoDatastore

      @BeforeTest
      fun setup() = helper.setup()

      @AfterTest
      fun tearDown() = helper.tearDown()
  }
  ```

- [ ] **3.7** Per-field preference test scenarios (suspending — in `AbstractProtoFieldPreferenceTest`)

  **Top-level field tests (level 1):**

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

  **Nested field tests (level 2 — `profile.*`):**

  - [ ] `field()` reads `profile.nickname` (level 2)
    ```kotlin
    val nicknamePref = protoDatastore.field(
        key = "profile_nickname",
        defaultValue = "",
        getter = { it.profile.nickname },
        updater = { proto, value ->
            proto.copy(profile = proto.profile.copy(nickname = value))
        },
    )
    ```
  - [ ] `field().get()` returns default `""` for `profile.nickname` when proto is at default state
  - [ ] `field().set()` on `profile.nickname` does not affect `profile.age` or top-level fields
  - [ ] `field().update()` on `profile.age` atomically increments
    ```kotlin
    agePref.update { it + 1 }
    ```

  **Deeply nested field tests (level 3 — `profile.address.*`):**

  - [ ] `field()` reads `profile.address.city` (level 3)
    ```kotlin
    val cityPref = protoDatastore.field(
        key = "profile_address_city",
        defaultValue = "",
        getter = { it.profile.address.city },
        updater = { proto, value ->
            proto.copy(
                profile = proto.profile.copy(
                    address = proto.profile.address.copy(city = value)
                )
            )
        },
    )
    ```
  - [ ] `field().get()` returns default `""` for `profile.address.city` when proto is at default
  - [ ] `field().set()` on `profile.address.city` does not affect `profile.address.street`
    or `profile.address.zipCode`
  - [ ] `field().set()` on `profile.address.street` does not affect `profile.nickname` (level 2)
    or `name` (level 1)
  - [ ] `field().asFlow()` on `profile.address.zipCode` emits when that field changes
  - [ ] `field().update()` on `profile.address.city` atomically appends suffix
  - [ ] `field().delete()` on `profile.address.city` resets to `""` without affecting sibling
    fields
  - [ ] Multiple field preferences from different nesting levels can coexist and update
    independently (set `name`, `profile.age`, and `profile.address.city` sequentially, verify
    all three retain their values)

- [ ] **3.8** Per-field preference test scenarios (blocking — in `AbstractProtoFieldPreferenceBlockingTest`)

  **Top-level (level 1):**

  - [ ] `field().getBlocking()` returns default value
  - [ ] `field().setBlocking()` and `getBlocking()` work correctly
  - [ ] `field().resetToDefaultBlocking()` resets to default
  - [ ] `field()` property delegation works (`by` syntax)

  **Nested (level 2 — `profile.*`):**

  - [ ] `field().getBlocking()` returns default for `profile.nickname`
  - [ ] `field().setBlocking()` on `profile.age` and `getBlocking()` round-trip correctly
  - [ ] `field()` property delegation works for `profile.nickname` (`by` syntax)

  **Deeply nested (level 3 — `profile.address.*`):**

  - [ ] `field().getBlocking()` returns default for `profile.address.city`
  - [ ] `field().setBlocking()` on `profile.address.city` and `getBlocking()` round-trip correctly
  - [ ] `field().resetToDefaultBlocking()` on `profile.address.street` resets without affecting
    sibling fields
  - [ ] `field()` property delegation works for `profile.address.zipCode` (`by` syntax)

- [ ] **3.9** Refactor regression tests

  - [ ] Existing `AbstractProtoDatastoreTest` and `AbstractProtoDatastoreBlockingTest` still
    pass after `GenericProtoPreferenceItem` delegation refactor
  - [ ] `data()` still returns a working `ProtoPreference<T>` backed by the refactored
    `GenericProtoPreferenceItem`

### Test data model

- [ ] **3.10** Expand `TestProtoData` with 3-level nesting

  The existing `TestProtoData(val id: Int, val name: String)` only has top-level fields.
  Add nested data classes to support 3-level deep field tests. The existing tests and
  serializer must continue to work (backward-compatible defaults).

  Location: `commonTest/.../proto/TestProtoData.kt`

  ```kotlin
  data class TestAddress(
      val street: String = "",
      val city: String = "",
      val zipCode: String = "",
  )

  data class TestProfile(
      val nickname: String = "",
      val age: Int = 0,
      val address: TestAddress = TestAddress(),
  )

  data class TestProtoData(
      val id: Int = 0,
      val name: String = "",
      val profile: TestProfile = TestProfile(),
  )
  ```

  Nesting depth:
  - Level 1: `TestProtoData.name`, `TestProtoData.id`
  - Level 2: `TestProtoData.profile.nickname`, `TestProtoData.profile.age`
  - Level 3: `TestProtoData.profile.address.city`, `TestProtoData.profile.address.street`,
    `TestProtoData.profile.address.zipCode`

- [ ] **3.11** Update `TestProtoDataSerializer` for the expanded model

  The CSV-based serializer needs to handle the new nested fields. Use a pipe-delimited format
  to avoid conflicts with commas in field values:

  ```kotlin
  object TestProtoDataSerializer : OkioSerializer<TestProtoData> {
      override val defaultValue: TestProtoData = TestProtoData()

      override suspend fun readFrom(source: BufferedSource): TestProtoData {
          val line = source.readUtf8()
          if (line.isBlank()) return defaultValue
          val parts = line.split("|", limit = 7)
          return TestProtoData(
              id = parts[0].toInt(),
              name = parts[1],
              profile = TestProfile(
                  nickname = parts[2],
                  age = parts[3].toInt(),
                  address = TestAddress(
                      street = parts[4],
                      city = parts[5],
                      zipCode = parts[6],
                  ),
              ),
          )
      }

      override suspend fun writeTo(t: TestProtoData, sink: BufferedSink) {
          sink.writeUtf8(
              "${t.id}|${t.name}|${t.profile.nickname}|${t.profile.age}" +
                  "|${t.profile.address.street}|${t.profile.address.city}" +
                  "|${t.profile.address.zipCode}"
          )
      }
  }
  ```

### Nullable proto3 test model (Wire-style)

Wire-generated Kotlin classes for proto3 follow specific nullability rules:
- **Scalar fields** (non-optional): non-nullable with identity defaults (`""`, `0`, `false`)
- **Message fields**: nullable with `null` default (even when not marked `optional`)
- **`optional` scalar fields**: nullable with `null` default
- **`oneof` fields**: nullable with `null` default

This means a `field()` preference for a nullable message or optional scalar must use `null` as
its `defaultValue`, and the `getter`/`updater` must handle the nullable chain.

- [ ] **3.12** Create `TestNullableProtoData` model with Wire-style proto3 nullability

  Location: `commonTest/.../proto/TestNullableProtoData.kt`

  ```kotlin
  data class TestCoordinates(
      val latitude: Double = 0.0,
      val longitude: Double = 0.0,
  )

  data class TestNullableAddress(
      val street: String = "",
      val city: String = "",
      val coordinates: TestCoordinates? = null,   // message field → nullable
  )

  data class TestNullableProfile(
      val nickname: String = "",
      val age: Int? = null,                        // optional scalar → nullable
      val address: TestNullableAddress? = null,     // message field → nullable
  )

  data class TestNullableProtoData(
      val id: Int = 0,
      val name: String = "",
      val label: String? = null,                   // optional scalar → nullable
      val profile: TestNullableProfile? = null,     // message field → nullable
  )
  ```

  Nullability at each nesting level:
  - Level 1: `name` (non-null `String`), `label` (nullable `String?`),
    `profile` (nullable `TestNullableProfile?`)
  - Level 2: `profile?.nickname` (non-null `String` inside nullable parent),
    `profile?.age` (nullable `Int?`), `profile?.address` (nullable `TestNullableAddress?`)
  - Level 3: `profile?.address?.city` (non-null `String` inside two nullable parents),
    `profile?.address?.coordinates` (nullable `TestCoordinates?`)
  - Level 4: `profile?.address?.coordinates?.latitude` (non-null `Double` inside three
    nullable parents)

- [ ] **3.13** Create `TestNullableProtoDataSerializer`

  Location: `commonTest/.../proto/TestNullableProtoData.kt`

  Uses JSON-based serialization via `kotlinx.serialization` to avoid complex delimiter escaping
  with nullable fields. All data classes should be annotated with `@Serializable`:

  ```kotlin
  @Serializable
  data class TestCoordinates(...)

  @Serializable
  data class TestNullableAddress(...)

  @Serializable
  data class TestNullableProfile(...)

  @Serializable
  data class TestNullableProtoData(...)

  object TestNullableProtoDataSerializer : OkioSerializer<TestNullableProtoData> {
      override val defaultValue: TestNullableProtoData = TestNullableProtoData()

      override suspend fun readFrom(source: BufferedSource): TestNullableProtoData {
          val json = source.readUtf8()
          if (json.isBlank()) return defaultValue
          return Json.decodeFromString(json)
      }

      override suspend fun writeTo(t: TestNullableProtoData, sink: BufferedSink) {
          sink.writeUtf8(Json.encodeToString(t))
      }
  }
  ```

- [ ] **3.14** Create `<Platform>NullableProtoTestHelper` classes

  Follow the same pattern as `<Platform>ProtoTestHelper` but create a
  `GenericProtoDatastore<TestNullableProtoData>` instead:

  | Platform | Helper Class                     | Location                                                         |
  |----------|----------------------------------|------------------------------------------------------------------|
  | Android  | `AndroidNullableProtoTestHelper` | `androidDeviceTest/.../proto/AndroidNullableProtoTestHelper.kt`  |
  | Desktop  | `DesktopNullableProtoTestHelper` | `desktopTest/.../proto/DesktopNullableProtoTestHelper.kt`        |
  | iOS      | `IosNullableProtoTestHelper`     | `iosSimulatorArm64Test/.../proto/IosNullableProtoTestHelper.kt`  |

  Each helper provides `standard(datastoreName)` and `blocking(datastoreName)` factory methods,
  using `TestNullableProtoDataSerializer` and `TestNullableProtoData()` as the default value.

- [ ] **3.15** Create abstract test classes in `commonTest`

  - `AbstractNullableProtoFieldPreferenceTest` — suspending tests for nullable per-field
    proto preferences. Requires `nullableProtoDatastore` and `testDispatcher` abstract properties.
  - `AbstractNullableProtoFieldPreferenceBlockingTest` — blocking tests. Only requires
    `nullableProtoDatastore`.

- [ ] **3.16** Create platform-specific test subclasses using nullable proto test helpers

  Same pattern as **3.6**, with each platform subclass using the corresponding
  `<Platform>NullableProtoTestHelper`.

- [ ] **3.17** Nullable per-field preference test scenarios (suspending —
  in `AbstractNullableProtoFieldPreferenceTest`)

  **Nullable top-level scalar (`label: String?`):**

  - [ ] `field().get()` returns `null` when `label` is not set
    ```kotlin
    val labelPref = protoDatastore.field(
        key = "label",
        defaultValue = null as String?,
        getter = { it.label },
        updater = { proto, value -> proto.copy(label = value) },
    )
    ```
  - [ ] `field().set(value)` sets `label` to a non-null value, `get()` returns it
  - [ ] `field().set(null)` clears `label` back to `null`
  - [ ] `field().delete()` resets `label` to `null` (its `defaultValue`)
  - [ ] `field().asFlow()` emits `null` → `"hello"` → `null` transition

  **Nullable message field (`profile: TestNullableProfile?`):**

  - [ ] `field().get()` returns `null` when `profile` is not set
    ```kotlin
    val profilePref = protoDatastore.field(
        key = "profile",
        defaultValue = null as TestNullableProfile?,
        getter = { it.profile },
        updater = { proto, value -> proto.copy(profile = value) },
    )
    ```
  - [ ] `field().set(value)` sets `profile` to a non-null value
  - [ ] `field().set(null)` clears `profile` back to `null`
  - [ ] `field().update()` on nullable message — update only if present
    ```kotlin
    profilePref.update { it?.copy(nickname = "updated") }
    ```

  **Non-null scalar inside nullable parent (`profile?.nickname`):**

  - [ ] `field()` with safe-call getter and default when parent is null
    ```kotlin
    val nicknamePref = protoDatastore.field(
        key = "profile_nickname",
        defaultValue = "",
        getter = { it.profile?.nickname ?: "" },
        updater = { proto, value ->
            proto.copy(
                profile = (proto.profile ?: TestNullableProfile()).copy(nickname = value)
            )
        },
    )
    ```
  - [ ] `field().get()` returns `""` when `profile` is `null`
  - [ ] `field().set()` auto-creates `profile` when it was `null`
  - [ ] `field().set()` on `nickname` does not affect `profile.age`

  **Nullable scalar inside nullable parent (`profile?.age: Int?`):**

  - [ ] `field().get()` returns `null` when both `profile` is `null` and `age` is `null`
    ```kotlin
    val agePref = protoDatastore.field(
        key = "profile_age",
        defaultValue = null as Int?,
        getter = { it.profile?.age },
        updater = { proto, value ->
            proto.copy(
                profile = (proto.profile ?: TestNullableProfile()).copy(age = value)
            )
        },
    )
    ```
  - [ ] `field().set(25)` sets age, auto-creates `profile` if null
  - [ ] `field().set(null)` clears age to `null` without clearing `profile`
  - [ ] `field().delete()` resets to `null`

  **Deeply nested non-null scalar through nullable chain (`profile?.address?.city`):**

  - [ ] `field()` with multi-level safe-call getter
    ```kotlin
    val cityPref = protoDatastore.field(
        key = "profile_address_city",
        defaultValue = "",
        getter = { it.profile?.address?.city ?: "" },
        updater = { proto, value ->
            val currentProfile = proto.profile ?: TestNullableProfile()
            val currentAddress = currentProfile.address ?: TestNullableAddress()
            proto.copy(
                profile = currentProfile.copy(
                    address = currentAddress.copy(city = value)
                )
            )
        },
    )
    ```
  - [ ] `field().get()` returns `""` when entire chain is null
  - [ ] `field().set()` auto-creates both `profile` and `address` when both are null
  - [ ] `field().set()` on `city` does not affect `address.street` or `address.coordinates`
  - [ ] `field().update()` appends suffix when chain is already populated
  - [ ] `field().delete()` resets `city` to `""` without nullifying `address` or `profile`

  **Nullable message at deepest level (`profile?.address?.coordinates: TestCoordinates?`):**

  - [ ] `field().get()` returns `null` when `coordinates` is not set
    ```kotlin
    val coordsPref = protoDatastore.field(
        key = "profile_address_coordinates",
        defaultValue = null as TestCoordinates?,
        getter = { it.profile?.address?.coordinates },
        updater = { proto, value ->
            val currentProfile = proto.profile ?: TestNullableProfile()
            val currentAddress = currentProfile.address ?: TestNullableAddress()
            proto.copy(
                profile = currentProfile.copy(
                    address = currentAddress.copy(coordinates = value)
                )
            )
        },
    )
    ```
  - [ ] `field().set(TestCoordinates(1.0, 2.0))` sets coordinates, auto-creates parents
  - [ ] `field().set(null)` clears coordinates without affecting `address.city`
  - [ ] `field().delete()` resets to `null`

  **Cross-level isolation with nullable parents:**

  - [ ] Set `label` (nullable level 1), `profile.nickname` (non-null level 2 inside nullable
    parent), and `profile.address.city` (non-null level 3 inside two nullable parents)
    sequentially — verify all three retain their values
  - [ ] Setting `profile` to `null` clears all nested fields, but does not affect `label`

- [ ] **3.18** Nullable per-field preference test scenarios (blocking —
  in `AbstractNullableProtoFieldPreferenceBlockingTest`)

  - [ ] `field().getBlocking()` returns `null` for nullable field at default
  - [ ] `field().setBlocking(value)` and `getBlocking()` round-trip for nullable scalar
  - [ ] `field().setBlocking(null)` clears nullable field
  - [ ] `field().resetToDefaultBlocking()` on nullable field resets to `null`
  - [ ] `field()` property delegation works for nullable field (`by` syntax, `var x: String? by pref`)
  - [ ] `field().getBlocking()` for non-null scalar inside nullable parent returns default when
    parent is `null`
  - [ ] `field().setBlocking()` for deeply nested field auto-creates nullable parents
  - [ ] `field()` property delegation works for deeply nested non-null field through nullable
    chain (`by` syntax)

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

### Nullable fields and Wire-style proto3 models

`ProtoFieldPreference<P, T>` supports nullable `T` out of the box — nothing special is needed
in the implementation since Kotlin generics allow `T` to be nullable. The complexity lives
entirely in the user-provided `getter`/`updater` lambdas:

- **Nullable field (`T = String?`)**: `defaultValue = null`, `getter` returns the nullable field
  directly, `updater` sets it (including to `null`). `delete()` / `resetToDefault()` sets the
  field back to `null`.
- **Non-null field inside nullable parent**: `getter` uses safe-call chains with a fallback
  (`it.profile?.nickname ?: ""`), `updater` auto-creates the parent if null
  (`(proto.profile ?: TestNullableProfile()).copy(nickname = value)`).
- **Nullable field inside nullable parent**: Both `getter` and `defaultValue` are nullable,
  `updater` auto-creates the parent but preserves the null-ability of the field itself.

This design avoids any nullable-specific changes to `ProtoFieldPreference` while fully supporting
Wire-generated proto3 models where message fields and optional scalars are nullable.

### `asFlow()` emits `map { getter(it) }` — no `distinctUntilChanged()`

`ProtoFieldPreference.asFlow()` maps the entire proto flow through `getter` without applying
`distinctUntilChanged()`. This means if an unrelated field changes, the flow re-emits the same
field value. This is intentional: the user can add `.distinctUntilChanged()` themselves if needed,
and omitting it keeps the default behavior simple and predictable.
