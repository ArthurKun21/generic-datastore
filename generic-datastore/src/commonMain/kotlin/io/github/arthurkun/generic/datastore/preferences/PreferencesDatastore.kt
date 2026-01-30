package io.github.arthurkun.generic.datastore.preferences

import io.github.arthurkun.generic.datastore.backup.BackupPreference
import io.github.arthurkun.generic.datastore.core.Prefs

/**
 * Defines the contract for a preference data store.
 *
 * This interface provides methods to create and access various types of preferences.
 */
interface PreferencesDatastore {
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
     * Exports all preferences as a list of [BackupPreference] objects.
     *
     * @param exportPrivate Whether to include private preferences (keys starting with __PRIVATE_).
     * @param exportAppState Whether to include app state preferences (keys starting with __APP_STATE_).
     * @return A list of [BackupPreference] containing all exported preferences.
     */
    suspend fun export(
        exportPrivate: Boolean = false,
        exportAppState: Boolean = false,
    ): List<BackupPreference>

    /**
     * Imports preferences from a list of [BackupPreference] objects.
     *
     * @param backupPreferences The list of preferences to import.
     */
    suspend fun import(backupPreferences: List<BackupPreference>)
}
