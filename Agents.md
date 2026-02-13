# Generic Datastore

This repository contains a Kotlin Multiplatform library that provides a thin wrapper around AndroidX
DataStore Preferences and Proto DataStore. The main module is `generic-datastore`, with optional
Jetpack Compose extensions in `generic-datastore-compose`.

## Modules

- `:generic-datastore` – core preference and proto datastore wrapper library.
  - `core/` – shared interfaces and utilities (`BasePreference`, `DelegatedPreference`,
      `PreferenceDefaults`, `PreferenceExtension`, `SystemFileSystem`).
  - `preferences/` – DataStore wrapper implementations for `Preference` types
      (`PreferencesDatastore`, `GenericPreferencesDatastore`, `CreatePreferencesDatastore`,
      `Preferences`).
  - `preferences/core/` – DataStore Preferences implementation for primitive types
      (`BooleanPrimitive`, `DoublePrimitive`, `FloatPrimitive`, `IntPrimitive`, `LongPrimitive`,
      `StringPrimitive`, `StringSetPrimitive`, `GenericPreferenceItem`).
    - `preferences/core/custom/` – custom-serializer and enum types (`EnumPreference`,
        `KSerializedPrimitive`, `KSerializedListPrimitive`, `SerializedListPrimitive`,
        `ObjectPrimitive`, `CustomGenericPreferenceItem`).
    - `preferences/core/customSet/` – set-based custom types (`EnumSetPreference`,
        `KSerializedSetPrimitive`, `SerializedSetPrimitive`, `CustomSetGenericPreferenceItem`).
  - `preferences/optional/` – nullable preference variants (`NullableBooleanPrimitive`,
      `NullableDoublePrimitive`, `NullableFloatPrimitive`, `NullableIntPrimitive`,
      `NullableLongPrimitive`, `NullableStringPrimitive`, `NullableStringSetPrimitive`,
      `NullableGenericPreferenceItem`).
    - `preferences/optional/custom/` – nullable custom types (`NullableEnumPreference`,
        `NullableKSerializedPrimitive`, `NullableKSerializedListPrimitive`,
        `NullableSerializedListPrimitive`, `NullableObjectPrimitive`,
        `NullableCustomGenericPreferenceItem`).
  - `preferences/utils/` – preference utility extensions (`MappedPreference`, `Extensions`).
  - `preferences/backup/` – backup/restore support for preferences datastore
      (`BackupPreference`, `PreferenceBackupCreator`, `PreferenceBackupRestorer`,
      `BackupParsingException`, `Migration`).
  - `proto/` – Proto DataStore support (`ProtoPreference`, `ProtoDatastore`,
      `GenericProtoDatastore`, `CreateProtoDatastore`, `ProtoFieldPrefs`).
    - `proto/core/` – core proto internals (`GenericProtoPreferenceItem`,
        `ProtoFieldPreference`).
    - `proto/custom/` – custom-serializer proto field types (`ProtoSerialFieldPreference`).
      - `proto/custom/core/` – non-nullable custom field implementations (`EnumField`,
          `KSerializedField`, `KSerializedListField`, `SerializedField`, `SerializedListField`,
          `DecodeUtils`).
      - `proto/custom/optional/` – nullable custom field implementations (`NullableEnumField`,
          `NullableKSerializedField`, `NullableKSerializedListField`, `NullableSerializedField`,
          `NullableSerializedListField`).
      - `proto/custom/set/` – set-based custom field implementations (`EnumSetField`,
          `KSerializedSetField`, `SerializedSetField`).
  - Top-level package contains deprecated compatibility aliases that redirect to `core/`,
      `preferences/`, `preferences/core/custom/`, and `preferences/utils/`.
- `:generic-datastore-compose` – Compose helpers built on the core module.
  - `Remember.kt` – `DelegatedPreference<T>.remember()` extension.
  - `PrefsComposeState.kt` – `MutableState` backed by a `DelegatedPreference`.
  - `batch/` – batch Compose extensions (`RememberBatchRead`, `RememberPreferences`,
      `RememberPreferencesLocal`, `BatchPrefsComposeState`, `PreferencesState`,
      `LocalPreferencesDatastore`).
- `:app` and `:composeApp` – sample apps for local development.

## KMP Targets

Both library modules target:

- **Android** (`androidMain`)
- **Desktop / JVM** (`jvm("desktop")`)
- **iOS** (`iosX64`, `iosArm64`, `iosSimulatorArm64`)

## Workflow

- Use Gradle Kotlin DSL (`*.gradle.kts`).
- Prefer Kotlin Multiplatform idioms and keep shared APIs in `commonMain`.
- Keep this library as a thin convenience wrapper over `DataStore<Preferences>` / Proto DataStore
  and avoid heavy abstractions.

## Code Style

- Follow Spotless + ktlint rules configured in the root `build.gradle.kts`.
- Keep APIs small, predictable, and documented in README when public behavior changes.
- Do not use wildcard imports (e.g., import foo.bar.*).
- Use `kotlinx.coroutines.IO` for CoroutineDispatcher instead of `kotlinx.coroutines.Dispatchers.IO` due to iOS compatibility.

## Testing and Quality Assurance

### Build verification

Ensure the module compiles by running the appropriate Gradle tasks and resolving any failures.

#### KMP modules targeting Android Build

- Compile the Android main source set `./gradlew :<module-name>:compileAndroidMain`

- Compile the Android device (instrumentation test) source set `./gradlew :<module-name>:compileAndroidDeviceTest`

#### KMP modules targeting Desktop (JVM) Build

- Compile the Desktop main source set `./gradlew :<module-name>:compileKotlinDesktop`

- Compile the Desktop test source set `./gradlew :<module-name>:compileTestKotlinDesktop`

### Running Tests

If no Android device or emulator is available, execute unit tests and at minimum compile the Android
instrumentation test source set to validate build correctness.

Prefer placing tests in `commonTest` whenever possible so they can be executed across all targets.
Platform-specific tests should only be added when platform behavior diverges.

Confirm all tests pass before merging changes. Resolve failures and add missing tests as needed.
Tests should verify expected behavior as well as edge cases, and must be placed in the appropriate
source sets:

- `commonTest` — Shared tests for all targets (preferred)
- `androidDeviceTest` — Android instrumentation tests (platform-specific only)
- `androidHostTest` — Android unit tests (platform-specific only)
- `desktopTest` — Desktop/JVM tests (platform-specific only)
- `iosSimulatorArm64Test` — iOS simulator tests (platform-specific only, requires macOS)

### Abstract test class pattern

Tests use an abstract base class pattern to avoid duplicating test logic across platforms. Shared
test methods live in abstract classes in `commonTest`, while platform source sets
(`androidDeviceTest`, `desktopTest`, `iosSimulatorArm64Test`) provide thin subclasses that only
handle DataStore initialization and teardown.

- `commonTest` — Abstract base classes (e.g. `AbstractDatastoreInstrumentedTest`,
  `AbstractDatastoreBlockingTest`) containing all test methods.
- `androidDeviceTest` / `desktopTest` / `iosSimulatorArm64Test` — Concrete subclasses that override
  abstract properties (`preferenceDatastore`, `dataStore`, `testDispatcher`) and supply
  platform-specific setup/teardown.

When adding new tests, add them to the abstract class in `commonTest` so they run on all platforms
automatically. Only add tests directly to a platform source set when the test requires
platform-specific APIs that cannot be abstracted.

### Separating blocking and suspending tests

Do not mix suspending and blocking assertions in the same abstract test class. When adding a new
preference type, create both:

- `Abstract<Feature>Test` — suspending tests (`get()`, `set()`, `asFlow()`, `delete()`, `update()`,
  `resetToDefault()`, `toggle()`). Requires `dataStore`, `testDispatcher`, and `preferenceDatastore`
  abstract properties; uses `runTest(testDispatcher)`.
- `Abstract<Feature>BlockingTest` — blocking tests (`getBlocking()`, `setBlocking()`,
  `resetToDefaultBlocking()`, property delegation). Only requires `preferenceDatastore`; runs as
  plain (non-suspending) test functions.

### Platform-specific test helpers

Each platform source set has a corresponding test helper class that encapsulates the common
setup/teardown logic for DataStore tests. These helpers reduce boilerplate in individual test files
and centralize platform-specific initialization code.

| Platform      | Helper Class        | Location                                     |
|---------------|---------------------|----------------------------------------------|
| Android       | `AndroidTestHelper` | `androidDeviceTest/.../AndroidTestHelper.kt` |
| Desktop (JVM) | `DesktopTestHelper` | `desktopTest/.../DesktopTestHelper.kt`       |
| iOS           | `IosTestHelper`     | `iosSimulatorArm64Test/.../IosTestHelper.kt` |

Each helper provides two factory methods:

- `standard(datastoreName)` — For suspending tests using `StandardTestDispatcher` with a custom
  `CoroutineScope`. Exposes `preferenceDatastore`, `dataStore`, and `testDispatcher` properties.
- `blocking(datastoreName)` — For blocking tests using `UnconfinedTestDispatcher` without a custom
  scope. Only exposes `preferenceDatastore`.

#### Usage examples

**Standard test (Android/iOS):**

```kotlin
class MyFeatureTest : AbstractMyFeatureTest() {
    private val helper = AndroidTestHelper.standard("test_my_feature")  // or IosTestHelper

    override val preferenceDatastore get() = helper.preferenceDatastore
    override val dataStore get() = helper.dataStore
    override val testDispatcher get() = helper.testDispatcher

    @Before  // or @BeforeTest for iOS
    fun setup() = helper.setup()

    @After   // or @AfterTest for iOS
    fun tearDown() = helper.tearDown()
}
```

**Standard test (Desktop — requires temp folder from JUnit's `@TempDir`):**

```kotlin
class MyFeatureTest : AbstractMyFeatureTest() {
    @TempDir
    lateinit var tempFolder: File

    private val helper = DesktopTestHelper.standard("test_my_feature")

    override val preferenceDatastore get() = helper.preferenceDatastore
    override val dataStore get() = helper.dataStore
    override val testDispatcher get() = helper.testDispatcher

    @BeforeTest
    fun setup() = helper.setup(tempFolder.absolutePath)

    @AfterTest
    fun tearDown() = helper.tearDown()
}
```

**Blocking test (Android with companion object):**

```kotlin
class MyFeatureBlockingTest : AbstractMyFeatureBlockingTest() {
    companion object {
        private val helper = AndroidTestHelper.blocking("test_my_feature_blocking")

        @JvmStatic
        @BeforeClass
        fun setupClass() = helper.setup()

        @JvmStatic
        @AfterClass
        fun tearDownClass() = helper.tearDown()
    }

    override val preferenceDatastore get() = helper.preferenceDatastore
}
```

#### KMP modules targeting Android Test

- Run Android instrumentation tests (requires device/emulator) `./gradlew :<module-name>:connectedAndroidDeviceTest`

#### KMP modules targeting Desktop (JVM) Test

- Run Desktop (JVM) tests (uses JUnit 5) `./gradlew :<module-name>:desktopTest`

#### KMP modules targeting iOS Test

- Run iOS simulator tests (requires macOS with Xcode) `./gradlew :<module-name>:iosSimulatorArm64Test`

### Proto DataStore test helpers

Proto DataStore tests follow the same abstract test class pattern but use separate helpers because
they wrap `GenericProtoDatastore<T>` instead of `GenericPreferencesDatastore`.

| Platform      | Helper Class                   | Proto Type              |
|---------------|--------------------------------|-------------------------|
| Android       | `AndroidProtoTestHelper`       | `TestProtoData`         |
| Desktop (JVM) | `DesktopProtoTestHelper`       | `TestProtoData`         |
| iOS           | `IosProtoTestHelper`           | `TestProtoData`         |
| Android       | `AndroidNullableProtoTestHelper` | `TestNullableProtoData` |
| Desktop (JVM) | `DesktopNullableProtoTestHelper` | `TestNullableProtoData` |
| iOS           | `IosNullableProtoTestHelper`   | `TestNullableProtoData` |

Each helper provides `standard(datastoreName)` and `blocking(datastoreName)` factory methods,
identical in contract to the Preferences DataStore helpers. The proto helpers expose
`protoDatastore` and `testDispatcher` instead of `preferenceDatastore` and `dataStore`.

**Proto abstract test classes in `commonTest`:**

- `AbstractProtoDatastoreTest` / `AbstractProtoDatastoreBlockingTest` — whole-object `data()`
  tests using `TestProtoData`.
- `AbstractProtoFieldPreferenceTest` / `AbstractProtoFieldPreferenceBlockingTest` — per-field
  `field()` tests using `TestProtoData` (non-nullable, 3-level nesting).
- `AbstractNullableProtoDatastoreTest` / `AbstractNullableProtoDatastoreBlockingTest` —
  whole-object `data()` and per-field `field()` tests using `TestNullableProtoData`
  (nullable fields at all nesting levels).
- `AbstractNullableProtoFieldPreferenceTest` / `AbstractNullableProtoFieldPreferenceBlockingTest`
  — per-field `field()` tests focused on nullable field edge cases using `TestNullableProtoData`.

**Proto custom field abstract test classes in `commonTest`:**

- `proto/custom/core/` — non-nullable custom field tests:
  - `AbstractProtoEnumFieldTest` / `AbstractProtoEnumFieldBlockingTest`
  - `AbstractProtoSerializedFieldTest` / `AbstractProtoSerializedFieldBlockingTest`
  - `AbstractProtoKserializedFieldTest` / `AbstractProtoKserializedFieldBlockingTest`
  - `AbstractProtoSerializedListFieldTest` / `AbstractProtoSerializedListFieldBlockingTest`
  - `AbstractProtoKserializedListFieldTest` / `AbstractProtoKserializedListFieldBlockingTest`
- `proto/custom/optional/` — nullable custom field tests:
  - `AbstractProtoNullableEnumFieldTest` / `AbstractProtoNullableEnumFieldBlockingTest`
  - `AbstractProtoNullableSerializedFieldTest` / `AbstractProtoNullableSerializedFieldBlockingTest`
  - `AbstractProtoNullableKserializedFieldTest` / `AbstractProtoNullableKserializedFieldBlockingTest`
  - `AbstractProtoNullableSerializedListFieldTest` / `AbstractProtoNullableSerializedListFieldBlockingTest`
  - `AbstractProtoNullableKserializedListFieldTest` / `AbstractProtoNullableKserializedListFieldBlockingTest`
- `proto/custom/set/` — set-based custom field tests:
  - `AbstractProtoEnumSetFieldTest` / `AbstractProtoEnumSetFieldBlockingTest`
  - `AbstractProtoSerializedSetFieldTest` / `AbstractProtoSerializedSetFieldBlockingTest`
  - `AbstractProtoKserializedSetFieldTest` / `AbstractProtoKserializedSetFieldBlockingTest`

### Proto DataStore architecture

The proto module uses a delegation pattern to avoid code duplication:

- `ProtoFieldPreference<P, T>` — internal class that implements `BasePreference<T>` with all
  DataStore access logic (get, set, update, delete, asFlow, stateIn, getBlocking, setBlocking).
  Uses `getter: (P) -> T` and `updater: (P, T) -> P` lambdas to map between the proto message
  and individual field values.
- `GenericProtoPreferenceItem<T>` — whole-proto wrapper. Delegates `BasePreference<T>` to a
  `ProtoFieldPreference<T, T>` with identity getter/updater. Adds `DelegatedPreference<T>`
  contract (`resetToDefaultBlocking()`, `getValue()`, `setValue()` for property delegation).
- `ProtoFieldPrefs<P, T>` — per-field wrapper. Delegates `BasePreference<T>` by a
  `ProtoSerialFieldPreference<P, T>`. Adds `ProtoPreference<T>` contract.
- `GenericProtoDatastore<T>` — factory that creates `GenericProtoPreferenceItem` via `data()`
  and `ProtoFieldPrefs` via `field()`.

The `delete()` method on field preferences resets only the targeted field to its default value
(via `resetToDefault()` → `set(defaultValue)` → `updater(current, fieldDefault)`). It does not
reset the entire proto to its default. There is no concept of removing a single field from a proto
message.

## Platform-Specific Notes

### Okio `FileSystem.SYSTEM` and iOS

Okio's `FileSystem.SYSTEM` cannot be referenced directly in `commonMain` when compiling for iOS
targets. The compiler reports `Unresolved reference 'SYSTEM'` during iOS publication or compilation.

**Workaround:** Use the `expect`/`actual` pattern. Declare an `internal expect val` in `commonMain`
and provide `actual` implementations in each platform source set (`androidMain`, `desktopMain`,
`iosMain`) that return `FileSystem.SYSTEM`. The `iosMain` source set covers all three iOS targets
(`iosX64`, `iosArm64`, `iosSimulatorArm64`) via `applyDefaultHierarchyTemplate()`.

Files involved:

- `commonMain/.../core/SystemFileSystem.kt` — `expect` declaration
- `androidMain/.../core/SystemFileSystem.android.kt` — Android `actual`
- `desktopMain/.../core/SystemFileSystem.desktop.kt` — Desktop/JVM `actual`
- `iosMain/.../core/SystemFileSystem.ios.kt` — iOS `actual`

When adding new code in `commonMain` that needs `FileSystem.SYSTEM`, use the `systemFileSystem`
property instead of referencing `FileSystem.SYSTEM` directly.
