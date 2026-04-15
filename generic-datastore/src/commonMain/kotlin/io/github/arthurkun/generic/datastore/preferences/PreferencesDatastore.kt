package io.github.arthurkun.generic.datastore.preferences

import io.github.arthurkun.generic.datastore.core.DelegatedPreference
import io.github.arthurkun.generic.datastore.core.PreferenceDefaults
import io.github.arthurkun.generic.datastore.preferences.backup.PreferencesBackup
import io.github.arthurkun.generic.datastore.preferences.batch.BatchReadScope
import io.github.arthurkun.generic.datastore.preferences.batch.BatchUpdateScope
import io.github.arthurkun.generic.datastore.preferences.batch.BatchWriteScope
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement


/**
 * Factory and utility contract for Preferences-backed [Preference] instances.
 *
 * Implementations expose primitive preferences, nullable variants, serializer-backed
 * preferences, batch operations, and backup import/export helpers over a single
 * `DataStore<Preferences>`.
 *
 * The `nullable*` APIs model absence explicitly: when a key is not stored they return `null`,
 * and writing `null` removes the key.
 */
public interface PreferencesDatastore {
    /**
     * Creates a String preference.
     *
     * @param key The preference key.
     * @param defaultValue The default String value (defaults to an empty string).
     * @return A [DelegatedPreference] instance for the String preference.
     */
    public fun string(key: String, defaultValue: String = ""): Preference<String>

    /**
     * Creates a Long preference.
     *
     * @param key The preference key.
     * @param defaultValue The default Long value (defaults to 0).
     * @return A [DelegatedPreference] instance for the Long preference.
     */
    public fun long(key: String, defaultValue: Long = 0): Preference<Long>

    /**
     * Creates an Int preference.
     *
     * @param key The preference key.
     * @param defaultValue The default Int value (defaults to 0).
     * @return A [DelegatedPreference] instance for the Int preference.
     */
    public fun int(key: String, defaultValue: Int = 0): Preference<Int>

    /**
     * Creates a Float preference.
     *
     * @param key The preference key.
     * @param defaultValue The default Float value (defaults to 0f).
     * @return A [DelegatedPreference] instance for the Float preference.
     */
    public fun float(key: String, defaultValue: Float = 0f): Preference<Float>

    /**
     * Creates a Double preference.
     *
     * @param key The preference key.
     * @param defaultValue The default Double value (defaults to 0.0).
     * @return A [DelegatedPreference] instance for the Double preference.
     */
    public fun double(key: String, defaultValue: Double = 0.0): Preference<Double>

    /**
     * Creates a Boolean preference.
     *
     * @param key The preference key.
     * @param defaultValue The default Boolean value (defaults to false).
     * @return A [DelegatedPreference] instance for the Boolean preference.
     */
    public fun bool(key: String, defaultValue: Boolean = false): Preference<Boolean>

    /**
     * Creates a nullable String preference.
     * Returns `null` when the key is not set in DataStore.
     *
     * @param key The preference key.
     * @return A [DelegatedPreference] instance for the nullable String preference.
     */
    public fun nullableString(key: String): Preference<String?>

    /**
     * Creates a nullable Set<String> preference.
     * Returns `null` when the key is not set in DataStore.
     *
     * @param key The preference key.
     * @return A [DelegatedPreference] instance for the nullable Set<String> preference.
     */
    public fun nullableStringSet(key: String): Preference<Set<String>?>

    /**
     * Creates a nullable Int preference.
     * Returns `null` when the key is not set in DataStore.
     *
     * @param key The preference key.
     * @return A [DelegatedPreference] instance for the nullable Int preference.
     */
    public fun nullableInt(key: String): Preference<Int?>

    /**
     * Creates a nullable Long preference.
     * Returns `null` when the key is not set in DataStore.
     *
     * @param key The preference key.
     * @return A [DelegatedPreference] instance for the nullable Long preference.
     */
    public fun nullableLong(key: String): Preference<Long?>

    /**
     * Creates a nullable Float preference.
     * Returns `null` when the key is not set in DataStore.
     *
     * @param key The preference key.
     * @return A [DelegatedPreference] instance for the nullable Float preference.
     */
    public fun nullableFloat(key: String): Preference<Float?>

    /**
     * Creates a nullable Double preference.
     * Returns `null` when the key is not set in DataStore.
     *
     * @param key The preference key.
     * @return A [DelegatedPreference] instance for the nullable Double preference.
     */
    public fun nullableDouble(key: String): Preference<Double?>

    /**
     * Creates a nullable Boolean preference.
     * Returns `null` when the key is not set in DataStore.
     *
     * @param key The preference key.
     * @return A [DelegatedPreference] instance for the nullable Boolean preference.
     */
    public fun nullableBool(key: String): Preference<Boolean?>

    /**
     * Creates a Set<String> preference.
     *
     * @param key The preference key.
     * @param defaultValue The default Set<String> value (defaults to an empty set).
     * @return A [DelegatedPreference] instance for the Set<String> preference.
     */
    public fun stringSet(key: String, defaultValue: Set<String> = emptySet()): Preference<Set<String>>

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
    public fun <T> serialized(
        key: String,
        defaultValue: T,
        serializer: (T) -> String,
        deserializer: (String) -> T,
    ): Preference<T>

    /**
     * Creates a preference for a [Set] of custom objects, stored using a string set preference key.
     * Each element is individually serialized to and deserialized from a String.
     *
     * @param T The type of each element in the set.
     * @param key The preference key.
     * @param defaultValue The default value for the set (defaults to an empty set).
     * @param serializer A function to serialize each element to a String.
     * @param deserializer A function to deserialize each String back to an element.
     * @return A [DelegatedPreference] instance for the Set preference.
     */
    public fun <T> serializedSet(
        key: String,
        defaultValue: Set<T> = emptySet(),
        serializer: (T) -> String,
        deserializer: (String) -> T,
    ): Preference<Set<T>>

    /**
     * Creates a preference for a custom object using Kotlin Serialization.
     * The object is encoded as a JSON string and stored under a string preference key.
     *
     * @param T The type of the custom object. Must be serializable using kotlinx.serialization.
     * @param key The preference key.
     * @param defaultValue The value returned when the key is missing or the stored payload cannot
     * be decoded.
     * @param serializer The [KSerializer] for the type [T].
     * @param json The [Json] configuration to use. Passing `null` lets the implementation choose
     * its configured default. [GenericPreferencesDatastore] uses [PreferenceDefaults.defaultJson]
     * unless it was constructed with a custom default.
     * @return A [DelegatedPreference] instance for the custom object preference.
     */
    public fun <T> kserialized(
        key: String,
        defaultValue: T,
        serializer: KSerializer<T>,
        json: Json? = null,
    ): Preference<T>

    /**
     * Creates a preference for a [Set] of custom objects using Kotlin Serialization.
     * Each element is encoded independently as JSON and stored in a string-set preference entry.
     *
     * Elements that fail to decode are skipped when the stored set is read back.
     *
     * @param T The type of each element in the set. Must be serializable using kotlinx.serialization.
     * @param key The preference key.
     * @param defaultValue The default value for the set (defaults to an empty set).
     * @param serializer The [KSerializer] for the type [T].
     * @param json The [Json] configuration to use. Passing `null` lets the implementation choose
     * its configured default. [GenericPreferencesDatastore] uses [PreferenceDefaults.defaultJson]
     * unless it was constructed with a custom default.
     * @return A [DelegatedPreference] instance for the Set preference.
     */
    public fun <T> kserializedSet(
        key: String,
        defaultValue: Set<T> = emptySet(),
        serializer: KSerializer<T>,
        json: Json? = null,
    ): Preference<Set<T>>

    /**
     * Creates a preference for a [List] of custom objects that can be serialized to and
     * deserialized from strings.
     *
     * The list is stored as a JSON array string inside a single string preference entry.
     * If the outer JSON array cannot be parsed the [defaultValue] is returned. Individual list
     * elements that fail to deserialize are skipped.
     *
     * @param T The type of each element in the list.
     * @param key The preference key.
     * @param defaultValue The default value for the list (defaults to an empty list).
     * @param serializer A function to serialize each element to a String.
     * @param deserializer A function to deserialize each String back to an element.
     * @return A [DelegatedPreference] instance for the List preference.
     */
    public fun <T> serializedList(
        key: String,
        defaultValue: List<T> = emptyList(),
        serializer: (T) -> String,
        deserializer: (String) -> T,
    ): Preference<List<T>>

    /**
     * Creates a preference for a [List] of custom objects using Kotlin Serialization.
     * The entire list is encoded as a JSON array string inside a single string preference entry.
     *
     * @param T The type of each element in the list. Must be serializable using kotlinx.serialization.
     * @param key The preference key.
     * @param defaultValue The default value for the list (defaults to an empty list).
     * @param serializer The [KSerializer] for the type [T].
     * @param json The [Json] configuration to use. Passing `null` lets the implementation choose
     * its configured default. [GenericPreferencesDatastore] uses [PreferenceDefaults.defaultJson]
     * unless it was constructed with a custom default.
     * @return A [DelegatedPreference] instance for the List preference.
     */
    public fun <T> kserializedList(
        key: String,
        defaultValue: List<T> = emptyList(),
        serializer: KSerializer<T>,
        json: Json? = null,
    ): Preference<List<T>>

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
     * @return A [DelegatedPreference] instance for the nullable custom object preference.
     */
    public fun <T : Any> nullableSerialized(
        key: String,
        serializer: (T) -> String,
        deserializer: (String) -> T,
    ): Preference<T?>

    /**
     * Creates a nullable preference for a custom object using Kotlin Serialization.
     * The object is encoded as JSON inside a string preference entry.
     *
     * Returns `null` when the key is not set in DataStore. Setting `null` removes the key.
     * If deserialization fails, `null` is returned instead of throwing.
     *
     * @param T The non-null type of the custom object. Must be serializable using kotlinx.serialization.
     * @param key The preference key.
     * @param serializer The [KSerializer] for the type [T].
     * @param json The [Json] configuration to use. Passing `null` lets the implementation choose
     * its configured default. [GenericPreferencesDatastore] uses [PreferenceDefaults.defaultJson]
     * unless it was constructed with a custom default.
     * @return A [DelegatedPreference] instance for the nullable custom object preference.
     */
    public fun <T : Any> nullableKserialized(
        key: String,
        serializer: KSerializer<T>,
        json: Json? = null,
    ): Preference<T?>

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
     * @return A [DelegatedPreference] instance for the nullable List preference.
     */
    public fun <T> nullableSerializedList(
        key: String,
        serializer: (T) -> String,
        deserializer: (String) -> T,
    ): Preference<List<T>?>

    /**
     * Creates a nullable preference for a [List] of custom objects using Kotlin Serialization.
     * The list is encoded as a JSON array string inside a string preference entry.
     *
     * Returns `null` when the key is not set in DataStore. Setting `null` removes the key.
     * If deserialization fails, `null` is returned instead of throwing.
     *
     * @param T The type of each element in the list. Must be serializable using kotlinx.serialization.
     * @param key The preference key.
     * @param serializer The [KSerializer] for the type [T].
     * @param json The [Json] configuration to use. Passing `null` lets the implementation choose
     * its configured default. [GenericPreferencesDatastore] uses [PreferenceDefaults.defaultJson]
     * unless it was constructed with a custom default.
     * @return A [DelegatedPreference] instance for the nullable List preference.
     */
    public fun <T> nullableKserializedList(
        key: String,
        serializer: KSerializer<T>,
        json: Json? = null,
    ): Preference<List<T>?>

    /**
     * Returns a [Flow] that re-runs [block] against a shared [BatchReadScope] snapshot on every
     * datastore change.
     *
     * All reads inside [block] observe the same snapshot, which avoids mixing values from
     * different emissions.
     *
     * @param R The return type of the block.
     * @param block A lambda with [BatchReadScope] receiver that reads one or more preferences.
     * @return A [Flow] emitting the value returned by [block] on every update.
     */
    public fun <R> batchReadFlow(block: BatchReadScope.() -> R): Flow<R>

    /**
     * One-shot batch read: collects the latest DataStore snapshot and executes [block]
     * within a [BatchReadScope].
     *
     * @param R The return type of the block.
     * @param block A lambda with [BatchReadScope] receiver that reads one or more preferences.
     * @return The value returned by [block].
     */
    public suspend fun <R> batchGet(block: BatchReadScope.() -> R): R

    /**
     * Executes [block] inside a single DataStore `edit` transaction.
     *
     * All [BatchWriteScope.set], [BatchWriteScope.delete], and [BatchWriteScope.resetToDefault]
     * calls share the same mutable transaction state.
     *
     * @param block A lambda with [BatchWriteScope] receiver that writes one or more preferences.
     */
    public suspend fun batchWrite(block: BatchWriteScope.() -> Unit)

    /**
     * Atomically reads and writes multiple preferences in a single DataStore `edit`
     * transaction.
     *
     * Reads through [BatchUpdateScope.get] observe earlier writes made through
     * [BatchUpdateScope.set] in the same block.
     *
     * @param block A lambda with [BatchUpdateScope] receiver that reads and writes preferences.
     */
    public suspend fun batchUpdate(block: BatchUpdateScope.() -> Unit)

    /**
     * Blocking variant of [batchGet].
     *
     * @param R The return type of the block.
     * @param block A lambda with [BatchReadScope] receiver.
     * @return The value returned by [block].
     */
    public fun <R> batchGetBlocking(block: BatchReadScope.() -> R): R

    /**
     * Blocking variant of [batchWrite].
     *
     * @param block A lambda with [BatchWriteScope] receiver.
     */
    public fun batchWriteBlocking(block: BatchWriteScope.() -> Unit)

    /**
     * Blocking variant of [batchUpdate].
     *
     * @param block A lambda with [BatchUpdateScope] receiver.
     */
    public fun batchUpdateBlocking(block: BatchUpdateScope.() -> Unit)

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
     * Reads current datastore contents and returns a [PreferencesBackup] snapshot.
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
