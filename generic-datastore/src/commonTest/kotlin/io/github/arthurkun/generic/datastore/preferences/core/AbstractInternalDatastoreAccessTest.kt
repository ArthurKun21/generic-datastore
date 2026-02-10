package io.github.arthurkun.generic.datastore.preferences.core

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import io.github.arthurkun.generic.datastore.preferences.GenericPreferencesDatastore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

abstract class AbstractInternalDatastoreAccessTest {

    abstract val preferenceDatastore: GenericPreferencesDatastore
    abstract val dataStore: DataStore<Preferences>
    abstract val testDispatcher: TestDispatcher

    @Test
    fun datastoreProperty_returnsSameInstance() = runTest(testDispatcher) {
        assertSame(dataStore, preferenceDatastore.datastore)
    }

    @Test
    fun writeViaDataStore_readViaPreferenceApi() = runTest(testDispatcher) {
        val key = "testWriteViaDataStore"
        val expected = "hello from datastore"

        val preferencesKey = stringPreferencesKey(key)
        dataStore.edit { settings ->
            settings[preferencesKey] = expected
        }

        val pref = preferenceDatastore.string(key)
        assertEquals(expected, pref.get())
    }

    @Test
    fun writeViaPreferenceApi_readViaDataStore() = runTest(testDispatcher) {
        val key = "testWriteViaPreferenceApi"
        val expected = "hello from preference api"

        val pref = preferenceDatastore.string(key)
        pref.set(expected)

        val preferencesKey = stringPreferencesKey(key)
        val actual = dataStore.data.first()[preferencesKey]
        assertEquals(expected, actual)
    }
}
