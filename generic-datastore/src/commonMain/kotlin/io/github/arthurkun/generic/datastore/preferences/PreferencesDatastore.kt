package io.github.arthurkun.generic.datastore.preferences

import io.github.arthurkun.generic.datastore.core.PreferenceDefaults
import io.github.arthurkun.generic.datastore.core.Prefs
import io.github.arthurkun.generic.datastore.preferences.backup.PreferencesBackup
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.serializer

/**
 * Defines the contract for a preference data store.
 *
 * This interface provides methods to create and access various types of preferences.
 */
public interface PreferencesDatastore {
    /**
     * Creates a String preference.
     *
     * @param key The preference key.
     * @param defaultValue The default String value (defaults to an empty string).
     * @return A [Prefs] instance for the String preference.
     */
    public fun string(key: String, defaultValue: String = ""): DatastorePreferenceItem<String>

    /**
     * Creates a Long preference.
     *
     * @param key The preference key.
     * @param defaultValue The default Long value (defaults to 0).
     * @return A [Prefs] instance for the Long preference.
     */
    public fun long(key: String, defaultValue: Long = 0): DatastorePreferenceItem<Long>

    /**
     * Creates an Int preference.
     *
     * @param key The preference key.
     * @param defaultValue The default Int value (defaults to 0).
     * @return A [Prefs] instance for the Int preference.
     */
    public fun int(key: String, defaultValue: Int = 0): DatastorePreferenceItem<Int>

    /**
     * Creates a Float preference.
     *
     * @param key The preference key.
     * @param defaultValue The default Float value (defaults to 0f).
     * @return A [Prefs] instance for the Float preference.
     */
    public fun float(key: String, defaultValue: Float = 0f): DatastorePreferenceItem<Float>

    /**
     * Creates a Double preference.
     *
     * @param key The preference key.
     * @param defaultValue The default Double value (defaults to 0.0).
     * @return A [Prefs] instance for the Double preference.
     */
    public fun double(key: String, defaultValue: Double = 0.0): DatastorePreferenceItem<Double>

    /**
     * Creates a Boolean preference.
     *
     * @param key The preference key.
     * @param defaultValue The default Boolean value (defaults to false).
     * @return A [Prefs] instance for the Boolean preference.
     */
    public fun bool(key: String, defaultValue: Boolean = false): DatastorePreferenceItem<Boolean>

    /**
     * Creates a nullable String preference.
     * Returns `null` when the key is not set in DataStore.
     *
     * @param key The preference key.
     * @return A [Prefs] instance for the nullable String preference.
     */
    public fun nullableString(key: String): DatastorePreferenceItem<String?>

    /**
     * Creates a nullable Set<String> preference.
     * Returns `null` when the key is not set in DataStore.
     *
     * @param key The preference key.
     * @return A [Prefs] instance for the nullable Set<String> preference.
     */
    public fun nullableStringSet(key: String): DatastorePreferenceItem<Set<String>?>

    /**
     * Creates a nullable Int preference.
     * Returns `null` when the key is not set in DataStore.
     *
     * @param key The preference key.
     * @return A [Prefs] instance for the nullable Int preference.
     */
    public fun nullableInt(key: String): DatastorePreferenceItem<Int?>

    /**
     * Creates a nullable Long preference.
     * Returns `null` when the key is not set in DataStore.
     *
     * @param key The preference key.
     * @return A [Prefs] instance for the nullable Long preference.
     */
    public fun nullableLong(key: String): DatastorePreferenceItem<Long?>

    /**
     * Creates a nullable Float preference.
     * Returns `null` when the key is not set in DataStore.
     *
     * @param key The preference key.
     * @return A [Prefs] instance for the nullable Float preference.
     */
    public fun nullableFloat(key: String): DatastorePreferenceItem<Float?>

    /**
     * Creates a nullable Double preference.
     * Returns `null` when the key is not set in DataStore.
     *
     * @param key The preference key.
     * @return A [Prefs] instance for the nullable Double preference.
     */
    public fun nullableDouble(key: String): DatastorePreferenceItem<Double?>

    /**
     * Creates a nullable Boolean preference.
     * Returns `null` when the key is not set in DataStore.
     *
     * @param key The preference key.
     * @return A [Prefs] instance for the nullable Boolean preference.
     */
    public fun nullableBool(key: String): DatastorePreferenceItem<Boolean?>

    /**
     * Creates a Set<String> preference.
     *
     * @param key The preference key.
     * @param defaultValue The default Set<String> value (defaults to an empty set).
     * @return A [Prefs] instance for the Set<String> preference.
     */
    public fun stringSet(key: String, defaultValue: Set<String> = emptySet()): DatastorePreferenceItem<Set<String>>

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
    public fun <T> serialized(
        key: String,
        defaultValue: T,
        serializer: (T) -> String,
        deserializer: (String) -> T,
    ): DatastorePreferenceItem<T>

    /**
     * Creates a preference for a [Set] of custom objects, stored using a string set preference key.
     * Each element is individually serialized to and deserialized from a String.
     *
     * @param T The type of each element in the set.
     * @param key The preference key.
     * @param defaultValue The default value for the set (defaults to an empty set).
     * @param serializer A function to serialize each element to a String.
     * @param deserializer A function to deserialize each String back to an element.
     * @return A [Prefs] instance for the Set preference.
     */
    public fun <T> serializedSet(
        key: String,
        defaultValue: Set<T> = emptySet(),
        serializer: (T) -> String,
        deserializer: (String) -> T,
    ): DatastorePreferenceItem<Set<T>>

    /**
     * Creates a preference for a custom object using Kotlin Serialization.
     * The object is serialized to JSON for storage.
     *
     * @param T The type of the custom object. Must be serializable using kotlinx.serialization.
     * @param key The preference key.
     * @param defaultValue The default value for the custom object.
     * @param serializer The [KSerializer] for the type [T].
     * @param json The [Json] instance to use for serialization/deserialization. Defaults to [PreferenceDefaults.defaultJson] from the library if not provided.
     * @return A [Prefs] instance for the custom object preference.
     */
    public fun <T> kserialized(
        key: String,
        defaultValue: T,
        serializer: KSerializer<T>,
        json: Json? = null,
    ): DatastorePreferenceItem<T>

    /**
     * Creates a preference for a [Set] of custom objects using Kotlin Serialization.
     * Each element is serialized to JSON for storage using [stringSetPreferencesKey].
     *
     * @param T The type of each element in the set. Must be serializable using kotlinx.serialization.
     * @param key The preference key.
     * @param defaultValue The default value for the set (defaults to an empty set).
     * @param serializer The [KSerializer] for the type [T].
     * @param json The [Json] instance to use for serialization/deserialization. Defaults to [PreferenceDefaults.defaultJson] from the library if not provided.
     * @return A [Prefs] instance for the Set preference.
     */
    public fun <T> kserializedSet(
        key: String,
        defaultValue: Set<T> = emptySet(),
        serializer: KSerializer<T>,
        json: Json? = null,
    ): DatastorePreferenceItem<Set<T>>

    /**
     * Creates a preference for a [List] of custom objects that can be serialized to and
     * deserialized from Strings. The list is stored as a JSON array string using
     * [stringPreferencesKey].
     *
     * @param T The type of each element in the list.
     * @param key The preference key.
     * @param defaultValue The default value for the list (defaults to an empty list).
     * @param serializer A function to serialize each element to a String.
     * @param deserializer A function to deserialize each String back to an element.
     * @return A [Prefs] instance for the List preference.
     */
    public fun <T> serializedList(
        key: String,
        defaultValue: List<T> = emptyList(),
        serializer: (T) -> String,
        deserializer: (String) -> T,
    ): DatastorePreferenceItem<List<T>>

    /**
     * Creates a preference for a [List] of custom objects using Kotlin Serialization.
     * The list is serialized to a JSON array string for storage using [stringPreferencesKey].
     *
     * @param T The type of each element in the list. Must be serializable using kotlinx.serialization.
     * @param key The preference key.
     * @param defaultValue The default value for the list (defaults to an empty list).
     * @param serializer The [KSerializer] for the type [T].
     * @param json The [Json] instance to use for serialization/deserialization. Defaults to [PreferenceDefaults.defaultJson] from the library if not provided.
     * @return A [Prefs] instance for the List preference.
     */
    public fun <T> kserializedList(
        key: String,
        defaultValue: List<T> = emptyList(),
        serializer: KSerializer<T>,
        json: Json? = null,
    ): DatastorePreferenceItem<List<T>>

    /**
     * Creates a nullable preference for a custom object that can be serialized to and
     * deserialized from a String.
     *
     * Returns `null` when the key is not set in DataStore. Setting `null` removes the key.
     * If deserialization fails, `null` is returned.
     *
     * @param T The non-null type of the custom object.
     * @param key The preference key.
     * @param serializer A function to serialize the object to a String.
     * @param deserializer A function to deserialize the String back to the object.
     * @return A [Prefs] instance for the nullable custom object preference.
     */
    public fun <T : Any> nullableSerialized(
        key: String,
        serializer: (T) -> String,
        deserializer: (String) -> T,
    ): DatastorePreferenceItem<T?>

    /**
     * Creates a nullable preference for a custom object using Kotlin Serialization.
     * The object is serialized to JSON for storage.
     *
     * Returns `null` when the key is not set in DataStore. Setting `null` removes the key.
     * If deserialization fails, `null` is returned.
     *
     * @param T The non-null type of the custom object. Must be serializable using kotlinx.serialization.
     * @param key The preference key.
     * @param serializer The [KSerializer] for the type [T].
     * @param json The [Json] instance to use for serialization/deserialization. Defaults to [PreferenceDefaults.defaultJson] from the library if not provided.
     * @return A [Prefs] instance for the nullable custom object preference.
     */
    public fun <T : Any> nullableKserialized(
        key: String,
        serializer: KSerializer<T>,
        json: Json? = null,
    ): DatastorePreferenceItem<T?>

    /**
     * Creates a nullable preference for a [List] of custom objects that can be serialized
     * to and deserialized from Strings. The list is stored as a JSON array string.
     *
     * Returns `null` when the key is not set in DataStore. Setting `null` removes the key.
     * If deserialization fails, `null` is returned.
     *
     * @param T The type of each element in the list.
     * @param key The preference key.
     * @param serializer A function to serialize each element to a String.
     * @param deserializer A function to deserialize each String back to an element.
     * @return A [Prefs] instance for the nullable List preference.
     */
    public fun <T> nullableSerializedList(
        key: String,
        serializer: (T) -> String,
        deserializer: (String) -> T,
    ): DatastorePreferenceItem<List<T>?>

    /**
     * Creates a nullable preference for a [List] of custom objects using Kotlin Serialization.
     * The list is serialized to a JSON array string for storage.
     *
     * Returns `null` when the key is not set in DataStore. Setting `null` removes the key.
     * If deserialization fails, `null` is returned.
     *
     * @param T The type of each element in the list. Must be serializable using kotlinx.serialization.
     * @param key The preference key.
     * @param serializer The [KSerializer] for the type [T].
     * @param json The [Json] instance to use for serialization/deserialization. Defaults to [PreferenceDefaults.defaultJson] from the library if not provided.
     * @return A [Prefs] instance for the nullable List preference.
     */
    public fun <T> nullableKserializedList(
        key: String,
        serializer: KSerializer<T>,
        json: Json? = null,
    ): DatastorePreferenceItem<List<T>?>

    /**
     * Clears all preferences stored in this datastore.
     *
     * After calling this, all preferences will return their default values.
     */
    public suspend fun clearAll()

    @Deprecated(
        message = "This method is deprecated in favor of exportAsData and exportAsString " +
            "for better type safety and flexibility.",
        replaceWith = ReplaceWith("exportAsData(exportPrivate, exportAppState)"),
        level = DeprecationLevel.WARNING,
    )
    public suspend fun export(
        exportPrivate: Boolean = false,
        exportAppState: Boolean = false,
    ): Map<String, JsonElement>

    @Deprecated(
        message = "This method is deprecated in favor of importData and importDataAsString " +
            "for better type safety and flexibility.",
        level = DeprecationLevel.WARNING,
    )
    public suspend fun import(
        data: Map<String, Any>,
    )

    /**
     * Reads current DataStore preferences and returns a [PreferencesBackup] snapshot.
     *
     * Only keys currently set in DataStore are included. Keys marked private
     * (prefixed `__PRIVATE_`) are excluded unless [exportPrivate] is true.
     * Keys marked app state (prefixed `__APP_STATE_`) are excluded unless
     * [exportAppState] is true.
     *
     * @param exportPrivate Whether to include private keys in the backup.
     * @param exportAppState Whether to include app-state keys in the backup.
     * @return A [PreferencesBackup] containing the exported preferences.
     */
    public suspend fun exportAsData(
        exportPrivate: Boolean = false,
        exportAppState: Boolean = false,
    ): PreferencesBackup

    /**
     * Exports current DataStore preferences as a JSON string.
     *
     * Serializes the backup produced by [exportAsData] using the provided [json]
     * instance or [PreferenceDefaults.defaultJson] if not provided.
     *
     * @param exportPrivate Whether to include private keys in the backup.
     * @param exportAppState Whether to include app-state keys in the backup.
     * @param json The [Json] instance to use for serialization. Defaults to [PreferenceDefaults.defaultJson] from the library if not provided.
     * @return A JSON-encoded string representing the exported preferences.
     * @throws kotlinx.serialization.SerializationException if serialization fails.
     */
    public suspend fun exportAsString(
        exportPrivate: Boolean = false,
        exportAppState: Boolean = false,
        json: Json? = null,
    ): String

    /**
     * Applies a [PreferencesBackup] into DataStore in a single transaction.
     *
     * Overwrites existing keys present in the backup but does **not** remove keys
     * absent from the backup. Call [clearAll] before import for full replace semantics.
     * Keys marked private (prefixed `__PRIVATE_`) are skipped unless [importPrivate]
     * is true. Keys marked app state (prefixed `__APP_STATE_`) are skipped unless
     * [importAppState] is true.
     *
     * @param backup The [PreferencesBackup] to import.
     * @param importPrivate Whether to import private keys from the backup.
     * @param importAppState Whether to import app-state keys from the backup.
     */
    public suspend fun importData(
        backup: PreferencesBackup,
        importPrivate: Boolean = false,
        importAppState: Boolean = false,
    )

    /**
     * Decodes a JSON string into a [PreferencesBackup] and imports it via [importData].
     *
     * Uses the provided [json] instance or [PreferenceDefaults.defaultJson] if not
     * provided for deserialization.
     *
     * @param backupString The JSON-encoded backup string to import.
     * @param importPrivate Whether to import private keys from the backup.
     * @param importAppState Whether to import app-state keys from the backup.
     * @param json The [Json] instance to use for deserialization. Defaults to [PreferenceDefaults.defaultJson] from the library if not provided.
     * @throws kotlinx.serialization.SerializationException if the JSON string is invalid.
     */
    public suspend fun importDataAsString(
        backupString: String,
        importPrivate: Boolean = false,
        importAppState: Boolean = false,
        json: Json? = null,
    )
}

/**
 * Creates a preference for a custom object using Kotlin Serialization,
 * inferring the [KSerializer] from the reified type parameter.
 *
 * The type [T] must be annotated with [kotlinx.serialization.Serializable].
 *
 * @param T The type of the custom object.
 * @param key The preference key.
 * @param defaultValue The default value for the custom object.
 * @param json The [Json] instance to use for serialization/deserialization. Defaults to [PreferenceDefaults.defaultJson] from the library if not provided.
 * @return A [Prefs] instance for the custom object preference.
 */
public inline fun <reified T> PreferencesDatastore.kserialized(
    key: String,
    defaultValue: T,
    json: Json? = null,
): DatastorePreferenceItem<T> = kserialized(
    key = key,
    defaultValue = defaultValue,
    serializer = serializer<T>(),
    json = json,
)

/**
 * Creates a preference for a [Set] of custom objects using Kotlin Serialization,
 * inferring the [KSerializer] from the reified type parameter.
 *
 * The type [T] must be annotated with [kotlinx.serialization.Serializable].
 *
 * @param T The type of each element in the set.
 * @param key The preference key.
 * @param defaultValue The default value for the set (defaults to an empty set).
 * @param json The [Json] instance to use for serialization/deserialization. Defaults to [PreferenceDefaults.defaultJson] from the library if not provided.
 * @return A [Prefs] instance for the Set preference.
 */
public inline fun <reified T> PreferencesDatastore.kserializedSet(
    key: String,
    defaultValue: Set<T> = emptySet(),
    json: Json? = null,
): DatastorePreferenceItem<Set<T>> = kserializedSet(
    key = key,
    defaultValue = defaultValue,
    serializer = serializer<T>(),
    json = json,
)

/**
 * Creates a preference for a [List] of custom objects using Kotlin Serialization,
 * inferring the [KSerializer] from the reified type parameter.
 *
 * The type [T] must be annotated with [kotlinx.serialization.Serializable].
 *
 * @param T The type of each element in the list.
 * @param key The preference key.
 * @param defaultValue The default value for the list (defaults to an empty list).
 * @param json The [Json] instance to use for serialization/deserialization. Defaults to [PreferenceDefaults.defaultJson] from the library if not provided.
 * @return A [Prefs] instance for the List preference.
 */
public inline fun <reified T> PreferencesDatastore.kserializedList(
    key: String,
    defaultValue: List<T> = emptyList(),
    json: Json? = null,
): DatastorePreferenceItem<List<T>> = kserializedList(
    key = key,
    defaultValue = defaultValue,
    serializer = serializer<T>(),
    json = json,
)

/**
 * Creates a nullable preference for a custom object using Kotlin Serialization,
 * inferring the [KSerializer] from the reified type parameter.
 *
 * The type [T] must be annotated with [kotlinx.serialization.Serializable].
 *
 * @param T The non-null type of the custom object.
 * @param key The preference key.
 * @param json The [Json] instance to use for serialization/deserialization. Defaults to [PreferenceDefaults.defaultJson] from the library if not provided.
 * @return A [Prefs] instance for the nullable custom object preference.
 */
public inline fun <reified T : Any> PreferencesDatastore.nullableKserialized(
    key: String,
    json: Json? = null,
): DatastorePreferenceItem<T?> = nullableKserialized(
    key = key,
    serializer = serializer<T>(),
    json = json,
)

/**
 * Creates a nullable preference for a [List] of custom objects using Kotlin Serialization,
 * inferring the [KSerializer] from the reified type parameter.
 *
 * The type [T] must be annotated with [kotlinx.serialization.Serializable].
 *
 * @param T The type of each element in the list. Must be serializable using kotlinx.serialization.
 * @param key The preference key.
 * @param json The [Json] instance to use for serialization/deserialization. Defaults to [PreferenceDefaults.defaultJson] from the library if not provided.
 * @return A [Prefs] instance for the nullable List preference.
 */
public inline fun <reified T> PreferencesDatastore.nullableKserializedList(
    key: String,
    json: Json? = null,
): DatastorePreferenceItem<List<T>?> = nullableKserializedList(
    key = key,
    serializer = serializer<T>(),
    json = json,
)

/**
 * Toggles an item in a [Set] preference.
 *
 * If the set contains the item, it is removed; otherwise, it is added.
 * Works with [stringSet], [serializedSet], and [enumSet][io.github.arthurkun.generic.datastore.preferences.core.customSet.enumSet] preferences.
 *
 * @param T The type of each element in the set.
 * @param item The item to toggle.
 */
public suspend inline fun <T> DatastorePreferenceItem<Set<T>>.toggle(item: T) {
    update { current ->
        if (item in current) current - item else current + item
    }
}

/**
 * Toggles a [Boolean] preference.
 *
 * Flips the current value: `true` becomes `false`, and `false` becomes `true`.
 */
public suspend inline fun DatastorePreferenceItem<Boolean>.toggle() {
    update { !it }
}
