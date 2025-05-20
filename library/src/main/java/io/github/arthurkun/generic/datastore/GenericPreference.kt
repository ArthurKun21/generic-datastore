package io.github.arthurkun.generic.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

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
    override val scope: CoroutineScope,
): Preference<T> {
    /**
     * Returns the key of the preference.
     */
    override fun key(): String = key

    /**
     * Gets the current value of the preference.
     *
     * @return The current preference value.
     */
    override suspend fun get(): T {
        return datastore
            .data
            .map { ds ->
                ds[preferences] ?: defaultValue
            }
            .first()
    }

    /**
     * Sets the value of the preference.
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
     *
     * @return A Flow of the preference value.
     */
    override fun asFlow(): Flow<T> {
        return return datastore
            .data
            .map { ds ->
                ds[preferences] ?: defaultValue
            }
    }

    /**
     * Converts the preference Flow into a StateFlow.
     *
     * @param scope The CoroutineScope to use for the StateFlow.
     * @return A StateFlow of the preference value.
     */
    override fun stateIn(scope: CoroutineScope): StateFlow<T> {
        return asFlow().stateIn(scope, SharingStarted.Companion.Eagerly, defaultValue)
    }

    /**
     * A GenericPreference for String values.
     *
     * @param datastore The DataStore instance.
     * @param preferencesKey The Preferences.Key for the String preference.
     * @param key The preference key.
     * @param defaultValue The default String value.
     */
    class StringPrimitive(
        datastore: DataStore<Preferences>,
        preferencesKey: Preferences.Key<String>,
        key: String,
        defaultValue: String,
        scope: CoroutineScope,
    ) : GenericPreference<String>(
        datastore = datastore,
        key = key,
        defaultValue = defaultValue,
        preferences = preferencesKey,
        scope = scope
    )

    /**
     * A GenericPreference for Long values.
     *
     * @param datastore The DataStore instance.
     * @param defaultValue The default Long value.
     */
    class LongPrimitive(
        datastore: DataStore<Preferences>,
        preferencesKey: Preferences.Key<Long>,
        key: String,
        defaultValue: Long,
        scope: CoroutineScope,
    ) : GenericPreference<Long>(
        datastore = datastore,
        key = key,
        preferences = preferencesKey,
        defaultValue = defaultValue,
        scope = scope
    )

    /**
     * A GenericPreference for Int values.
     *
     * @param defaultValue The default Int value.
     */
    class IntPrimitive(
        datastore: DataStore<Preferences>,
        preferencesKey: Preferences.Key<Int>,
        key: String,
        defaultValue: Int,
        scope: CoroutineScope,
    ) : GenericPreference<Int>(
        datastore = datastore,
        key = key,
        preferences = preferencesKey,
        defaultValue = defaultValue,
        scope = scope
    )

    /**
     * A GenericPreference for Float values.
     *
     * @param defaultValue The default Float value.
     */
    class FloatPrimitive(
        datastore: DataStore<Preferences>,
        preferencesKey: Preferences.Key<Float>,
        key: String,
        defaultValue: Float,
        scope: CoroutineScope,
    ) : GenericPreference<Float>(
        datastore = datastore,
        key = key,
        preferences = preferencesKey,
        defaultValue = defaultValue,
        scope = scope
    )

    /**
     * A GenericPreference for Boolean values.
     *
     * @param defaultValue The default Boolean value.
     */
    class BooleanPrimitive(
        datastore: DataStore<Preferences>,
        preferencesKey: Preferences.Key<Boolean>,
        key: String,
        defaultValue: Boolean,
        scope: CoroutineScope,
    ) : GenericPreference<Boolean>(
        datastore = datastore,
        key = key,
        preferences = preferencesKey,
        defaultValue = defaultValue,
        scope = scope
    )

    /**
     * A GenericPreference for Set<String> values.
     *
     * @param defaultValue The default Set<String> value.
     */
    class StringSetPrimitive(
        datastore: DataStore<Preferences>,
        preferencesKey: Preferences.Key<Set<String>>,
        key: String,
        defaultValue: Set<String>,
        scope: CoroutineScope,
    ) : GenericPreference<Set<String>>(
        datastore = datastore,
        key = key,
        preferences = preferencesKey,
        defaultValue = defaultValue,
        scope = scope
    )
}