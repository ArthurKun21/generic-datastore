# Rethrow `CorruptionException` — Implementation Plan

- **Branch:** `fix/problems`
- **Status:** Proposed (2026-09-19)
- **Scope:** `:generic-datastore-preferences`, `:generic-datastore-proto`, `:generic-datastore-compose` (secondary), README/docs, API dump files

## 1. Goal

`androidx.datastore.core.CorruptionException` extends `java.io.IOException` (verified by
inspecting `datastore-core-jvm-1.3.0-alpha11.jar`). Any `catch` / `Flow.catch` that matches on
`IOException` therefore also matches `CorruptionException` and silently swallows it, making
on-disk corruption indistinguishable from a transient I/O error.

DataStore's contract is:

- If the user supplies a `ReplaceFileCorruptionHandler`, DataStore invokes it and replaces the
  file — the exception never reaches the flow.
- If the user does **not** supply a handler (the default in this library — both
  `createPreferencesDatastore(...)` and `createProtoDatastore(...)` default
  `corruptionHandler = null`), DataStore rethrows the `CorruptionException` into
  `dataStore.data` so the caller can react (log, crash, backup-restore, etc.).

This library currently breaks that contract in two places by treating `CorruptionException` as
a plain `IOException` and substituting a default snapshot.

**Decided behavior (user-confirmed, 2026-09-19): the two modules intentionally diverge.**

- **Preferences (`dataOrEmpty`)** keeps its resilience contract for transient I/O failures:
  `IOException` → emit `emptyPreferences()`; only `CorruptionException` is rethrown.
- **Proto (`ProtoSerialFieldPreference`)** rethrows **both**: `CorruptionException` propagates
  as-is, and a plain `IOException` is rethrown **wrapped as `CorruptionException`** (with the
  original as cause). Rationale: a proto file is a single opaque serialized blob — any read
  failure means the whole message is unusable, so emitting `defaultProtoValue` would silently
  hide total data loss. Preferences can plausibly degrade to per-key defaults; proto cannot.

The `dataOrEmpty` helper stays preferences-only — do **not** share it with proto. The proto
module keeps its own flow pipeline in `ProtoSerialFieldPreference` with the stricter
semantics above (this is the "split" of the two error-handling contracts).

## 2. Primary defect — where the exception is swallowed

### 2.1 Preferences: `dataOrEmpty`

**File:**
`generic-datastore-preferences/src/commonMain/kotlin/io/github/arthurkun/generic/datastore/preferences/utils/Extensions.kt`

```kotlin
internal val DataStore<Preferences>.dataOrEmpty: Flow<Preferences>
    get() = data
        .catch { error ->
            if (error is androidx.datastore.core.IOException) {
                emit(emptyPreferences())   // <- also catches CorruptionException
            } else {
                throw error
            }
        }
```

`dataOrEmpty` is the single funnel for **all** preference reads — it is imported by
`GenericPreferenceItem`, `NullableGenericPreferenceItem`, `CustomGenericPreferenceItem`,
`NullableCustomGenericPreferenceItem`, `CustomSetGenericPreferenceItem` and used by
`GenericPreferencesDatastore` batch reads. Every preference silently degrades to defaults when
the file is corrupted and no handler was registered.

**Fix:**

```kotlin
.catch { error ->
    when {
        error is CorruptionException -> throw error
        error is androidx.datastore.core.IOException -> emit(emptyPreferences())
        else -> throw error
    }
}
```

Notes:

- Import `androidx.datastore.core.CorruptionException` (the fully-qualified
  `androidx.datastore.core.IOException` can become a normal import; keep ktlint ordering).
- Update the KDoc: corruption now propagates instead of returning `emptyPreferences()`;
  document that registering a `ReplaceFileCorruptionHandler` via `createPreferencesDatastore`
  is the opt-in way to get auto-replace behavior.


### 2.2 Proto: `ProtoSerialFieldPreference.asFlow()`

**File:**
`generic-datastore-proto/src/commonMain/kotlin/io/github/arthurkun/generic/datastore/proto/custom/ProtoSerialFieldPreference.kt` (lines 62–67)

```kotlin
override fun asFlow(): Flow<T> = datastore.data
    .catch { e ->
        if (e is IOException) emit(defaultProtoValue) else throw e   // <- same problem
    }
    .map { getter(it) }
    .distinctUntilChanged()
```

This class backs every proto preference (`ProtoFieldPreference` extends it, and
`GenericProtoPreferenceItem` / all custom field types delegate to it), so proto reads have the
same swallowing behavior.

**Fix (proto rethrows BOTH `IOException` and `CorruptionException` as `CorruptionException`):**

```kotlin
.catch { e ->
    when (e) {
        is CorruptionException -> throw e
        is IOException -> throw CorruptionException(
            message = e.message ?: "Proto datastore read failed",
            cause = e,
        )
        else -> throw e
    }
}
```

Notes:

- `androidx.datastore.core.CorruptionException(String, Throwable?)` has a public two-arg
  constructor (verified in the 1.3.0-alpha11 jar), so wrapping is straightforward.
- Remove the `defaultProtoValue` fallback from `asFlow()` entirely. The `defaultProtoValue`
  constructor parameter may still be needed by `GenericProtoPreferenceItem` identity
  getter/updater paths — audit usages before removing the parameter; if it becomes unused,
  remove it and thread the change through `ProtoFieldPreference`, `GenericProtoDatastore`, and
  all custom field factories (they are `internal`, so no public API break, but the `api/`
  klib/android/jvm dumps must be regenerated if signatures leak).
- `get()` / `getBlocking()` automatically rethrow too, since they are built on
  `asFlow().first()`. Writes (`set`/`update` via `datastore.updateData`) already propagate
  exceptions unchanged — no change needed there.

Also update the stale KDoc on `ProtoFieldPreference`
(`generic-datastore-proto/.../proto/core/ProtoFieldPreference.kt` line 19) which says
"used as fallback on [IOException]" — with the new behavior there is no I/O fallback at all;
rewrite the doc for `defaultProtoValue` (or delete the doc line if the parameter is removed).

### 2.3 Intentional behavior change

This is a deliberate behavioral break, and it differs per module:

**Preferences:**

- **Before:** corrupted file + no handler → flows silently emit defaults; data loss is invisible.
- **After:** corrupted file + no handler → `CorruptionException` propagates to the flow
  collector / `get()` caller. Plain `IOException` still falls back to `emptyPreferences()`
  (unchanged resilience contract). Users who want auto-replace pass
  `ReplaceFileCorruptionHandler` — which the factory functions already expose.

**Proto (stricter):**

- **Before:** corrupted file (or any read `IOException`) + no handler → flows silently emit
  `defaultProtoValue` and every field reads its default; data loss is invisible.
- **After:** **any** read failure (`CorruptionException` or `IOException`) surfaces as
  `CorruptionException` from `asFlow()` / `get()` / `getBlocking()`. There is no silent
  default path. Auto-replace is only available by registering a `ReplaceFileCorruptionHandler`
  in `createProtoDatastore`, which replaces the file before the exception reaches the flow.

Call both out in README (see §5) and in the changelog/commit message.

## 3. Test plan

Follow the repo's abstract-test-class pattern. New tests go in `commonTest` abstract classes;
platform subclasses already exist or are thin shells.

### 3.1 Preferences — extend `AbstractDataOrEmptyTest`

**File:**
`generic-datastore-preferences/src/commonTest/kotlin/io/github/arthurkun/generic/datastore/preferences/AbstractDataOrEmptyTest.kt`

Add (mirroring the fake-`DataStore` style already used there):

1. `dataOrEmpty_rethrowsCorruptionException` — fake `DataStore` whose `data` throws
   `CorruptionException("corrupt", null)`; assert `assertFailsWith<CorruptionException>` on
   `.first()`.
2. `dataOrEmpty_doesNotEmitFallbackOnCorruption` — assert the flow never emitted
   `emptyPreferences()` before the throw (e.g. `toList()` inside `assertFailsWith`).

The existing `dataOrEmpty_emitsEmptyPreferencesOnIOException` test proves the plain
`IOException` fallback still works and must stay green unchanged.

### 3.2 Proto — new abstract test for the catch block

There is no existing abstract test for `ProtoSerialFieldPreference`'s error path, and the class
is `internal`. Approach:

- Create `AbstractProtoCorruptionTest` in
  `generic-datastore-proto/src/commonTest/.../proto/core/` that builds a broken fake
  `DataStore<T>` (throws from `data`) and constructs the preference through
  `GenericProtoDatastore(...).field(...)` with the fake injected. Verify the
  `GenericProtoDatastore` constructor visibility from `commonTest` first; if it cannot accept
  an external `DataStore`, test at the `ProtoFieldPrefs` level or add an internal-for-test seam.
- Tests to add:
  1. `asFlow` rethrows `CorruptionException` unchanged (fake store throws it from `data`;
     assert the same instance reaches the collector).
  2. `asFlow` wraps a plain `IOException` into `CorruptionException` — assert
     `assertFailsWith<CorruptionException>` and that `cause` is the original `IOException`.
  3. `get()` propagates the same failure (it is `asFlow().first()` under the hood).
  4. Non-I/O exceptions (e.g. `IllegalStateException`) still propagate unchanged.
- Also check whether any existing proto abstract tests relied on the old `defaultProtoValue`
  fallback behavior and update them.

### 3.3 End-to-end (real file corruption) — JVM only

Optionally add `jvmTest` integration tests that overwrite a real datastore file with garbage
bytes and assert the full pipeline against real androidx DataStore. Place next to
`DesktopPreferencesDatastoreLifecycleTest` / `DesktopProtoDatastoreLifecycleTest`:

- **Preferences:** `get()` throws `CorruptionException` with no handler; with a
  `ReplaceFileCorruptionHandler` registered, defaults are returned (documents the opt-out).
- **Proto:** `get()` throws `CorruptionException` with no handler; with a handler registered,
  the default proto is returned.

### 3.4 Verification commands

```bash
./gradlew :generic-datastore-preferences:jvmTest :generic-datastore-proto:jvmTest
./gradlew :generic-datastore-preferences:compileAndroidMain :generic-datastore-proto:compileAndroidMain
./gradlew :generic-datastore-preferences:compileAndroidDeviceTest :generic-datastore-proto:compileAndroidDeviceTest
./gradlew spotlessCheck   # or spotlessApply if it complains
```

The preferences change is internal-only (`dataOrEmpty` is `internal`, `ProtoSerialFieldPreference`
is `internal`), so the `api/` dump files should not change for the basic fix. If the proto fix
also removes the now-unused `defaultProtoValue` fallback parameter (§2.2 notes), re-run the API
dump task for `:generic-datastore-proto` to confirm nothing public leaks and regenerate dumps
if needed.

## 4. Secondary problems found (same PR or follow-up)

### 4.1 Compose write-path swallows `CorruptionException`

**Files:**
- `generic-datastore-compose/src/commonMain/kotlin/io/github/arthurkun/generic/datastore/PrefsComposeState.kt` (lines 67–82)
- `generic-datastore-compose/src/commonMain/kotlin/io/github/arthurkun/generic/datastore/batch/BatchPrefsComposeState.kt` (lines 73–86)

Both `scope.launch` write blocks do:

```kotlin
try { prefs.set(value) }            // / datastore.batchWrite { ... }
catch (e: CancellationException) { throw e }
catch (_: Exception) { /* clear local override */ }
```

The blanket `catch (_: Exception)` swallows `CorruptionException` (and every other write
failure) — the UI just reverts to the last persisted/default value with no signal. Suggested
fix: add `catch (e: CorruptionException) { throw e }` (rethrowing into the caller-supplied
`scope`, where a `CoroutineExceptionHandler` can observe it) before the generic catch. Lower
priority than §2 because it only affects the Compose write path, not reads.

### 4.2 Stale KDoc

- `ProtoFieldPreference.kt` line 19: "used as fallback on [IOException]" → with the new proto
  behavior there is no I/O fallback at all; rewrite or remove together with the
  `defaultProtoValue` parameter audit (§2.2).
- `Extensions.kt` (`dataOrEmpty`) KDoc: "substituting [emptyPreferences] for I/O failures" →
  clarify that corruption propagates while transient I/O errors still fall back.
- `Serialization.kt` / `DecodeUtils.kt` fallback helpers: behavior is fine (per-field decode
  fallback ≠ whole-file corruption), no change needed.

### 4.3 Not problems (checked, no action)

- `Serialization.kt` (`deserializeOrDefault`/`deserializeOrNull`/`deserializeSet`/`deserializeList`)
  and `proto/custom/**` element-level skips: per-field JSON fallback after a successful file
  read is deliberate, documented design. Leave as-is.
- `MappedPreference` convert/reverse fallbacks: documented API behavior. Leave as-is.
- `Dispatchers.IO` usages all import `kotlinx.coroutines.IO` (iOS-safe) — AGENTS.md rule satisfied.
- Sample apps swallow exceptions in UI handlers — samples, not library; out of scope.

## 5. Documentation updates

- README: in the corruption-handler section (around the `createPreferencesDatastore` example
  with `corruptionHandler`), document the divergent contracts explicitly:
  - **Preferences:** without a handler, `CorruptionException` propagates from `asFlow()`/`get()`;
    transient `IOException` still falls back to empty/default values. With a handler, the file
    is replaced as before.
  - **Proto:** without a handler, **any** read failure surfaces as `CorruptionException` —
    there is no silent default fallback. With a handler, the file is replaced as before.
- KDoc updates listed in §2.1/§2.2/§4.2.

## 6. Implementation order

1. Fix `Extensions.kt` (`dataOrEmpty`) + KDoc: rethrow `CorruptionException`, keep the
   `IOException` → `emptyPreferences()` fallback (preferences contract).
2. Fix `ProtoSerialFieldPreference.asFlow()`: rethrow `CorruptionException` as-is and wrap
   plain `IOException` into `CorruptionException` (proto contract — no `defaultProtoValue`
   fallback). Audit `defaultProtoValue` usages; remove the parameter if it becomes unused and
   thread the change through `ProtoFieldPreference`, `GenericProtoDatastore`, and the custom
   field factories. Update `ProtoFieldPreference` KDoc.
3. Add/extend tests per §3.1, §3.2 (+ optional §3.3).
4. (Same PR or follow-up) Compose write-path rethrow per §4.1 + tests in
   `PrefsComposeStateTest` / compose batch tests.
5. README note (divergent contracts) + run `spotlessApply`, full `jvmTest`, Android compile
   tasks, and proto API dump check if §6.2 removed a parameter.

