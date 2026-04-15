package io.github.arthurkun.generic.datastore.preferences

import io.github.arthurkun.generic.datastore.core.DelegatedPreference
import io.github.arthurkun.generic.datastore.core.PreferenceDefaults
import io.github.arthurkun.generic.datastore.preferences.backup.internalToJsonElement
import io.github.arthurkun.generic.datastore.preferences.backup.internalToJsonMap
import io.github.arthurkun.generic.datastore.preferences.core.custom.internalEnum
import io.github.arthurkun.generic.datastore.preferences.core.customSet.internalEnumSet
import io.github.arthurkun.generic.datastore.preferences.optional.custom.internalNullableEnum
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.serializer
import io.github.arthurkun.generic.datastore.preferences.utils.map as internalMap
import io.github.arthurkun.generic.datastore.preferences.utils.mapIO as internalMapIO

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
 * @return A [DelegatedPreference] instance for the custom object preference.
 */
public inline fun <reified T> PreferencesDatastore.kserialized(
    key: String,
    defaultValue: T,
    json: Json? = null,
): Preference<T> = kserialized(
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
 * @return A [DelegatedPreference] instance for the Set preference.
 */
public inline fun <reified T> PreferencesDatastore.kserializedSet(
    key: String,
    defaultValue: Set<T> = emptySet(),
    json: Json? = null,
): Preference<Set<T>> = kserializedSet(
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
 * @return A [DelegatedPreference] instance for the List preference.
 */
public inline fun <reified T> PreferencesDatastore.kserializedList(
    key: String,
    defaultValue: List<T> = emptyList(),
    json: Json? = null,
): Preference<List<T>> = kserializedList(
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
 * @return A [DelegatedPreference] instance for the nullable custom object preference.
 */
public inline fun <reified T : Any> PreferencesDatastore.nullableKserialized(
    key: String,
    json: Json? = null,
): Preference<T?> = nullableKserialized(
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
 * @return A [DelegatedPreference] instance for the nullable List preference.
 */
public inline fun <reified T> PreferencesDatastore.nullableKserializedList(
    key: String,
    json: Json? = null,
): Preference<List<T>?> = nullableKserializedList(
    key = key,
    serializer = serializer<T>(),
    json = json,
)

/**
 * Maps a [Preference] to a different value type by converting the stored value's default.
 *
 * This variant evaluates [convert] against the original preference's default value during
 * initialization and uses that result as the mapped preference default.
 */
public fun <T, R> Preference<T>.mapIO(
    convert: (T) -> R,
    reverse: (R) -> T,
): Preference<R> = internalMapIO(convert, reverse)

/**
 * Maps a [Preference] to a different value type using an explicit mapped default value.
 */
public fun <T, R> Preference<T>.map(
    defaultValue: R,
    convert: (T) -> R,
    reverse: (R) -> T,
): Preference<R> = internalMap(defaultValue, convert, reverse)

/**
 * Creates a preference for storing a [Set] of enum values using a string set preference key.
 */
public inline fun <reified T : Enum<T>> PreferencesDatastore.enumSet(
    key: String,
    defaultValue: Set<T> = emptySet(),
): Preference<Set<T>> = internalEnumSet(
    key = key,
    defaultValue = defaultValue,
)

/**
 * Creates a nullable preference for storing enum values.
 */
public inline fun <reified T : Enum<T>> PreferencesDatastore.nullableEnum(
    key: String,
): Preference<T?> = internalNullableEnum(key)

/**
 * Converts this value to a [JsonElement].
 */
public fun Any?.toJsonElement(): JsonElement = internalToJsonElement()

/**
 * Parses this JSON string into a [Map] of [String] keys to [Any] values.
 */
public fun String.toJsonMap(): Map<String, Any> = internalToJsonMap()

/**
 * Toggles an item in a [Set] preference.
 *
 * If the set contains the item, it is removed; otherwise, it is added.
 * Works with [stringSet], [serializedSet], and [enumSet] preferences.
 *
 * @param T The type of each element in the set.
 * @param item The item to toggle.
 */
public suspend inline fun <T> Preference<Set<T>>.toggle(item: T) {
    update { current ->
        if (item in current) current - item else current + item
    }
}

/**
 * Toggles a [Boolean] preference.
 *
 * Flips the current value: `true` becomes `false`, and `false` becomes `true`.
 */
public suspend inline fun Preference<Boolean>.toggle() {
    update { !it }
}

/**
 * Creates a [Prefs] for storing enum values.
 *
 * The enum is serialized using its [name][Enum.name] and deserialized via
 * [enumValueOf]. If the stored string does not match any enum constant,
 * the [defaultValue] is returned.
 *
 * @param T The enum type.
 * @param key The unique string key for the preference.
 * @param defaultValue The default enum value to use if the key is not found or
 *   deserialization fails.
 * @return A [Prefs] instance backed by [PreferencesDatastore.serialized].
 */
public inline fun <reified T : Enum<T>> PreferencesDatastore.enum(
    key: String,
    defaultValue: T,
): Preference<T> = internalEnum(
    key = key,
    defaultValue = defaultValue,
)
