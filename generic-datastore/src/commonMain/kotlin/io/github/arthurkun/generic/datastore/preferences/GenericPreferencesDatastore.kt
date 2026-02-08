@file:Suppress("unused")

package io.github.arthurkun.generic.datastore.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import io.github.arthurkun.generic.datastore.core.Preference
import io.github.arthurkun.generic.datastore.core.PreferenceDefaults
import io.github.arthurkun.generic.datastore.core.Prefs
import io.github.arthurkun.generic.datastore.core.PrefsImpl
import io.github.arthurkun.generic.datastore.core.toJsonElement
import io.github.arthurkun.generic.datastore.preferences.backup.PreferenceBackupCreator
import io.github.arthurkun.generic.datastore.preferences.backup.PreferenceBackupRestorer
import io.github.arthurkun.generic.datastore.preferences.backup.PreferencesBackup
import io.github.arthurkun.generic.datastore.preferences.default.BooleanPrimitive
import io.github.arthurkun.generic.datastore.preferences.default.DoublePrimitive
import io.github.arthurkun.generic.datastore.preferences.default.FloatPrimitive
import io.github.arthurkun.generic.datastore.preferences.default.IntPrimitive
import io.github.arthurkun.generic.datastore.preferences.default.KSerializedListPrimitive
import io.github.arthurkun.generic.datastore.preferences.default.KSerializedPrimitive
import io.github.arthurkun.generic.datastore.preferences.default.KSerializedSetPrimitive
import io.github.arthurkun.generic.datastore.preferences.default.LongPrimitive
import io.github.arthurkun.generic.datastore.preferences.default.ObjectPrimitive
import io.github.arthurkun.generic.datastore.preferences.default.SerializedListPrimitive
import io.github.arthurkun.generic.datastore.preferences.default.SerializedSetPrimitive
import io.github.arthurkun.generic.datastore.preferences.default.StringPrimitive
import io.github.arthurkun.generic.datastore.preferences.default.StringSetPrimitive
import io.github.arthurkun.generic.datastore.preferences.optional.NullableBooleanPrimitive
import io.github.arthurkun.generic.datastore.preferences.optional.NullableDoublePrimitive
import io.github.arthurkun.generic.datastore.preferences.optional.NullableFloatPrimitive
import io.github.arthurkun.generic.datastore.preferences.optional.NullableIntPrimitive
import io.github.arthurkun.generic.datastore.preferences.optional.NullableLongPrimitive
import io.github.arthurkun.generic.datastore.preferences.optional.NullableStringPrimitive
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
 * @property defaultJson The default [Json] instance to use for Kotlin Serialization-based preferences.
 */
public class GenericPreferencesDatastore(
    private val datastore: DataStore<Preferences>,
    private val defaultJson: Json = PreferenceDefaults.defaultJson,
) : PreferencesDatastore {

    private val backupCreator = PreferenceBackupCreator(datastore)
    private val backupRestorer = PreferenceBackupRestorer(datastore)

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
     * Creates a Double preference.
     *
     * @param key The preference key.
     * @param defaultValue The default Double value.
     * @return A [Prefs] instance for the Double preference.
     */
    override fun double(key: String, defaultValue: Double): Prefs<Double> =
        PrefsImpl(
            DoublePrimitive(
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
     * Creates a nullable String preference.
     * Returns `null` when the key is not set in DataStore.
     *
     * @param key The preference key.
     * @return A [Prefs] instance for the nullable String preference.
     */
    override fun nullableString(key: String): Prefs<String?> =
        PrefsImpl(
            NullableStringPrimitive(
                datastore = datastore,
                key = key,
            ),
        )

    /**
     * Creates a nullable Int preference.
     * Returns `null` when the key is not set in DataStore.
     *
     * @param key The preference key.
     * @return A [Prefs] instance for the nullable Int preference.
     */
    override fun nullableInt(key: String): Prefs<Int?> =
        PrefsImpl(
            NullableIntPrimitive(
                datastore = datastore,
                key = key,
            ),
        )

    /**
     * Creates a nullable Long preference.
     * Returns `null` when the key is not set in DataStore.
     *
     * @param key The preference key.
     * @return A [Prefs] instance for the nullable Long preference.
     */
    override fun nullableLong(key: String): Prefs<Long?> =
        PrefsImpl(
            NullableLongPrimitive(
                datastore = datastore,
                key = key,
            ),
        )

    /**
     * Creates a nullable Float preference.
     * Returns `null` when the key is not set in DataStore.
     *
     * @param key The preference key.
     * @return A [Prefs] instance for the nullable Float preference.
     */
    override fun nullableFloat(key: String): Prefs<Float?> =
        PrefsImpl(
            NullableFloatPrimitive(
                datastore = datastore,
                key = key,
            ),
        )

    /**
     * Creates a nullable Double preference.
     * Returns `null` when the key is not set in DataStore.
     *
     * @param key The preference key.
     * @return A [Prefs] instance for the nullable Double preference.
     */
    override fun nullableDouble(key: String): Prefs<Double?> =
        PrefsImpl(
            NullableDoublePrimitive(
                datastore = datastore,
                key = key,
            ),
        )

    /**
     * Creates a nullable Boolean preference.
     * Returns `null` when the key is not set in DataStore.
     *
     * @param key The preference key.
     * @return A [Prefs] instance for the nullable Boolean preference.
     */
    override fun nullableBool(key: String): Prefs<Boolean?> =
        PrefsImpl(
            NullableBooleanPrimitive(
                datastore = datastore,
                key = key,
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

    /**
     * Creates a preference for a [Set] of custom objects, stored using a string set preference key.
     * Each element is individually serialized to and deserialized from a String.
     *
     * @param T The type of each element in the set.
     * @param key The preference key.
     * @param defaultValue The default value for the set.
     * @param serializer A function to serialize each element to a String.
     * @param deserializer A function to deserialize each String back to an element.
     * @return A [Prefs] instance for the Set preference.
     */
    override fun <T> serializedSet(
        key: String,
        defaultValue: Set<T>,
        serializer: (T) -> String,
        deserializer: (String) -> T,
    ): Prefs<Set<T>> = PrefsImpl(
        SerializedSetPrimitive(
            datastore = datastore,
            key = key,
            defaultValue = defaultValue,
            serializer = serializer,
            deserializer = deserializer,
        ),
    )

    /**
     * Creates a preference for a custom object using Kotlin Serialization.
     * The object is serialized to JSON for storage.
     *
     * @param T The type of the custom object. Must be serializable using kotlinx.serialization.
     * @param key The preference key.
     * @param defaultValue The default value for the custom object.
     * @param serializer The [KSerializer] for the type [T].
     * @param json The [Json] instance to use for serialization/deserialization.
     * @return A [Prefs] instance for the custom object preference.
     */
    override fun <T> kserialized(
        key: String,
        defaultValue: T,
        serializer: KSerializer<T>,
        json: Json?,
    ): Prefs<T> = PrefsImpl(
        KSerializedPrimitive(
            datastore = datastore,
            key = key,
            defaultValue = defaultValue,
            serializer = serializer,
            json = json ?: defaultJson,
        ),
    )

    /**
     * Creates a preference for a [Set] of custom objects using Kotlin Serialization.
     * Each element is serialized to JSON for storage using [stringSetPreferencesKey].
     *
     * @param T The type of each element in the set. Must be serializable using kotlinx.serialization.
     * @param key The preference key.
     * @param defaultValue The default value for the set (defaults to an empty set).
     * @param serializer The [KSerializer] for the type [T].
     * @param json The [Json] instance to use for serialization/deserialization.
     * @return A [Prefs] instance for the Set preference.
     */
    override fun <T> kserializedSet(
        key: String,
        defaultValue: Set<T>,
        serializer: KSerializer<T>,
        json: Json?,
    ): Prefs<Set<T>> = PrefsImpl(
        KSerializedSetPrimitive(
            datastore = datastore,
            key = key,
            defaultValue = defaultValue,
            serializer = serializer,
            json = json ?: defaultJson,
        ),
    )

    /**
     * Creates a preference for a [List] of custom objects that can be serialized to and
     * deserialized from Strings. The list is stored as a JSON array string.
     *
     * @param T The type of each element in the list.
     * @param key The preference key.
     * @param defaultValue The default value for the list (defaults to an empty list).
     * @param serializer A function to serialize each element to a String.
     * @param deserializer A function to deserialize each String back to an element.
     * @return A [Prefs] instance for the List preference.
     */
    override fun <T> serializedList(
        key: String,
        defaultValue: List<T>,
        serializer: (T) -> String,
        deserializer: (String) -> T,
    ): Prefs<List<T>> = PrefsImpl(
        SerializedListPrimitive(
            datastore = datastore,
            key = key,
            defaultValue = defaultValue,
            serializer = serializer,
            deserializer = deserializer,
        ),
    )

    /**
     * Creates a preference for a [List] of custom objects using Kotlin Serialization.
     * The list is serialized to a JSON array string for storage.
     *
     * @param T The type of each element in the list. Must be serializable using kotlinx.serialization.
     * @param key The preference key.
     * @param defaultValue The default value for the list (defaults to an empty list).
     * @param serializer The [KSerializer] for the type [T].
     * @param json The [Json] instance to use for serialization/deserialization.
     * @return A [Prefs] instance for the List preference.
     */
    override fun <T> kserializedList(
        key: String,
        defaultValue: List<T>,
        serializer: KSerializer<T>,
        json: Json?,
    ): Prefs<List<T>> = PrefsImpl(
        KSerializedListPrimitive(
            datastore = datastore,
            key = key,
            defaultValue = defaultValue,
            serializer = serializer,
            json = json ?: defaultJson,
        ),
    )

    /**
     * Clears all preferences stored in this datastore.
     *
     * After calling this, all preferences will return their default values.
     */
    override suspend fun clearAll() {
        datastore.edit { it.clear() }
    }

    /**
     * Exports all preferences as a map of keys to [JsonElement] values.
     *
     * @param exportPrivate Whether to include private preferences in the export.
     * @param exportAppState Whether to include app state preferences in the export.
     * @return A map of preference keys to their [JsonElement] representations.
     */
    override suspend fun export(exportPrivate: Boolean, exportAppState: Boolean): Map<String, JsonElement> {
        return datastore
            .data
            .first()
            .toPreferences()
            .asMap()
            .mapNotNull { (key, values) ->
                if (!exportPrivate && Preference.isPrivate(key.name)) {
                    null
                } else if (!exportAppState && Preference.isAppState(key.name)) {
                    null
                } else {
                    key.name to values.toJsonElement()
                }
            }
            .toMap()
    }

    /**
     * Imports preferences from a map of keys to values, merging them into existing preferences.
     *
     * Supported value types: [String], [Long], [Int], [Float], [Double], [Boolean],
     * and [Collection] of [String]. Other types are stored as their JSON string representation.
     *
     * @param data The map of preference keys to values to import.
     */
    override suspend fun import(data: Map<String, Any>) {
        datastore.updateData { currentPreferences ->
            val mutablePreferences = currentPreferences.toMutablePreferences()
            data.forEach { (key, value) ->
                when (value) {
                    is String -> mutablePreferences[stringPreferencesKey(key)] = value

                    is Long -> mutablePreferences[longPreferencesKey(key)] = value

                    is Int -> mutablePreferences[intPreferencesKey(key)] = value

                    is Float -> mutablePreferences[floatPreferencesKey(key)] = value

                    is Double -> mutablePreferences[doublePreferencesKey(key)] = value

                    is Boolean -> mutablePreferences[booleanPreferencesKey(key)] = value

                    is Collection<*> -> {
                        if (value.all { it is String }) {
                            @Suppress("UNCHECKED_CAST")
                            mutablePreferences[stringSetPreferencesKey(key)] = (value as Collection<String>).toSet()
                        } else {
                            val stringValue = value.toJsonElement().toString()
                            mutablePreferences[stringPreferencesKey(key)] = stringValue
                        }
                    }

                    else -> {
                        val stringValue = when (value) {
                            is Map<*, *>, is Collection<*> -> value.toJsonElement().toString()
                            else -> value.toString()
                        }
                        mutablePreferences[stringPreferencesKey(key)] = stringValue
                    }
                }
            }
            mutablePreferences.toPreferences()
        }
    }

    override suspend fun exportAsData(
        exportPrivate: Boolean,
        exportAppState: Boolean,
    ): PreferencesBackup {
        return backupCreator.exportAsData(
            exportPrivate = exportPrivate,
            exportAppState = exportAppState,
        )
    }

    override suspend fun exportAsString(
        exportPrivate: Boolean,
        exportAppState: Boolean,
        json: Json?,
    ): String {
        return backupCreator.exportAsString(
            exportPrivate = exportPrivate,
            exportAppState = exportAppState,
            json = json ?: defaultJson,
        )
    }

    override suspend fun importData(
        backup: PreferencesBackup,
        importPrivate: Boolean,
        importAppState: Boolean,
    ) {
        backupRestorer.importData(
            backup = backup,
            importPrivate = importPrivate,
            importAppState = importAppState,
        )
    }

    override suspend fun importDataAsString(
        backupString: String,
        importPrivate: Boolean,
        importAppState: Boolean,
        json: Json?,
    ) {
        backupRestorer.importDataAsString(
            backupString = backupString,
            importPrivate = importPrivate,
            importAppState = importAppState,
            json = json ?: defaultJson,
        )
    }
}
