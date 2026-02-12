# Proto DataStore Custom Field Types — Implementation Plan

## Overview

Add custom field factory methods to `ProtoDatastore<T>` that mirror the preference-side custom types
(`custom/`, `customSet/`, `optional/custom/`). These methods compose on top of the existing `field()`
method by wrapping getter/updater lambdas with serialization/deserialization logic, enabling Proto
DataStore users to store enum, serialized-object, list, and set fields without manual conversion
boilerplate.

### Key Difference from Preferences Custom Types

In Preferences DataStore, custom types need their own `CustomGenericPreferenceItem` /
`NullableCustomGenericPreferenceItem` base classes because they must marshal values through
`stringPreferencesKey` with serializer/deserializer functions.

In Proto DataStore, the proto message itself holds the raw data. The `field()` method already
abstracts field access via `getter`/`updater` lambdas. Custom field types therefore **compose on
top of `field()`** by transforming the getter and updater to handle serialization transparently.
No new `ProtoFieldPreference` subclasses are needed — only new factory methods on `ProtoDatastore`
and their implementations in `GenericProtoDatastore`.

---

## New Factory Methods

All methods go on the `ProtoDatastore<T>` interface and are implemented in
`GenericProtoDatastore<T>`. Extension functions for reified variants go in the same file as the
interface (matching the pattern in `PreferencesDatastore.kt`).

### Group 1: Enum Fields

| Method | Description | Reference |
|--------|-------------|-----------|
| `enumField<F : Enum<F>>` | Store an enum field; serialized via `Enum.name`, deserialized via `enumValueOf`. The proto field type is `String`. | `EnumPreference.kt` |
| `nullableEnumField<F : Enum<F>>` | Nullable variant; returns `null` if stored string doesn't match any constant. The proto field type is `String?`. | `NullableEnumPreference.kt` |
| `enumSetField<F : Enum<F>>` | Store a `Set<Enum>` field; each element serialized via `Enum.name`. The proto field type is `Set<String>`. | `EnumSetPreference.kt` |

### Group 2: KSerialized Fields (kotlinx.serialization)

| Method | Description | Reference |
|--------|-------------|-----------|
| `kserializedField<F>` | Store a custom object field using `KSerializer<F>`. The proto field type is `String` (JSON-encoded). | `KSerializedPrimitive.kt` |
| `nullableKserializedField<F : Any>` | Nullable variant; returns `null` on deserialization failure. The proto field type is `String?`. | `NullableKSerializedPrimitive.kt` |
| `kserializedListField<F>` | Store a `List<F>` field using `KSerializer<F>` + `ListSerializer`. The proto field type is `String` (JSON array). | `KSerializedListPrimitive.kt` |
| `nullableKserializedListField<F>` | Nullable variant of list. The proto field type is `String?`. | `NullableKSerializedListPrimitive.kt` |
| `kserializedSetField<F>` | Store a `Set<F>` field using `KSerializer<F>`. The proto field type is `Set<String>` (each element JSON-encoded). | `KSerializedSetPrimitive.kt` |

### Group 3: Serialized Fields (caller-provided functions)

| Method | Description | Reference |
|--------|-------------|-----------|
| `serializedField<F>` | Store a custom object field using caller-provided `serializer: (F) -> String` and `deserializer: (String) -> F`. The proto field type is `String`. | `ObjectPrimitive.kt` |
| `nullableSerializedField<F : Any>` | Nullable variant; returns `null` on failure. The proto field type is `String?`. | `NullableObjectPrimitive.kt` |
| `serializedListField<F>` | Store a `List<F>` field using per-element serializer/deserializer. The proto field type is `String` (JSON array). | `SerializedListPrimitive.kt` |
| `nullableSerializedListField<F>` | Nullable variant of list. The proto field type is `String?`. | `NullableSerializedListPrimitive.kt` |
| `serializedSetField<F>` | Store a `Set<F>` field using per-element serializer/deserializer. The proto field type is `Set<String>`. | `SerializedSetPrimitive.kt` |

---

## API Signatures

### ProtoDatastore.kt — Interface Methods

```kotlin
// --- Enum fields ---

public fun <F : Enum<F>> enumField(
    key: String,
    defaultValue: F,
    enumValues: Array<F>,
    getter: (T) -> String,
    updater: (T, String) -> T,
): ProtoPreference<F>

public fun <F : Enum<F>> nullableEnumField(
    key: String,
    enumValues: Array<F>,
    getter: (T) -> String?,
    updater: (T, String?) -> T,
): ProtoPreference<F?>

public fun <F : Enum<F>> enumSetField(
    key: String,
    defaultValue: Set<F> = emptySet(),
    enumValues: Array<F>,
    getter: (T) -> Set<String>,
    updater: (T, Set<String>) -> T,
): ProtoPreference<Set<F>>

// --- KSerialized fields ---

public fun <F> kserializedField(
    key: String,
    defaultValue: F,
    serializer: KSerializer<F>,
    json: Json? = null,
    getter: (T) -> String,
    updater: (T, String) -> T,
): ProtoPreference<F>

public fun <F : Any> nullableKserializedField(
    key: String,
    serializer: KSerializer<F>,
    json: Json? = null,
    getter: (T) -> String?,
    updater: (T, String?) -> T,
): ProtoPreference<F?>

public fun <F> kserializedListField(
    key: String,
    defaultValue: List<F> = emptyList(),
    serializer: KSerializer<F>,
    json: Json? = null,
    getter: (T) -> String,
    updater: (T, String) -> T,
): ProtoPreference<List<F>>

public fun <F> nullableKserializedListField(
    key: String,
    serializer: KSerializer<F>,
    json: Json? = null,
    getter: (T) -> String?,
    updater: (T, String?) -> T,
): ProtoPreference<List<F>?>

public fun <F> kserializedSetField(
    key: String,
    defaultValue: Set<F> = emptySet(),
    serializer: KSerializer<F>,
    json: Json? = null,
    getter: (T) -> Set<String>,
    updater: (T, Set<String>) -> T,
): ProtoPreference<Set<F>>

// --- Serialized fields (caller-provided functions) ---

public fun <F> serializedField(
    key: String,
    defaultValue: F,
    serializer: (F) -> String,
    deserializer: (String) -> F,
    getter: (T) -> String,
    updater: (T, String) -> T,
): ProtoPreference<F>

public fun <F : Any> nullableSerializedField(
    key: String,
    serializer: (F) -> String,
    deserializer: (String) -> F,
    getter: (T) -> String?,
    updater: (T, String?) -> T,
): ProtoPreference<F?>

public fun <F> serializedListField(
    key: String,
    defaultValue: List<F> = emptyList(),
    elementSerializer: (F) -> String,
    elementDeserializer: (String) -> F,
    getter: (T) -> String,
    updater: (T, String) -> T,
): ProtoPreference<List<F>>

public fun <F> nullableSerializedListField(
    key: String,
    elementSerializer: (F) -> String,
    elementDeserializer: (String) -> F,
    getter: (T) -> String?,
    updater: (T, String?) -> T,
): ProtoPreference<List<F>?>

public fun <F> serializedSetField(
    key: String,
    defaultValue: Set<F> = emptySet(),
    serializer: (F) -> String,
    deserializer: (String) -> F,
    getter: (T) -> Set<String>,
    updater: (T, Set<String>) -> T,
): ProtoPreference<Set<F>>
```

### Extension Functions (reified convenience)

In the same file, add inline reified extensions mirroring `PreferencesDatastore.kt`:

```kotlin
public inline fun <T, reified F : Enum<F>> ProtoDatastore<T>.enumField(
    key: String,
    defaultValue: F,
    noinline getter: (T) -> String,
    noinline updater: (T, String) -> T,
): ProtoPreference<F> = enumField(
    key = key,
    defaultValue = defaultValue,
    enumValues = enumValues<F>(),
    getter = getter,
    updater = updater,
)

public inline fun <T, reified F : Enum<F>> ProtoDatastore<T>.nullableEnumField(
    key: String,
    noinline getter: (T) -> String?,
    noinline updater: (T, String?) -> T,
): ProtoPreference<F?> = nullableEnumField(
    key = key,
    enumValues = enumValues<F>(),
    getter = getter,
    updater = updater,
)

public inline fun <T, reified F : Enum<F>> ProtoDatastore<T>.enumSetField(
    key: String,
    defaultValue: Set<F> = emptySet(),
    noinline getter: (T) -> Set<String>,
    noinline updater: (T, Set<String>) -> T,
): ProtoPreference<Set<F>> = enumSetField(
    key = key,
    defaultValue = defaultValue,
    enumValues = enumValues<F>(),
    getter = getter,
    updater = updater,
)

public inline fun <T, reified F> ProtoDatastore<T>.kserializedField(
    key: String,
    defaultValue: F,
    json: Json? = null,
    noinline getter: (T) -> String,
    noinline updater: (T, String) -> T,
): ProtoPreference<F> = kserializedField(
    key = key,
    defaultValue = defaultValue,
    serializer = serializer<F>(),
    json = json,
    getter = getter,
    updater = updater,
)

public inline fun <T, reified F : Any> ProtoDatastore<T>.nullableKserializedField(
    key: String,
    json: Json? = null,
    noinline getter: (T) -> String?,
    noinline updater: (T, String?) -> T,
): ProtoPreference<F?> = nullableKserializedField(
    key = key,
    serializer = serializer<F>(),
    json = json,
    getter = getter,
    updater = updater,
)

public inline fun <T, reified F> ProtoDatastore<T>.kserializedListField(
    key: String,
    defaultValue: List<F> = emptyList(),
    json: Json? = null,
    noinline getter: (T) -> String,
    noinline updater: (T, String) -> T,
): ProtoPreference<List<F>> = kserializedListField(
    key = key,
    defaultValue = defaultValue,
    serializer = serializer<F>(),
    json = json,
    getter = getter,
    updater = updater,
)

public inline fun <T, reified F> ProtoDatastore<T>.nullableKserializedListField(
    key: String,
    json: Json? = null,
    noinline getter: (T) -> String?,
    noinline updater: (T, String?) -> T,
): ProtoPreference<List<F>?> = nullableKserializedListField(
    key = key,
    serializer = serializer<F>(),
    json = json,
    getter = getter,
    updater = updater,
)

public inline fun <T, reified F> ProtoDatastore<T>.kserializedSetField(
    key: String,
    defaultValue: Set<F> = emptySet(),
    json: Json? = null,
    noinline getter: (T) -> Set<String>,
    noinline updater: (T, Set<String>) -> T,
): ProtoPreference<Set<F>> = kserializedSetField(
    key = key,
    defaultValue = defaultValue,
    serializer = serializer<F>(),
    json = json,
    getter = getter,
    updater = updater,
)
```

---

## Implementation Strategy

### Core Principle: Compose on `field()` via Getter/Updater Transformation

Each custom field method delegates to the existing `field()` by wrapping the user-supplied
`getter`/`updater` (which operate on the raw proto `String`/`Set<String>` field) with
serialization/deserialization. This avoids new `ProtoFieldPreference` subclasses.

### Implementation Pattern (in `GenericProtoDatastore`)

#### Example: `kserializedField`

```kotlin
override fun <F> kserializedField(
    key: String,
    defaultValue: F,
    serializer: KSerializer<F>,
    json: Json?,
    getter: (T) -> String,
    updater: (T, String) -> T,
): ProtoPreference<F> {
    val jsonInstance = json ?: PreferenceDefaults.defaultJson
    return field(
        key = key,
        defaultValue = defaultValue,
        getter = { proto ->
            val raw = getter(proto)
            if (raw.isBlank()) defaultValue
            else safeDeserialize(raw, defaultValue) { jsonInstance.decodeFromString(serializer, it) }
        },
        updater = { proto, value ->
            updater(proto, jsonInstance.encodeToString(serializer, value))
        },
    )
}
```

#### Example: `enumField`

```kotlin
override fun <F : Enum<F>> enumField(
    key: String,
    defaultValue: F,
    enumValues: Array<F>,
    getter: (T) -> String,
    updater: (T, String) -> T,
): ProtoPreference<F> = field(
    key = key,
    defaultValue = defaultValue,
    getter = { proto ->
        val raw = getter(proto)
        safeDeserialize(raw, defaultValue) { name ->
            enumValues.first { it.name == name }
        }
    },
    updater = { proto, value ->
        updater(proto, value.name)
    },
)
```

#### Nullable Pattern: `nullableKserializedField`

For nullable variants, the proto field is `String?`. When the raw value is `null` or deserialization
fails, `null` is returned. When setting `null`, the updater receives `null`.

```kotlin
override fun <F : Any> nullableKserializedField(
    key: String,
    serializer: KSerializer<F>,
    json: Json?,
    getter: (T) -> String?,
    updater: (T, String?) -> T,
): ProtoPreference<F?> {
    val jsonInstance = json ?: PreferenceDefaults.defaultJson
    return field(
        key = key,
        defaultValue = null,
        getter = { proto ->
            val raw = getter(proto)
            raw?.let { safeDeserialize<F?>(it, null) { s -> jsonInstance.decodeFromString(serializer, s) } }
        },
        updater = { proto, value ->
            updater(proto, value?.let { jsonInstance.encodeToString(serializer, it) })
        },
    )
}
```

#### Set Pattern: `kserializedSetField`

The proto field is `Set<String>`. Each element is individually serialized/deserialized.

```kotlin
override fun <F> kserializedSetField(
    key: String,
    defaultValue: Set<F>,
    serializer: KSerializer<F>,
    json: Json?,
    getter: (T) -> Set<String>,
    updater: (T, Set<String>) -> T,
): ProtoPreference<Set<F>> {
    val jsonInstance = json ?: PreferenceDefaults.defaultJson
    return field(
        key = key,
        defaultValue = defaultValue,
        getter = { proto ->
            getter(proto).mapNotNull { raw ->
                safeDeserialize<F?>(raw, null) { jsonInstance.decodeFromString(serializer, it) }
            }.toSet()
        },
        updater = { proto, value ->
            updater(proto, value.map { jsonInstance.encodeToString(serializer, it) }.toSet())
        },
    )
}
```

### Helper: `safeDeserialize`

Add a private utility in `GenericProtoDatastore` (or as a top-level `internal` function in the
proto package):

```kotlin
private inline fun <T> safeDeserialize(
    raw: String,
    fallback: T,
    deserialize: (String) -> T,
): T = try {
    deserialize(raw)
} catch (e: CancellationException) {
    throw e
} catch (_: Exception) {
    fallback
}
```

---

## File Changes Summary

### Modified Files

| File | Changes |
|------|---------|
| `proto/ProtoDatastore.kt` | Add 15 new interface methods + reified extension functions |
| `proto/GenericProtoDatastore.kt` | Implement all 15 methods, add `safeDeserialize` helper |

### New Files

None required. All implementations compose on top of existing `field()` / `ProtoFieldPreference`.

---

## Test Plan

Tests follow the existing abstract test class pattern: shared test logic in `commonTest` abstract
classes, with thin platform subclasses in `androidDeviceTest`, `desktopTest`, and
`iosSimulatorArm64Test` that only handle DataStore initialization and teardown via platform-specific
test helpers.

Suspending and blocking tests are separated into distinct abstract classes (matching the pattern
of `AbstractProtoFieldPreferenceTest` / `AbstractProtoFieldPreferenceBlockingTest`).

### Test Data Model

Create `TestCustomFieldProtoData` in `commonTest/.../proto/TestCustomFieldProtoData.kt`.
This data class holds raw `String`, `String?`, and `Set<String>` fields that the custom field
methods will wrap. It uses kotlinx.serialization for its `OkioSerializer` (same approach as
`TestNullableProtoData`).

```kotlin
// commonTest/.../proto/TestCustomFieldProtoData.kt
package io.github.arthurkun.generic.datastore.proto

import androidx.datastore.core.okio.OkioSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okio.BufferedSink
import okio.BufferedSource

enum class TestColor { RED, GREEN, BLUE }

@Serializable
data class TestItem(val name: String = "", val quantity: Int = 0)

@Serializable
data class TestCustomFieldProtoData(
    val enumRaw: String = "",                    // backs enumField
    val nullableEnumRaw: String? = null,         // backs nullableEnumField
    val enumSetRaw: Set<String> = emptySet(),    // backs enumSetField
    val jsonRaw: String = "",                    // backs kserializedField / serializedField
    val nullableJsonRaw: String? = null,         // backs nullableKserializedField / nullableSerializedField
    val jsonListRaw: String = "",                // backs kserializedListField / serializedListField
    val nullableJsonListRaw: String? = null,     // backs nullableKserializedListField / nullableSerializedListField
    val jsonSetRaw: Set<String> = emptySet(),    // backs kserializedSetField / serializedSetField
)

object TestCustomFieldProtoDataSerializer : OkioSerializer<TestCustomFieldProtoData> {
    override val defaultValue: TestCustomFieldProtoData = TestCustomFieldProtoData()

    override suspend fun readFrom(source: BufferedSource): TestCustomFieldProtoData {
        val json = source.readUtf8()
        if (json.isBlank()) return defaultValue
        return Json.decodeFromString(json)
    }

    override suspend fun writeTo(t: TestCustomFieldProtoData, sink: BufferedSink) {
        sink.writeUtf8(Json.encodeToString(t))
    }
}
```

### Test Helpers

Create a `CustomFieldProtoTestHelper` for each platform. These follow the exact same pattern as
the existing `DesktopProtoTestHelper`, `AndroidProtoTestHelper`, and `IosProtoTestHelper` but
produce `GenericProtoDatastore<TestCustomFieldProtoData>` instead.

#### Desktop — `DesktopCustomFieldProtoTestHelper`

```kotlin
// desktopTest/.../proto/DesktopCustomFieldProtoTestHelper.kt
package io.github.arthurkun.generic.datastore.proto

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain

class DesktopCustomFieldProtoTestHelper private constructor(
    private val datastoreName: String,
    private val useStandardDispatcher: Boolean,
) {
    private lateinit var _protoDatastore: GenericProtoDatastore<TestCustomFieldProtoData>
    private lateinit var _testDispatcher: TestDispatcher
    private lateinit var testScope: CoroutineScope

    val protoDatastore: GenericProtoDatastore<TestCustomFieldProtoData> get() = _protoDatastore
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
            serializer = TestCustomFieldProtoDataSerializer,
            defaultValue = TestCustomFieldProtoData(),
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
        fun standard(datastoreName: String): DesktopCustomFieldProtoTestHelper {
            return DesktopCustomFieldProtoTestHelper(datastoreName, useStandardDispatcher = true)
        }

        fun blocking(datastoreName: String): DesktopCustomFieldProtoTestHelper {
            return DesktopCustomFieldProtoTestHelper(datastoreName, useStandardDispatcher = false)
        }
    }
}
```

#### Android — `AndroidCustomFieldProtoTestHelper`

Same pattern as `AndroidProtoTestHelper`, but uses `TestCustomFieldProtoDataSerializer` /
`TestCustomFieldProtoData()`. Uses `ApplicationProvider.getApplicationContext()` for path,
`@Before`/`@After` lifecycle, and file cleanup on teardown.

#### iOS — `IosCustomFieldProtoTestHelper`

Same pattern as `IosProtoTestHelper`, but uses `TestCustomFieldProtoDataSerializer` /
`TestCustomFieldProtoData()`. Uses `NSTemporaryDirectory()` + `NSUUID()` for path,
`@BeforeTest`/`@AfterTest` lifecycle, and `NSFileManager` cleanup on teardown.

---

### Abstract Test Classes — `commonTest`

All abstract classes go in `commonTest/.../proto/`. Each exposes an abstract
`protoDatastore: GenericProtoDatastore<TestCustomFieldProtoData>` property (and
`testDispatcher: TestDispatcher` for suspending tests).

#### 1. `AbstractProtoEnumFieldTest` (suspending)

```kotlin
// commonTest/.../proto/AbstractProtoEnumFieldTest.kt
package io.github.arthurkun.generic.datastore.proto

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

abstract class AbstractProtoEnumFieldTest {

    abstract val protoDatastore: GenericProtoDatastore<TestCustomFieldProtoData>
    abstract val testDispatcher: TestDispatcher

    @Test
    fun enumField_getReturnsDefaultWhenNotSet() = runTest(testDispatcher) {
        val colorPref = protoDatastore.enumField<TestCustomFieldProtoData, TestColor>(
            key = "color",
            defaultValue = TestColor.RED,
            getter = { it.enumRaw },
            updater = { proto, raw -> proto.copy(enumRaw = raw) },
        )
        assertEquals(TestColor.RED, colorPref.get())
    }

    @Test
    fun enumField_setAndGet() = runTest(testDispatcher) {
        val colorPref = protoDatastore.enumField<TestCustomFieldProtoData, TestColor>(
            key = "color",
            defaultValue = TestColor.RED,
            getter = { it.enumRaw },
            updater = { proto, raw -> proto.copy(enumRaw = raw) },
        )
        colorPref.set(TestColor.BLUE)
        assertEquals(TestColor.BLUE, colorPref.get())
    }

    @Test
    fun enumField_asFlowEmitsUpdates() = runTest(testDispatcher) {
        val colorPref = protoDatastore.enumField<TestCustomFieldProtoData, TestColor>(
            key = "color",
            defaultValue = TestColor.RED,
            getter = { it.enumRaw },
            updater = { proto, raw -> proto.copy(enumRaw = raw) },
        )
        assertEquals(TestColor.RED, colorPref.asFlow().first())
        colorPref.set(TestColor.GREEN)
        assertEquals(TestColor.GREEN, colorPref.asFlow().first())
    }

    @Test
    fun enumField_updateAtomically() = runTest(testDispatcher) {
        val colorPref = protoDatastore.enumField<TestCustomFieldProtoData, TestColor>(
            key = "color",
            defaultValue = TestColor.RED,
            getter = { it.enumRaw },
            updater = { proto, raw -> proto.copy(enumRaw = raw) },
        )
        colorPref.set(TestColor.RED)
        colorPref.update { if (it == TestColor.RED) TestColor.GREEN else it }
        assertEquals(TestColor.GREEN, colorPref.get())
    }

    @Test
    fun enumField_deleteResetsToDefault() = runTest(testDispatcher) {
        val colorPref = protoDatastore.enumField<TestCustomFieldProtoData, TestColor>(
            key = "color",
            defaultValue = TestColor.RED,
            getter = { it.enumRaw },
            updater = { proto, raw -> proto.copy(enumRaw = raw) },
        )
        colorPref.set(TestColor.BLUE)
        colorPref.delete()
        assertEquals(TestColor.RED, colorPref.get())
    }

    @Test
    fun enumField_resetToDefault() = runTest(testDispatcher) {
        val colorPref = protoDatastore.enumField<TestCustomFieldProtoData, TestColor>(
            key = "color",
            defaultValue = TestColor.RED,
            getter = { it.enumRaw },
            updater = { proto, raw -> proto.copy(enumRaw = raw) },
        )
        colorPref.set(TestColor.GREEN)
        colorPref.resetToDefault()
        assertEquals(TestColor.RED, colorPref.get())
    }

    @Test
    fun enumField_keyReturnsConfiguredKey() = runTest(testDispatcher) {
        val colorPref = protoDatastore.enumField<TestCustomFieldProtoData, TestColor>(
            key = "my_color_key",
            defaultValue = TestColor.RED,
            getter = { it.enumRaw },
            updater = { proto, raw -> proto.copy(enumRaw = raw) },
        )
        assertEquals("my_color_key", colorPref.key())
    }

    @Test
    fun enumField_blankKeyThrows() {
        assertFailsWith<IllegalArgumentException> {
            protoDatastore.enumField<TestCustomFieldProtoData, TestColor>(
                key = " ",
                defaultValue = TestColor.RED,
                getter = { it.enumRaw },
                updater = { proto, raw -> proto.copy(enumRaw = raw) },
            )
        }
    }

    @Test
    fun enumField_invalidStoredStringFallsBackToDefault() = runTest(testDispatcher) {
        // Write a raw string that doesn't match any TestColor constant
        val rawPref = protoDatastore.field(
            key = "enum_raw_direct",
            defaultValue = "",
            getter = { it.enumRaw },
            updater = { proto, value -> proto.copy(enumRaw = value) },
        )
        rawPref.set("INVALID_VALUE")
        val colorPref = protoDatastore.enumField<TestCustomFieldProtoData, TestColor>(
            key = "color",
            defaultValue = TestColor.RED,
            getter = { it.enumRaw },
            updater = { proto, raw -> proto.copy(enumRaw = raw) },
        )
        assertEquals(TestColor.RED, colorPref.get())
    }

    @Test
    fun enumField_doesNotAffectOtherFields() = runTest(testDispatcher) {
        val colorPref = protoDatastore.enumField<TestCustomFieldProtoData, TestColor>(
            key = "color",
            defaultValue = TestColor.RED,
            getter = { it.enumRaw },
            updater = { proto, raw -> proto.copy(enumRaw = raw) },
        )
        val jsonPref = protoDatastore.field(
            key = "json_raw",
            defaultValue = "",
            getter = { it.jsonRaw },
            updater = { proto, value -> proto.copy(jsonRaw = value) },
        )
        jsonPref.set("keepme")
        colorPref.set(TestColor.BLUE)
        assertEquals("keepme", jsonPref.get())
    }
}
```

#### 2. `AbstractProtoEnumFieldBlockingTest` (blocking)

```kotlin
// commonTest/.../proto/AbstractProtoEnumFieldBlockingTest.kt
package io.github.arthurkun.generic.datastore.proto

import kotlin.test.Test
import kotlin.test.assertEquals

abstract class AbstractProtoEnumFieldBlockingTest {

    abstract val protoDatastore: GenericProtoDatastore<TestCustomFieldProtoData>

    @Test
    fun enumField_getBlockingReturnsDefault() {
        val colorPref = protoDatastore.enumField<TestCustomFieldProtoData, TestColor>(
            key = "color",
            defaultValue = TestColor.RED,
            getter = { it.enumRaw },
            updater = { proto, raw -> proto.copy(enumRaw = raw) },
        )
        assertEquals(TestColor.RED, colorPref.getBlocking())
    }

    @Test
    fun enumField_setBlockingAndGetBlocking() {
        val colorPref = protoDatastore.enumField<TestCustomFieldProtoData, TestColor>(
            key = "color",
            defaultValue = TestColor.RED,
            getter = { it.enumRaw },
            updater = { proto, raw -> proto.copy(enumRaw = raw) },
        )
        colorPref.setBlocking(TestColor.GREEN)
        assertEquals(TestColor.GREEN, colorPref.getBlocking())
    }

    @Test
    fun enumField_resetToDefaultBlocking() {
        val colorPref = protoDatastore.enumField<TestCustomFieldProtoData, TestColor>(
            key = "color",
            defaultValue = TestColor.RED,
            getter = { it.enumRaw },
            updater = { proto, raw -> proto.copy(enumRaw = raw) },
        )
        colorPref.setBlocking(TestColor.BLUE)
        colorPref.resetToDefaultBlocking()
        assertEquals(TestColor.RED, colorPref.getBlocking())
    }

    @Test
    fun enumField_propertyDelegation() {
        val colorPref = protoDatastore.enumField<TestCustomFieldProtoData, TestColor>(
            key = "color",
            defaultValue = TestColor.RED,
            getter = { it.enumRaw },
            updater = { proto, raw -> proto.copy(enumRaw = raw) },
        )
        var color: TestColor by colorPref
        color = TestColor.BLUE
        assertEquals(TestColor.BLUE, color)
    }
}
```

#### 3. `AbstractProtoKserializedFieldTest` (suspending)

```kotlin
// commonTest/.../proto/AbstractProtoKserializedFieldTest.kt
package io.github.arthurkun.generic.datastore.proto

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

abstract class AbstractProtoKserializedFieldTest {

    abstract val protoDatastore: GenericProtoDatastore<TestCustomFieldProtoData>
    abstract val testDispatcher: TestDispatcher

    @Test
    fun kserializedField_getReturnsDefaultWhenNotSet() = runTest(testDispatcher) {
        val itemPref = protoDatastore.kserializedField<TestCustomFieldProtoData, TestItem>(
            key = "item",
            defaultValue = TestItem(),
            getter = { it.jsonRaw },
            updater = { proto, raw -> proto.copy(jsonRaw = raw) },
        )
        assertEquals(TestItem(), itemPref.get())
    }

    @Test
    fun kserializedField_setAndGetRoundTrip() = runTest(testDispatcher) {
        val itemPref = protoDatastore.kserializedField<TestCustomFieldProtoData, TestItem>(
            key = "item",
            defaultValue = TestItem(),
            getter = { it.jsonRaw },
            updater = { proto, raw -> proto.copy(jsonRaw = raw) },
        )
        val item = TestItem(name = "Widget", quantity = 5)
        itemPref.set(item)
        assertEquals(item, itemPref.get())
    }

    @Test
    fun kserializedField_asFlowEmitsUpdates() = runTest(testDispatcher) {
        val itemPref = protoDatastore.kserializedField<TestCustomFieldProtoData, TestItem>(
            key = "item",
            defaultValue = TestItem(),
            getter = { it.jsonRaw },
            updater = { proto, raw -> proto.copy(jsonRaw = raw) },
        )
        assertEquals(TestItem(), itemPref.asFlow().first())
        itemPref.set(TestItem(name = "Gadget", quantity = 3))
        assertEquals(TestItem(name = "Gadget", quantity = 3), itemPref.asFlow().first())
    }

    @Test
    fun kserializedField_updateAtomically() = runTest(testDispatcher) {
        val itemPref = protoDatastore.kserializedField<TestCustomFieldProtoData, TestItem>(
            key = "item",
            defaultValue = TestItem(),
            getter = { it.jsonRaw },
            updater = { proto, raw -> proto.copy(jsonRaw = raw) },
        )
        itemPref.set(TestItem(name = "Widget", quantity = 5))
        itemPref.update { it.copy(quantity = it.quantity + 1) }
        assertEquals(TestItem(name = "Widget", quantity = 6), itemPref.get())
    }

    @Test
    fun kserializedField_deleteResetsToDefault() = runTest(testDispatcher) {
        val itemPref = protoDatastore.kserializedField<TestCustomFieldProtoData, TestItem>(
            key = "item",
            defaultValue = TestItem(),
            getter = { it.jsonRaw },
            updater = { proto, raw -> proto.copy(jsonRaw = raw) },
        )
        itemPref.set(TestItem(name = "ToDelete", quantity = 1))
        itemPref.delete()
        assertEquals(TestItem(), itemPref.get())
    }

    @Test
    fun kserializedField_resetToDefault() = runTest(testDispatcher) {
        val itemPref = protoDatastore.kserializedField<TestCustomFieldProtoData, TestItem>(
            key = "item",
            defaultValue = TestItem(),
            getter = { it.jsonRaw },
            updater = { proto, raw -> proto.copy(jsonRaw = raw) },
        )
        itemPref.set(TestItem(name = "ToReset", quantity = 1))
        itemPref.resetToDefault()
        assertEquals(TestItem(), itemPref.get())
    }

    @Test
    fun kserializedField_corruptedDataFallsBackToDefault() = runTest(testDispatcher) {
        // Write garbage JSON into the raw field
        val rawPref = protoDatastore.field(
            key = "json_raw_direct",
            defaultValue = "",
            getter = { it.jsonRaw },
            updater = { proto, value -> proto.copy(jsonRaw = value) },
        )
        rawPref.set("{not valid json!!")
        val itemPref = protoDatastore.kserializedField<TestCustomFieldProtoData, TestItem>(
            key = "item",
            defaultValue = TestItem(),
            getter = { it.jsonRaw },
            updater = { proto, raw -> proto.copy(jsonRaw = raw) },
        )
        assertEquals(TestItem(), itemPref.get())
    }

    @Test
    fun kserializedField_doesNotAffectOtherFields() = runTest(testDispatcher) {
        val itemPref = protoDatastore.kserializedField<TestCustomFieldProtoData, TestItem>(
            key = "item",
            defaultValue = TestItem(),
            getter = { it.jsonRaw },
            updater = { proto, raw -> proto.copy(jsonRaw = raw) },
        )
        val enumRawPref = protoDatastore.field(
            key = "enum_raw",
            defaultValue = "",
            getter = { it.enumRaw },
            updater = { proto, value -> proto.copy(enumRaw = value) },
        )
        enumRawPref.set("KEEP")
        itemPref.set(TestItem(name = "Widget", quantity = 1))
        assertEquals("KEEP", enumRawPref.get())
    }
}
```

#### 4. `AbstractProtoKserializedFieldBlockingTest` (blocking)

```kotlin
// commonTest/.../proto/AbstractProtoKserializedFieldBlockingTest.kt
package io.github.arthurkun.generic.datastore.proto

import kotlin.test.Test
import kotlin.test.assertEquals

abstract class AbstractProtoKserializedFieldBlockingTest {

    abstract val protoDatastore: GenericProtoDatastore<TestCustomFieldProtoData>

    @Test
    fun kserializedField_getBlockingReturnsDefault() {
        val itemPref = protoDatastore.kserializedField<TestCustomFieldProtoData, TestItem>(
            key = "item",
            defaultValue = TestItem(),
            getter = { it.jsonRaw },
            updater = { proto, raw -> proto.copy(jsonRaw = raw) },
        )
        assertEquals(TestItem(), itemPref.getBlocking())
    }

    @Test
    fun kserializedField_setBlockingAndGetBlocking() {
        val itemPref = protoDatastore.kserializedField<TestCustomFieldProtoData, TestItem>(
            key = "item",
            defaultValue = TestItem(),
            getter = { it.jsonRaw },
            updater = { proto, raw -> proto.copy(jsonRaw = raw) },
        )
        val item = TestItem(name = "Widget", quantity = 5)
        itemPref.setBlocking(item)
        assertEquals(item, itemPref.getBlocking())
    }

    @Test
    fun kserializedField_resetToDefaultBlocking() {
        val itemPref = protoDatastore.kserializedField<TestCustomFieldProtoData, TestItem>(
            key = "item",
            defaultValue = TestItem(),
            getter = { it.jsonRaw },
            updater = { proto, raw -> proto.copy(jsonRaw = raw) },
        )
        itemPref.setBlocking(TestItem(name = "ToReset", quantity = 1))
        itemPref.resetToDefaultBlocking()
        assertEquals(TestItem(), itemPref.getBlocking())
    }

    @Test
    fun kserializedField_propertyDelegation() {
        val itemPref = protoDatastore.kserializedField<TestCustomFieldProtoData, TestItem>(
            key = "item",
            defaultValue = TestItem(),
            getter = { it.jsonRaw },
            updater = { proto, raw -> proto.copy(jsonRaw = raw) },
        )
        var item: TestItem by itemPref
        item = TestItem(name = "Delegated", quantity = 7)
        assertEquals(TestItem(name = "Delegated", quantity = 7), item)
    }
}
```

#### 5. `AbstractProtoNullableEnumFieldTest` (suspending)

```kotlin
// commonTest/.../proto/AbstractProtoNullableEnumFieldTest.kt
package io.github.arthurkun.generic.datastore.proto

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

abstract class AbstractProtoNullableEnumFieldTest {

    abstract val protoDatastore: GenericProtoDatastore<TestCustomFieldProtoData>
    abstract val testDispatcher: TestDispatcher

    @Test
    fun nullableEnumField_getReturnsNullWhenNotSet() = runTest(testDispatcher) {
        val colorPref = protoDatastore.nullableEnumField<TestCustomFieldProtoData, TestColor>(
            key = "nullable_color",
            getter = { it.nullableEnumRaw },
            updater = { proto, raw -> proto.copy(nullableEnumRaw = raw) },
        )
        assertNull(colorPref.get())
    }

    @Test
    fun nullableEnumField_setToNonNull() = runTest(testDispatcher) {
        val colorPref = protoDatastore.nullableEnumField<TestCustomFieldProtoData, TestColor>(
            key = "nullable_color",
            getter = { it.nullableEnumRaw },
            updater = { proto, raw -> proto.copy(nullableEnumRaw = raw) },
        )
        colorPref.set(TestColor.GREEN)
        assertEquals(TestColor.GREEN, colorPref.get())
    }

    @Test
    fun nullableEnumField_setToNull() = runTest(testDispatcher) {
        val colorPref = protoDatastore.nullableEnumField<TestCustomFieldProtoData, TestColor>(
            key = "nullable_color",
            getter = { it.nullableEnumRaw },
            updater = { proto, raw -> proto.copy(nullableEnumRaw = raw) },
        )
        colorPref.set(TestColor.GREEN)
        colorPref.set(null)
        assertNull(colorPref.get())
    }

    @Test
    fun nullableEnumField_deleteResetsToNull() = runTest(testDispatcher) {
        val colorPref = protoDatastore.nullableEnumField<TestCustomFieldProtoData, TestColor>(
            key = "nullable_color",
            getter = { it.nullableEnumRaw },
            updater = { proto, raw -> proto.copy(nullableEnumRaw = raw) },
        )
        colorPref.set(TestColor.BLUE)
        colorPref.delete()
        assertNull(colorPref.get())
    }

    @Test
    fun nullableEnumField_asFlowNullTransitions() = runTest(testDispatcher) {
        val colorPref = protoDatastore.nullableEnumField<TestCustomFieldProtoData, TestColor>(
            key = "nullable_color",
            getter = { it.nullableEnumRaw },
            updater = { proto, raw -> proto.copy(nullableEnumRaw = raw) },
        )
        assertNull(colorPref.asFlow().first())
        colorPref.set(TestColor.RED)
        assertEquals(TestColor.RED, colorPref.asFlow().first())
        colorPref.set(null)
        assertNull(colorPref.asFlow().first())
    }

    @Test
    fun nullableEnumField_updateFromNullToValue() = runTest(testDispatcher) {
        val colorPref = protoDatastore.nullableEnumField<TestCustomFieldProtoData, TestColor>(
            key = "nullable_color",
            getter = { it.nullableEnumRaw },
            updater = { proto, raw -> proto.copy(nullableEnumRaw = raw) },
        )
        colorPref.update { TestColor.BLUE }
        assertEquals(TestColor.BLUE, colorPref.get())
    }
}
```

#### 6. `AbstractProtoNullableEnumFieldBlockingTest` (blocking)

```kotlin
// commonTest/.../proto/AbstractProtoNullableEnumFieldBlockingTest.kt
package io.github.arthurkun.generic.datastore.proto

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

abstract class AbstractProtoNullableEnumFieldBlockingTest {

    abstract val protoDatastore: GenericProtoDatastore<TestCustomFieldProtoData>

    @Test
    fun nullableEnumField_getBlockingReturnsNull() {
        val colorPref = protoDatastore.nullableEnumField<TestCustomFieldProtoData, TestColor>(
            key = "nullable_color",
            getter = { it.nullableEnumRaw },
            updater = { proto, raw -> proto.copy(nullableEnumRaw = raw) },
        )
        assertNull(colorPref.getBlocking())
    }

    @Test
    fun nullableEnumField_setBlockingAndGetBlocking() {
        val colorPref = protoDatastore.nullableEnumField<TestCustomFieldProtoData, TestColor>(
            key = "nullable_color",
            getter = { it.nullableEnumRaw },
            updater = { proto, raw -> proto.copy(nullableEnumRaw = raw) },
        )
        colorPref.setBlocking(TestColor.BLUE)
        assertEquals(TestColor.BLUE, colorPref.getBlocking())
    }

    @Test
    fun nullableEnumField_setBlockingNull() {
        val colorPref = protoDatastore.nullableEnumField<TestCustomFieldProtoData, TestColor>(
            key = "nullable_color",
            getter = { it.nullableEnumRaw },
            updater = { proto, raw -> proto.copy(nullableEnumRaw = raw) },
        )
        colorPref.setBlocking(TestColor.RED)
        colorPref.setBlocking(null)
        assertNull(colorPref.getBlocking())
    }

    @Test
    fun nullableEnumField_resetToDefaultBlocking() {
        val colorPref = protoDatastore.nullableEnumField<TestCustomFieldProtoData, TestColor>(
            key = "nullable_color",
            getter = { it.nullableEnumRaw },
            updater = { proto, raw -> proto.copy(nullableEnumRaw = raw) },
        )
        colorPref.setBlocking(TestColor.GREEN)
        colorPref.resetToDefaultBlocking()
        assertNull(colorPref.getBlocking())
    }

    @Test
    fun nullableEnumField_propertyDelegation() {
        val colorPref = protoDatastore.nullableEnumField<TestCustomFieldProtoData, TestColor>(
            key = "nullable_color",
            getter = { it.nullableEnumRaw },
            updater = { proto, raw -> proto.copy(nullableEnumRaw = raw) },
        )
        var color: TestColor? by colorPref
        assertNull(color)
        color = TestColor.BLUE
        assertEquals(TestColor.BLUE, color)
        color = null
        assertNull(color)
    }
}
```

#### 7. `AbstractProtoEnumSetFieldTest` (suspending)

Follows the same pattern. Tests: get returns empty default, set and get round-trip, asFlow emits,
update adds/removes elements, delete resets to empty, toggle extension adds and removes element.

#### 8. `AbstractProtoEnumSetFieldBlockingTest` (blocking)

Tests: getBlocking returns empty default, setBlocking/getBlocking round-trip,
resetToDefaultBlocking, property delegation.

#### 9–10. `AbstractProtoNullableKserializedFieldTest` / `...BlockingTest`

Nullable variant of `kserializedField`. Tests follow the exact pattern of
`AbstractNullableProtoFieldPreferenceTest`: get returns null when not set, set to non-null,
set to null, delete resets to null, asFlow null transitions, update from null to value.
Blocking: getBlocking null, setBlocking/getBlocking, setBlocking null, resetToDefaultBlocking,
property delegation with nullable type.

#### 11–12. `AbstractProtoKserializedListFieldTest` / `...BlockingTest`

Tests: get returns empty default list, set and get round-trip with `List<TestItem>`, asFlow emits
list changes, update appends element atomically, delete resets to empty default list.
Blocking: getBlocking returns empty list, setBlocking/getBlocking round-trip,
resetToDefaultBlocking, property delegation.

#### 13–14. `AbstractProtoNullableKserializedListFieldTest` / `...BlockingTest`

Nullable list variant. Tests: get returns null, set non-null list, set null, delete resets to null,
asFlow null transitions. Blocking: same pattern.

#### 15–16. `AbstractProtoKserializedSetFieldTest` / `...BlockingTest`

Tests: get returns empty default set, set and get round-trip with `Set<TestItem>`, asFlow emits,
update adds element, delete resets to empty, individual deserialization failures are skipped
(corrupted element doesn't break the set). Blocking: same pattern.

#### 17–18. `AbstractProtoSerializedFieldTest` / `...BlockingTest`

Same structure as `AbstractProtoKserializedFieldTest` but uses caller-provided
`serializer: (TestItem) -> String` and `deserializer: (String) -> TestItem` lambdas instead
of `KSerializer`. Tests cover the same operations: get default, set/get round-trip, asFlow,
update, delete, resetToDefault, corrupted data fallback, field isolation.

#### 19–20. `AbstractProtoNullableSerializedFieldTest` / `...BlockingTest`

Same structure as `AbstractProtoNullableKserializedFieldTest` but with caller-provided functions.

#### 21–22. `AbstractProtoSerializedListFieldTest` / `...BlockingTest`

Same structure as `AbstractProtoKserializedListFieldTest` but with caller-provided
`elementSerializer`/`elementDeserializer` functions.

#### 23–24. `AbstractProtoNullableSerializedListFieldTest` / `...BlockingTest`

Nullable variant of serialized list.

#### 25–26. `AbstractProtoSerializedSetFieldTest` / `...BlockingTest`

Same structure as `AbstractProtoKserializedSetFieldTest` but with caller-provided functions.

---

### Platform Test Subclasses

For **each** abstract class above, create thin concrete subclasses in all three platform
source sets. Each subclass only wires up the test helper — no test logic.

#### Desktop (JVM) — `desktopTest/.../proto/`

Uses `DesktopCustomFieldProtoTestHelper`, `@TempDir`, `@BeforeTest`/`@AfterTest`.

**Standard (suspending) test example:**

```kotlin
// desktopTest/.../proto/DesktopProtoEnumFieldTest.kt
package io.github.arthurkun.generic.datastore.proto

import kotlinx.coroutines.test.TestDispatcher
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.test.AfterTest
import kotlin.test.BeforeTest

class DesktopProtoEnumFieldTest : AbstractProtoEnumFieldTest() {

    @TempDir
    lateinit var tempFolder: File

    private val helper = DesktopCustomFieldProtoTestHelper.standard("test_proto_enum_field")

    override val protoDatastore get() = helper.protoDatastore
    override val testDispatcher: TestDispatcher get() = helper.testDispatcher

    @BeforeTest
    fun setup() = helper.setup(tempFolder.absolutePath)

    @AfterTest
    fun tearDown() = helper.tearDown()
}
```

**Blocking test example:**

```kotlin
// desktopTest/.../proto/DesktopProtoEnumFieldBlockingTest.kt
package io.github.arthurkun.generic.datastore.proto

import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.test.AfterTest
import kotlin.test.BeforeTest

class DesktopProtoEnumFieldBlockingTest : AbstractProtoEnumFieldBlockingTest() {

    @TempDir
    lateinit var tempFolder: File

    private val helper = DesktopCustomFieldProtoTestHelper.blocking("test_proto_enum_field_blocking")

    override val protoDatastore get() = helper.protoDatastore

    @BeforeTest
    fun setup() = helper.setup(tempFolder.absolutePath)

    @AfterTest
    fun tearDown() = helper.tearDown()
}
```

#### Android — `androidDeviceTest/.../proto/`

Uses `AndroidCustomFieldProtoTestHelper`, `@RunWith(AndroidJUnit4::class)`,
`@Before`/`@After`.

**Standard test example:**

```kotlin
// androidDeviceTest/.../proto/AndroidProtoEnumFieldTest.kt
package io.github.arthurkun.generic.datastore.proto

import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.test.TestDispatcher
import org.junit.After
import org.junit.Before
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AndroidProtoEnumFieldTest : AbstractProtoEnumFieldTest() {

    private val helper = AndroidCustomFieldProtoTestHelper.standard("test_proto_enum_field")

    override val protoDatastore get() = helper.protoDatastore
    override val testDispatcher: TestDispatcher get() = helper.testDispatcher

    @Before
    fun setup() = helper.setup()

    @After
    fun tearDown() = helper.tearDown()
}
```

**Blocking test example:**

```kotlin
// androidDeviceTest/.../proto/AndroidProtoEnumFieldBlockingTest.kt
package io.github.arthurkun.generic.datastore.proto

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Before
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AndroidProtoEnumFieldBlockingTest : AbstractProtoEnumFieldBlockingTest() {

    private val helper = AndroidCustomFieldProtoTestHelper.blocking("test_proto_enum_field_blocking")

    override val protoDatastore get() = helper.protoDatastore

    @Before
    fun setup() = helper.setup()

    @After
    fun tearDown() = helper.tearDown()
}
```

#### iOS — `iosSimulatorArm64Test/.../proto/`

Uses `IosCustomFieldProtoTestHelper`, `@BeforeTest`/`@AfterTest`.

**Standard test example:**

```kotlin
// iosSimulatorArm64Test/.../proto/IosProtoEnumFieldTest.kt
package io.github.arthurkun.generic.datastore.proto

import kotlinx.coroutines.test.TestDispatcher
import kotlin.test.AfterTest
import kotlin.test.BeforeTest

class IosProtoEnumFieldTest : AbstractProtoEnumFieldTest() {

    private val helper = IosCustomFieldProtoTestHelper.standard("test_proto_enum_field")

    override val protoDatastore get() = helper.protoDatastore
    override val testDispatcher: TestDispatcher get() = helper.testDispatcher

    @BeforeTest
    fun setup() = helper.setup()

    @AfterTest
    fun tearDown() = helper.tearDown()
}
```

**Blocking test example:**

```kotlin
// iosSimulatorArm64Test/.../proto/IosProtoEnumFieldBlockingTest.kt
package io.github.arthurkun.generic.datastore.proto

import kotlin.test.AfterTest
import kotlin.test.BeforeTest

class IosProtoEnumFieldBlockingTest : AbstractProtoEnumFieldBlockingTest() {

    private val helper = IosCustomFieldProtoTestHelper.blocking("test_proto_enum_field_blocking")

    override val protoDatastore get() = helper.protoDatastore

    @BeforeTest
    fun setup() = helper.setup()

    @AfterTest
    fun tearDown() = helper.tearDown()
}
```

---

### Full Test File List

#### `commonTest/.../proto/` (new files)

| File | Type |
|------|------|
| `TestCustomFieldProtoData.kt` | Test data model + serializer + `TestColor` enum + `TestItem` class |
| `AbstractProtoEnumFieldTest.kt` | Suspending |
| `AbstractProtoEnumFieldBlockingTest.kt` | Blocking |
| `AbstractProtoNullableEnumFieldTest.kt` | Suspending |
| `AbstractProtoNullableEnumFieldBlockingTest.kt` | Blocking |
| `AbstractProtoEnumSetFieldTest.kt` | Suspending |
| `AbstractProtoEnumSetFieldBlockingTest.kt` | Blocking |
| `AbstractProtoKserializedFieldTest.kt` | Suspending |
| `AbstractProtoKserializedFieldBlockingTest.kt` | Blocking |
| `AbstractProtoNullableKserializedFieldTest.kt` | Suspending |
| `AbstractProtoNullableKserializedFieldBlockingTest.kt` | Blocking |
| `AbstractProtoKserializedListFieldTest.kt` | Suspending |
| `AbstractProtoKserializedListFieldBlockingTest.kt` | Blocking |
| `AbstractProtoNullableKserializedListFieldTest.kt` | Suspending |
| `AbstractProtoNullableKserializedListFieldBlockingTest.kt` | Blocking |
| `AbstractProtoKserializedSetFieldTest.kt` | Suspending |
| `AbstractProtoKserializedSetFieldBlockingTest.kt` | Blocking |
| `AbstractProtoSerializedFieldTest.kt` | Suspending |
| `AbstractProtoSerializedFieldBlockingTest.kt` | Blocking |
| `AbstractProtoNullableSerializedFieldTest.kt` | Suspending |
| `AbstractProtoNullableSerializedFieldBlockingTest.kt` | Blocking |
| `AbstractProtoSerializedListFieldTest.kt` | Suspending |
| `AbstractProtoSerializedListFieldBlockingTest.kt` | Blocking |
| `AbstractProtoNullableSerializedListFieldTest.kt` | Suspending |
| `AbstractProtoNullableSerializedListFieldBlockingTest.kt` | Blocking |
| `AbstractProtoSerializedSetFieldTest.kt` | Suspending |
| `AbstractProtoSerializedSetFieldBlockingTest.kt` | Blocking |

**Total: 1 data model + 26 abstract test classes**

#### `desktopTest/.../proto/` (new files)

| File |
|------|
| `DesktopCustomFieldProtoTestHelper.kt` |
| `DesktopProtoEnumFieldTest.kt` |
| `DesktopProtoEnumFieldBlockingTest.kt` |
| `DesktopProtoNullableEnumFieldTest.kt` |
| `DesktopProtoNullableEnumFieldBlockingTest.kt` |
| `DesktopProtoEnumSetFieldTest.kt` |
| `DesktopProtoEnumSetFieldBlockingTest.kt` |
| `DesktopProtoKserializedFieldTest.kt` |
| `DesktopProtoKserializedFieldBlockingTest.kt` |
| `DesktopProtoNullableKserializedFieldTest.kt` |
| `DesktopProtoNullableKserializedFieldBlockingTest.kt` |
| `DesktopProtoKserializedListFieldTest.kt` |
| `DesktopProtoKserializedListFieldBlockingTest.kt` |
| `DesktopProtoNullableKserializedListFieldTest.kt` |
| `DesktopProtoNullableKserializedListFieldBlockingTest.kt` |
| `DesktopProtoKserializedSetFieldTest.kt` |
| `DesktopProtoKserializedSetFieldBlockingTest.kt` |
| `DesktopProtoSerializedFieldTest.kt` |
| `DesktopProtoSerializedFieldBlockingTest.kt` |
| `DesktopProtoNullableSerializedFieldTest.kt` |
| `DesktopProtoNullableSerializedFieldBlockingTest.kt` |
| `DesktopProtoSerializedListFieldTest.kt` |
| `DesktopProtoSerializedListFieldBlockingTest.kt` |
| `DesktopProtoNullableSerializedListFieldTest.kt` |
| `DesktopProtoNullableSerializedListFieldBlockingTest.kt` |
| `DesktopProtoSerializedSetFieldTest.kt` |
| `DesktopProtoSerializedSetFieldBlockingTest.kt` |

**Total: 1 helper + 26 subclasses**

#### `androidDeviceTest/.../proto/` (new files)

Same 27 files as Desktop, prefixed with `Android` instead of `Desktop`, using
`AndroidCustomFieldProtoTestHelper`.

#### `iosSimulatorArm64Test/.../proto/` (new files)

Same 27 files as Desktop, prefixed with `Ios` instead of `Desktop`, using
`IosCustomFieldProtoTestHelper`.

---

### Build Verification

```bash
# Compile Android
./gradlew :generic-datastore:compileAndroidMain

# Compile Android instrumentation test source set
./gradlew :generic-datastore:compileAndroidDeviceTest

# Compile Desktop
./gradlew :generic-datastore:compileKotlinDesktop

# Compile Desktop test source set
./gradlew :generic-datastore:compileTestKotlinDesktop

# Run Desktop tests
./gradlew :generic-datastore:desktopTest
```

---

## Implementation Order

1. **Add `safeDeserialize` helper** to `GenericProtoDatastore.kt` (or as internal top-level).
2. **Add interface methods** to `ProtoDatastore.kt` (all 15 methods).
3. **Add reified extension functions** to `ProtoDatastore.kt`.
4. **Implement all methods** in `GenericProtoDatastore.kt`.
5. **Add `TestCustomProtoData`** + serializer in `commonTest`.
6. **Add abstract test classes** in `commonTest`.
7. **Add platform test subclasses** in `androidDeviceTest`, `desktopTest`, `iosSimulatorArm64Test`.
8. **Verify build** — compile all targets.
9. **Run tests** — desktop first, then Android/iOS.

---

## Usage Examples

### Enum Field

```kotlin
enum class Theme { LIGHT, DARK, SYSTEM }

data class AppSettings(
    val themeRaw: String = Theme.SYSTEM.name,
)

val themePref = protoDatastore.enumField<AppSettings, Theme>(
    key = "theme",
    defaultValue = Theme.SYSTEM,
    getter = { it.themeRaw },
    updater = { proto, raw -> proto.copy(themeRaw = raw) },
)

// Usage:
val current: Theme = themePref.get()          // Theme.SYSTEM
themePref.set(Theme.DARK)
```

### KSerialized Field

```kotlin
@Serializable
data class UserProfile(val name: String, val age: Int)

data class AppData(
    val profileJson: String = "",
)

val profilePref = protoDatastore.kserializedField<AppData, UserProfile>(
    key = "profile",
    defaultValue = UserProfile("", 0),
    getter = { it.profileJson },
    updater = { proto, raw -> proto.copy(profileJson = raw) },
)

// Usage:
profilePref.set(UserProfile("Alice", 30))
val profile: UserProfile = profilePref.get() // UserProfile("Alice", 30)
```

### Serialized Set Field

```kotlin
data class AppData(
    val tagsRaw: Set<String> = emptySet(),
)

val tagsPref = protoDatastore.serializedSetField<AppData, Tag>(
    key = "tags",
    defaultValue = emptySet(),
    serializer = { it.toJsonString() },
    deserializer = { Tag.fromJsonString(it) },
    getter = { it.tagsRaw },
    updater = { proto, raw -> proto.copy(tagsRaw = raw) },
)
```
