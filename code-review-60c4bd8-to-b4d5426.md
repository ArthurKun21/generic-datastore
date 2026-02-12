# Code Review: `60c4bd8..b4d5426`

**Focus:** Maintainability & Developer Experience  
**Scope:** 200 files changed, +13,166 / −425 lines across 22 commits  
**Summary:** Major expansion of the Proto DataStore module — adds per-field preferences, custom-type field factories (enum, serialized, kserialized, sets), nullable variants, a new `protoApp` sample application, and comprehensive cross-platform tests.

---

## Architecture & Design

### ✅ Strengths

| Area | Detail |
|------|--------|
| **Delegation pattern** | `ProtoFieldPreference` is the single source of truth for all DataStore access logic. `GenericProtoPreferenceItem` and `ProtoFieldPrefs` delegate to it, eliminating duplicated IO/flow/blocking code. |
| **Abstract test class pattern** | All test logic lives in `commonTest` abstract classes; platform source sets contain only thin ~20-line subclasses. This is excellent for maintainability. |
| **Platform test helpers** | `AndroidProtoTestHelper`, `DesktopProtoTestHelper`, `IosProtoTestHelper` (and their custom/nullable counterparts) centralize setup/teardown boilerplate effectively. |
| **Reified extension functions** | The inline `reified` extensions on `ProtoDatastore` (e.g., `enumField`, `kserializedField`) significantly improve call-site ergonomics by eliminating explicit `enumValues()` / `serializer()` arguments. |
| **`safeDeserialize` utility** | Centralised deserialisation error handling with proper `CancellationException` re-throw is correct and prevents silent coroutine swallowing. |
| **`distinctUntilChanged` on field flows** | Smart addition — without it, every proto-level write would re-emit to *all* field observers even if their specific field didn't change. |
| **Documentation** | KDoc on all public API surface (`ProtoDatastore` interface methods) is thorough with code examples. |

---

## Issues Found

### 🔴 Issue #1 — Duplicate `ProtoFieldPreference` instantiation (MEDIUM)

**File:** `generic-datastore/.../proto/core/GenericProtoPreferenceItem.kt` (lines 24–40)

**Problem:** Two identical `ProtoFieldPreference` instances are created — one for the `by` delegation clause (line 24) and another as a `private val delegate` (line 33). These are separate objects pointing at the same DataStore, which is functionally correct but wasteful and confusing.

**Why it matters:** Future maintainers may change one and forget the other, introducing subtle divergence bugs. The `by` clause delegates `BasePreference` methods to one instance, while `resetToDefaultBlocking`/`getValue`/`setValue` use the other.

**Fix:** Extract a single `ProtoFieldPreference` instance and delegate to it in both places:

```kotlin
internal class GenericProtoPreferenceItem<T>(
    datastore: DataStore<T>,
    defaultValue: T,
    key: String = "proto_data",
) : ProtoPreference<T> {

    private val delegate = ProtoFieldPreference(
        datastore = datastore,
        key = key,
        defaultValue = defaultValue,
        getter = { it },
        updater = { _, value -> value },
        defaultProtoValue = defaultValue,
    )

    // BasePreference delegation
    override fun key() = delegate.key()
    override suspend fun get() = delegate.get()
    override suspend fun set(value: T) = delegate.set(value)
    // ... etc., or use a companion factory approach
}
```

Alternatively, make `ProtoFieldPrefs<T, T>` the single implementation for the whole-proto case, unifying it with the field case.

---

### 🔴 Issue #2 — `data()` allocates a new instance on every call (MEDIUM)

**File:** `generic-datastore/.../proto/GenericProtoDatastore.kt` (line 39)

**Problem:** `data()` creates a new `GenericProtoPreferenceItem` each time it's called. Callers who write `protoDatastore.data().get()` repeatedly will create garbage preference objects.

**Why it matters:** It's a DX footgun — developers may expect `data()` to return a stable object (like `field()` calls they'd typically store in a `val`). While functionally harmless (all instances share the same `DataStore`), it creates unnecessary allocations and confuses equality semantics.

**Fix:** Cache the `ProtoPreference<T>` as a lazy property:

```kotlin
private val _data by lazy {
    GenericProtoPreferenceItem(datastore, defaultValue, key)
}
override fun data(): ProtoPreference<T> = _data
```

---

### 🟡 Issue #3 — `enumField` lookup uses `first()` which throws on unknown values (LOW)

**File:** `generic-datastore/.../proto/core/custom/EnumField.kt` (line 18), and similar in `EnumSetField.kt`, `NullableEnumField.kt`

**Problem:** `enumValues.first { it.name == name }` throws `NoSuchElementException` if the stored string doesn't match any enum constant. While `safeDeserialize` catches this and returns the fallback, using `firstOrNull` would be more intentional and avoid relying on exception-driven control flow for a predictable scenario (e.g., enum values evolving over app versions).

**Fix:** Use `firstOrNull` with explicit fallback:

```kotlin
enumValues.firstOrNull { it.name == name } ?: defaultValue
```

---

### 🟡 Issue #4 — Massive platform test file proliferation (LOW — DX concern)

**Files:** 102 new platform-specific test files across `androidDeviceTest`, `desktopTest`, `iosSimulatorArm64Test`

**Problem:** Each custom field type requires **6 platform test files** (2 per platform × 3 platforms: standard + blocking). Each file is ~20 lines of pure boilerplate. With 17+ abstract test classes, this creates 100+ nearly identical files.

**Why it matters:** This is the biggest maintainability burden in this diff. Adding a new field type means creating 6+ boilerplate files by hand. File navigation becomes painful with so many similarly named files.

**Suggestions:**
- Consider a Gradle codegen task or KSP processor to auto-generate platform subclasses from a manifest of abstract test classes.
- At minimum, document a "new field type checklist" so contributors know which files to create.
- Evaluate whether some tests could use JUnit 5's `@TestFactory` or parameterised tests to reduce the number of classes.

---

### 🟡 Issue #5 — `ProtoDatastore<T>` interface is very large (LOW — DX concern)

**File:** `generic-datastore/.../proto/ProtoDatastore.kt` — interface grew from ~20 lines to 460+ lines

**Problem:** The `ProtoDatastore` interface now has 18 methods. This is a lot of surface area for implementors and makes the interface hard to scan. All 17 custom field methods ultimately delegate to `field()`.

**Why it matters:** Anyone implementing `ProtoDatastore` directly (rather than using `GenericProtoDatastore`) must implement all 18 methods. The interface is also harder to read/discover in IDE autocompletion.

**Suggestion:** Move the 17 custom field methods to extension functions on `ProtoDatastore<T>` instead of interface members, since they all delegate to `field()`. This would reduce the interface to 2 methods (`data()` and `field()`) and make it trivial to implement:

```kotlin
// Interface stays minimal
interface ProtoDatastore<T> {
    fun data(): ProtoPreference<T>
    fun <F> field(key: String, defaultValue: F, getter: (T) -> F, updater: (T, F) -> T): ProtoPreference<F>
}

// All custom field methods become extension functions (they already essentially are)
fun <T, F : Enum<F>> ProtoDatastore<T>.enumField(...) = field(...)
```

This is partially done already with the reified extensions — the non-reified versions could follow the same pattern.

---

### 🟡 Issue #6 — Plan documents committed to repo root (LOW)

**Files:** `proto-plan.md` (+1,035 lines), `proto-field-custom-types-plan.md` (+1,607 lines)

**Problem:** 2,600+ lines of implementation planning documents are committed to the repository root. These are internal working documents, not user-facing docs.

**Why it matters:** They add noise to the repo root, will become stale over time, and are not useful for library consumers.

**Fix:** Move to a `docs/internal/` directory, or remove them once the features are implemented and tested (the code + tests are the living documentation). At minimum, add a note that they are historical planning artifacts.

---

### 🟡 Issue #7 — Sample app numeric fields can't be cleared (LOW)

**Files:** `protoApp/.../Proto2Screen.kt` (line 150), `protoApp/.../Proto3Screen.kt` (lines 192, 238, 272, 289, 306)

**Problem:** Numeric input fields use `toIntOrNull()` and skip the callback when null. This prevents users from clearing the field to type a new value — the field snaps back to the previous value on recomposition.

**Why it matters:** The sample app is the primary onboarding tool for library consumers. Buggy UX in the sample may suggest the library itself is problematic.

**Fix:** Use local `String` state for the text field and only push valid values to the ViewModel:

```kotlin
var text by remember { mutableStateOf(value.toString()) }
TextField(
    value = text,
    onValueChange = { input ->
        text = input
        input.toIntOrNull()?.let { onValueChange(it) }
    }
)
```

---

### 🟢 Issue #8 — `GenericProtoDatastore` internal `datastore` property is `internal` (NITPICK)

**File:** `GenericProtoDatastore.kt` (line 34)

**Problem:** `datastore` is marked `internal` but is also accessed by the extension functions in separate files (e.g., custom field `*Internal` functions). These functions are defined as extensions on `ProtoDatastore<T>`, not `GenericProtoDatastore<T>`, so they access it through `field()` — which is correct. However, the `internal` visibility on the constructor property is unnecessarily broad; `private` would suffice.

---

## Summary

| Severity | Count | Key Themes |
|----------|-------|------------|
| 🔴 Medium | 2 | Duplicate object allocation, `data()` not cached |
| 🟡 Low | 5 | Test boilerplate explosion, large interface, stale docs, sample app UX, enum lookup style |
| 🟢 Nitpick | 1 | Visibility modifier |

### Overall Assessment

The architecture is **well-designed** — the delegation pattern through `ProtoFieldPreference` is clean, the custom field type decomposition into focused single-file internal functions is easy to navigate, and the test coverage is thorough across all 3 platforms. The reified extension functions are a strong DX win.

The main maintainability risks are:
1. **The duplicate `ProtoFieldPreference` construction** in `GenericProtoPreferenceItem` — easy to fix.
2. **The 100+ boilerplate test files** — sustainable only if codegen is added or the pattern is well-documented for contributors.
3. **The large `ProtoDatastore` interface** — could be simplified by converting factory methods to extension functions without breaking the public API.
