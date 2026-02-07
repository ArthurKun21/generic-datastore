package io.github.arthurkun.generic.datastore.preferences

import io.github.arthurkun.generic.datastore.core.PreferenceDefaults.defaultJson
import io.github.arthurkun.generic.datastore.core.Prefs
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
    public fun string(key: String, defaultValue: String = ""): Prefs<String>

    /**
     * Creates a Long preference.
     *
     * @param key The preference key.
     * @param defaultValue The default Long value (defaults to 0).
     * @return A [Prefs] instance for the Long preference.
     */
    public fun long(key: String, defaultValue: Long = 0): Prefs<Long>

    /**
     * Creates an Int preference.
     *
     * @param key The preference key.
     * @param defaultValue The default Int value (defaults to 0).
     * @return A [Prefs] instance for the Int preference.
     */
    public fun int(key: String, defaultValue: Int = 0): Prefs<Int>

    /**
     * Creates a Float preference.
     *
     * @param key The preference key.
     * @param defaultValue The default Float value (defaults to 0f).
     * @return A [Prefs] instance for the Float preference.
     */
    public fun float(key: String, defaultValue: Float = 0f): Prefs<Float>

    /**
     * Creates a Double preference.
     *
     * @param key The preference key.
     * @param defaultValue The default Double value (defaults to 0.0).
     * @return A [Prefs] instance for the Double preference.
     */
    public fun double(key: String, defaultValue: Double = 0.0): Prefs<Double>

    /**
     * Creates a Boolean preference.
     *
     * @param key The preference key.
     * @param defaultValue The default Boolean value (defaults to false).
     * @return A [Prefs] instance for the Boolean preference.
     */
    public fun bool(key: String, defaultValue: Boolean = false): Prefs<Boolean>

    /**
     * Creates a Set<String> preference.
     *
     * @param key The preference key.
     * @param defaultValue The default Set<String> value (defaults to an empty set).
     * @return A [Prefs] instance for the Set<String> preference.
     */
    public fun stringSet(key: String, defaultValue: Set<String> = emptySet()): Prefs<Set<String>>

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
    ): Prefs<T>

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
    ): Prefs<Set<T>>

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
    public fun <T> kserialized(
        key: String,
        defaultValue: T,
        serializer: KSerializer<T>,
        json: Json = defaultJson,
    ): Prefs<T>

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
    public fun <T> kserializedSet(
        key: String,
        defaultValue: Set<T> = emptySet(),
        serializer: KSerializer<T>,
        json: Json = defaultJson,
    ): Prefs<Set<T>>

    /**
     * Clears all preferences stored in this datastore.
     *
     * After calling this, all preferences will return their default values.
     */
    public suspend fun clearAll()

    public suspend fun export(
        exportPrivate: Boolean = false,
        exportAppState: Boolean = false,
    ): Map<String, JsonElement>

    public suspend fun import(
        data: Map<String, Any>,
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
 * @param json The [Json] instance to use for serialization/deserialization.
 * @return A [Prefs] instance for the custom object preference.
 */
public inline fun <reified T> PreferencesDatastore.kserialized(
    key: String,
    defaultValue: T,
    json: Json = defaultJson,
): Prefs<T> = kserialized(
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
 * @param json The [Json] instance to use for serialization/deserialization.
 * @return A [Prefs] instance for the Set preference.
 */
public inline fun <reified T> PreferencesDatastore.kserializedSet(
    key: String,
    defaultValue: Set<T> = emptySet(),
    json: Json = defaultJson,
): Prefs<Set<T>> = kserializedSet(
    key = key,
    defaultValue = defaultValue,
    serializer = serializer<T>(),
    json = json,
)

/**
 * Toggles an item in a [Set] preference.
 *
 * If the set contains the item, it is removed; otherwise, it is added.
 * Works with [stringSet], [serializedSet], and [enumSet][PreferencesDatastore.enumSet] preferences.
 *
 * @param T The type of each element in the set.
 * @param item The item to toggle.
 */
public suspend inline fun <T> Prefs<Set<T>>.toggle(item: T) {
    update { current ->
        if (item in current) current - item else current + item
    }
}

/**
 * Toggles a [Boolean] preference.
 *
 * Flips the current value: `true` becomes `false`, and `false` becomes `true`.
 */
public suspend inline fun Prefs<Boolean>.toggle() {
    update { !it }
}
