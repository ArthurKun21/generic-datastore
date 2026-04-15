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
 * Creates a Kotlin-serialization-backed preference, inferring the [KSerializer] from [T].
 *
 * The value is stored as JSON in a string preference entry. Passing `null` for [json] delegates
 * to the datastore implementation's configured default. [GenericPreferencesDatastore] falls back
 * to [PreferenceDefaults.defaultJson] unless it was created with a custom default.
 *
 * Example:
 * ```kotlin
 * @Serializable
 * data class User(val id: Int, val name: String)
 *
 * val user = datastore.kserialized("user", User(0, "Guest"))
 * ```
 *
 * @param T The type of the custom object.
 * @param key The preference key.
 * @param defaultValue The value returned when the key is missing or the stored payload cannot be decoded.
 * @param json The [Json] configuration to use, or `null` to use the datastore default.
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
 * Creates a Kotlin-serialization-backed [Set] preference, inferring the [KSerializer] from [T].
 *
 * Each element is encoded separately into the underlying string-set entry. Elements that fail to
 * decode are skipped when the set is read.
 *
 * @param T The type of each element in the set.
 * @param key The preference key.
 * @param defaultValue The default value for the set (defaults to an empty set).
 * @param json The [Json] configuration to use, or `null` to use the datastore default.
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
 * Creates a Kotlin-serialization-backed [List] preference, inferring the [KSerializer] from [T].
 *
 * The list is stored as a single JSON array string.
 *
 * @param T The type of each element in the list.
 * @param key The preference key.
 * @param defaultValue The default value for the list (defaults to an empty list).
 * @param json The [Json] configuration to use, or `null` to use the datastore default.
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
 * Creates a nullable Kotlin-serialization-backed preference, inferring the [KSerializer] from [T].
 *
 * Missing keys and failed deserialization both surface as `null`. Writing `null` removes the key.
 *
 * Example:
 * ```kotlin
 * @Serializable
 * data class User(val id: Int, val name: String)
 *
 * val optionalUser = datastore.nullableKserialized<User>("user")
 * ```
 *
 * @param T The non-null type of the custom object.
 * @param key The preference key.
 * @param json The [Json] configuration to use, or `null` to use the datastore default.
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
 * Creates a nullable Kotlin-serialization-backed [List] preference, inferring the
 * [KSerializer] from [T].
 *
 * Missing keys and failed deserialization both surface as `null`. Writing `null` removes the key.
 *
 * @param T The type of each element in the list. Must be serializable using kotlinx.serialization.
 * @param key The preference key.
 * @param json The [Json] configuration to use, or `null` to use the datastore default.
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
 * Maps a [Preference] to a different value type by deriving the mapped default from the source
 * preference's default value.
 *
 * This is convenient when [convert] is guaranteed to succeed for the original default value.
 * If that assumption is not safe, prefer [map] and supply an explicit mapped default instead.
 */
public fun <T, R> Preference<T>.mapIO(
    convert: (T) -> R,
    reverse: (R) -> T,
): Preference<R> = internalMapIO(convert, reverse)

/**
 * Maps a [Preference] to a different value type using an explicit mapped default value.
 *
 * Reads use [defaultValue] when [convert] throws. Writes fall back to the source preference's
 * default when [reverse] throws.
 *
 * Example:
 * ```kotlin
 * val temperatureC = datastore.int("temperature_f")
 *     .map(
 *         defaultValue = 0.0,
 *         convert = { fahrenheit -> (fahrenheit - 32) * 5.0 / 9.0 },
 *         reverse = { celsius -> ((celsius * 9.0 / 5.0) + 32).toInt() },
 *     )
 * ```
 */
public fun <T, R> Preference<T>.map(
    defaultValue: R,
    convert: (T) -> R,
    reverse: (R) -> T,
): Preference<R> = internalMap(defaultValue, convert, reverse)

/**
 * Creates a preference for storing a [Set] of enum values.
 *
 * Each enum constant is stored by [Enum.name]. Stored entries that no longer match any enum
 * constant are skipped when the set is read.
 */
public inline fun <reified T : Enum<T>> PreferencesDatastore.enumSet(
    key: String,
    defaultValue: Set<T> = emptySet(),
): Preference<Set<T>> = internalEnumSet(
    key = key,
    defaultValue = defaultValue,
)

/**
 * Creates a nullable preference for storing enum values by [Enum.name].
 *
 * Missing keys and unknown stored enum names both produce `null`.
 */
public inline fun <reified T : Enum<T>> PreferencesDatastore.nullableEnum(
    key: String,
): Preference<T?> = internalNullableEnum(key)

/**
 * Converts this value into a [JsonElement] using the same loose conversion rules as the backup
 * import/export helpers.
 */
public fun Any?.toJsonElement(): JsonElement = internalToJsonElement()

/**
 * Parses this JSON object string into a [Map] of [String] keys to Kotlin values.
 */
public fun String.toJsonMap(): Map<String, Any> = internalToJsonMap()

/**
 * Toggles an item in a [Set] preference.
 *
 * If the set contains [item], it is removed; otherwise, it is added.
 * This is useful with [PreferencesDatastore.stringSet], [PreferencesDatastore.serializedSet],
 * and [enumSet].
 *
 * Example:
 * ```kotlin
 * favoriteTags.toggle("kotlin")
 * ```
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
 * `true` becomes `false` and `false` becomes `true`.
 */
public suspend inline fun Preference<Boolean>.toggle() {
    update { !it }
}

/**
 * Creates a preference for storing enum values by [Enum.name].
 *
 * If the stored string does not match any enum constant, [defaultValue] is returned.
 *
 * @param T The enum type.
 * @param key The unique string key for the preference.
 * @param defaultValue The enum value to use when the key is missing or cannot be decoded.
 * @return A [Preference] instance backed by [PreferencesDatastore.serialized].
 */
public inline fun <reified T : Enum<T>> PreferencesDatastore.enum(
    key: String,
    defaultValue: T,
): Preference<T> = internalEnum(
    key = key,
    defaultValue = defaultValue,
)
