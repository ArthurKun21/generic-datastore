package io.github.arthurkun.generic.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
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
    private val datastore: DataStore<Preferences>,
    private val key: String,
    private val defaultValue: T,
    private val preferences: Preferences.Key<T> = preferencesKey(key, defaultValue),
): Preference<T> {
    /**
     * Reads the preference value from DataStore.
     *
     * @param datastore The DataStore instance.
     * @param key The preference key.
     * @param defaultValue The default value to return if the key is not found.
     * @return The preference value.
     */
    abstract suspend fun read(
        datastore: DataStore<Preferences>,
        key: String,
        defaultValue: T,
    ): T

    /**
     * Writes the preference value to DataStore.
     *
     * @param datastore The DataStore instance.
     * @param key The preference key.
     * @param value The value to write.
     */
    abstract suspend fun write(
        datastore: DataStore<Preferences>,
        key: String,
        value: T
    )

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
     * Returns the default value of the preference.
     */
    override fun defaultValue(): T {
        return defaultValue
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
    override suspend fun stateIn(scope: CoroutineScope): StateFlow<T> {
        return asFlow().stateIn(scope, SharingStarted.Companion.Eagerly, get())
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
    ) : GenericPreference<String>(
        datastore = datastore,
        key = key,
        defaultValue = defaultValue,
        preferences = preferencesKey,
    ) {
        /**
         * Reads the String preference value.
         *
         * @param defaultValue The default value (unused, uses class property).
         * @return The String preference value.
         */
        override suspend fun read(
            datastore: DataStore<Preferences>,
            key: String,
            defaultValue: String
        ): String = get()

        /**
         * Writes the String preference value.
         *
         * @param value The String value to write.
         */
        override suspend fun write(
            datastore: DataStore<Preferences>,
            key: String,
            value: String
        ) = set(value)

    }

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
    ) : GenericPreference<Long>(
        datastore = datastore,
        key = key,
        preferences = preferencesKey,
        defaultValue = defaultValue,
    ) {
        /**
         * Reads the Long preference value.
         *
         * @param defaultValue The default value (unused, uses class property).
         * @return The Long preference value.
         */
        override suspend fun read(
            datastore: DataStore<Preferences>,
            key: String,
            defaultValue: Long
        ): Long = get()

        /**
         * Writes the Long preference value.
         *
         * @param value The Long value to write.
         */
        override suspend fun write(
            datastore: DataStore<Preferences>,
            key: String,
            value: Long
        ) = set(value)
    }

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
    ) : GenericPreference<Int>(
        datastore = datastore,
        key = key,
        preferences = preferencesKey,
        defaultValue = defaultValue,
    ) {
        /**
         * Reads the Int preference value.
         *
         * @param defaultValue The default value (unused, uses class property).
         * @return The Int preference value.
         */
        override suspend fun read(
            datastore: DataStore<Preferences>,
            key: String,
            defaultValue: Int
        ): Int = get()

        /**
         * Writes the Int preference value.
         *
         * @param value The Int value to write.
         */
        override suspend fun write(
            datastore: DataStore<Preferences>,
            key: String,
            value: Int
        ) = set(value)
    }

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
    ) : GenericPreference<Float>(
        datastore = datastore,
        key = key,
        preferences = preferencesKey,
        defaultValue = defaultValue,
    ) {
        /**
         * Reads the Float preference value.
         *
         * @param defaultValue The default value (unused, uses class property).
         * @return The Float preference value.
         */
        override suspend fun read(
            datastore: DataStore<Preferences>,
            key: String,
            defaultValue: Float
        ): Float = get()

        /**
         * Writes the Float preference value.
         *
         * @param value The Float value to write.
         */
        override suspend fun write(
            datastore: DataStore<Preferences>,
            key: String,
            value: Float
        ) = set(value)
    }

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
    ) : GenericPreference<Boolean>(
        datastore = datastore,
        key = key,
        preferences = preferencesKey,
        defaultValue = defaultValue,
    ) {
        /**
         * Reads the Boolean preference value.
         *
         * @param defaultValue The default value (unused, uses class property).
         * @return The Boolean preference value.
         */
        override suspend fun read(
            datastore: DataStore<Preferences>,
            key: String,
            defaultValue: Boolean
        ): Boolean = get()

        /**
         * Writes the Boolean preference value.
         *
         * @param value The Boolean value to write.
         */
        override suspend fun write(
            datastore: DataStore<Preferences>,
            key: String,
            value: Boolean
        ) = set(value)
    }

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
    ) : GenericPreference<Set<String>>(
        datastore = datastore,
        key = key,
        preferences = preferencesKey,
        defaultValue = defaultValue,
    ) {
        /**
         * Reads the Set<String> preference value.
         *
         * @param defaultValue The default value (unused, uses class property).
         * @return The Set<String> preference value.
         */
        override suspend fun read(
            datastore: DataStore<Preferences>,
            key: String,
            defaultValue: Set<String>
        ): Set<String> = get()

        /**
         * Writes the Set<String> preference value.
         *
         * @param value The Set<String> value to write.
         */
        override suspend fun write(
            datastore: DataStore<Preferences>,
            key: String,
            value: Set<String>
        ) = set(value)
    }

    /**
     * A GenericPreference for custom Object values.
     *
     * @param T The type of the custom object.
     * @param deserializer A function to deserialize the String representation back to the object.
     */
    class ObjectPrimitive<T>(
        datastore: DataStore<Preferences>,
        key: String,
        defaultValue: T,
        val serializer: (T) -> String,
        val deserializer: (String) -> T,
    ) : GenericPreference<T>(
        datastore = datastore,
        key = key,
        defaultValue = defaultValue,
    ) {
        /**
         * Reads the custom Object preference value.
         *
         * @param defaultValue The default value to return if the key is not found.
         * @return The custom Object preference value.
         */
        override suspend fun read(
            datastore: DataStore<Preferences>,
            key: String,
            defaultValue: T
        ): T = datastore
            .data
            .map { ds ->
                val prefs = stringPreferencesKey(key)
                ds[prefs]?.let { deserializer(it) } ?: defaultValue
            }
            .first()

        /**
         * Writes the custom Object preference value.
         *
         * @param value The custom Object value to write.
         */
        override suspend fun write(
            datastore: DataStore<Preferences>,
            key: String,
            value: T
        ) {
            datastore.edit { ds ->
                val prefs = stringPreferencesKey(key)
                ds[prefs] = serializer(value)
            }
        }
    }
}