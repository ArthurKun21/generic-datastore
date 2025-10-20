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
import io.github.arthurkun.generic.datastore.GenericPreference.BooleanPrimitive
import io.github.arthurkun.generic.datastore.GenericPreference.FloatPrimitive
import io.github.arthurkun.generic.datastore.GenericPreference.IntPrimitive
import io.github.arthurkun.generic.datastore.GenericPreference.LongPrimitive
import io.github.arthurkun.generic.datastore.GenericPreference.StringPrimitive
import io.github.arthurkun.generic.datastore.GenericPreference.StringSetPrimitive
import io.github.arthurkun.generic.datastore.Preference.Companion.isAppState
import io.github.arthurkun.generic.datastore.Preference.Companion.isPrivate
import kotlinx.coroutines.flow.first
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement

/**
 * A DataStore implementation that provides methods for creating and managing various types of preferences.
 *
 * This class wraps a [DataStore<Preferences>] instance and offers convenient functions
 * to define preferences for common data types like String, Long, Int, Float, Boolean,
 * and Set<String>, as well as custom serialized objects.
 *
 * @property datastore The underlying [DataStore<Preferences>] instance.
 */
@Suppress("unused")
class GenericPreferenceDatastore(
    private val datastore: DataStore<Preferences>,
) : PreferenceDatastore {

    /**
     * Creates a String preference.
     *
     * @param key The preference key.
     * @param defaultValue The default String value.
     * @return A [Prefs] instance for the String preference.
     * @throws IllegalArgumentException if key is blank
     */
    override fun string(key: String, defaultValue: String): Prefs<String> {
        require(key.isNotBlank()) { "Preference key must not be blank" }
        return PrefsImpl(
            StringPrimitive(
                datastore = datastore,
                key = key,
                defaultValue = defaultValue,
            ),
        )
    }

    /**
     * Creates a Long preference.
     *
     * @param key The preference key.
     * @param defaultValue The default Long value.
     * @return A [Prefs] instance for the Long preference.
     * @throws IllegalArgumentException if key is blank
     */
    override fun long(key: String, defaultValue: Long): Prefs<Long> {
        require(key.isNotBlank()) { "Preference key must not be blank" }
        return PrefsImpl(
            LongPrimitive(
                datastore = datastore,
                key = key,
                defaultValue = defaultValue,
            ),
        )
    }

    /**
     * Creates an Int preference.
     *
     * @param key The preference key.
     * @param defaultValue The default Int value.
     * @return A [Prefs] instance for the Int preference.
     * @throws IllegalArgumentException if key is blank
     */
    override fun int(key: String, defaultValue: Int): Prefs<Int> {
        require(key.isNotBlank()) { "Preference key must not be blank" }
        return PrefsImpl(
            IntPrimitive(
                datastore = datastore,
                key = key,
                defaultValue = defaultValue,
            ),
        )
    }

    /**
     * Creates a Float preference.
     *
     * @param key The preference key.
     * @param defaultValue The default Float value.
     * @return A [Prefs] instance for the Float preference.
     * @throws IllegalArgumentException if key is blank
     */
    override fun float(key: String, defaultValue: Float): Prefs<Float> {
        require(key.isNotBlank()) { "Preference key must not be blank" }
        return PrefsImpl(
            FloatPrimitive(
                datastore = datastore,
                key = key,
                defaultValue = defaultValue,
            ),
        )
    }

    /**
     * Creates a Boolean preference.
     *
     * @param key The preference key.
     * @param defaultValue The default Boolean value.
     * @return A [Prefs] instance for the Boolean preference.
     * @throws IllegalArgumentException if key is blank
     */
    override fun bool(key: String, defaultValue: Boolean): Prefs<Boolean> {
        require(key.isNotBlank()) { "Preference key must not be blank" }
        return PrefsImpl(
            BooleanPrimitive(
                datastore = datastore,
                key = key,
                defaultValue = defaultValue,
            ),
        )
    }

    /**
     * Creates a Set<String> preference.
     *
     * @param key The preference key.
     * @param defaultValue The default Set<String> value.
     * @return A [Prefs] instance for the Set<String> preference.
     * @throws IllegalArgumentException if key is blank
     */
    override fun stringSet(
        key: String,
        defaultValue: Set<String>,
    ): Prefs<Set<String>> {
        require(key.isNotBlank()) { "Preference key must not be blank" }
        return PrefsImpl(
            StringSetPrimitive(
                datastore = datastore,
                key = key,
                defaultValue = defaultValue,
            ),
        )
    }

    /**
     * Creates a preference for a custom object that can be serialized to and deserialized from a String.
     *
     * @param T The type of the custom object.
     * @param key The preference key.
     * @param defaultValue The default value for the custom object.
     * @param serializer A function to serialize the object to a String.
     * @param deserializer A function to deserialize the String back to the object.
     * @return A [Prefs] instance for the custom object preference.
     * @throws IllegalArgumentException if key is blank
     */
    override fun <T> serialized(
        key: String,
        defaultValue: T,
        serializer: (T) -> String,
        deserializer: (String) -> T,
    ): Prefs<T> {
        require(key.isNotBlank()) { "Preference key must not be blank" }
        return PrefsImpl(
            ObjectPrimitive(
                datastore = datastore,
                key = key,
                defaultValue = defaultValue,
                serializer = serializer,
                deserializer = deserializer,
            ),
        )
    }

    /**
     * Creates a preference for a custom object using kotlinx.serialization KSerializer.
     * The object is serialized to JSON and stored as a String in DataStore.
     *
     * @param T The type of the custom object (must be serializable with kotlinx.serialization).
     * @param key The preference key.
     * @param defaultValue The default value for the custom object.
     * @param serializer The [KSerializer] for type T.
     * @param json Optional [Json] instance for customizing serialization (defaults to Json.Default).
     * @return A [Prefs] instance for the custom object preference.
     * @throws IllegalArgumentException if key is blank
     */
    override fun <T> kserializer(
        key: String,
        defaultValue: T,
        serializer: KSerializer<T>,
        json: Json,
    ): Prefs<T> {
        require(key.isNotBlank()) { "Preference key must not be blank" }
        return PrefsImpl(
            KSerializerPreference(
                datastore = datastore,
                key = key,
                defaultValue = defaultValue,
                serializer = serializer,
                json = json,
            ),
        )
    }

    /**
     * Creates a preference for a list/collection of custom objects using kotlinx.serialization KSerializer.
     * Each element is serialized to JSON individually and stored as a Set<String> in DataStore.
     *
     * @param T The type of elements in the collection (must be serializable with kotlinx.serialization).
     * @param key The preference key.
     * @param defaultValue The default list value.
     * @param serializer The [KSerializer] for type T.
     * @param json Optional [Json] instance for customizing serialization (defaults to Json.Default).
     * @return A [Prefs] instance for the list preference.
     * @throws IllegalArgumentException if key is blank
     */
    override fun <T> kserializerList(
        key: String,
        defaultValue: List<T>,
        serializer: KSerializer<T>,
        json: Json,
    ): Prefs<List<T>> {
        require(key.isNotBlank()) { "Preference key must not be blank" }
        return PrefsImpl(
            KSerializerListPreference(
                datastore = datastore,
                key = key,
                defaultValue = defaultValue,
                serializer = serializer,
                json = json,
            ),
        )
    }

    override suspend fun export(exportPrivate: Boolean, exportAppState: Boolean): Map<String, JsonElement> {
        return try {
            datastore
                .data
                .first()
                .toPreferences()
                .asMap()
                .mapNotNull { (key, values) ->
                    when {
                        !exportPrivate && isPrivate(key.name) -> null
                        !exportAppState && isAppState(key.name) -> null
                        else -> key.name to values.toJsonElement()
                    }
                }
                .toMap()
        } catch (e: Exception) {
            ConsoleLogger.error("Failed to export preferences", e)
            emptyMap()
        }
    }

    override suspend fun import(data: Map<String, Any>) {
        try {
            datastore.updateData { currentPreferences ->
                val mutablePreferences = currentPreferences.toMutablePreferences()
                data.forEach { (key, value) ->
                    try {
                        when (value) {
                            is String -> mutablePreferences[stringPreferencesKey(key)] = value
                            is Long -> mutablePreferences[longPreferencesKey(key)] = value
                            is Int -> mutablePreferences[intPreferencesKey(key)] = value
                            is Float -> mutablePreferences[floatPreferencesKey(key)] = value
                            is Boolean -> mutablePreferences[booleanPreferencesKey(key)] = value
                            is Collection<*> -> {
                                if (value.all { it is String }) {
                                    @Suppress("UNCHECKED_CAST")
                                    mutablePreferences[stringSetPreferencesKey(key)] =
                                        (value as Collection<String>).toSet()
                                } else {
                                    // Fallback for mixed-type or non-string collections
                                    val stringValue = value.toJsonElement().toString()
                                    mutablePreferences[stringPreferencesKey(key)] = stringValue
                                }
                            }

                            else -> {
                                // Handle custom objects or unsupported types by serializing them back to a JSON string.
                                val stringValue = when (value) {
                                    is Map<*, *>, is Collection<*> -> value.toJsonElement().toString()
                                    else -> value.toString()
                                }
                                mutablePreferences[stringPreferencesKey(key)] = stringValue
                            }
                        }
                    } catch (e: Exception) {
                        ConsoleLogger.error("Failed to import preference key: $key", e)
                    }
                }
                mutablePreferences.toPreferences()
            }
        } catch (e: Exception) {
            ConsoleLogger.error("Failed to import preferences", e)
        }
    }

    /**
     * Batch get operation for multiple preferences.
     * Retrieves values for multiple preferences in a single DataStore read operation.
     * This is much more efficient than calling get() on each preference individually.
     *
     * @param preferences List of preferences to retrieve values for
     * @return Map of preference keys to their current values
     */
    override suspend fun <T> batchGet(preferences: List<Prefs<T>>): Map<String, T> {
        if (preferences.isEmpty()) return emptyMap()

        return try {
            val currentPreferences = datastore.data.first()
            preferences.associate { pref ->
                val key = pref.key()
                val value = pref.get() // Uses cache if available
                key to value
            }
        } catch (e: Exception) {
            ConsoleLogger.error("Failed to batch get preferences", e)
            // Return defaults for all preferences
            preferences.associate { it.key() to it.defaultValue }
        }
    }

    /**
     * Batch set operation for multiple preferences.
     * Sets values for multiple preferences in a single DataStore write operation.
     * This is significantly more efficient than calling set() on each preference individually.
     *
     * Important: This operation invalidates the cache for all affected preferences.
     *
     * @param updates Map of preferences to their new values
     */
    override suspend fun batchSet(updates: Map<Prefs<*>, Any?>) {
        if (updates.isEmpty()) return

        try {
            datastore.edit { mutablePreferences ->
                updates.forEach { (pref, value) ->
                    try {
                        // Get the underlying GenericPreference to access its key
                        val genericPref = when (pref) {
                            is PrefsImpl<*> -> pref.pref
                            else -> pref
                        }

                        when (genericPref) {
                            is StringPrimitive -> {
                                @Suppress("UNCHECKED_CAST")
                                mutablePreferences[stringPreferencesKey(pref.key())] = value as String
                            }
                            is IntPrimitive -> {
                                @Suppress("UNCHECKED_CAST")
                                mutablePreferences[intPreferencesKey(pref.key())] = value as Int
                            }
                            is LongPrimitive -> {
                                @Suppress("UNCHECKED_CAST")
                                mutablePreferences[longPreferencesKey(pref.key())] = value as Long
                            }
                            is FloatPrimitive -> {
                                @Suppress("UNCHECKED_CAST")
                                mutablePreferences[floatPreferencesKey(pref.key())] = value as Float
                            }
                            is BooleanPrimitive -> {
                                @Suppress("UNCHECKED_CAST")
                                mutablePreferences[booleanPreferencesKey(pref.key())] = value as Boolean
                            }
                            is StringSetPrimitive -> {
                                @Suppress("UNCHECKED_CAST")
                                mutablePreferences[stringSetPreferencesKey(pref.key())] = value as Set<String>
                            }
                            is ObjectPrimitive<*> -> {
                                // For custom objects, we need to serialize
                                @Suppress("UNCHECKED_CAST")
                                val serializer = (genericPref as ObjectPrimitive<Any?>).serializer
                                mutablePreferences[stringPreferencesKey(pref.key())] = serializer(value)
                            }
                            is KSerializerPreference<*> -> {
                                // For KSerializer objects, we need to serialize
                                @Suppress("UNCHECKED_CAST")
                                val kserializer = (genericPref as KSerializerPreference<Any?>).serializer
                                val json = (genericPref as KSerializerPreference<Any?>).json
                                mutablePreferences[stringPreferencesKey(pref.key())] =
                                    json.encodeToString(kserializer, value)
                            }
                            is KSerializerListPreference<*> -> {
                                // For KSerializer lists, serialize each element
                                @Suppress("UNCHECKED_CAST")
                                val list = value as List<Any?>
                                val kserializer = (genericPref as KSerializerListPreference<Any?>).serializer
                                val json = (genericPref as KSerializerListPreference<Any?>).json
                                val serializedSet = list.mapNotNull { item ->
                                    try {
                                        json.encodeToString(kserializer, item)
                                    } catch (e: Exception) {
                                        ConsoleLogger.error("Failed to serialize list item in batch", e)
                                        null
                                    }
                                }.toSet()
                                mutablePreferences[stringSetPreferencesKey(pref.key())] = serializedSet
                            }
                            else -> {
                                ConsoleLogger.error(
                                    "Unsupported preference type in batch set: ${genericPref::class.simpleName}",
                                    null,
                                )
                            }
                        }
                    } catch (e: Exception) {
                        ConsoleLogger.error("Failed to set preference ${pref.key()} in batch", e)
                    }
                }
            }

            // Invalidate cache for all updated preferences
            updates.keys.forEach { it.invalidateCache() }
        } catch (e: Exception) {
            ConsoleLogger.error("Failed to batch set preferences", e)
        }
    }

    /**
     * Batch delete operation for multiple preferences.
     * Deletes multiple preferences in a single DataStore write operation.
     * This is more efficient than calling delete() on each preference individually.
     *
     * @param preferences List of preferences to delete
     */
    override suspend fun batchDelete(preferences: List<Prefs<*>>) {
        if (preferences.isEmpty()) return

        try {
            datastore.edit { mutablePreferences ->
                preferences.forEach { pref ->
                    try {
                        // Get the underlying GenericPreference to access its key
                        val genericPref = when (pref) {
                            is PrefsImpl<*> -> pref.pref
                            else -> pref
                        }

                        when (genericPref) {
                            is StringPrimitive -> mutablePreferences.remove(stringPreferencesKey(pref.key()))
                            is IntPrimitive -> mutablePreferences.remove(intPreferencesKey(pref.key()))
                            is LongPrimitive -> mutablePreferences.remove(longPreferencesKey(pref.key()))
                            is FloatPrimitive -> mutablePreferences.remove(floatPreferencesKey(pref.key()))
                            is BooleanPrimitive -> mutablePreferences.remove(booleanPreferencesKey(pref.key()))
                            is StringSetPrimitive -> mutablePreferences.remove(stringSetPreferencesKey(pref.key()))
                            is ObjectPrimitive<*> -> mutablePreferences.remove(stringPreferencesKey(pref.key()))
                            is KSerializerPreference<*> -> mutablePreferences.remove(stringPreferencesKey(pref.key()))
                            is KSerializerListPreference<*> -> mutablePreferences.remove(
                                stringSetPreferencesKey(pref.key()),
                            )
                            else -> {
                                ConsoleLogger.error(
                                    "Unsupported preference type in batch delete: ${genericPref::class.simpleName}",
                                    null,
                                )
                            }
                        }
                    } catch (e: Exception) {
                        ConsoleLogger.error("Failed to delete preference ${pref.key()} in batch", e)
                    }
                }
            }

            // Invalidate cache for all deleted preferences
            preferences.forEach { it.invalidateCache() }
        } catch (e: Exception) {
            ConsoleLogger.error("Failed to batch delete preferences", e)
        }
    }
}
