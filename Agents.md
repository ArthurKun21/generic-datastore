# Generic Datastore

This repository contains a Kotlin Multiplatform library that provides a thin wrapper around AndroidX
DataStore Preferences and Proto DataStore. The main module is `generic-datastore`, with optional
Jetpack Compose extensions in `generic-datastore-compose`.

## Modules

- `:generic-datastore` – core preference and proto datastore wrapper library.
    - `core/` – shared interfaces and utilities (`Preference`, `Prefs`, `PrefsImpl`,
      `MappedPreference`, `Migration`, `PreferenceDefaults`, `PreferenceExtension`).
  - `preferences/` - DataStore wrapper implementations for `Preference` types.
  - `preferences/default/` – DataStore Preferences implementation (primitive types, enum types,
      kotlinx.serialization-backed types, and custom-serializer types).
  - `preferences/optional/` – nullable preference variants.
  - `preferences/backup/` – backup/restore support for preferences datastore.
  - `proto/` – Proto DataStore support (`ProtoPreference`, `ProtoDatastore`,
      `GenericProtoDatastore`).
  - Top-level package contains deprecated compatibility aliases that redirect to `core/`,
    `preferences/` and `preferences/default/`.
- `:generic-datastore-compose` – Compose helpers (e.g. `Prefs<T>.remember()`) built on the core
  module.
- `:app` and `:composeApp` – sample apps for local development.

## KMP Targets

Both library modules target:

- **Android** (`androidMain`)
- **Desktop / JVM** (`jvm("desktop")`)

## Workflow

- Use Gradle Kotlin DSL (`*.gradle.kts`).
- Prefer Kotlin Multiplatform idioms and keep shared APIs in `commonMain`.
- Keep this library as a thin convenience wrapper over `DataStore<Preferences>` / Proto DataStore
  and avoid heavy abstractions.

## Code Style

- Follow Spotless + ktlint rules configured in the root `build.gradle.kts`.
- Keep APIs small, predictable, and documented in README when public behavior changes.
- Do not use wildcard imports (e.g., import foo.bar.*).

## Testing and Quality Assurance

### Build verification

Ensure the module compiles by running the appropriate Gradle tasks and resolving any failures.

#### KMP modules targeting Android Build

- Compile the Android main source set:

    ```shell
    ./gradlew :<module-name>:compileAndroidMain
    ```

- Compile the Android host (unit test) source set:

    ```shell
    ./gradlew :<module-name>:compileAndroidHostTest
    ```

- Compile the Android device (instrumentation test) source set:

    ```shell
    ./gradlew :<module-name>:compileAndroidDeviceTest
    ```

#### KMP modules targeting Desktop (JVM) Build

- Compile the Desktop main source set:

    ```shell
    ./gradlew :<module-name>:compileKotlinDesktop
    ```

- Compile the Desktop test source set:

    ```shell
    ./gradlew :<module-name>:compileTestKotlinDesktop
    ```

### Running Tests

If no Android device or emulator is available, execute unit tests and at minimum compile the Android
instrumentation test source set to validate build correctness.

Prefer placing tests in `commonTest` whenever possible so they can be executed across all targets.
Platform-specific tests should only be added when platform behavior diverges.

Confirm all tests pass before merging changes. Resolve failures and add missing tests as needed.
Tests should verify expected behavior as well as edge cases, and must be placed in the appropriate
source sets:

* `commonTest` — Shared tests for all targets (preferred)
* `androidDeviceTest` — Android instrumentation tests (platform-specific only)
* `androidHostTest` — Android unit tests (platform-specific only)
* `desktopTest` — Desktop/JVM tests (platform-specific only)

### Abstract test class pattern

Tests use an abstract base class pattern to avoid duplicating test logic across platforms. Shared
test methods live in abstract classes in `commonTest`, while platform source sets
(`androidDeviceTest`, `desktopTest`) provide thin subclasses that only handle DataStore
initialization and teardown.

- `commonTest` — Abstract base classes (e.g. `AbstractDatastoreInstrumentedTest`,
  `AbstractDatastoreBlockingTest`) containing all test methods.
- `androidDeviceTest` / `desktopTest` — Concrete subclasses that override abstract properties
  (`preferenceDatastore`, `dataStore`, `testDispatcher`) and supply platform-specific setup/teardown.

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

#### KMP modules targeting Android Test

- Run Android unit tests:

    ```shell
    ./gradlew :<module-name>:testAndroidHostTest
    ```

- Run Android instrumentation tests (requires device/emulator):

    ```shell
    ./gradlew :<module-name>:connectedAndroidDeviceTest
    ```

#### KMP modules targeting Desktop (JVM) Test

- Run Desktop (JVM) tests (uses JUnit 5):

    ```shell
    ./gradlew :<module-name>:desktopTest
    ```
