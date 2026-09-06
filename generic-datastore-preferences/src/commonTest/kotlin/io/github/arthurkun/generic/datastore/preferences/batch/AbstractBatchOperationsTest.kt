package io.github.arthurkun.generic.datastore.preferences.batch

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import io.github.arthurkun.generic.datastore.preferences.GenericPreferencesDatastore
import io.github.arthurkun.generic.datastore.preferences.Preference
import io.github.arthurkun.generic.datastore.preferences.enum
import io.github.arthurkun.generic.datastore.preferences.enumSet
import io.github.arthurkun.generic.datastore.preferences.kserialized
import io.github.arthurkun.generic.datastore.preferences.kserializedList
import io.github.arthurkun.generic.datastore.preferences.kserializedSet
import io.github.arthurkun.generic.datastore.preferences.mapIO
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.Serializable
import kotlin.reflect.KProperty
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

private enum class BatchTestEnum { FIRST, SECOND }

@Serializable
internal data class BatchTestPayload(val id: Int, val label: String)

abstract class AbstractBatchOperationsTest {

    abstract val preferenceDatastore: GenericPreferencesDatastore
    abstract val dataStore: DataStore<Preferences>
    abstract val testDispatcher: TestDispatcher

    // -- prefBatch builder --

    @Test
    fun prefBatch_declaresPreferencesInOrder() = runTest(testDispatcher) {
        var first: BatchPref<Int>? = null
        var second: BatchPref<String>? = null
        var third: BatchPref<Boolean>? = null

        val batch = prefBatch {
            first = int("builder_first", 1)
            second = string("builder_second", "a")
            third = bool("builder_third", true)
        }

        assertEquals(3, batch.size)
        assertEquals("builder_first", batch[0].key)
        assertEquals("builder_second", batch[1].key)
        assertEquals("builder_third", batch[2].key)
        assertEquals(1, requireNotNull(first).defaultValue)
        assertEquals("a", requireNotNull(second).defaultValue)
        assertEquals(true, requireNotNull(third).defaultValue)
    }

    @Test
    fun prefBatch_usesImplicitPrimitiveDefaults() = runTest(testDispatcher) {
        var intPref: BatchPref<Int>? = null
        var longPref: BatchPref<Long>? = null
        var floatPref: BatchPref<Float>? = null
        var doublePref: BatchPref<Double>? = null
        var boolPref: BatchPref<Boolean>? = null
        var stringPref: BatchPref<String>? = null
        var stringSetPref: BatchPref<Set<String>>? = null
        var stringListPref: BatchPref<List<String>>? = null

        prefBatch {
            intPref = int("builder_default_int")
            longPref = long("builder_default_long")
            floatPref = float("builder_default_float")
            doublePref = double("builder_default_double")
            boolPref = bool("builder_default_bool")
            stringPref = string("builder_default_string")
            stringSetPref = stringSet("builder_default_stringSet")
            stringListPref = stringList("builder_default_stringList")
        }

        assertEquals(0, requireNotNull(intPref).defaultValue)
        assertEquals(0L, requireNotNull(longPref).defaultValue)
        assertEquals(0f, requireNotNull(floatPref).defaultValue)
        assertEquals(0.0, requireNotNull(doublePref).defaultValue)
        assertEquals(false, requireNotNull(boolPref).defaultValue)
        assertEquals("", requireNotNull(stringPref).defaultValue)
        assertEquals(emptySet(), requireNotNull(stringSetPref).defaultValue)
        assertEquals(emptyList(), requireNotNull(stringListPref).defaultValue)
    }

    @Test
    fun prefBatch_rejectsDuplicateKeys() = runTest(testDispatcher) {
        assertFailsWith<IllegalArgumentException> {
            prefBatch {
                int("builder_duplicate", 1)
                string("builder_duplicate", "x")
            }
        }
    }

    @Test
    fun prefBatch_rejectsBlankKeys() = runTest(testDispatcher) {
        assertFailsWith<IllegalArgumentException> {
            prefBatch {
                int("   ", 1)
            }
        }
    }

    @Test
    fun prefBatch_addRegistersExistingHandle() = runTest(testDispatcher) {
        val original = prefBatch {
            int("builder_add_int", 3)
        }
        val handle = original[0] as BatchPref<Int>

        val batch = prefBatch {
            add(handle)
            bool("builder_add_bool", true)
        }

        assertEquals(2, batch.size)
        assertEquals("builder_add_int", batch[0].key)
        assertEquals("builder_add_bool", batch[1].key)
        assertEquals(handle, batch[0])
    }

    // -- batchRead --

    @Test
    fun batchRead_returnsDefaultsWhenKeysAreAbsent() = runTest(testDispatcher) {
        var intPref: BatchPref<Int>? = null
        var stringPref: BatchPref<String>? = null
        var boolPref: BatchPref<Boolean>? = null
        var stringSetPref: BatchPref<Set<String>>? = null
        var stringListPref: BatchPref<List<String>>? = null

        val batch = prefBatch {
            intPref = int("read_default_int", 7)
            stringPref = string("read_default_string", "gone")
            boolPref = bool("read_default_bool", true)
            stringSetPref = stringSet("read_default_stringSet", setOf("a"))
            stringListPref = stringList("read_default_stringList", listOf("x"))
        }

        val values = preferenceDatastore.batchRead(batch)

        assertEquals(7, values[requireNotNull(intPref)])
        assertEquals("gone", values[requireNotNull(stringPref)])
        assertEquals(true, values[requireNotNull(boolPref)])
        assertEquals(setOf("a"), values[requireNotNull(stringSetPref)])
        assertEquals(listOf("x"), values[requireNotNull(stringListPref)])
    }

    @Test
    fun batchRead_returnsStoredPrimitiveValues() = runTest(testDispatcher) {
        var intPref: BatchPref<Int>? = null
        var longPref: BatchPref<Long>? = null
        var floatPref: BatchPref<Float>? = null
        var doublePref: BatchPref<Double>? = null
        var stringSetPref: BatchPref<Set<String>>? = null

        val batch = prefBatch {
            intPref = int("read_stored_int", 0)
            longPref = long("read_stored_long", 0L)
            floatPref = float("read_stored_float", 0f)
            doublePref = double("read_stored_double", 0.0)
            stringSetPref = stringSet("read_stored_stringSet")
        }

        preferenceDatastore.int("read_stored_int", 0).set(42)
        preferenceDatastore.long("read_stored_long", 0L).set(99L)
        preferenceDatastore.float("read_stored_float", 0f).set(1.5f)
        preferenceDatastore.double("read_stored_double", 0.0).set(2.5)
        preferenceDatastore.stringSet("read_stored_stringSet").set(setOf("a", "b"))

        val values = preferenceDatastore.batchRead(batch)

        assertEquals(42, values[requireNotNull(intPref)])
        assertEquals(99L, values[requireNotNull(longPref)])
        assertEquals(1.5f, values[requireNotNull(floatPref)])
        assertEquals(2.5, values[requireNotNull(doublePref)])
        assertEquals(setOf("a", "b"), values[requireNotNull(stringSetPref)])
    }

    @Test
    fun batchRead_readsNullablePreferences() = runTest(testDispatcher) {
        var nullableString: BatchPref<String?>? = null
        var nullableInt: BatchPref<Int?>? = null
        var nullableBool: BatchPref<Boolean?>? = null
        var nullableStringSet: BatchPref<Set<String>?>? = null

        val batch = prefBatch {
            nullableString = nullableString("read_nullable_string")
            nullableInt = nullableInt("read_nullable_int")
            nullableBool = nullableBool("read_nullable_bool")
            nullableStringSet = nullableStringSet("read_nullable_stringSet")
        }

        val values = preferenceDatastore.batchRead(batch)

        assertNull(values[requireNotNull(nullableString)])
        assertNull(values[requireNotNull(nullableInt)])
        assertNull(values[requireNotNull(nullableBool)])
        assertNull(values[requireNotNull(nullableStringSet)])

        preferenceDatastore.nullableString("read_nullable_string").set("present")
        preferenceDatastore.nullableInt("read_nullable_int").set(9)

        val updated = preferenceDatastore.batchRead(batch)
        assertEquals("present", updated[requireNotNull(nullableString)])
        assertEquals(9, updated[requireNotNull(nullableInt)])
    }

    @Test
    fun batchRead_roundTripsCustomSerializedValues() = runTest(testDispatcher) {
        var payloadPref: BatchPref<BatchTestPayload>? = null
        var enumPref: BatchPref<BatchTestEnum>? = null
        var setPref: BatchPref<Set<Int>>? = null
        var listPref: BatchPref<List<Int>>? = null
        var kserializedPref: BatchPref<BatchTestPayload>? = null

        val batch = prefBatch {
            payloadPref = serialized(
                key = "read_custom_payload",
                defaultValue = BatchTestPayload(0, "none"),
                serializer = { "${it.id}|${it.label}" },
                deserializer = { raw ->
                    val parts = raw.split("|")
                    BatchTestPayload(parts[0].toInt(), parts[1])
                },
            )
            enumPref = enum("read_custom_enum", BatchTestEnum.FIRST)
            setPref = serializedSet(
                key = "read_custom_set",
                serializer = { it.toString() },
                deserializer = { it.toInt() },
            )
            listPref = serializedList(
                key = "read_custom_list",
                serializer = { it.toString() },
                deserializer = { it.toInt() },
            )
            kserializedPref = kserialized("read_custom_kserialized", BatchTestPayload(0, "none"))
        }

        preferenceDatastore.serialized(
            key = "read_custom_payload",
            defaultValue = BatchTestPayload(0, "none"),
            serializer = { "${it.id}|${it.label}" },
            deserializer = { raw ->
                val parts = raw.split("|")
                BatchTestPayload(parts[0].toInt(), parts[1])
            },
        ).set(BatchTestPayload(3, "three"))
        preferenceDatastore.enum("read_custom_enum", BatchTestEnum.FIRST).set(BatchTestEnum.SECOND)
        preferenceDatastore.serializedSet(
            key = "read_custom_set",
            defaultValue = emptySet(),
            serializer = { it.toString() },
            deserializer = { it.toInt() },
        ).set(setOf(1, 2))
        preferenceDatastore.serializedList(
            key = "read_custom_list",
            defaultValue = emptyList(),
            serializer = { it.toString() },
            deserializer = { it.toInt() },
        ).set(listOf(1, 2))
        preferenceDatastore.kserialized(
            "read_custom_kserialized",
            BatchTestPayload(0, "none"),
        ).set(BatchTestPayload(4, "four"))

        val values = preferenceDatastore.batchRead(batch)

        assertEquals(BatchTestPayload(3, "three"), values[requireNotNull(payloadPref)])
        assertEquals(BatchTestEnum.SECOND, values[requireNotNull(enumPref)])
        assertEquals(setOf(1, 2), values[requireNotNull(setPref)])
        assertEquals(listOf(1, 2), values[requireNotNull(listPref)])
        assertEquals(BatchTestPayload(4, "four"), values[requireNotNull(kserializedPref)])
    }

    @Test
    fun batchRead_fallsBackToDefaultOnCorruptedValues() = runTest(testDispatcher) {
        var payloadPref: BatchPref<BatchTestPayload>? = null
        var enumPref: BatchPref<BatchTestEnum>? = null
        var setPref: BatchPref<Set<Int>>? = null
        var listPref: BatchPref<List<Int>>? = null
        var stringListPref: BatchPref<List<String>>? = null

        val batch = prefBatch {
            payloadPref = serialized(
                key = "read_corrupt_payload",
                defaultValue = BatchTestPayload(-1, "fallback"),
                serializer = { "${it.id}|${it.label}" },
                deserializer = { raw ->
                    val parts = raw.split("|")
                    BatchTestPayload(parts[0].toInt(), parts[1])
                },
            )
            enumPref = enum("read_corrupt_enum", BatchTestEnum.FIRST)
            setPref = serializedSet(
                key = "read_corrupt_set",
                defaultValue = setOf(7),
                serializer = { it.toString() },
                deserializer = { it.toInt() },
            )
            listPref = serializedList(
                key = "read_corrupt_list",
                defaultValue = listOf(7),
                serializer = { it.toString() },
                deserializer = { it.toInt() },
            )
            stringListPref = stringList("read_corrupt_stringList", listOf("fallback"))
        }

        dataStore.edit { prefs ->
            prefs[stringPreferencesKey("read_corrupt_payload")] = "not|a|valid|payload"
            prefs[stringPreferencesKey("read_corrupt_enum")] = "UNKNOWN_NAME"
            prefs[stringSetPreferencesKey("read_corrupt_set")] = setOf("1", "nope", "2")
            prefs[stringPreferencesKey("read_corrupt_list")] = "not-json"
            prefs[stringPreferencesKey("read_corrupt_stringList")] = "not-json"
        }

        val values = preferenceDatastore.batchRead(batch)

        assertEquals(BatchTestPayload(-1, "fallback"), values[requireNotNull(payloadPref)])
        assertEquals(BatchTestEnum.FIRST, values[requireNotNull(enumPref)])
        assertEquals(setOf(1, 2), values[requireNotNull(setPref)])
        assertEquals(listOf(7), values[requireNotNull(listPref)])
        assertEquals(listOf("fallback"), values[requireNotNull(stringListPref)])
    }

    @Test
    fun batchRead_fallsBackToNullOnCorruptedNullableCustomValues() = runTest(testDispatcher) {
        var nullablePayload: BatchPref<BatchTestPayload?>? = null
        var nullableEnumPref: BatchPref<BatchTestEnum?>? = null
        var nullableStringListPref: BatchPref<List<String>?>? = null

        val batch = prefBatch {
            nullablePayload = nullableSerialized(
                key = "read_corrupt_nullable_payload",
                serializer = { "${it.id}|${it.label}" },
                deserializer = { raw ->
                    val parts = raw.split("|")
                    BatchTestPayload(parts[0].toInt(), parts[1])
                },
            )
            nullableEnumPref = nullableEnum("read_corrupt_nullable_enum")
            nullableStringListPref = nullableStringList("read_corrupt_nullable_stringList")
        }

        dataStore.edit { prefs ->
            prefs[stringPreferencesKey("read_corrupt_nullable_payload")] = "bogus"
            prefs[stringPreferencesKey("read_corrupt_nullable_enum")] = "UNKNOWN_NAME"
            prefs[stringPreferencesKey("read_corrupt_nullable_stringList")] = "bogus"
        }

        val values = preferenceDatastore.batchRead(batch)

        assertNull(values[requireNotNull(nullablePayload)])
        assertNull(values[requireNotNull(nullableEnumPref)])
        assertNull(values[requireNotNull(nullableStringListPref)])
    }

    @Test
    fun batchRead_skipsFailingElementsInNullableList() = runTest(testDispatcher) {
        var nullableList: BatchPref<List<Int>?>? = null

        val batch = prefBatch {
            nullableList = nullableSerializedList(
                key = "read_nullable_corrupt_list",
                serializer = { it.toString() },
                deserializer = { it.toInt() },
            )
        }

        dataStore.edit { prefs ->
            prefs[stringPreferencesKey("read_nullable_corrupt_list")] = """["1","bad","2"]"""
        }

        val values = preferenceDatastore.batchRead(batch)

        assertEquals(listOf(1, 2), values[requireNotNull(nullableList)])
    }

    @Test
    fun batchRead_exposesTypedAccessAndWholeBatchMap() = runTest(testDispatcher) {
        var intPref: BatchPref<Int>? = null
        var stringPref: BatchPref<String>? = null

        val batch = prefBatch {
            intPref = int("read_map_int", 5)
            stringPref = string("read_map_string", "s")
        }

        val values = preferenceDatastore.batchRead(batch)

        assertEquals(5, values[requireNotNull(intPref)])
        assertEquals("s", values[requireNotNull(stringPref)])

        val map = values.toMap()
        assertEquals(2, map.size)
        assertEquals(5, map[requireNotNull(intPref)])
        assertEquals("s", map[requireNotNull(stringPref)])
    }

    @Test
    fun batchRead_rejectsHandlesOutsideTheBatch() = runTest(testDispatcher) {
        var declared: BatchPref<Int>? = null
        val batch = prefBatch {
            declared = int("read_outside_declared", 0)
        }

        val values = preferenceDatastore.batchRead(batch)
        val undeclared = prefBatch { int("read_outside_undeclared", 0) }[0]

        assertEquals(0, values[requireNotNull(declared)])
        assertFailsWith<IllegalStateException> {
            @Suppress("UNUSED_EXPRESSION")
            values[undeclared as BatchPref<Int>]
        }
    }

    @Test
    fun batchRead_supportsExistingPreferencesViaAdd() = runTest(testDispatcher) {
        val stringSingle = preferenceDatastore.string("read_adapter_string", "default")
        val mapped = preferenceDatastore.int("read_adapter_int", 1).mapIO(
            convert = { it.toString() },
            reverse = { it.toInt() },
        )
        stringSingle.set("stored")
        mapped.set("41")

        var stringHandle: BatchPref<String>? = null
        var mappedHandle: BatchPref<String>? = null
        val batch = prefBatch {
            stringHandle = add(stringSingle)
            mappedHandle = add(mapped)
        }

        val values = preferenceDatastore.batchRead(batch)

        assertEquals("stored", values[requireNotNull(stringHandle)])
        assertEquals("41", values[requireNotNull(mappedHandle)])
    }

    @Test
    fun batchRead_rejectsForeignPreferenceImplementations() = runTest(testDispatcher) {
        val foreign = object : Preference<Int> {
            override fun key(): String = "read_foreign"
            override suspend fun get(): Int = defaultValue
            override suspend fun set(value: Int) = Unit
            override suspend fun update(transform: (Int) -> Int) = Unit
            override suspend fun delete() = Unit
            override suspend fun resetToDefault() = Unit
            override val defaultValue: Int = 0
            override fun asFlow(): Flow<Int> = flowOf(defaultValue)
            override fun stateIn(scope: CoroutineScope, started: SharingStarted): StateFlow<Int> =
                MutableStateFlow(defaultValue)
            override fun getBlocking(): Int = defaultValue
            override fun setBlocking(value: Int) = Unit
            override fun resetToDefaultBlocking() = Unit
            override fun getValue(thisRef: Any?, property: KProperty<*>): Int = defaultValue
            override fun setValue(thisRef: Any?, property: KProperty<*>, value: Int) = Unit
        }

        assertFailsWith<IllegalStateException> {
            prefBatch { add(foreign) }
        }
    }

    // -- batchReadFlow --

    @Test
    fun batchReadFlow_emitsSnapshotsOnChange() = runTest(testDispatcher) {
        var stringPref: BatchPref<String>? = null
        val batch = prefBatch {
            stringPref = string("flow_string", "before")
        }
        val handle = requireNotNull(stringPref)

        val initial = preferenceDatastore.batchReadFlow(batch).first()
        assertEquals("before", initial[handle])

        preferenceDatastore.string("flow_string", "before").set("after")

        val updated = preferenceDatastore.batchReadFlow(batch).first()
        assertEquals("after", updated[handle])
    }

    @Test
    fun batchReadFlow_distinctUntilChangedSuppressesEqualSnapshots() = runTest(testDispatcher) {
        var intPref: BatchPref<Int>? = null
        val batch = prefBatch {
            intPref = int("flow_distinct_int", 0)
        }
        val handle = requireNotNull(intPref)
        val single = preferenceDatastore.int("flow_distinct_int", 0)
        val emissions = mutableListOf<Int>()

        val collection = backgroundScope.launch(testDispatcher) {
            preferenceDatastore
                .batchReadFlow(batch, distinctUntilChanged = true) { this[handle] }
                .take(2)
                .toList(emissions)
        }

        runCurrent()
        single.set(0) // unchanged snapshot -> suppressed
        runCurrent()
        single.set(5) // changed -> emitted
        collection.join()

        assertEquals(listOf(0, 5), emissions)
    }

    // -- batchWrite --

    @Test
    fun batchWrite_setsMultiplePreferences() = runTest(testDispatcher) {
        var stringPref: BatchPref<String>? = null
        var intPref: BatchPref<Int>? = null
        var boolPref: BatchPref<Boolean>? = null

        val batch = prefBatch {
            stringPref = string("write_string", "default")
            intPref = int("write_int", 0)
            boolPref = bool("write_bool", false)
        }

        preferenceDatastore.batchWrite(batch) {
            set(requireNotNull(stringPref), "written")
            set(requireNotNull(intPref), 123)
            set(requireNotNull(boolPref), true)
        }

        assertEquals("written", preferenceDatastore.string("write_string", "default").get())
        assertEquals(123, preferenceDatastore.int("write_int", 0).get())
        assertEquals(true, preferenceDatastore.bool("write_bool", false).get())
    }

    @Test
    fun batchWrite_indexOperatorSyntax() = runTest(testDispatcher) {
        var stringPref: BatchPref<String>? = null
        val batch = prefBatch {
            stringPref = string("write_idx_string", "default")
        }
        val handle = requireNotNull(stringPref)

        preferenceDatastore.batchWrite(batch) {
            this[handle] = "via_operator"
        }

        assertEquals("via_operator", preferenceDatastore.string("write_idx_string", "default").get())
    }

    @Test
    fun batchWrite_deleteAndSetInSameTransaction() = runTest(testDispatcher) {
        var first: BatchPref<String>? = null
        var second: BatchPref<String>? = null
        val batch = prefBatch {
            first = string("write_tx_first", "default1")
            second = string("write_tx_second", "default2")
        }

        preferenceDatastore.string("write_tx_first", "default1").set("existing1")
        preferenceDatastore.string("write_tx_second", "default2").set("existing2")

        preferenceDatastore.batchWrite(batch) {
            delete(requireNotNull(first))
            set(requireNotNull(second), "new2")
        }

        assertEquals("default1", preferenceDatastore.string("write_tx_first", "default1").get())
        assertEquals("new2", preferenceDatastore.string("write_tx_second", "default2").get())
    }

    @Test
    fun batchWrite_resetToDefault() = runTest(testDispatcher) {
        var intPref: BatchPref<Int>? = null
        val batch = prefBatch {
            intPref = int("write_reset_int", 42)
        }
        val single = preferenceDatastore.int("write_reset_int", 42)
        single.set(99)

        preferenceDatastore.batchWrite(batch) {
            resetToDefault(requireNotNull(intPref))
        }

        assertEquals(42, single.get())
    }

    @Test
    fun batchWrite_nullableNullWriteRemovesKey() = runTest(testDispatcher) {
        var nullableString: BatchPref<String?>? = null
        val batch = prefBatch {
            nullableString = nullableString("write_nullable_string")
        }
        val handle = requireNotNull(nullableString)
        val single = preferenceDatastore.nullableString("write_nullable_string")

        preferenceDatastore.batchWrite(batch) {
            set(handle, "value")
        }
        assertEquals("value", single.get())

        preferenceDatastore.batchWrite(batch) {
            set(handle, null)
        }
        assertNull(single.get())
        assertTrue(
            dataStore.data.first().asMap().none { it.key.name == "write_nullable_string" },
        )
    }

    @Test
    fun batchWrite_writesListAndSetFormats() = runTest(testDispatcher) {
        var stringListPref: BatchPref<List<String>>? = null
        var nullableStringListPref: BatchPref<List<String>?>? = null
        var serializedSetPref: BatchPref<Set<Int>>? = null
        var kserializedSetPref: BatchPref<Set<BatchTestPayload>>? = null

        val batch = prefBatch {
            stringListPref = stringList("write_stringList")
            nullableStringListPref = nullableStringList("write_nullable_stringList")
            serializedSetPref = serializedSet(
                key = "write_serializedSet",
                serializer = { it.toString() },
                deserializer = { it.toInt() },
            )
            kserializedSetPref = kserializedSet("write_kserializedSet")
        }

        preferenceDatastore.batchWrite(batch) {
            set(requireNotNull(stringListPref), listOf("a", "b"))
            set(requireNotNull(nullableStringListPref), listOf("x"))
            set(requireNotNull(serializedSetPref), setOf(1, 2))
            set(requireNotNull(kserializedSetPref), setOf(BatchTestPayload(1, "one")))
        }

        assertEquals(listOf("a", "b"), preferenceDatastore.stringList("write_stringList").get())
        assertEquals(
            listOf("x"),
            preferenceDatastore.nullableStringList("write_nullable_stringList").get(),
        )
        assertEquals(
            setOf(1, 2),
            preferenceDatastore.serializedSet(
                key = "write_serializedSet",
                serializer = { it.toString() },
                deserializer = { it.toInt() },
            ).get(),
        )
        assertEquals(
            setOf(BatchTestPayload(1, "one")),
            preferenceDatastore.kserializedSet<BatchTestPayload>("write_kserializedSet").get(),
        )
    }

    @Test
    fun batchWrite_allowsHandlesOutsideTheBatch() = runTest(testDispatcher) {
        val declaredBatch = prefBatch {
            int("write_outside_declared", 0)
        }
        var undeclared: BatchPref<Int>? = null
        prefBatch {
            undeclared = int("write_outside_undeclared", 0)
        }

        preferenceDatastore.batchWrite(declaredBatch) {
            set(requireNotNull(undeclared), 5)
        }

        assertEquals(5, preferenceDatastore.int("write_outside_undeclared", 0).get())
    }

    // -- batchDelete --

    @Test
    fun batchDelete_removesEveryDeclaredKey() = runTest(testDispatcher) {
        var intPref: BatchPref<Int>? = null
        var stringPref: BatchPref<String>? = null
        var nullableString: BatchPref<String?>? = null
        var listPref: BatchPref<List<String>>? = null

        val batch = prefBatch {
            intPref = int("delete_int", 1)
            stringPref = string("delete_string", "d")
            nullableString = nullableString("delete_nullable")
            listPref = stringList("delete_stringList")
        }

        preferenceDatastore.batchWrite(batch) {
            set(requireNotNull(intPref), 10)
            set(requireNotNull(stringPref), "stored")
            set(requireNotNull(nullableString), "present")
            set(requireNotNull(listPref), listOf("a"))
        }
        preferenceDatastore.batchDelete(batch)

        val values = preferenceDatastore.batchRead(batch)
        assertEquals(1, values[requireNotNull(intPref)])
        assertEquals("d", values[requireNotNull(stringPref)])
        assertNull(values[requireNotNull(nullableString)])
        assertEquals(emptyList(), values[requireNotNull(listPref)])
    }

    @Test
    fun batchDelete_keepsUndeclaredKeys() = runTest(testDispatcher) {
        var declared: BatchPref<Int>? = null
        val batch = prefBatch {
            declared = int("delete_partial_declared", 0)
        }
        val undeclared = preferenceDatastore.int("delete_partial_undeclared", 0)
        undeclared.set(77)

        preferenceDatastore.batchDelete(batch)

        assertEquals(77, undeclared.get())
    }

    // -- batchUpdate --

    @Test
    fun batchUpdate_readsAndWritesAtomically() = runTest(testDispatcher) {
        var counter: BatchPref<Int>? = null
        var label: BatchPref<String>? = null
        val batch = prefBatch {
            counter = int("update_counter", 10)
            label = string("update_label", "count:")
        }

        preferenceDatastore.batchUpdate(batch) {
            val currentCount = get(requireNotNull(counter))
            val currentLabel = get(requireNotNull(label))
            set(requireNotNull(counter), currentCount + 5)
            set(requireNotNull(label), "$currentLabel $currentCount")
        }

        assertEquals(15, preferenceDatastore.int("update_counter", 10).get())
        assertEquals("count: 10", preferenceDatastore.string("update_label", "count:").get())
    }

    @Test
    fun batchUpdate_transformFunction() = runTest(testDispatcher) {
        var intPref: BatchPref<Int>? = null
        val batch = prefBatch {
            intPref = int("update_transform", 10)
        }

        preferenceDatastore.batchUpdate(batch) {
            update(requireNotNull(intPref)) { it * 3 }
        }

        assertEquals(30, preferenceDatastore.int("update_transform", 10).get())
    }

    @Test
    fun batchUpdate_readsWritesMadeEarlierInSameTransaction() = runTest(testDispatcher) {
        var intPref: BatchPref<Int>? = null
        val batch = prefBatch {
            intPref = int("update_intra_tx", 1)
        }
        val handle = requireNotNull(intPref)

        preferenceDatastore.batchUpdate(batch) {
            update(handle) { it + 1 }
            update(handle) { it + 1 }
            set(handle, get(handle) + 1)
        }

        assertEquals(4, preferenceDatastore.int("update_intra_tx", 1).get())
    }

    @Test
    fun batchUpdate_indexOperatorSyntax() = runTest(testDispatcher) {
        var intPref: BatchPref<Int>? = null
        val batch = prefBatch {
            intPref = int("update_idx", 5)
        }
        val handle = requireNotNull(intPref)

        preferenceDatastore.batchUpdate(batch) {
            val current = this[handle]
            this[handle] = current * 4
        }

        assertEquals(20, preferenceDatastore.int("update_idx", 5).get())
    }

    @Test
    fun batchUpdate_deleteAndResetToDefaultInUpdateScope() = runTest(testDispatcher) {
        var stringPref: BatchPref<String>? = null
        var intPref: BatchPref<Int>? = null
        val batch = prefBatch {
            stringPref = string("update_delete_string", "default_val")
            intPref = int("update_reset_int", 42)
        }

        preferenceDatastore.string("update_delete_string", "default_val").set("set_value")
        preferenceDatastore.int("update_reset_int", 42).set(99)

        preferenceDatastore.batchUpdate(batch) {
            delete(requireNotNull(stringPref))
            resetToDefault(requireNotNull(intPref))
        }

        assertEquals(
            "default_val",
            preferenceDatastore.string("update_delete_string", "default_val").get(),
        )
        assertEquals(42, preferenceDatastore.int("update_reset_int", 42).get())
    }

    @Test
    fun batchUpdate_nullableTransformToNullRemovesKey() = runTest(testDispatcher) {
        var nullableString: BatchPref<String?>? = null
        val batch = prefBatch {
            nullableString = nullableString("update_nullable")
        }
        val handle = requireNotNull(nullableString)
        val single = preferenceDatastore.nullableString("update_nullable")
        single.set("present")

        preferenceDatastore.batchUpdate(batch) {
            update(handle) { current -> current?.uppercase() }
        }
        assertEquals("PRESENT", single.get())

        preferenceDatastore.batchUpdate(batch) {
            update(handle) { null }
        }
        assertNull(single.get())
    }

    // -- storage interop between batch declarations and single preferences --

    @Test
    fun batchWrite_isReadableByEquivalentSinglePreferences() = runTest(testDispatcher) {
        var intPref: BatchPref<Int>? = null
        var stringSetPref: BatchPref<Set<String>>? = null
        var payloadPref: BatchPref<BatchTestPayload>? = null
        var enumSetPref: BatchPref<Set<BatchTestEnum>>? = null
        var kserializedListPref: BatchPref<List<BatchTestPayload>>? = null

        val batch = prefBatch {
            intPref = int("interop_int", 0)
            stringSetPref = stringSet("interop_stringSet")
            payloadPref = kserialized("interop_payload", BatchTestPayload(0, "none"))
            enumSetPref = enumSet("interop_enumSet")
            kserializedListPref = kserializedList("interop_kserializedList")
        }

        preferenceDatastore.batchWrite(batch) {
            set(requireNotNull(intPref), 8)
            set(requireNotNull(stringSetPref), setOf("x", "y"))
            set(requireNotNull(payloadPref), BatchTestPayload(2, "two"))
            set(requireNotNull(enumSetPref), setOf(BatchTestEnum.FIRST, BatchTestEnum.SECOND))
            set(requireNotNull(kserializedListPref), listOf(BatchTestPayload(5, "five")))
        }

        assertEquals(8, preferenceDatastore.int("interop_int", 0).get())
        assertEquals(setOf("x", "y"), preferenceDatastore.stringSet("interop_stringSet").get())
        assertEquals(
            BatchTestPayload(2, "two"),
            preferenceDatastore
                .kserialized<BatchTestPayload>("interop_payload", BatchTestPayload(0, "none"))
                .get(),
        )
        assertEquals(
            setOf(BatchTestEnum.FIRST, BatchTestEnum.SECOND),
            preferenceDatastore.enumSet<BatchTestEnum>("interop_enumSet").get(),
        )
        assertEquals(
            listOf(BatchTestPayload(5, "five")),
            preferenceDatastore.kserializedList<BatchTestPayload>("interop_kserializedList").get(),
        )
    }

    @Test
    fun batchRead_seesValuesWrittenBySinglePreferences() = runTest(testDispatcher) {
        preferenceDatastore.int("interop_single_int", 0).set(31)
        preferenceDatastore.stringList("interop_single_stringList").set(listOf("m", "n"))
        preferenceDatastore.nullableStringList("interop_single_nullableStringList").set(listOf("o"))
        preferenceDatastore
            .enum<BatchTestEnum>("interop_single_enum", BatchTestEnum.FIRST)
            .set(BatchTestEnum.SECOND)

        var intPref: BatchPref<Int>? = null
        var stringListPref: BatchPref<List<String>>? = null
        var nullableStringListPref: BatchPref<List<String>?>? = null
        var enumPref: BatchPref<BatchTestEnum>? = null

        val batch = prefBatch {
            intPref = int("interop_single_int", 0)
            stringListPref = stringList("interop_single_stringList")
            nullableStringListPref = nullableStringList("interop_single_nullableStringList")
            enumPref = enum("interop_single_enum", BatchTestEnum.FIRST)
        }

        val values = preferenceDatastore.batchRead(batch)

        assertEquals(31, values[requireNotNull(intPref)])
        assertEquals(listOf("m", "n"), values[requireNotNull(stringListPref)])
        assertEquals(listOf("o"), values[requireNotNull(nullableStringListPref)])
        assertEquals(BatchTestEnum.SECOND, values[requireNotNull(enumPref)])
    }

    @Test
    fun stringList_storesRawJsonArrayOfStrings() = runTest(testDispatcher) {
        preferenceDatastore.stringList("raw_stringList").set(listOf("a", "b"))

        val raw = dataStore.data.first().asMap()
            .entries
            .single { it.key.name == "raw_stringList" }
            .value

        assertEquals("""["a","b"]""", raw)
    }
}
