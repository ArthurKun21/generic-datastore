package io.github.arthurkun.generic.datastore.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import io.github.arthurkun.generic.datastore.core.InternalGenericDatastoreApi
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.Serializable
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@Serializable
internal data class DecodeFailurePayload(val id: Int)

@OptIn(InternalGenericDatastoreApi::class)
abstract class AbstractDecodeFailureCallbackTest {

    abstract val dataStore: DataStore<Preferences>
    abstract val testDispatcher: TestDispatcher

    @Test
    fun onDecodeFailure_reportsKeyAndError() = runTest(testDispatcher) {
        val failures = mutableListOf<Pair<String, Throwable>>()
        val datastore = GenericPreferencesDatastore(
            datastore = dataStore,
            onDecodeFailure = { key, error -> failures.add(key to error) },
        )
        val pref = datastore.kserialized(
            "decode_failure_key",
            DecodeFailurePayload(0),
            DecodeFailurePayload.serializer(),
        )

        pref.set(DecodeFailurePayload(7))
        assertEquals(DecodeFailurePayload(7), pref.get())
        assertTrue(failures.isEmpty())

        dataStore.edit { prefs ->
            prefs[stringPreferencesKey("decode_failure_key")] = "not-json"
        }

        assertEquals(DecodeFailurePayload(0), pref.get())
        assertEquals(1, failures.size)
        assertEquals("decode_failure_key", failures.single().first)
        assertTrue(failures.single().second is Exception)
    }

    @Test
    fun onDecodeFailure_reportsSkippedSetElements() = runTest(testDispatcher) {
        val failures = mutableListOf<Pair<String, Throwable>>()
        val datastore = GenericPreferencesDatastore(
            datastore = dataStore,
            onDecodeFailure = { key, error -> failures.add(key to error) },
        )
        val pref = datastore.kserializedSet(
            "decode_failure_set",
            emptySet<DecodeFailurePayload>(),
            DecodeFailurePayload.serializer(),
        )

        pref.set(setOf(DecodeFailurePayload(1)))
        dataStore.edit { prefs ->
            prefs[stringSetPreferencesKey("decode_failure_set")] = setOf("""{"id":2}""", "not-json")
        }

        assertEquals(setOf(DecodeFailurePayload(2)), pref.get())
        assertEquals(1, failures.size)
        assertEquals("decode_failure_set", failures.single().first)
    }

    @Test
    fun onDecodeFailure_nullCallbackStaysSilent() = runTest(testDispatcher) {
        val datastore = GenericPreferencesDatastore(datastore = dataStore)
        val pref = datastore.kserialized(
            "silent_failure_key",
            DecodeFailurePayload(0),
            DecodeFailurePayload.serializer(),
        )
        pref.set(DecodeFailurePayload(7))
        dataStore.edit { prefs ->
            prefs[stringPreferencesKey("silent_failure_key")] = "not-json"
        }

        assertEquals(DecodeFailurePayload(0), pref.get())
    }
}
