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
import io.github.arthurkun.generic.datastore.core.BasePreference
import io.github.arthurkun.generic.datastore.core.DelegatedPreference
import io.github.arthurkun.generic.datastore.core.DelegatedPreferenceImpl
import io.github.arthurkun.generic.datastore.core.PreferenceDefaults
import io.github.arthurkun.generic.datastore.preferences.backup.PreferenceBackupCreator
import io.github.arthurkun.generic.datastore.preferences.backup.PreferenceBackupRestorer
import io.github.arthurkun.generic.datastore.preferences.backup.PreferencesBackup
import io.github.arthurkun.generic.datastore.preferences.backup.toJsonElement
import io.github.arthurkun.generic.datastore.preferences.core.BooleanPrimitive
import io.github.arthurkun.generic.datastore.preferences.core.DoublePrimitive
import io.github.arthurkun.generic.datastore.preferences.core.FloatPrimitive
import io.github.arthurkun.generic.datastore.preferences.core.IntPrimitive
import io.github.arthurkun.generic.datastore.preferences.core.LongPrimitive
import io.github.arthurkun.generic.datastore.preferences.core.StringPrimitive
import io.github.arthurkun.generic.datastore.preferences.core.StringSetPrimitive
import io.github.arthurkun.generic.datastore.preferences.core.custom.KSerializedListPrimitive
import io.github.arthurkun.generic.datastore.preferences.core.custom.KSerializedPrimitive
import io.github.arthurkun.generic.datastore.preferences.core.custom.ObjectPrimitive
import io.github.arthurkun.generic.datastore.preferences.core.custom.SerializedListPrimitive
import io.github.arthurkun.generic.datastore.preferences.core.customSet.KSerializedSetPrimitive
import io.github.arthurkun.generic.datastore.preferences.core.customSet.SerializedSetPrimitive
import io.github.arthurkun.generic.datastore.preferences.optional.NullableBooleanPrimitive
import io.github.arthurkun.generic.datastore.preferences.optional.NullableDoublePrimitive
import io.github.arthurkun.generic.datastore.preferences.optional.NullableFloatPrimitive
import io.github.arthurkun.generic.datastore.preferences.optional.NullableIntPrimitive
import io.github.arthurkun.generic.datastore.preferences.optional.NullableLongPrimitive
import io.github.arthurkun.generic.datastore.preferences.optional.NullableStringPrimitive
import io.github.arthurkun.generic.datastore.preferences.optional.NullableStringSetPrimitive
import io.github.arthurkun.generic.datastore.preferences.optional.custom.NullableKSerializedListPrimitive
import io.github.arthurkun.generic.datastore.preferences.optional.custom.NullableKSerializedPrimitive
import io.github.arthurkun.generic.datastore.preferences.optional.custom.NullableObjectPrimitive
import io.github.arthurkun.generic.datastore.preferences.optional.custom.NullableSerializedListPrimitive
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
    internal val datastore: DataStore<Preferences>,
    private val defaultJson: Json = PreferenceDefaults.defaultJson,
) : PreferencesDatastore {

    private val backupCreator = PreferenceBackupCreator(datastore)
    private val backupRestorer = PreferenceBackupRestorer(datastore)

    /**
     * Creates a String preference.
     *
     * @param key The preference key.
     * @param defaultValue The default String value.
     * @return A [DelegatedPreference] instance for the String preference.
     */
    override fun string(
        key: String,
        defaultValue: String,
    ): io.github.arthurkun.generic.datastore.preferences.Preferences<String> =
        DelegatedPreferenceImpl(
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
     * @return A [DelegatedPreference] instance for the Long preference.
     */
    override fun long(
        key: String,
        defaultValue: Long,
    ): io.github.arthurkun.generic.datastore.preferences.Preferences<Long> =
        DelegatedPreferenceImpl(
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
     * @return A [DelegatedPreference] instance for the Int preference.
     */
    override fun int(
        key: String,
        defaultValue: Int,
    ): io.github.arthurkun.generic.datastore.preferences.Preferences<Int> =
        DelegatedPreferenceImpl(
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
     * @return A [DelegatedPreference] instance for the Float preference.
     */
    override fun float(
        key: String,
        defaultValue: Float,
    ): io.github.arthurkun.generic.datastore.preferences.Preferences<Float> =
        DelegatedPreferenceImpl(
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
     * @return A [DelegatedPreference] instance for the Double preference.
     */
    override fun double(
        key: String,
        defaultValue: Double,
    ): io.github.arthurkun.generic.datastore.preferences.Preferences<Double> =
        DelegatedPreferenceImpl(
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
     * @return A [DelegatedPreference] instance for the Boolean preference.
     */
    override fun bool(
        key: String,
        defaultValue: Boolean,
    ): io.github.arthurkun.generic.datastore.preferences.Preferences<Boolean> =
        DelegatedPreferenceImpl(
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
     * @return A [DelegatedPreference] instance for the nullable String preference.
     */
    override fun nullableString(key: String): io.github.arthurkun.generic.datastore.preferences.Preferences<String?> =
        DelegatedPreferenceImpl(
            NullableStringPrimitive(
                datastore = datastore,
                key = key,
            ),
        )

    /**
     * Creates a nullable Set<String> preference.
     * Returns `null` when the key is not set in DataStore.
     *
     * @param key The preference key.
     * @return A [DelegatedPreference] instance for the nullable Set<String> preference.
     */
    override fun nullableStringSet(key: String): io.github.arthurkun.generic.datastore.preferences.Preferences<Set<String>?> =
        DelegatedPreferenceImpl(
            NullableStringSetPrimitive(
                datastore = datastore,
                key = key,
            ),
        )

    /**
     * Creates a nullable Int preference.
     * Returns `null` when the key is not set in DataStore.
     *
     * @param key The preference key.
     * @return A [DelegatedPreference] instance for the nullable Int preference.
     */
    override fun nullableInt(key: String): io.github.arthurkun.generic.datastore.preferences.Preferences<Int?> =
        DelegatedPreferenceImpl(
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
     * @return A [DelegatedPreference] instance for the nullable Long preference.
     */
    override fun nullableLong(key: String): io.github.arthurkun.generic.datastore.preferences.Preferences<Long?> =
        DelegatedPreferenceImpl(
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
     * @return A [DelegatedPreference] instance for the nullable Float preference.
     */
    override fun nullableFloat(key: String): io.github.arthurkun.generic.datastore.preferences.Preferences<Float?> =
        DelegatedPreferenceImpl(
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
     * @return A [DelegatedPreference] instance for the nullable Double preference.
     */
    override fun nullableDouble(key: String): io.github.arthurkun.generic.datastore.preferences.Preferences<Double?> =
        DelegatedPreferenceImpl(
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
     * @return A [DelegatedPreference] instance for the nullable Boolean preference.
     */
    override fun nullableBool(key: String): io.github.arthurkun.generic.datastore.preferences.Preferences<Boolean?> =
        DelegatedPreferenceImpl(
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
     * @return A [DelegatedPreference] instance for the Set<String> preference.
     */
    override fun stringSet(
        key: String,
        defaultValue: Set<String>,
    ): io.github.arthurkun.generic.datastore.preferences.Preferences<Set<String>> =
        DelegatedPreferenceImpl(
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
     * @return A [DelegatedPreference] instance for the custom object preference.
     */
    override fun <T> serialized(
        key: String,
        defaultValue: T,
        serializer: (T) -> String,
        deserializer: (String) -> T,
    ): io.github.arthurkun.generic.datastore.preferences.Preferences<T> = DelegatedPreferenceImpl(
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
     * @return A [DelegatedPreference] instance for the Set preference.
     */
    override fun <T> serializedSet(
        key: String,
        defaultValue: Set<T>,
        serializer: (T) -> String,
        deserializer: (String) -> T,
    ): io.github.arthurkun.generic.datastore.preferences.Preferences<Set<T>> = DelegatedPreferenceImpl(
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
     * @return A [DelegatedPreference] instance for the custom object preference.
     */
    override fun <T> kserialized(
        key: String,
        defaultValue: T,
        serializer: KSerializer<T>,
        json: Json?,
    ): io.github.arthurkun.generic.datastore.preferences.Preferences<T> = DelegatedPreferenceImpl(
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
     * @return A [DelegatedPreference] instance for the Set preference.
     */
    override fun <T> kserializedSet(
        key: String,
        defaultValue: Set<T>,
        serializer: KSerializer<T>,
        json: Json?,
    ): io.github.arthurkun.generic.datastore.preferences.Preferences<Set<T>> = DelegatedPreferenceImpl(
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
     * @return A [DelegatedPreference] instance for the List preference.
     */
    override fun <T> serializedList(
        key: String,
        defaultValue: List<T>,
        serializer: (T) -> String,
        deserializer: (String) -> T,
    ): io.github.arthurkun.generic.datastore.preferences.Preferences<List<T>> = DelegatedPreferenceImpl(
        SerializedListPrimitive(
            datastore = datastore,
            key = key,
            defaultValue = defaultValue,
            elementSerializer = serializer,
            elementDeserializer = deserializer,
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
     * @return A [DelegatedPreference] instance for the List preference.
     */
    override fun <T> kserializedList(
        key: String,
        defaultValue: List<T>,
        serializer: KSerializer<T>,
        json: Json?,
    ): io.github.arthurkun.generic.datastore.preferences.Preferences<List<T>> = DelegatedPreferenceImpl(
        KSerializedListPrimitive(
            datastore = datastore,
            key = key,
            defaultValue = defaultValue,
            serializer = serializer,
            json = json ?: defaultJson,
        ),
    )

    override fun <T : Any> nullableSerialized(
        key: String,
        serializer: (T) -> String,
        deserializer: (String) -> T,
    ): io.github.arthurkun.generic.datastore.preferences.Preferences<T?> = DelegatedPreferenceImpl(
        NullableObjectPrimitive(
            datastore = datastore,
            key = key,
            serializer = serializer,
            deserializer = deserializer,
        ),
    )

    override fun <T : Any> nullableKserialized(
        key: String,
        serializer: KSerializer<T>,
        json: Json?,
    ): io.github.arthurkun.generic.datastore.preferences.Preferences<T?> = DelegatedPreferenceImpl(
        NullableKSerializedPrimitive(
            datastore = datastore,
            key = key,
            serializer = serializer,
            json = json ?: defaultJson,
        ),
    )

    override fun <T> nullableSerializedList(
        key: String,
        serializer: (T) -> String,
        deserializer: (String) -> T,
    ): io.github.arthurkun.generic.datastore.preferences.Preferences<List<T>?> = DelegatedPreferenceImpl(
        NullableSerializedListPrimitive(
            datastore = datastore,
            key = key,
            elementSerializer = serializer,
            elementDeserializer = deserializer,
        ),
    )

    override fun <T> nullableKserializedList(
        key: String,
        serializer: KSerializer<T>,
        json: Json?,
    ): io.github.arthurkun.generic.datastore.preferences.Preferences<List<T>?> = DelegatedPreferenceImpl(
        NullableKSerializedListPrimitive(
            datastore = datastore,
            key = key,
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
    @Deprecated(
        "This method is deprecated in favor of exportAsData and exportAsString for better type safety and flexibility.",
        replaceWith = ReplaceWith("exportAsData(exportPrivate, exportAppState)"),
        level = DeprecationLevel.WARNING,
    )
    override suspend fun export(exportPrivate: Boolean, exportAppState: Boolean): Map<String, JsonElement> {
        return datastore
            .data
            .first()
            .toPreferences()
            .asMap()
            .mapNotNull { (key, values) ->
                if (!exportPrivate && BasePreference.isPrivate(key.name)) {
                    null
                } else if (!exportAppState && BasePreference.isAppState(key.name)) {
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
    @Deprecated(
        "This method is deprecated in favor of importData and importDataAsString for better type safety and flexibility.",
        level = DeprecationLevel.WARNING,
    )
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
