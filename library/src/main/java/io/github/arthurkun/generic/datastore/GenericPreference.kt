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

/**
 * Represents a generic preference that can be stored in and retrieved from a DataStore.
 *
 * This sealed class defines the basic operations for a preference, such as reading,
 * writing, deleting, and observing its value as a Flow or StateFlow.
 *
 * @param T The type of the preference value.
 * @property datastore The DataStore instance used for storing preferences.
 * @property key The key for the preference.
 * @property defaultValue The default value for the preference.
 * @property preferences The Preferences.Key used to access the preference in DataStore.
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
     * Gets the current value of the preference from the DataStore.
     * If the value is not found, it returns the `defaultValue`.
     *
     * @return The current preference value.
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
     * Sets the value of the preference in the DataStore.
     *
     * @param value The new value for the preference.
     */
    override suspend fun set(value: T) {
        datastore.edit { ds ->
            ds[preferences] = value
        }
    }

    /**
     * Deletes the preference from DataStore.
     */
    override suspend fun delete() {
        datastore.edit { ds ->
            ds.remove(preferences)
        }
    }

    /**
     * Returns a Flow that emits the preference value whenever it changes.
     * If the value is not found, it emits the `defaultValue`.
     *
     * @return A Flow of the preference value.
     */
    override fun asFlow(): Flow<T> {
        return datastore
            .data
            .map { preferences ->
                preferences[this.preferences] ?: defaultValue
            }
    }

    /**
     * Converts the preference Flow into a StateFlow.
     * The StateFlow is started eagerly and shares the latest value.
     *
     * @param scope The CoroutineScope to use for the StateFlow.
     * @return A StateFlow of the preference value.
     */
    override fun stateIn(scope: CoroutineScope): StateFlow<T> =
        asFlow().stateIn(scope, SharingStarted.Eagerly, defaultValue)

    override fun getValue(): T = stateIn(scope).value

    override fun setValue(value: T) {
        scope.launch {
            set(value)
        }
    }

    /**
     * A [GenericPreference] for String values.
     * @param datastore The DataStore instance.
     * @param key The preference key.
     * @param defaultValue The default String value.
     * @param scope The CoroutineScope for managing coroutines.
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
     * A [GenericPreference] for Long values.
     * @param datastore The DataStore instance.
     * @param key The preference key.
     * @param defaultValue The default Long value.
     * @param scope The CoroutineScope for managing coroutines.
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
     * A [GenericPreference] for Int values.
     * @param datastore The DataStore instance.
     * @param key The preference key.
     * @param defaultValue The default Int value.
     * @param scope The CoroutineScope for managing coroutines.
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
     * A [GenericPreference] for Float values.
     * @param datastore The DataStore instance.
     * @param key The preference key.
     * @param defaultValue The default Float value.
     * @param scope The CoroutineScope for managing coroutines.
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
     * A [GenericPreference] for Boolean values.
     * @param datastore The DataStore instance.
     * @param key The preference key.
     * @param defaultValue The default Boolean value.
     * @param scope The CoroutineScope for managing coroutines.
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
     * A [GenericPreference] for Set<String> values.
     * @param datastore The DataStore instance.
     * @param key The preference key.
     * @param defaultValue The default Set<String> value.
     * @param scope The CoroutineScope for managing coroutines.
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