package io.github.arthurkun.generic.datastore.preferences

import io.github.arthurkun.generic.datastore.core.Prefs
import kotlinx.serialization.json.JsonElement

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

    public suspend fun export(
        exportPrivate: Boolean = false,
        exportAppState: Boolean = false,
    ): Map<String, JsonElement>

    public suspend fun import(
        data: Map<String, Any>,
    )
}
