package io.github.arthurkun.generic.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
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
     */
    override fun string(key: String, defaultValue: String): Prefs<String> =
        PrefsImpl(
            StringPrimitive(
                datastore = datastore,
                key = key,
                defaultValue = defaultValue,
            ),
        )

    /**
     * Creates a Long preference.
     *
     * @param key The preference key.
     * @param defaultValue The default Long value.
     * @return A [Prefs] instance for the Long preference.
     */
    override fun long(key: String, defaultValue: Long): Prefs<Long> =
        PrefsImpl(
            LongPrimitive(
                datastore = datastore,
                key = key,
                defaultValue = defaultValue,
            ),
        )

    /**
     * Creates an Int preference.
     *
     * @param key The preference key.
     * @param defaultValue The default Int value.
     * @return A [Prefs] instance for the Int preference.
     */
    override fun int(key: String, defaultValue: Int): Prefs<Int> =
        PrefsImpl(
            IntPrimitive(
                datastore = datastore,
                key = key,
                defaultValue = defaultValue,
            ),
        )

    /**
     * Creates a Float preference.
     *
     * @param key The preference key.
     * @param defaultValue The default Float value.
     * @return A [Prefs] instance for the Float preference.
     */
    override fun float(key: String, defaultValue: Float): Prefs<Float> =
        PrefsImpl(
            FloatPrimitive(
                datastore = datastore,
                key = key,
                defaultValue = defaultValue,
            ),
        )

    /**
     * Creates a Boolean preference.
     *
     * @param key The preference key.
     * @param defaultValue The default Boolean value.
     * @return A [Prefs] instance for the Boolean preference.
     */
    override fun bool(key: String, defaultValue: Boolean): Prefs<Boolean> =
        PrefsImpl(
            BooleanPrimitive(
                datastore = datastore,
                key = key,
                defaultValue = defaultValue,
            ),
        )

    /**
     * Creates a Set<String> preference.
     *
     * @param key The preference key.
     * @param defaultValue The default Set<String> value.
     * @return A [Prefs] instance for the Set<String> preference.
     */
    override fun stringSet(
        key: String,
        defaultValue: Set<String>,
    ): Prefs<Set<String>> =
        PrefsImpl(
            StringSetPrimitive(
                datastore = datastore,
                key = key,
                defaultValue = defaultValue,
            ),
        )

    /**
     * Creates a preference for a custom object that can be serialized to and deserialized from a String.
     *
     * @param T The type of the custom object.
     * @param key The preference key.
     * @param defaultValue The default value for the custom object.
     * @param serializer A function to serialize the object to a String.
     * @param deserializer A function to deserialize the String back to the object.
     * @return A [Prefs] instance for the custom object preference.
     */
    override fun <T> serialized(
        key: String,
        defaultValue: T,
        serializer: (T) -> String,
        deserializer: (String) -> T,
    ): Prefs<T> = PrefsImpl(
        ObjectPrimitive(
            datastore = datastore,
            key = key,
            defaultValue = defaultValue,
            serializer = serializer,
            deserializer = deserializer,
        ),
    )

    override suspend fun export(exportPrivate: Boolean, exportAppState: Boolean): Map<String, JsonElement> {
        return datastore
            .data
            .first()
            .toPreferences()
            .asMap()
            .mapNotNull { (key, values) ->
                if (!exportPrivate && isPrivate(key.name)) {
                    null
                } else if (!exportAppState && isAppState(key.name)) {
                    null
                } else {
                    key.name to values.toJsonElement()
                }
            }
            .toMap()
    }

    override suspend fun import(data: Map<String, Any>) {
        datastore.updateData { currentPreferences ->
            val mutablePreferences = currentPreferences.toMutablePreferences()
            data.map { (key, value) ->
                when (value) {
                    is String -> mutablePreferences[stringPreferencesKey(key)] = value
                    is Long -> mutablePreferences[longPreferencesKey(key)] = value
                    is Int -> mutablePreferences[intPreferencesKey(key)] = value
                    is Float -> mutablePreferences[floatPreferencesKey(key)] = value
                    is Boolean -> mutablePreferences[booleanPreferencesKey(key)] = value
                    is Set<*> -> {
                        @Suppress("UNCHECKED_CAST")
                        mutablePreferences[stringSetPreferencesKey(key)] = value as Set<String>
                    }

                    else -> {
                        // Handle custom objects or unsupported types
                        mutablePreferences[stringPreferencesKey(key)] = "$value"
                    }
                }
            }
            mutablePreferences.toPreferences()
        }
    }
}
