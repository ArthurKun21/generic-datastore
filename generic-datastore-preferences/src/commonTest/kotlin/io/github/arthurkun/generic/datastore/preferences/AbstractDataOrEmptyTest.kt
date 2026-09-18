package io.github.arthurkun.generic.datastore.preferences

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.DataStore
import androidx.datastore.core.IOException
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import io.github.arthurkun.generic.datastore.preferences.utils.dataOrEmpty
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertSame
import kotlin.test.assertTrue

abstract class AbstractDataOrEmptyTest {

    abstract val preferenceDatastore: GenericPreferencesDatastore
    abstract val dataStore: DataStore<Preferences>
    abstract val testDispatcher: TestDispatcher

    @Test
    fun dataOrEmpty_returnsDataWhenNoError() = runTest(testDispatcher) {
        val key = stringPreferencesKey("dataOrEmptyNormal")
        dataStore.edit { it[key] = "hello" }

        val prefs = dataStore.dataOrEmpty.first()
        assertEquals("hello", prefs[key])
    }

    @Test
    fun dataOrEmpty_emitsEmptyPreferencesOnIOException() = runTest(testDispatcher) {
        val errorDataStore = object : DataStore<Preferences> {
            override val data = flow<Preferences> {
                throw IOException("disk error")
            }

            override suspend fun updateData(
                transform: suspend (t: Preferences) -> Preferences,
            ): Preferences {
                throw UnsupportedOperationException()
            }
        }

        val prefs = errorDataStore.dataOrEmpty.first()
        assertEquals(emptyPreferences(), prefs)
    }

    @Test
    fun dataOrEmpty_rethrowsNonIOException() = runTest(testDispatcher) {
        val errorDataStore = object : DataStore<Preferences> {
            override val data = flow<Preferences> {
                throw IllegalStateException("unexpected error")
            }

            override suspend fun updateData(
                transform: suspend (t: Preferences) -> Preferences,
            ): Preferences {
                throw UnsupportedOperationException()
            }
        }

        assertFailsWith<IllegalStateException> {
            errorDataStore.dataOrEmpty.first()
        }
    }

    @Test
    fun dataOrEmpty_rethrowsCorruptionExceptionUnchanged() = runTest(testDispatcher) {
        val corruption = CorruptionException("corrupted preferences file")
        val errorDataStore = object : DataStore<Preferences> {
            override val data = flow<Preferences> {
                throw corruption
            }

            override suspend fun updateData(
                transform: suspend (t: Preferences) -> Preferences,
            ): Preferences {
                throw UnsupportedOperationException()
            }
        }

        val thrown = assertFailsWith<CorruptionException> {
            errorDataStore.dataOrEmpty.first()
        }
        assertSame(corruption, thrown)
    }

    @Test
    fun dataOrEmpty_doesNotEmitFallbackOnCorruption() = runTest(testDispatcher) {
        val errorDataStore = object : DataStore<Preferences> {
            override val data = flow<Preferences> {
                throw CorruptionException("corrupted preferences file")
            }

            override suspend fun updateData(
                transform: suspend (t: Preferences) -> Preferences,
            ): Preferences {
                throw UnsupportedOperationException()
            }
        }

        // Collecting fully must fail instead of completing with emptyPreferences().
        assertFailsWith<CorruptionException> {
            errorDataStore.dataOrEmpty.toList()
        }
    }

    @Test
    fun dataOrEmpty_emitsEmptyOnIOExceptionThenStops() = runTest(testDispatcher) {
        val errorDataStore = object : DataStore<Preferences> {
            override val data = flow<Preferences> {
                throw IOException("disk error")
            }

            override suspend fun updateData(
                transform: suspend (t: Preferences) -> Preferences,
            ): Preferences {
                throw UnsupportedOperationException()
            }
        }

        val results = errorDataStore.dataOrEmpty.toList()
        assertEquals(1, results.size)
        assertTrue(results.first().asMap().isEmpty())
    }
}
