package io.github.arthurkun.generic.datastore.preferences.core.custom

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import io.github.arthurkun.generic.datastore.preferences.GenericPreferencesDatastore
import io.github.arthurkun.generic.datastore.preferences.enum
import io.github.arthurkun.generic.datastore.preferences.enumSet
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

enum class EnumTestTheme { LIGHT, DARK, SYSTEM }

abstract class AbstractEnumTest {

    abstract val preferenceDatastore: GenericPreferencesDatastore
    abstract val dataStore: DataStore<Preferences>
    abstract val testDispatcher: TestDispatcher

    // --- enum ---

    @Test
    fun enumPreference_defaultValueWhenNotSet() = runTest(testDispatcher) {
        val pref = preferenceDatastore.enum("enumDefault", EnumTestTheme.SYSTEM)
        assertEquals(EnumTestTheme.SYSTEM, pref.get())
    }

    @Test
    fun enumPreference_setAndGetValue() = runTest(testDispatcher) {
        val pref = preferenceDatastore.enum("enumSetGet", EnumTestTheme.SYSTEM)
        pref.set(EnumTestTheme.DARK)
        assertEquals(EnumTestTheme.DARK, pref.get())
    }

    @Test
    fun enumPreference_writeStoresEnumName() = runTest(testDispatcher) {
        val key = "enumRawFormat"
        val pref = preferenceDatastore.enum(key, EnumTestTheme.LIGHT)
        pref.set(EnumTestTheme.DARK)
        assertEquals("DARK", dataStore.data.first()[stringPreferencesKey(key)])
    }

    @Test
    fun enumPreference_unknownStoredNameFallsBackToDefault() = runTest(testDispatcher) {
        val key = "enumUnknown"
        val pref = preferenceDatastore.enum(key, EnumTestTheme.SYSTEM)
        dataStore.edit { settings ->
            settings[stringPreferencesKey(key)] = "REMOVED_CONSTANT"
        }
        assertEquals(EnumTestTheme.SYSTEM, pref.get())
    }

    @Test
    fun enumPreference_deleteResetsToDefault() = runTest(testDispatcher) {
        val pref = preferenceDatastore.enum("enumDelete", EnumTestTheme.SYSTEM)
        pref.set(EnumTestTheme.LIGHT)
        pref.delete()
        assertEquals(EnumTestTheme.SYSTEM, pref.get())
    }

    @Test
    fun enumPreference_resetToDefault() = runTest(testDispatcher) {
        val pref = preferenceDatastore.enum("enumReset", EnumTestTheme.SYSTEM)
        pref.set(EnumTestTheme.LIGHT)
        pref.resetToDefault()
        assertEquals(EnumTestTheme.SYSTEM, pref.get())
    }

    @Test
    fun enumPreference_update() = runTest(testDispatcher) {
        val pref = preferenceDatastore.enum("enumUpdate", EnumTestTheme.SYSTEM)
        pref.set(EnumTestTheme.LIGHT)
        pref.update { current -> if (current == EnumTestTheme.LIGHT) EnumTestTheme.DARK else current }
        assertEquals(EnumTestTheme.DARK, pref.get())
    }

    @Test
    fun enumPreference_observeValue() = runTest(testDispatcher) {
        val pref = preferenceDatastore.enum("enumFlow", EnumTestTheme.SYSTEM)
        pref.set(EnumTestTheme.DARK)
        assertEquals(EnumTestTheme.DARK, pref.asFlow().first())
    }

    @Test
    fun enumPreference_explicitEnumValuesParameterWorks() = runTest(testDispatcher) {
        val pref = preferenceDatastore.enum(
            key = "enumExplicitValues",
            defaultValue = EnumTestTheme.SYSTEM,
            enumValues = EnumTestTheme.entries.toTypedArray(),
        )
        pref.set(EnumTestTheme.LIGHT)
        assertEquals(EnumTestTheme.LIGHT, pref.get())
    }

    // --- enumSet ---

    @Test
    fun enumSetPreference_defaultValueWhenNotSet() = runTest(testDispatcher) {
        val pref = preferenceDatastore.enumSet<EnumTestTheme>("enumSetDefault")
        assertEquals(emptySet(), pref.get())
    }

    @Test
    fun enumSetPreference_defaultValueCustom() = runTest(testDispatcher) {
        val default = setOf(EnumTestTheme.DARK)
        val pref = preferenceDatastore.enumSet("enumSetDefaultCustom", default)
        assertEquals(default, pref.get())
    }

    @Test
    fun enumSetPreference_setAndGetValue() = runTest(testDispatcher) {
        val pref = preferenceDatastore.enumSet<EnumTestTheme>("enumSetSetGet")
        pref.set(setOf(EnumTestTheme.LIGHT, EnumTestTheme.DARK))
        assertEquals(setOf(EnumTestTheme.LIGHT, EnumTestTheme.DARK), pref.get())
    }

    @Test
    fun enumSetPreference_writeStoresEnumNames() = runTest(testDispatcher) {
        val key = "enumSetRawFormat"
        val pref = preferenceDatastore.enumSet<EnumTestTheme>(key)
        pref.set(setOf(EnumTestTheme.SYSTEM))
        assertEquals(setOf("SYSTEM"), dataStore.data.first()[stringSetPreferencesKey(key)])
    }

    @Test
    fun enumSetPreference_skipsUnknownStoredNames() = runTest(testDispatcher) {
        val key = "enumSetUnknown"
        val pref = preferenceDatastore.enumSet<EnumTestTheme>(key)
        dataStore.edit { settings ->
            settings[stringSetPreferencesKey(key)] = setOf("LIGHT", "REMOVED_CONSTANT")
        }
        assertEquals(setOf(EnumTestTheme.LIGHT), pref.get())
    }

    @Test
    fun enumSetPreference_deleteResetsToDefault() = runTest(testDispatcher) {
        val default = setOf(EnumTestTheme.SYSTEM)
        val pref = preferenceDatastore.enumSet("enumSetDelete", default)
        pref.set(setOf(EnumTestTheme.LIGHT))
        pref.delete()
        assertEquals(default, pref.get())
    }

    @Test
    fun enumSetPreference_resetToDefault() = runTest(testDispatcher) {
        val default = setOf(EnumTestTheme.DARK)
        val pref = preferenceDatastore.enumSet("enumSetReset", default)
        pref.set(setOf(EnumTestTheme.LIGHT))
        pref.resetToDefault()
        assertEquals(default, pref.get())
    }

    @Test
    fun enumSetPreference_observeValue() = runTest(testDispatcher) {
        val pref = preferenceDatastore.enumSet<EnumTestTheme>("enumSetFlow")
        pref.set(setOf(EnumTestTheme.DARK))
        assertEquals(setOf(EnumTestTheme.DARK), pref.asFlow().first())
    }

    @Test
    fun enumSetPreference_explicitEnumValuesParameterWorks() = runTest(testDispatcher) {
        val pref = preferenceDatastore.enumSet(
            key = "enumSetExplicitValues",
            defaultValue = emptySet(),
            enumValues = EnumTestTheme.entries.toTypedArray(),
        )
        pref.set(setOf(EnumTestTheme.DARK))
        assertEquals(setOf(EnumTestTheme.DARK), pref.get())
    }
}
