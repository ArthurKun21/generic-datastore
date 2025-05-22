package io.github.arthurkun.generic.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

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
 * @property scope The [CoroutineScope] used for launching asynchronous operations, such as `setValue`.
 */
sealed class GenericPreference<T>(
    internal val datastore: DataStore<Preferences>,
    private val key: String,
    override val defaultValue: T,
    private val preferences: Preferences.Key<T>,
    private val scope: CoroutineScope,
) : Preference<T> {
    /**
     * Returns the key of the preference.
     * @return The preference key as a String.
     */
    override fun key(): String = key

    /**
     * Asynchronously retrieves the current value of the preference from the DataStore.
     * If the preference key is not found in the DataStore, this function returns the `defaultValue`.
     * This is a suspending function and should be called from a coroutine or another suspending function.
     *
     * @return The current value of the preference, or `defaultValue` if not set.
     */
    override suspend fun get(): T {
        return datastore
            .data
            .map { preferences ->
                preferences[this.preferences] ?: defaultValue
            }
            .first()
    }

    /**
     * Asynchronously sets the value of the preference in the DataStore.
     * This is a suspending function and should be called from a coroutine or another suspending function.
     *
     * @param value The new value to be stored for the preference.
     */
    override suspend fun set(value: T) {
        datastore.edit { ds ->
            ds[preferences] = value
        }
    }

    /**
     * Asynchronously deletes the preference from the DataStore.
     * If the key does not exist, this operation has no effect.
     * This is a suspending function and should be called from a coroutine or another suspending function.
     */
    override suspend fun delete() {
        datastore.edit { ds ->
            ds.remove(preferences)
        }
    }

    /**
     * Returns a [Flow] that emits the preference value whenever it changes in the DataStore.
     * If the preference key is not found, the Flow emits the `defaultValue`.
     * This Flow can be collected to reactively observe changes to the preference.
     *
     * @return A [Flow] emitting the current or updated preference value.
     */
    override fun asFlow(): Flow<T> {
        return datastore
            .data
            .map { preferences ->
                preferences[this.preferences] ?: defaultValue
            }
    }

    /**
     * Converts the preference [Flow] into a [StateFlow].
     * The [StateFlow] is started lazily ([SharingStarted.Lazily]) and shares the latest emitted
     * value with all collectors. It is initialized with the `defaultValue`.
     *
     * @param scope The [CoroutineScope] in which the sharing of the [StateFlow] is started.
     * @return A [StateFlow] representing the current value of the preference.
     */
    override fun stateIn(scope: CoroutineScope): StateFlow<T> =
        asFlow().stateIn(scope, SharingStarted.Lazily, defaultValue)

    /**
     * Synchronously gets the current value of the preference by blocking the current thread
     * until the value is retrieved from the DataStore.
     * This method directly calls the suspending `get()` function within `runBlocking`.
     * Use with caution, especially on the main thread, as it can lead to UI unresponsiveness
     * if the DataStore operation is slow.
     * For non-blocking alternatives, consider using `asFlow()`, `stateIn()`, or `get()` within a coroutine.
     *
     * @return The current value of the preference.
     */
    override fun getValue(): T = runBlocking { get() }

    /**
     * Asynchronously sets the value of the preference.
     * This method launches a new coroutine within the provided `scope` to update the value
     * in the DataStore using the [set] suspending function. This allows for a fire-and-forget
     * style of updating the preference from non-suspending contexts.
     *
     * @param value The new value to be stored for the preference.
     */
    override fun setValue(value: T) {
        scope.launch {
            set(value)
        }
    }

    /**
     * A [GenericPreference] implementation specifically for String values.
     *
     * @param datastore The [DataStore<Preferences>] instance.
     * @param key The String key for this preference.
     * @param defaultValue The default String value to use if the preference is not set.
     * @param scope The [CoroutineScope] for managing coroutines related to this preference.
     */
    class StringPrimitive(
        datastore: DataStore<Preferences>,
        key: String,
        defaultValue: String,
        scope: CoroutineScope,
    ) : GenericPreference<String>(
        datastore = datastore,
        key = key,
        defaultValue = defaultValue,
        preferences = stringPreferencesKey(key),
        scope = scope
    )

    /**
     * A [GenericPreference] implementation specifically for Long values.
     *
     * @param datastore The [DataStore<Preferences>] instance.
     * @param key The String key for this preference.
     * @param defaultValue The default Long value to use if the preference is not set.
     * @param scope The [CoroutineScope] for managing coroutines related to this preference.
     */
    class LongPrimitive(
        datastore: DataStore<Preferences>,
        key: String,
        defaultValue: Long,
        scope: CoroutineScope,
    ) : GenericPreference<Long>(
        datastore = datastore,
        key = key,
        defaultValue = defaultValue,
        preferences = longPreferencesKey(key),
        scope = scope
    )

    /**
     * A [GenericPreference] implementation specifically for Int values.
     *
     * @param datastore The [DataStore<Preferences>] instance.
     * @param key The String key for this preference.
     * @param defaultValue The default Int value to use if the preference is not set.
     * @param scope The [CoroutineScope] for managing coroutines related to this preference.
     */
    class IntPrimitive(
        datastore: DataStore<Preferences>,
        key: String,
        defaultValue: Int,
        scope: CoroutineScope,
    ) : GenericPreference<Int>(
        datastore = datastore,
        key = key,
        defaultValue = defaultValue,
        preferences = intPreferencesKey(key),
        scope = scope
    )

    /**
     * A [GenericPreference] implementation specifically for Float values.
     *
     * @param datastore The [DataStore<Preferences>] instance.
     * @param key The String key for this preference.
     * @param defaultValue The default Float value to use if the preference is not set.
     * @param scope The [CoroutineScope] for managing coroutines related to this preference.
     */
    class FloatPrimitive(
        datastore: DataStore<Preferences>,
        key: String,
        defaultValue: Float,
        scope: CoroutineScope,
    ) : GenericPreference<Float>(
        datastore = datastore,
        key = key,
        defaultValue = defaultValue,
        preferences = floatPreferencesKey(key),
        scope = scope
    )

    /**
     * A [GenericPreference] implementation specifically for Boolean values.
     *
     * @param datastore The [DataStore<Preferences>] instance.
     * @param key The String key for this preference.
     * @param defaultValue The default Boolean value to use if the preference is not set.
     * @param scope The [CoroutineScope] for managing coroutines related to this preference.
     */
    class BooleanPrimitive(
        datastore: DataStore<Preferences>,
        key: String,
        defaultValue: Boolean,
        scope: CoroutineScope,
    ) : GenericPreference<Boolean>(
        datastore = datastore,
        key = key,
        defaultValue = defaultValue,
        preferences = booleanPreferencesKey(key),
        scope = scope
    )

    /**
     * A [GenericPreference] implementation specifically for Set<String> values.
     *
     * @param datastore The [DataStore<Preferences>] instance.
     * @param key The String key for this preference.
     * @param defaultValue The default Set<String> value to use if the preference is not set.
     * @param scope The [CoroutineScope] for managing coroutines related to this preference.
     */
    class StringSetPrimitive(
        datastore: DataStore<Preferences>,
        key: String,
        defaultValue: Set<String>,
        scope: CoroutineScope,
    ) : GenericPreference<Set<String>>(
        datastore = datastore,
        key = key,
        defaultValue = defaultValue,
        preferences = stringSetPreferencesKey(key),
        scope = scope
    )
}