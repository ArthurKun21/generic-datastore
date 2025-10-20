package io.github.arthurkun.generic.datastore

import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement

/**
 * Defines the contract for a preference data store.
 *
 * This interface provides methods to create and access various types of preferences.
 */
interface PreferenceDatastore {
    /**
     * Creates a String preference.
     *
     * @param key The preference key.
     * @param defaultValue The default String value (defaults to an empty string).
     * @return A [Prefs] instance for the String preference.
     */
    fun string(key: String, defaultValue: String = ""): Prefs<String>

    /**
     * Creates a Long preference.
     *
     * @param key The preference key.
     * @param defaultValue The default Long value (defaults to 0).
     * @return A [Prefs] instance for the Long preference.
     */
    fun long(key: String, defaultValue: Long = 0): Prefs<Long>

    /**
     * Creates an Int preference.
     *
     * @param key The preference key.
     * @param defaultValue The default Int value (defaults to 0).
     * @return A [Prefs] instance for the Int preference.
     */
    fun int(key: String, defaultValue: Int = 0): Prefs<Int>

    /**
     * Creates a Float preference.
     *
     * @param key The preference key.
     * @param defaultValue The default Float value (defaults to 0f).
     * @return A [Prefs] instance for the Float preference.
     */
    fun float(key: String, defaultValue: Float = 0f): Prefs<Float>

    /**
     * Creates a Boolean preference.
     *
     * @param key The preference key.
     * @param defaultValue The default Boolean value (defaults to false).
     * @return A [Prefs] instance for the Boolean preference.
     */
    fun bool(key: String, defaultValue: Boolean = false): Prefs<Boolean>

    /**
     * Creates a Set<String> preference.
     *
     * @param key The preference key.
     * @param defaultValue The default Set<String> value (defaults to an empty set).
     * @return A [Prefs] instance for the Set<String> preference.
     */
    fun stringSet(key: String, defaultValue: Set<String> = emptySet()): Prefs<Set<String>>

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
    fun <T> serialized(
        key: String,
        defaultValue: T,
        serializer: (T) -> String,
        deserializer: (String) -> T,
    ): Prefs<T>

    /**
     * Creates a preference for a custom object using kotlinx.serialization KSerializer.
     * The object is serialized to JSON and stored as a String in DataStore.
     * This provides type-safe serialization with compile-time validation.
     *
     * @param T The type of the custom object (must be serializable with kotlinx.serialization).
     * @param key The preference key.
     * @param defaultValue The default value for the custom object.
     * @param serializer The [KSerializer] for type T.
     * @param json Optional [Json] instance for customizing serialization (defaults to Json.Default).
     * @return A [Prefs] instance for the custom object preference.
     */
    fun <T> kserializer(
        key: String,
        defaultValue: T,
        serializer: KSerializer<T>,
        json: Json = Json.Default,
    ): Prefs<T>

    /**
     * Creates a preference for a list/collection of custom objects using kotlinx.serialization KSerializer.
     * Each element is serialized to JSON individually and the collection is stored as a Set<String> in DataStore.
     * This allows for efficient storage and retrieval of collections.
     *
     * @param T The type of elements in the collection (must be serializable with kotlinx.serialization).
     * @param key The preference key.
     * @param defaultValue The default list value.
     * @param serializer The [KSerializer] for type T.
     * @param json Optional [Json] instance for customizing serialization (defaults to Json.Default).
     * @return A [Prefs] instance for the list preference.
     */
    fun <T> kserializerList(
        key: String,
        defaultValue: List<T>,
        serializer: KSerializer<T>,
        json: Json = Json.Default,
    ): Prefs<List<T>>

    suspend fun export(
        exportPrivate: Boolean = false,
        exportAppState: Boolean = false,
    ): Map<String, JsonElement>

    suspend fun import(
        data: Map<String, Any>,
    )

    /**
     * Batch get operation for multiple preferences.
     * Retrieves values for multiple preferences in a single DataStore read operation.
     *
     * @param preferences List of preferences to retrieve values for
     * @return Map of preference keys to their current values
     */
    suspend fun <T> batchGet(preferences: List<Prefs<T>>): Map<String, T>

    /**
     * Batch set operation for multiple preferences.
     * Sets values for multiple preferences in a single DataStore write operation.
     * This is significantly more efficient than calling set() on each preference individually.
     *
     * @param updates Map of preferences to their new values
     */
    suspend fun batchSet(updates: Map<Prefs<*>, Any?>)

    /**
     * Batch delete operation for multiple preferences.
     * Deletes multiple preferences in a single DataStore write operation.
     *
     * @param preferences List of preferences to delete
     */
    suspend fun batchDelete(preferences: List<Prefs<*>>)
}
