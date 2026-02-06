package io.github.arthurkun.generic.datastore.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import io.github.arthurkun.generic.datastore.core.Preference
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

/**
 * Represents a generic preference that can be stored in and retrieved from a DataStore.
 *
 * This sealed class provides a type-safe way to handle different preference types (String, Int, Long, etc.)
 * while abstracting the underlying DataStore operations. It defines common operations for a preference,
 * such as reading, writing, deleting, and observing its value as a Kotlin Flow or StateFlow.
 *
 * Each specific preference type (e.g., [StringPrimitive], [IntPrimitive]) is implemented as a nested class
 * inheriting from [GenericPreference].
 *
 * @param T The data type of the preference value (e.g., String, Int, Boolean).
 * @property datastore The [DataStore<Preferences>] instance used for storing and retrieving preferences.
 * @property key The unique String key used to identify this preference within the DataStore.
 * @property defaultValue The default value to be returned if the preference is not set or an error occurs.
 * @property preferences The [Preferences.Key] specific to the type `T`, used to access the preference in DataStore.
 */
sealed class GenericPreference<T>(
    internal val datastore: DataStore<Preferences>,
    private val key: String,
    override val defaultValue: T,
    private val preferences: Preferences.Key<T>,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : Preference<T> {
    /**
     * Returns the unique String key used to identify this preference within the DataStore.
     */
    override fun key(): String = key

    /**
     * Retrieves the current value of the preference from DataStore.
     * If the key is not found in DataStore or an error occurs during retrieval,
     * this function returns the [defaultValue]. This is a suspending function.
     */
    override suspend fun get(): T {
        return withContext(ioDispatcher) {
            datastore
                .data
                .map { preferences ->
                    preferences[this@GenericPreference.preferences] ?: defaultValue
                }
                .first()
        }
    }

    /**
     * Sets the value of the preference in the DataStore.
     * This is a suspending function.
     * @param value The new value to store for this preference.
     */
    override suspend fun set(value: T) {
        withContext(ioDispatcher) {
            datastore.edit { ds ->
                ds[preferences] = value
            }
        }
    }

    /**
     * Removes the preference from the DataStore.
     * This is a suspending function.
     */
    override suspend fun delete() {
        withContext(ioDispatcher) {
            datastore.edit { ds ->
                ds.remove(preferences)
            }
        }
    }

    override suspend fun resetToDefault() = set(defaultValue)

    /**
     * Returns a [Flow] that emits the preference's current value and subsequent updates from DataStore.
     * If the preference is not set in the DataStore or an error occurs during retrieval,
     * the flow will emit the [defaultValue].
     */
    override fun asFlow(): Flow<T> {
        return datastore
            .data
            .map { preferences ->
                preferences[this.preferences] ?: defaultValue
            }
    }

    /**
     * Converts the preference [Flow] into a [StateFlow] within the given [scope].
     * The [StateFlow] is typically started when there are subscribers and shares the most recent value.
     * It will be initialized with the current preference value (or [defaultValue] if not set or on error).
     * @param scope The [CoroutineScope] in which to launch the [StateFlow].
     * @return A [StateFlow] representing the preference's value.
     */
    override fun stateIn(scope: CoroutineScope): StateFlow<T> =
        asFlow().stateIn(scope, SharingStarted.Eagerly, defaultValue)

    /**
     * Synchronously retrieves the current value of the preference.
     * This operation may block the calling thread while accessing DataStore.
     * If the key is not found or an error occurs, this function returns the [defaultValue].
     * Use with caution due to potential blocking.
     */
    override fun getBlocking(): T = runBlocking {
        get()
    }

    /**
     * Synchronously sets the value of the preference.
     * This operation may block the calling thread while accessing DataStore.
     * Use with caution due to potential blocking.
     * @param value The new value to store for this preference.
     */
    override fun setBlocking(value: T) {
        runBlocking {
            set(value)
        }
    }

    /**
     * A [GenericPreference] for storing [String] values.
     * @param datastore The [DataStore<Preferences>] instance used for storing and retrieving preferences.
     * @param key The unique String key used to identify this preference within the DataStore.
     * @param defaultValue The default value to use if the preference is not set or on retrieval error.
     */
    class StringPrimitive(
        datastore: DataStore<Preferences>,
        key: String,
        defaultValue: String,
    ) : GenericPreference<String>(
        datastore = datastore,
        key = key,
        defaultValue = defaultValue,
        preferences = stringPreferencesKey(key),
    )

    /**
     * A [GenericPreference] for storing [Long] values.
     * @param datastore The [DataStore<Preferences>] instance used for storing and retrieving preferences.
     * @param key The unique String key used to identify this preference within the DataStore.
     * @param defaultValue The default value to use if the preference is not set or on retrieval error.
     */
    class LongPrimitive(
        datastore: DataStore<Preferences>,
        key: String,
        defaultValue: Long,
    ) : GenericPreference<Long>(
        datastore = datastore,
        key = key,
        defaultValue = defaultValue,
        preferences = longPreferencesKey(key),
    )

    /**
     * A [GenericPreference] for storing [Int] values.
     * @param datastore The [DataStore<Preferences>] instance used for storing and retrieving preferences.
     * @param key The unique String key used to identify this preference within the DataStore.
     * @param defaultValue The default value to use if the preference is not set or on retrieval error.
     */
    class IntPrimitive(
        datastore: DataStore<Preferences>,
        key: String,
        defaultValue: Int,
    ) : GenericPreference<Int>(
        datastore = datastore,
        key = key,
        defaultValue = defaultValue,
        preferences = intPreferencesKey(key),
    )

    /**
     * A [GenericPreference] for storing [Float] values.
     * @param datastore The [DataStore<Preferences>] instance used for storing and retrieving preferences.
     * @param key The unique String key used to identify this preference within the DataStore.
     * @param defaultValue The default value to use if the preference is not set or on retrieval error.
     */
    class FloatPrimitive(
        datastore: DataStore<Preferences>,
        key: String,
        defaultValue: Float,
    ) : GenericPreference<Float>(
        datastore = datastore,
        key = key,
        defaultValue = defaultValue,
        preferences = floatPreferencesKey(key),
    )

    /**
     * A [GenericPreference] for storing [Boolean] values.
     * @param datastore The [DataStore<Preferences>] instance used for storing and retrieving preferences.
     * @param key The unique String key used to identify this preference within the DataStore.
     * @param defaultValue The default value to use if the preference is not set or on retrieval error.
     */
    class BooleanPrimitive(
        datastore: DataStore<Preferences>,
        key: String,
        defaultValue: Boolean,
    ) : GenericPreference<Boolean>(
        datastore = datastore,
        key = key,
        defaultValue = defaultValue,
        preferences = booleanPreferencesKey(key),
    )

    /**
     * A [GenericPreference] for storing [Set] of [String] values.
     * @param datastore The [DataStore<Preferences>] instance used for storing and retrieving preferences.
     * @param key The unique String key used to identify this preference within the DataStore.
     * @param defaultValue The default value to use if the preference is not set or on retrieval error.
     */
    class StringSetPrimitive(
        datastore: DataStore<Preferences>,
        key: String,
        defaultValue: Set<String>,
    ) : GenericPreference<Set<String>>(
        datastore = datastore,
        key = key,
        defaultValue = defaultValue,
        preferences = stringSetPreferencesKey(key),
    )
}
