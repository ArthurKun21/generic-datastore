package io.github.arthurkun.generic.datastore.preferences.optional

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import io.github.arthurkun.generic.datastore.preferences.GenericPreferencesDatastore
import io.github.arthurkun.generic.datastore.preferences.core.KSerUser
import io.github.arthurkun.generic.datastore.preferences.optional.custom.nullableEnum
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

enum class NullableTestColor { RED, GREEN, BLUE }

abstract class AbstractNullableCustomSerializedTest {

    abstract val preferenceDatastore: GenericPreferencesDatastore
    abstract val dataStore: DataStore<Preferences>
    abstract val testDispatcher: TestDispatcher

    // --- nullableSerialized tests ---

    @Test
    fun nullableSerialized_defaultIsNull() = runTest(testDispatcher) {
        val pref = preferenceDatastore.nullableSerialized<KSerUser>(
            key = "nullableSerDefault",
            serializer = { "${it.name}:${it.age}" },
            deserializer = { s ->
                val parts = s.split(":")
                KSerUser(name = parts[0], age = parts[1].toInt())
            },
        )
        assertNull(pref.get())
    }

    @Test
    fun nullableSerialized_setAndGet() = runTest(testDispatcher) {
        val pref = preferenceDatastore.nullableSerialized<KSerUser>(
            key = "nullableSerSetGet",
            serializer = { "${it.name}:${it.age}" },
            deserializer = { s ->
                val parts = s.split(":")
                KSerUser(name = parts[0], age = parts[1].toInt())
            },
        )
        val user = KSerUser(name = "Alice", age = 30)
        pref.set(user)
        assertEquals(user, pref.get())
    }

    @Test
    fun nullableSerialized_setNullClearsValue() = runTest(testDispatcher) {
        val pref = preferenceDatastore.nullableSerialized<KSerUser>(
            key = "nullableSerSetNull",
            serializer = { "${it.name}:${it.age}" },
            deserializer = { s ->
                val parts = s.split(":")
                KSerUser(name = parts[0], age = parts[1].toInt())
            },
        )
        pref.set(KSerUser(name = "Bob", age = 25))
        assertEquals(KSerUser(name = "Bob", age = 25), pref.get())
        pref.set(null)
        assertNull(pref.get())
    }

    @Test
    fun nullableSerialized_deleteResetsToNull() = runTest(testDispatcher) {
        val pref = preferenceDatastore.nullableSerialized<KSerUser>(
            key = "nullableSerDelete",
            serializer = { "${it.name}:${it.age}" },
            deserializer = { s ->
                val parts = s.split(":")
                KSerUser(name = parts[0], age = parts[1].toInt())
            },
        )
        pref.set(KSerUser(name = "Charlie", age = 35))
        pref.delete()
        assertNull(pref.get())
    }

    @Test
    fun nullableSerialized_observeDefaultValue() = runTest(testDispatcher) {
        val pref = preferenceDatastore.nullableSerialized<KSerUser>(
            key = "nullableSerFlowDefault",
            serializer = { "${it.name}:${it.age}" },
            deserializer = { s ->
                val parts = s.split(":")
                KSerUser(name = parts[0], age = parts[1].toInt())
            },
        )
        val value = pref.asFlow().first()
        assertNull(value)
    }

    @Test
    fun nullableSerialized_observeSetValue() = runTest(testDispatcher) {
        val pref = preferenceDatastore.nullableSerialized<KSerUser>(
            key = "nullableSerFlowSet",
            serializer = { "${it.name}:${it.age}" },
            deserializer = { s ->
                val parts = s.split(":")
                KSerUser(name = parts[0], age = parts[1].toInt())
            },
        )
        val user = KSerUser(name = "Diana", age = 28)
        pref.set(user)
        val value = pref.asFlow().first()
        assertEquals(user, value)
    }

    @Test
    fun nullableSerialized_updateFromNull() = runTest(testDispatcher) {
        val pref = preferenceDatastore.nullableSerialized<KSerUser>(
            key = "nullableSerUpdateNull",
            serializer = { "${it.name}:${it.age}" },
            deserializer = { s ->
                val parts = s.split(":")
                KSerUser(name = parts[0], age = parts[1].toInt())
            },
        )
        pref.update { current -> current ?: KSerUser(name = "Default", age = 0) }
        assertEquals(KSerUser(name = "Default", age = 0), pref.get())
    }

    @Test
    fun nullableSerialized_updateExistingValue() = runTest(testDispatcher) {
        val pref = preferenceDatastore.nullableSerialized<KSerUser>(
            key = "nullableSerUpdateExisting",
            serializer = { "${it.name}:${it.age}" },
            deserializer = { s ->
                val parts = s.split(":")
                KSerUser(name = parts[0], age = parts[1].toInt())
            },
        )
        pref.set(KSerUser(name = "Eve", age = 20))
        pref.update { current -> current?.copy(age = 21) }
        assertEquals(KSerUser(name = "Eve", age = 21), pref.get())
    }

    @Test
    fun nullableSerialized_resetToDefaultResetsToNull() = runTest(testDispatcher) {
        val pref = preferenceDatastore.nullableSerialized<KSerUser>(
            key = "nullableSerResetDefault",
            serializer = { "${it.name}:${it.age}" },
            deserializer = { s ->
                val parts = s.split(":")
                KSerUser(name = parts[0], age = parts[1].toInt())
            },
        )
        pref.set(KSerUser(name = "Frank", age = 40))
        pref.resetToDefault()
        assertNull(pref.get())
    }

    @Test
    fun nullableSerialized_corruptedDataReturnsNull() = runTest(testDispatcher) {
        val pref = preferenceDatastore.nullableSerialized<KSerUser>(
            key = "nullableSerCorrupt",
            serializer = { "${it.name}:${it.age}" },
            deserializer = { s ->
                val parts = s.split(":")
                KSerUser(name = parts[0], age = parts[1].toInt())
            },
        )
        val stringKey = stringPreferencesKey("nullableSerCorrupt")
        dataStore.edit { settings ->
            settings[stringKey] = "INVALID_DATA"
        }
        assertNull(pref.get())
    }

    // --- nullableEnum tests ---

    @Test
    fun nullableEnum_defaultIsNull() = runTest(testDispatcher) {
        val pref = preferenceDatastore.nullableEnum<NullableTestColor>("nullableEnumDefault")
        assertNull(pref.get())
    }

    @Test
    fun nullableEnum_setAndGet() = runTest(testDispatcher) {
        val pref = preferenceDatastore.nullableEnum<NullableTestColor>("nullableEnumSetGet")
        pref.set(NullableTestColor.GREEN)
        assertEquals(NullableTestColor.GREEN, pref.get())
    }

    @Test
    fun nullableEnum_setNullClearsValue() = runTest(testDispatcher) {
        val pref = preferenceDatastore.nullableEnum<NullableTestColor>("nullableEnumSetNull")
        pref.set(NullableTestColor.BLUE)
        pref.set(null)
        assertNull(pref.get())
    }

    @Test
    fun nullableEnum_deleteResetsToNull() = runTest(testDispatcher) {
        val pref = preferenceDatastore.nullableEnum<NullableTestColor>("nullableEnumDelete")
        pref.set(NullableTestColor.RED)
        pref.delete()
        assertNull(pref.get())
    }

    @Test
    fun nullableEnum_corruptedDataReturnsNull() = runTest(testDispatcher) {
        val pref = preferenceDatastore.nullableEnum<NullableTestColor>("nullableEnumCorrupt")
        val stringKey = stringPreferencesKey("nullableEnumCorrupt")
        dataStore.edit { settings ->
            settings[stringKey] = "INVALID_ENUM"
        }
        assertNull(pref.get())
    }

    @Test
    fun nullableEnum_resetToDefaultResetsToNull() = runTest(testDispatcher) {
        val pref = preferenceDatastore.nullableEnum<NullableTestColor>("nullableEnumResetDefault")
        pref.set(NullableTestColor.GREEN)
        pref.resetToDefault()
        assertNull(pref.get())
    }

    @Test
    fun nullableEnum_observeDefaultValue() = runTest(testDispatcher) {
        val pref = preferenceDatastore.nullableEnum<NullableTestColor>("nullableEnumFlowDefault")
        val value = pref.asFlow().first()
        assertNull(value)
    }

    @Test
    fun nullableEnum_updateFromNull() = runTest(testDispatcher) {
        val pref = preferenceDatastore.nullableEnum<NullableTestColor>("nullableEnumUpdateNull")
        pref.update { current -> current ?: NullableTestColor.RED }
        assertEquals(NullableTestColor.RED, pref.get())
    }
}
