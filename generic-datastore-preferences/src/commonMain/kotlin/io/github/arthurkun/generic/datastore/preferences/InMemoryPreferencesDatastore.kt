package io.github.arthurkun.generic.datastore.preferences

import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json

/**
 *
 * Memory-backed variants of every factory above. They behave identically except that
 * values live in a process-local in-memory store: they are never persisted and are
 * excluded from batch operations, backups, and [clearAll]. The store is scoped to this
 * datastore instance, so two datastore instances never share in-memory values.
 *
 * The `*InMemory` factory variants mirror every factory but store values in a process-local
 * in-memory store: identical read/write semantics, never persisted, and excluded from batch
 * operations, backups, and [clearAll].
 */
public interface InMemoryPreferencesDatastore {
    /**
     * Creates a memory-backed String preference.
     *
     * Behaves exactly like [string] except the value is stored in a process-local in-memory
     * store: it is never persisted and is excluded from batch operations, backups, and
     * [clearAll].
     *
     * @param key The preference key.
     * @param defaultValue The default String value (defaults to an empty string).
     * @return A [Preference] instance for the in-memory String preference.
     */
    public fun stringInMemory(key: String, defaultValue: String = ""): Preference<String>

    /**
     * Creates a memory-backed Long preference.
     *
     * Behaves exactly like [PreferencesDatastore.long] except the value is stored in a process-local in-memory
     * store: it is never persisted and is excluded from batch operations, backups, and
     * [PreferencesDatastore.clearAll].
     *
     * @param key The preference key.
     * @param defaultValue The default Long value (defaults to 0).
     * @return A [Preference] instance for the in-memory Long preference.
     */
    public fun longInMemory(key: String, defaultValue: Long = 0): Preference<Long>

    /**
     * Creates a memory-backed Int preference.
     *
     * Behaves exactly like [PreferencesDatastore.int] except the value is stored in a process-local in-memory
     * store: it is never persisted and is excluded from batch operations, backups, and
     * [PreferencesDatastore.clearAll].
     *
     * @param key The preference key.
     * @param defaultValue The default Int value (defaults to 0).
     * @return A [Preference] instance for the in-memory Int preference.
     */
    public fun intInMemory(key: String, defaultValue: Int = 0): Preference<Int>

    /**
     * Creates a memory-backed Float preference.
     *
     * Behaves exactly like [PreferencesDatastore.float] except the value is stored in a process-local in-memory
     * store: it is never persisted and is excluded from batch operations, backups, and
     * [PreferencesDatastore.clearAll].
     *
     * @param key The preference key.
     * @param defaultValue The default Float value (defaults to 0f).
     * @return A [Preference] instance for the in-memory Float preference.
     */
    public fun floatInMemory(key: String, defaultValue: Float = 0f): Preference<Float>

    /**
     * Creates a memory-backed Double preference.
     *
     * Behaves exactly like [PreferencesDatastore.double] except the value is stored in a process-local in-memory
     * store: it is never persisted and is excluded from batch operations, backups, and
     * [PreferencesDatastore.clearAll].
     *
     * @param key The preference key.
     * @param defaultValue The default Double value (defaults to 0.0).
     * @return A [Preference] instance for the in-memory Double preference.
     */
    public fun doubleInMemory(key: String, defaultValue: Double = 0.0): Preference<Double>

    /**
     * Creates a memory-backed Boolean preference.
     *
     * Behaves exactly like [PreferencesDatastore.bool] except the value is stored in a process-local in-memory
     * store: it is never persisted and is excluded from batch operations, backups, and
     * [PreferencesDatastore.clearAll].
     *
     * @param key The preference key.
     * @param defaultValue The default Boolean value (defaults to false).
     * @return A [Preference] instance for the in-memory Boolean preference.
     */
    public fun boolInMemory(key: String, defaultValue: Boolean = false): Preference<Boolean>

    /**
     * Creates a memory-backed nullable String preference.
     *
     * Behaves exactly like [PreferencesDatastore.nullableString] except the value is stored in a process-local
     * in-memory store: it is never persisted and is excluded from batch operations, backups,
     * and [PreferencesDatastore.clearAll].
     *
     * @param key The preference key.
     * @return A [Preference] instance for the in-memory nullable String preference.
     */
    public fun nullableStringInMemory(key: String): Preference<String?>

    /**
     * Creates a memory-backed nullable Set<String> preference.
     *
     * Behaves exactly like [PreferencesDatastore.nullableStringSet] except the value is stored in a process-local
     * in-memory store: it is never persisted and is excluded from batch operations, backups,
     * and [PreferencesDatastore.clearAll].
     *
     * @param key The preference key.
     * @return A [Preference] instance for the in-memory nullable Set<String> preference.
     */
    public fun nullableStringSetInMemory(key: String): Preference<Set<String>?>

    /**
     * Creates a memory-backed `List<String>` preference stored as a JSON array string.
     *
     * Behaves exactly like [PreferencesDatastore.stringList] except the value is stored in a process-local
     * in-memory store: it is never persisted and is excluded from batch operations, backups,
     * and [PreferencesDatastore.clearAll].
     *
     * @param key The preference key.
     * @param defaultValue The default List<String> value (defaults to an empty list).
     * @return A [Preference] instance for the in-memory List<String> preference.
     */
    public fun stringListInMemory(key: String, defaultValue: List<String> = emptyList()): Preference<List<String>>

    /**
     * Creates a memory-backed nullable `List<String>` preference.
     *
     * Behaves exactly like [PreferencesDatastore.nullableStringList] except the value is stored in a process-local
     * in-memory store: it is never persisted and is excluded from batch operations, backups,
     * and [PreferencesDatastore.clearAll].
     *
     * @param key The preference key.
     * @return A [Preference] instance for the in-memory nullable List<String> preference.
     */
    public fun nullableStringListInMemory(key: String): Preference<List<String>?>

    /**
     * Creates a memory-backed nullable Int preference.
     *
     * Behaves exactly like [PreferencesDatastore.nullableInt] except the value is stored in a process-local
     * in-memory store: it is never persisted and is excluded from batch operations, backups,
     * and [PreferencesDatastore.clearAll].
     *
     * @param key The preference key.
     * @return A [Preference] instance for the in-memory nullable Int preference.
     */
    public fun nullableIntInMemory(key: String): Preference<Int?>

    /**
     * Creates a memory-backed nullable Long preference.
     *
     * Behaves exactly like [PreferencesDatastore.nullableLong] except the value is stored in a process-local
     * in-memory store: it is never persisted and is excluded from batch operations, backups,
     * and [PreferencesDatastore.clearAll].
     *
     * @param key The preference key.
     * @return A [Preference] instance for the in-memory nullable Long preference.
     */
    public fun nullableLongInMemory(key: String): Preference<Long?>

    /**
     * Creates a memory-backed nullable Float preference.
     *
     * Behaves exactly like [PreferencesDatastore.nullableFloat] except the value is stored in a process-local
     * in-memory store: it is never persisted and is excluded from batch operations, backups,
     * and [PreferencesDatastore.clearAll].
     *
     * @param key The preference key.
     * @return A [Preference] instance for the in-memory nullable Float preference.
     */
    public fun nullableFloatInMemory(key: String): Preference<Float?>

    /**
     * Creates a memory-backed nullable Double preference.
     *
     * Behaves exactly like [PreferencesDatastore.nullableDouble] except the value is stored in a process-local
     * in-memory store: it is never persisted and is excluded from batch operations, backups,
     * and [PreferencesDatastore.clearAll].
     *
     * @param key The preference key.
     * @return A [Preference] instance for the in-memory nullable Double preference.
     */
    public fun nullableDoubleInMemory(key: String): Preference<Double?>

    /**
     * Creates a memory-backed nullable Boolean preference.
     *
     * Behaves exactly like [PreferencesDatastore.nullableBool] except the value is stored in a process-local
     * in-memory store: it is never persisted and is excluded from batch operations, backups,
     * and [PreferencesDatastore.clearAll].
     *
     * @param key The preference key.
     * @return A [Preference] instance for the in-memory nullable Boolean preference.
     */
    public fun nullableBoolInMemory(key: String): Preference<Boolean?>

    /**
     * Creates a memory-backed Set<String> preference.
     *
     * Behaves exactly like [PreferencesDatastore.stringSet] except the value is stored in a process-local
     * in-memory store: it is never persisted and is excluded from batch operations, backups,
     * and [PreferencesDatastore.clearAll].
     *
     * @param key The preference key.
     * @param defaultValue The default Set<String> value (defaults to an empty set).
     * @return A [Preference] instance for the in-memory Set<String> preference.
     */
    public fun stringSetInMemory(key: String, defaultValue: Set<String> = emptySet()): Preference<Set<String>>

    /**
     * Creates a memory-backed preference for a custom object that can be serialized to and
     * deserialized from a String.
     *
     * Behaves exactly like [PreferencesDatastore.serialized] except the value is stored in a process-local
     * in-memory store: it is never persisted and is excluded from batch operations, backups,
     * and [PreferencesDatastore.clearAll].
     *
     * @param T The type of the custom object.
     * @param key The preference key.
     * @param defaultValue The default value for the custom object.
     * @param serializer A function to serialize the object to a String.
     * @param deserializer A function to deserialize the String back to the object.
     * @return A [Preference] instance for the in-memory custom object preference.
     */
    public fun <T> serializedInMemory(
        key: String,
        defaultValue: T,
        serializer: (T) -> String,
        deserializer: (String) -> T,
    ): Preference<T>

    /**
     * Creates a memory-backed preference for a [Set] of custom objects, stored using a string
     * set preference key.
     *
     * Behaves exactly like [PreferencesDatastore.serializedSet] except the value is stored in a process-local
     * in-memory store: it is never persisted and is excluded from batch operations, backups,
     * and [PreferencesDatastore.clearAll].
     *
     * @param T The type of each element in the set.
     * @param key The preference key.
     * @param defaultValue The default value for the set (defaults to an empty set).
     * @param serializer A function to serialize each element to a String.
     * @param deserializer A function to deserialize each String back to an element.
     * @return A [Preference] instance for the in-memory Set preference.
     */
    public fun <T> serializedSetInMemory(
        key: String,
        defaultValue: Set<T> = emptySet(),
        serializer: (T) -> String,
        deserializer: (String) -> T,
    ): Preference<Set<T>>

    /**
     * Creates a memory-backed preference for a custom object using Kotlin Serialization.
     *
     * Behaves exactly like [kserialized] except the value is stored in a process-local
     * in-memory store: it is never persisted and is excluded from batch operations, backups,
     * and [PreferencesDatastore.clearAll].
     *
     * @param T The type of the custom object. Must be serializable using kotlinx.serialization.
     * @param key The preference key.
     * @param defaultValue The value returned when the key is missing or the stored payload
     * cannot be decoded.
     * @param serializer The [KSerializer] for the type [T].
     * @param json The [Json] configuration to use. Passing `null` lets the implementation
     * choose its configured default.
     * @return A [Preference] instance for the in-memory custom object preference.
     */
    public fun <T> kserializedInMemory(
        key: String,
        defaultValue: T,
        serializer: KSerializer<T>,
        json: Json? = null,
    ): Preference<T>

    /**
     * Creates a memory-backed preference for a [Set] of custom objects using Kotlin
     * Serialization.
     *
     * Behaves exactly like [kserializedSet] except the value is stored in a process-local
     * in-memory store: it is never persisted and is excluded from batch operations, backups,
     * and [PreferencesDatastore.clearAll].
     *
     * @param T The type of each element in the set. Must be serializable using kotlinx.serialization.
     * @param key The preference key.
     * @param defaultValue The default value for the set (defaults to an empty set).
     * @param serializer The [KSerializer] for the type [T].
     * @param json The [Json] configuration to use. Passing `null` lets the implementation
     * choose its configured default.
     * @return A [Preference] instance for the in-memory Set preference.
     */
    public fun <T> kserializedSetInMemory(
        key: String,
        defaultValue: Set<T> = emptySet(),
        serializer: KSerializer<T>,
        json: Json? = null,
    ): Preference<Set<T>>

    /**
     * Creates a memory-backed preference for a [List] of custom objects that can be
     * serialized to and deserialized from Strings. The list is stored as a JSON array string.
     *
     * Behaves exactly like [PreferencesDatastore.serializedList] except the value is stored in a process-local
     * in-memory store: it is never persisted and is excluded from batch operations, backups,
     * and [PreferencesDatastore.clearAll].
     *
     * @param T The type of each element in the list.
     * @param key The preference key.
     * @param defaultValue The default value for the list (defaults to an empty list).
     * @param serializer A function to serialize each element to a String.
     * @param deserializer A function to deserialize each String back to an element.
     * @return A [Preference] instance for the in-memory List preference.
     */
    public fun <T> serializedListInMemory(
        key: String,
        defaultValue: List<T> = emptyList(),
        serializer: (T) -> String,
        deserializer: (String) -> T,
    ): Preference<List<T>>

    /**
     * Creates a memory-backed preference for a [List] of custom objects using Kotlin
     * Serialization.
     *
     * Behaves exactly like [kserializedList] except the value is stored in a process-local
     * in-memory store: it is never persisted and is excluded from batch operations, backups,
     * and [PreferencesDatastore.clearAll].
     *
     * @param T The type of each element in the list. Must be serializable using kotlinx.serialization.
     * @param key The preference key.
     * @param defaultValue The default value for the list (defaults to an empty list).
     * @param serializer The [KSerializer] for the type [T].
     * @param json The [Json] configuration to use. Passing `null` lets the implementation
     * choose its configured default.
     * @return A [Preference] instance for the in-memory List preference.
     */
    public fun <T> kserializedListInMemory(
        key: String,
        defaultValue: List<T> = emptyList(),
        serializer: KSerializer<T>,
        json: Json? = null,
    ): Preference<List<T>>

    /**
     * Creates a memory-backed nullable preference for a custom object that can be serialized
     * to and deserialized from a String.
     *
     * Behaves exactly like [PreferencesDatastore.nullableSerialized] except the value is stored in a process-local
     * in-memory store: it is never persisted and is excluded from batch operations, backups,
     * and [PreferencesDatastore.clearAll].
     *
     * @param T The non-null type of the custom object.
     * @param key The preference key.
     * @param serializer A function to serialize the object to a String.
     * @param deserializer A function to deserialize the String back to the object.
     * @return A [Preference] instance for the in-memory nullable custom object preference.
     */
    public fun <T : Any> nullableSerializedInMemory(
        key: String,
        serializer: (T) -> String,
        deserializer: (String) -> T,
    ): Preference<T?>

    /**
     * Creates a memory-backed nullable preference for a custom object using Kotlin
     * Serialization.
     *
     * Behaves exactly like [nullableKserialized] except the value is stored in a process-local
     * in-memory store: it is never persisted and is excluded from batch operations, backups,
     * and [PreferencesDatastore.clearAll].
     *
     * @param T The non-null type of the custom object. Must be serializable using kotlinx.serialization.
     * @param key The preference key.
     * @param serializer The [KSerializer] for the type [T].
     * @param json The [Json] configuration to use. Passing `null` lets the implementation
     * choose its configured default.
     * @return A [Preference] instance for the in-memory nullable custom object preference.
     */
    public fun <T : Any> nullableKserializedInMemory(
        key: String,
        serializer: KSerializer<T>,
        json: Json? = null,
    ): Preference<T?>

    /**
     * Creates a memory-backed nullable preference for a [List] of custom objects that can be
     * serialized to and deserialized from Strings.
     *
     * Behaves exactly like [PreferencesDatastore.nullableSerializedList] except the value is stored in a
     * process-local in-memory store: it is never persisted and is excluded from batch
     * operations, backups, and [PreferencesDatastore.clearAll].
     *
     * @param T The type of each element in the list.
     * @param key The preference key.
     * @param serializer A function to serialize each element to a String.
     * @param deserializer A function to deserialize each String back to an element.
     * @return A [Preference] instance for the in-memory nullable List preference.
     */
    public fun <T> nullableSerializedListInMemory(
        key: String,
        serializer: (T) -> String,
        deserializer: (String) -> T,
    ): Preference<List<T>?>

    /**
     * Creates a memory-backed nullable preference for a [List] of custom objects using Kotlin
     * Serialization.
     *
     * Behaves exactly like [nullableKserializedList] except the value is stored in a
     * process-local in-memory store: it is never persisted and is excluded from batch
     * operations, backups, and [PreferencesDatastore.clearAll].
     *
     * @param T The type of each element in the list. Must be serializable using kotlinx.serialization.
     * @param key The preference key.
     * @param serializer The [KSerializer] for the type [T].
     * @param json The [Json] configuration to use. Passing `null` lets the implementation
     * choose its configured default.
     * @return A [Preference] instance for the in-memory nullable List preference.
     */
    public fun <T> nullableKserializedListInMemory(
        key: String,
        serializer: KSerializer<T>,
        json: Json? = null,
    ): Preference<List<T>?>

    /**
     * Creates a memory-backed nullable preference for a [Set] of custom objects that can be
     * serialized to and deserialized from a String.
     *
     * Behaves exactly like [PreferencesDatastore.nullableSerializedSet] except the value is stored in a
     * process-local in-memory store: it is never persisted and is excluded from batch
     * operations, backups, and [PreferencesDatastore.clearAll].
     *
     * @param T The non-null type of the custom object.
     * @param key The preference key.
     * @param serializer A function to serialize each element to a String.
     * @param deserializer A function to deserialize each String back to an element.
     * @return A [Preference] instance for the in-memory nullable Set preference.
     */
    public fun <T : Any> nullableSerializedSetInMemory(
        key: String,
        serializer: (T) -> String,
        deserializer: (String) -> T,
    ): Preference<Set<T>?>

    /**
     * Creates a memory-backed nullable preference for a [Set] of custom objects using Kotlin
     * Serialization.
     *
     * Behaves exactly like [nullableKserializedSet] except the value is stored in a
     * process-local in-memory store: it is never persisted and is excluded from batch
     * operations, backups, and [PreferencesDatastore.clearAll].
     *
     * @param T The non-null type of the custom object. Must be serializable using
     *   kotlinx.serialization.
     * @param key The preference key.
     * @param serializer The [KSerializer] for the type [T].
     * @param json The [Json] configuration to use. Passing `null` lets the implementation
     *   choose its configured default.
     * @return A [Preference] instance for the in-memory nullable Set preference.
     */
    public fun <T : Any> nullableKserializedSetInMemory(
        key: String,
        serializer: KSerializer<T>,
        json: Json? = null,
    ): Preference<Set<T>?>

    /**
     * Creates a memory-backed preference storing a single enum constant by [Enum.name].
     *
     * Behaves exactly like [enum] except the value is stored in a process-local in-memory
     * store: it is never persisted and is excluded from batch operations, backups, and
     * [PreferencesDatastore.clearAll].
     *
     * @param T The enum type.
     * @param key The preference key.
     * @param defaultValue The value returned when the key is missing or the stored name is unknown.
     * @param enumValues All constants of [T], used to decode the stored name.
     * @return A [Preference] instance for the in-memory enum preference.
     */
    public fun <T : Enum<T>> enumInMemory(
        key: String,
        defaultValue: T,
        enumValues: Array<T>,
    ): Preference<T>

    /**
     * Creates a memory-backed preference storing a [Set] of enum constants by [Enum.name].
     *
     * Behaves exactly like [enumSet] except the value is stored in a process-local in-memory
     * store: it is never persisted and is excluded from batch operations, backups, and
     * [PreferencesDatastore.clearAll].
     *
     * @param T The enum type.
     * @param key The preference key.
     * @param defaultValue The default value for the set.
     * @param enumValues All constants of [T], used to decode each stored name.
     * @return A [Preference] instance for the in-memory Set preference.
     */
    public fun <T : Enum<T>> enumSetInMemory(
        key: String,
        defaultValue: Set<T>,
        enumValues: Array<T>,
    ): Preference<Set<T>>

    /**
     * Creates a memory-backed nullable preference storing a single enum constant by
     * [Enum.name].
     *
     * Behaves exactly like [nullableEnum] except the value is stored in a process-local
     * in-memory store: it is never persisted and is excluded from batch operations, backups,
     * and [PreferencesDatastore.clearAll].
     *
     * @param T The enum type.
     * @param key The preference key.
     * @param enumValues All constants of [T], used to decode the stored name.
     * @return A [Preference] instance for the in-memory nullable enum preference.
     */
    public fun <T : Enum<T>> nullableEnumInMemory(
        key: String,
        enumValues: Array<T>,
    ): Preference<T?>

    /**
     * Creates a memory-backed nullable preference storing a [Set] of enum constants by
     * [Enum.name].
     *
     * Behaves exactly like [nullableEnumSet] except the value is stored in a process-local
     * in-memory store: it is never persisted and is excluded from batch operations, backups,
     * and [PreferencesDatastore.clearAll].
     *
     * @param T The enum type.
     * @param key The preference key.
     * @param enumValues All constants of [T], used to decode each stored name.
     * @return A [Preference] instance for the in-memory nullable Set preference.
     */
    public fun <T : Enum<T>> nullableEnumSetInMemory(
        key: String,
        enumValues: Array<T>,
    ): Preference<Set<T>?>
}
