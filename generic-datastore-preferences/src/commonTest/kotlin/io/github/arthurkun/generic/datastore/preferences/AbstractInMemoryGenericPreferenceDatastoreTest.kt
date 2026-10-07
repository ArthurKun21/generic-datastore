package io.github.arthurkun.generic.datastore.preferences

import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import io.github.arthurkun.generic.datastore.core.InternalGenericDatastoreApi
import io.github.arthurkun.generic.datastore.preferences.core.SerializableObject
import io.github.arthurkun.generic.datastore.preferences.core.TestEnum
import io.github.arthurkun.generic.datastore.preferences.core.custom.KSerAddress
import io.github.arthurkun.generic.datastore.preferences.core.custom.KSerUser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.serializer
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull

/**
 * Shared suite for the `*InMemory` preference factories. Platform subclasses only supply a
 * datastore instance; the in-memory store is exercised through the public API while the
 * underlying file-backed datastore stays untouched.
 */
abstract class AbstractInMemoryGenericPreferenceDatastoreTest {

    abstract val preferenceDatastore: GenericPreferencesDatastore
    abstract val testDispatcher: TestDispatcher

    @Test
    fun stringInMemory_defaultValueWhenNotSet() = runTest(testDispatcher) {
        val pref = preferenceDatastore.stringInMemory("memString", "defaultValue")

        assertEquals("defaultValue", pref.get())
    }

    @Test
    fun stringInMemory_setAndGet() = runTest(testDispatcher) {
        val pref = preferenceDatastore.stringInMemory("memString", "defaultValue")

        pref.set("newValue")

        assertEquals("newValue", pref.get())
    }

    @Test
    fun stringInMemory_update() = runTest(testDispatcher) {
        val pref = preferenceDatastore.stringInMemory("memStringUpdate", "hello")

        pref.update { current -> "$current world" }

        assertEquals("hello world", pref.get())
    }

    @Test
    fun stringInMemory_deleteReturnsToDefault() = runTest(testDispatcher) {
        val pref = preferenceDatastore.stringInMemory("memStringDelete", "defaultValue")

        pref.set("valueToDelete")
        pref.delete()

        assertEquals("defaultValue", pref.get())
    }

    @Test
    fun stringInMemory_resetToDefault() = runTest(testDispatcher) {
        val pref = preferenceDatastore.stringInMemory("memStringReset", "defaultValue")

        pref.set("valueToReset")
        pref.resetToDefault()

        assertEquals("defaultValue", pref.get())
    }

    @Test
    fun stringInMemory_asFlowEmitsUpdate() = runTest(testDispatcher) {
        val pref = preferenceDatastore.stringInMemory("memStringFlow", "defaultFlow")

        pref.set("newFlowValue")

        assertEquals("newFlowValue", pref.asFlow().first())
    }

    @Test
    fun stringInMemory_stateInReflectsUpdate() = runTest(testDispatcher) {
        val pref = preferenceDatastore.stringInMemory("memStringState", "defaultState")
        val scope = CoroutineScope(Job() + testDispatcher)

        try {
            val state = pref.stateIn(scope)
            pref.set("stateValue")
            testDispatcher.scheduler.advanceUntilIdle()

            assertEquals("stateValue", state.value)
        } finally {
            scope.cancel()
        }
    }

    @Test
    fun intInMemory_setAndGet() = runTest(testDispatcher) {
        val pref = preferenceDatastore.intInMemory("memInt", 0)

        pref.set(42)
        pref.update { it + 8 }

        assertEquals(50, pref.get())
    }

    @Test
    fun longInMemory_setAndGet() = runTest(testDispatcher) {
        val pref = preferenceDatastore.longInMemory("memLong", 0L)

        pref.set(150L)

        assertEquals(150L, pref.get())
    }

    @Test
    fun floatInMemory_setAndGet() = runTest(testDispatcher) {
        val pref = preferenceDatastore.floatInMemory("memFloat", 0f)

        pref.set(2.5f)

        assertEquals(2.5f, pref.get())
    }

    @Test
    fun doubleInMemory_setAndGet() = runTest(testDispatcher) {
        val pref = preferenceDatastore.doubleInMemory("memDouble", 0.0)

        pref.set(9.99)

        assertEquals(9.99, pref.get())
    }

    @Test
    fun boolInMemory_setGetAndToggle() = runTest(testDispatcher) {
        val pref = preferenceDatastore.boolInMemory("memBool", false)

        pref.set(true)
        assertEquals(true, pref.get())

        pref.toggle()
        assertEquals(false, pref.get())
    }

    @Test
    fun stringSetInMemory_setAndGet() = runTest(testDispatcher) {
        val pref = preferenceDatastore.stringSetInMemory("memStringSet", emptySet())

        pref.set(setOf("a", "b"))
        pref.toggle("c")

        assertEquals(setOf("a", "b", "c"), pref.get())
    }

    @Test
    fun stringListInMemory_roundTrip() = runTest(testDispatcher) {
        val pref = preferenceDatastore.stringListInMemory("memStringList", emptyList())

        pref.set(listOf("one", "two", "three"))

        assertEquals(listOf("one", "two", "three"), pref.get())
    }

    @Test
    fun nullableStringInMemory_nullWhenMissingAndAfterNullWrite() = runTest(testDispatcher) {
        val pref = preferenceDatastore.nullableStringInMemory("memNullableString")

        assertNull(pref.get())

        pref.set("present")
        assertEquals("present", pref.get())

        pref.set(null)
        assertNull(pref.get())
    }

    @Test
    fun nullableStringInMemory_resetToDefaultRemovesKey() = runTest(testDispatcher) {
        val pref = preferenceDatastore.nullableStringInMemory("memNullableStringReset")

        pref.set("value")
        pref.resetToDefault()

        assertNull(pref.get())
    }

    @Test
    fun nullableIntInMemory_setAndGet() = runTest(testDispatcher) {
        val pref = preferenceDatastore.nullableIntInMemory("memNullableInt")

        pref.set(7)

        assertEquals(7, pref.get())
    }

    @Test
    fun nullableLongInMemory_setAndGet() = runTest(testDispatcher) {
        val pref = preferenceDatastore.nullableLongInMemory("memNullableLong")

        pref.set(77L)

        assertEquals(77L, pref.get())
    }

    @Test
    fun nullableFloatInMemory_setAndGet() = runTest(testDispatcher) {
        val pref = preferenceDatastore.nullableFloatInMemory("memNullableFloat")

        pref.set(1.25f)

        assertEquals(1.25f, pref.get())
    }

    @Test
    fun nullableDoubleInMemory_setAndGet() = runTest(testDispatcher) {
        val pref = preferenceDatastore.nullableDoubleInMemory("memNullableDouble")

        pref.set(3.5)

        assertEquals(3.5, pref.get())
    }

    @Test
    fun nullableBoolInMemory_setAndGet() = runTest(testDispatcher) {
        val pref = preferenceDatastore.nullableBoolInMemory("memNullableBool")

        assertNull(pref.get())

        pref.set(true)

        assertEquals(true, pref.get())
    }

    @Test
    fun nullableStringSetInMemory_setAndRemove() = runTest(testDispatcher) {
        val pref = preferenceDatastore.nullableStringSetInMemory("memNullableStringSet")

        assertNull(pref.get())

        pref.set(setOf("x", "y"))
        assertEquals(setOf("x", "y"), pref.get())

        pref.set(null)
        assertNull(pref.get())
    }

    @Test
    fun nullableStringListInMemory_setAndRemove() = runTest(testDispatcher) {
        val pref = preferenceDatastore.nullableStringListInMemory("memNullableStringList")

        pref.set(listOf("a", "b"))
        assertEquals(listOf("a", "b"), pref.get())

        pref.set(null)
        assertNull(pref.get())
    }

    @Test
    fun serializedInMemory_roundTrip() = runTest(testDispatcher) {
        val pref = preferenceDatastore.serializedInMemory(
            key = "memSerialized",
            defaultValue = SerializableObject(0, "default"),
            serializer = { "${it.id}|${it.name}" },
            deserializer = { raw ->
                val parts = raw.split("|", limit = 2)
                SerializableObject(parts[0].toInt(), parts[1])
            },
        )

        pref.set(SerializableObject(3, "target"))

        assertEquals(SerializableObject(3, "target"), pref.get())
    }

    @Test
    fun serializedInMemory_decodeFailureReturnsDefault() = runTest(testDispatcher) {
        val pref = preferenceDatastore.serializedInMemory(
            key = "memSerializedBad",
            defaultValue = SerializableObject(0, "default"),
            serializer = { "${it.id}|${it.name}" },
            deserializer = { raw ->
                val parts = raw.split("|", limit = 2)
                SerializableObject(parts[0].toInt(), parts[1])
            },
        )

        preferenceDatastore.inMemoryDatastore.edit {
            it[stringPreferencesKey("memSerializedBad")] = "garbage"
        }

        assertEquals(SerializableObject(0, "default"), pref.get())
    }

    @Test
    fun serializedSetInMemory_skipsUndecodableElements() = runTest(testDispatcher) {
        val pref = preferenceDatastore.serializedSetInMemory(
            key = "memSerializedSet",
            defaultValue = emptySet(),
            serializer = { "${it.id}|${it.name}" },
            deserializer = { raw ->
                val parts = raw.split("|", limit = 2)
                SerializableObject(parts[0].toInt(), parts[1])
            },
        )

        preferenceDatastore.inMemoryDatastore.edit {
            it[stringSetPreferencesKey("memSerializedSet")] = setOf("1|kept", "garbage")
        }

        assertEquals(setOf(SerializableObject(1, "kept")), pref.get())
    }

    @Test
    fun serializedListInMemory_roundTrip() = runTest(testDispatcher) {
        val pref = preferenceDatastore.serializedListInMemory(
            key = "memSerializedList",
            defaultValue = emptyList(),
            serializer = { "${it.id}|${it.name}" },
            deserializer = { raw ->
                val parts = raw.split("|", limit = 2)
                SerializableObject(parts[0].toInt(), parts[1])
            },
        )

        pref.set(listOf(SerializableObject(1, "a"), SerializableObject(2, "b")))

        assertEquals(listOf(SerializableObject(1, "a"), SerializableObject(2, "b")), pref.get())
    }

    @Test
    fun kserializedInMemory_roundTrip() = runTest(testDispatcher) {
        val pref = preferenceDatastore.kserializedInMemory("memKSerialized", KSerUser())

        pref.set(KSerUser(name = "Ada", age = 36))

        assertEquals(KSerUser(name = "Ada", age = 36), pref.get())
    }

    @Test
    fun kserializedInMemory_decodeFailureReturnsDefault() = runTest(testDispatcher) {
        val pref = preferenceDatastore.kserializedInMemory(
            key = "memKSerializedBad",
            defaultValue = KSerUser(name = "fallback"),
            serializer = serializer<KSerUser>(),
        )

        preferenceDatastore.inMemoryDatastore.edit {
            it[stringPreferencesKey("memKSerializedBad")] = "not json"
        }

        assertEquals(KSerUser(name = "fallback"), pref.get())
    }

    @Test
    fun kserializedSetInMemory_roundTrip() = runTest(testDispatcher) {
        val pref = preferenceDatastore.kserializedSetInMemory(
            key = "memKSerializedSet",
            defaultValue = emptySet(),
            serializer = serializer<KSerUser>(),
        )

        val expected = setOf(KSerUser(name = "Ada"), KSerUser(name = "Alan"))
        pref.set(expected)

        assertEquals(expected, pref.get())
    }

    @Test
    fun kserializedListInMemory_roundTrip() = runTest(testDispatcher) {
        val pref = preferenceDatastore.kserializedListInMemory(
            key = "memKSerializedList",
            defaultValue = emptyList(),
            serializer = serializer<KSerUser>(),
        )

        val expected = listOf(KSerUser(name = "Ada"), KSerUser(name = "Alan"))
        pref.set(expected)

        assertEquals(expected, pref.get())
    }

    @Test
    fun nullableSerializedInMemory_setAndRemove() = runTest(testDispatcher) {
        val pref = preferenceDatastore.nullableSerializedInMemory(
            key = "memNullableSerialized",
            serializer = { "${it.id}|${it.name}" },
            deserializer = { raw ->
                val parts = raw.split("|", limit = 2)
                SerializableObject(parts[0].toInt(), parts[1])
            },
        )

        assertNull(pref.get())

        pref.set(SerializableObject(9, "present"))
        assertEquals(SerializableObject(9, "present"), pref.get())

        pref.set(null)
        assertNull(pref.get())
    }

    @Test
    fun nullableKserializedInMemory_setAndRemove() = runTest(testDispatcher) {
        val pref = preferenceDatastore.nullableKserializedInMemory<KSerAddress>("memNullableKSerialized")

        assertNull(pref.get())

        pref.set(KSerAddress(street = "Main", city = "Springfield"))
        assertEquals(KSerAddress(street = "Main", city = "Springfield"), pref.get())

        pref.set(null)
        assertNull(pref.get())
    }

    @Test
    fun nullableSerializedListInMemory_setAndRemove() = runTest(testDispatcher) {
        val pref = preferenceDatastore.nullableSerializedListInMemory(
            key = "memNullableSerializedList",
            serializer = { it },
            deserializer = { it },
        )

        pref.set(listOf("a", "b"))
        assertEquals(listOf("a", "b"), pref.get())

        pref.set(null)
        assertNull(pref.get())
    }

    @Test
    fun nullableKserializedListInMemory_setAndRemove() = runTest(testDispatcher) {
        val pref = preferenceDatastore.nullableKserializedListInMemory(
            key = "memNullableKSerializedList",
            serializer = serializer<KSerUser>(),
        )

        val expected = listOf(KSerUser(name = "Ada"))
        pref.set(expected)
        assertEquals(expected, pref.get())

        pref.set(null)
        assertNull(pref.get())
    }

    @Test
    fun nullableSerializedSetInMemory_setAndRemove() = runTest(testDispatcher) {
        val pref = preferenceDatastore.nullableSerializedSetInMemory(
            key = "memNullableSerializedSet",
            serializer = { it },
            deserializer = { it },
        )

        pref.set(setOf("a", "b"))
        assertEquals(setOf("a", "b"), pref.get())

        pref.set(null)
        assertNull(pref.get())
    }

    @Test
    fun nullableKserializedSetInMemory_setAndRemove() = runTest(testDispatcher) {
        val pref = preferenceDatastore.nullableKserializedSetInMemory(
            key = "memNullableKSerializedSet",
            serializer = serializer<KSerUser>(),
        )

        val expected = setOf(KSerUser(name = "Ada"))
        pref.set(expected)
        assertEquals(expected, pref.get())

        pref.set(null)
        assertNull(pref.get())
    }

    @Test
    fun enumInMemory_setAndGetWithUnknownNameFallback() = runTest(testDispatcher) {
        val pref = preferenceDatastore.enumInMemory(
            key = "memEnum",
            defaultValue = TestEnum.VALUE_A,
            enumValues = TestEnum.entries.toTypedArray(),
        )

        assertEquals(TestEnum.VALUE_A, pref.get())

        pref.set(TestEnum.VALUE_C)
        assertEquals(TestEnum.VALUE_C, pref.get())

        preferenceDatastore.inMemoryDatastore.edit {
            it[stringPreferencesKey("memEnum")] = "NOT_A_REAL_VALUE"
        }

        assertEquals(TestEnum.VALUE_A, pref.get())
    }

    @Test
    fun enumSetInMemory_roundTrip() = runTest(testDispatcher) {
        val pref = preferenceDatastore.enumSetInMemory(
            key = "memEnumSet",
            defaultValue = emptySet(),
            enumValues = TestEnum.entries.toTypedArray(),
        )

        pref.set(setOf(TestEnum.VALUE_A, TestEnum.VALUE_B))

        assertEquals(setOf(TestEnum.VALUE_A, TestEnum.VALUE_B), pref.get())
    }

    @Test
    fun nullableEnumInMemory_setRemoveAndUnknownNameFallback() = runTest(testDispatcher) {
        val pref = preferenceDatastore.nullableEnumInMemory(
            key = "memNullableEnum",
            enumValues = TestEnum.entries.toTypedArray(),
        )

        assertNull(pref.get())

        pref.set(TestEnum.VALUE_B)
        assertEquals(TestEnum.VALUE_B, pref.get())

        preferenceDatastore.inMemoryDatastore.edit {
            it[stringPreferencesKey("memNullableEnum")] = "NOPE"
        }
        assertNull(pref.get())

        pref.set(TestEnum.VALUE_B)
        pref.set(null)
        assertNull(pref.get())
    }

    @Test
    fun nullableEnumSetInMemory_setAndRemove() = runTest(testDispatcher) {
        val pref = preferenceDatastore.nullableEnumSetInMemory(
            key = "memNullableEnumSet",
            enumValues = TestEnum.entries.toTypedArray(),
        )

        pref.set(setOf(TestEnum.VALUE_B, TestEnum.VALUE_C))
        assertEquals(setOf(TestEnum.VALUE_B, TestEnum.VALUE_C), pref.get())

        pref.set(null)
        assertNull(pref.get())
    }

    @Test
    fun inMemory_prefIsIndependentFromDiskPrefWithSameKey() = runTest(testDispatcher) {
        val diskPref = preferenceDatastore.string("memIso", "diskDefault")
        val memPref = preferenceDatastore.stringInMemory("memIso", "memDefault")

        assertEquals("diskDefault", diskPref.get())
        assertEquals("memDefault", memPref.get())

        memPref.set("memValue")
        assertEquals("diskDefault", diskPref.get())

        diskPref.set("diskValue")
        assertEquals("memValue", memPref.get())
        assertEquals("diskValue", diskPref.get())
    }

    @Test
    fun inMemory_handlesSharingKeySeeSameValue() = runTest(testDispatcher) {
        val first = preferenceDatastore.stringInMemory("memShared", "default")
        val second = preferenceDatastore.stringInMemory("memShared", "default")

        first.set("updated")

        assertEquals("updated", second.get())
    }

    @Test
    fun inMemory_datastoresAreIndependent() = runTest(testDispatcher) {
        val memPref = preferenceDatastore.stringInMemory("memCross", "default")
        val otherDatastore = createInMemoryBackedDatastore()

        try {
            val otherMemPref = otherDatastore.stringInMemory("memCross", "default")

            memPref.set("fromThis")

            assertEquals("default", otherMemPref.get())
        } finally {
            otherDatastore.close()
        }
    }

    @Test
    fun clearAll_doesNotAffectInMemoryPreferences() = runTest(testDispatcher) {
        val diskPref = preferenceDatastore.string("memClearDisk", "diskDefault")
        val memPref = preferenceDatastore.stringInMemory("memClearMem", "memDefault")

        diskPref.set("diskValue")
        memPref.set("memValue")

        preferenceDatastore.clearAll()

        assertEquals("diskDefault", diskPref.get())
        assertEquals("memValue", memPref.get())
    }

    @Test
    fun exportAsString_excludesInMemoryKeys() = runTest(testDispatcher) {
        preferenceDatastore.stringInMemory("memExport", "default").set("memSecret")

        val backupJson = preferenceDatastore.exportAsString()

        assertFalse(backupJson.contains("memExport"))
    }

    @Test
    fun importData_doesNotTouchInMemoryPreferences() = runTest(testDispatcher) {
        val diskPref = preferenceDatastore.string("memImport", "diskDefault")
        val memPref = preferenceDatastore.stringInMemory("memImport", "memDefault")

        diskPref.set("diskValue")
        val backup = preferenceDatastore.exportAsData()

        memPref.set("memValue")
        preferenceDatastore.importData(backup)

        assertEquals("diskValue", diskPref.get())
        assertEquals("memValue", memPref.get())
    }

    @Test
    fun batch_operationsRejectInMemoryPreferences() = runTest(testDispatcher) {
        val memPref = preferenceDatastore.stringInMemory("memBatchGuard", "default")

        assertFailsWith<IllegalStateException> {
            preferenceDatastore.batchReadValues { add(memPref) }
        }
        assertFailsWith<IllegalStateException> {
            preferenceDatastore.batchWrite { this[add(memPref)] = "written" }
        }
        assertFailsWith<IllegalStateException> {
            preferenceDatastore.batchDelete { add(memPref) }
        }

        assertEquals("default", memPref.get())
    }

    @OptIn(InternalGenericDatastoreApi::class)
    private fun createInMemoryBackedDatastore(): GenericPreferencesDatastore =
        GenericPreferencesDatastore(datastore = InMemoryGenericPreferenceDatastore())
}
