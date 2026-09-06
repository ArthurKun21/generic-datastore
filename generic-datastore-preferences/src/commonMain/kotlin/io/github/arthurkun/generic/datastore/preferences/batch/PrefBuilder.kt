package io.github.arthurkun.generic.datastore.preferences.batch

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import io.github.arthurkun.generic.datastore.core.PreferenceDefaults
import io.github.arthurkun.generic.datastore.preferences.Preference
import io.github.arthurkun.generic.datastore.preferences.utils.deserializeList
import io.github.arthurkun.generic.datastore.preferences.utils.serializeList
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer

/**
 * Builder scope for declaring the preferences a batch touches. Obtain it from [prefBatch].
 *
 * Every declaration function creates, registers, and returns the typed [BatchPref] handle, so
 * callers can capture the returned values for typed snapshot access:
 *
 * ```kotlin
 * class AppSettings {
 *     val batch: PreferenceBatch
 *     val volume: BatchPref<Int>
 *     val name: BatchPref<String>
 *
 *     init {
 *         var volumeH: BatchPref<Int>? = null
 *         var nameH: BatchPref<String>? = null
 *         batch = prefBatch {
 *             volumeH = int("volume", 50) // capture the returned handle
 *             nameH = string("name")
 *         }
 *         volume = requireNotNull(volumeH)
 *         name = requireNotNull(nameH)
 *     }
 * }
 * ```
 *
 * Keys must be unique and non-blank inside one batch; violations throw [IllegalArgumentException].
 *
 * This builder is also the declare receiver for the inline batch operations on
 * [io.github.arthurkun.generic.datastore.preferences.PreferencesDatastore]
 * (`batchReadValues`, `batchReadFlowValues`, `batchDelete` take a `PrefBuilder.() -> Unit`
 * declaration directly). [BatchWriteScope] and [BatchUpdateScope] extend this builder so a
 * single `datastore.batchWrite { … }` / `datastore.batchUpdate { … }` block both declares
 * preferences (via `string(…)`, `int(…)`, `add(…)`…) and operates on them.
 */
@PreferencesBatchDsl
public open class PrefBuilder internal constructor() {

    private val prefs = mutableListOf<BatchPref<*>>()
    private val keys = mutableSetOf<String>()

    protected fun <P : BatchPref<*>> register(pref: P): P {
        require(keys.add(pref.key)) { "Duplicate batch preference key '${pref.key}'." }
        prefs += pref
        return pref
    }

    /**
     * Declares an [Int] preference.
     *
     * @param key The preference key.
     * @param defaultValue The value used when the key is absent.
     */
    public fun int(key: String, defaultValue: Int = 0): BatchPref<Int> =
        register(BatchTypedPref(intPreferencesKey(key), key, defaultValue))

    /**
     * Declares a [Long] preference.
     *
     * @param key The preference key.
     * @param defaultValue The value used when the key is absent.
     */
    public fun long(key: String, defaultValue: Long = 0): BatchPref<Long> =
        register(BatchTypedPref(longPreferencesKey(key), key, defaultValue))

    /**
     * Declares a [Float] preference.
     *
     * @param key The preference key.
     * @param defaultValue The value used when the key is absent.
     */
    public fun float(key: String, defaultValue: Float = 0f): BatchPref<Float> =
        register(BatchTypedPref(floatPreferencesKey(key), key, defaultValue))

    /**
     * Declares a [Double] preference.
     *
     * @param key The preference key.
     * @param defaultValue The value used when the key is absent.
     */
    public fun double(key: String, defaultValue: Double = 0.0): BatchPref<Double> =
        register(BatchTypedPref(doublePreferencesKey(key), key, defaultValue))

    /**
     * Declares a [Boolean] preference.
     *
     * @param key The preference key.
     * @param defaultValue The value used when the key is absent.
     */
    public fun bool(key: String, defaultValue: Boolean = false): BatchPref<Boolean> =
        register(BatchTypedPref(booleanPreferencesKey(key), key, defaultValue))

    /**
     * Declares a [String] preference.
     *
     * @param key The preference key.
     * @param defaultValue The value used when the key is absent.
     */
    public fun string(key: String, defaultValue: String = ""): BatchPref<String> =
        register(BatchTypedPref(stringPreferencesKey(key), key, defaultValue))

    /**
     * Declares a `Set<String>` preference.
     *
     * @param key The preference key.
     * @param defaultValue The value used when the key is absent.
     */
    public fun stringSet(key: String, defaultValue: Set<String> = emptySet()): BatchPref<Set<String>> =
        register(BatchTypedPref(stringSetPreferencesKey(key), key, defaultValue))

    /**
     * Declares a `List<String>` preference stored as one JSON array string.
     *
     * @param key The preference key.
     * @param defaultValue The value used when the key is absent or the stored array cannot be
     *   parsed; elements that fail to deserialize are skipped.
     */
    public fun stringList(
        key: String,
        defaultValue: List<String> = emptyList(),
    ): BatchPref<List<String>> = register(
        BatchCustomPref(
            key = key,
            defaultValue = defaultValue,
            serializer = { list -> serializeList(list) { it } },
            deserializer = { str -> deserializeList(str) { it } },
        ),
    )

    /**
     * Declares a nullable [Int] preference. Reads `null` when the key is absent; writing `null`
     * removes the key.
     */
    public fun nullableInt(key: String): BatchPref<Int?> =
        register(BatchNullableTypedPref(intPreferencesKey(key), key))

    /**
     * Declares a nullable [Long] preference. Reads `null` when the key is absent; writing `null`
     * removes the key.
     */
    public fun nullableLong(key: String): BatchPref<Long?> =
        register(BatchNullableTypedPref(longPreferencesKey(key), key))

    /**
     * Declares a nullable [Float] preference. Reads `null` when the key is absent; writing `null`
     * removes the key.
     */
    public fun nullableFloat(key: String): BatchPref<Float?> =
        register(BatchNullableTypedPref(floatPreferencesKey(key), key))

    /**
     * Declares a nullable [Double] preference. Reads `null` when the key is absent; writing `null`
     * removes the key.
     */
    public fun nullableDouble(key: String): BatchPref<Double?> =
        register(BatchNullableTypedPref(doublePreferencesKey(key), key))

    /**
     * Declares a nullable [Boolean] preference. Reads `null` when the key is absent; writing
     * `null` removes the key.
     */
    public fun nullableBool(key: String): BatchPref<Boolean?> =
        register(BatchNullableTypedPref(booleanPreferencesKey(key), key))

    /**
     * Declares a nullable [String] preference. Reads `null` when the key is absent; writing `null`
     * removes the key.
     */
    public fun nullableString(key: String): BatchPref<String?> =
        register(BatchNullableTypedPref(stringPreferencesKey(key), key))

    /**
     * Declares a nullable `Set<String>` preference. Reads `null` when the key is absent; writing
     * `null` removes the key.
     */
    public fun nullableStringSet(key: String): BatchPref<Set<String>?> =
        register(BatchNullableTypedPref(stringSetPreferencesKey(key), key))

    /**
     * Declares a nullable `List<String>` preference stored as one JSON array string. Reads `null`
     * when the key is absent or the stored array cannot be parsed; writing `null` removes the key.
     */
    public fun nullableStringList(key: String): BatchPref<List<String>?> = register(
        BatchNullableCustomPref(
            key = key,
            serializer = { list -> serializeList(list) { it } },
            deserializer = { str -> deserializeList(str) { it } },
        ),
    )

    /**
     * Declares a preference for a custom object that is converted to and from a [String] by the
     * supplied functions. Missing keys and decode failures read back as [defaultValue].
     *
     * @param T The type of the custom object.
     * @param key The preference key.
     * @param defaultValue The value used when the key is absent or deserialization fails.
     * @param serializer Converts [T] to its stored [String] representation.
     * @param deserializer Converts a stored [String] back to [T].
     */
    public fun <T> serialized(
        key: String,
        defaultValue: T,
        serializer: (T) -> String,
        deserializer: (String) -> T,
    ): BatchPref<T> = register(
        BatchCustomPref(key, defaultValue, serializer, deserializer),
    )

    /**
     * Declares a preference for a [Set] of custom objects stored in a string-set entry, where each
     * element is individually converted to and from a [String]. Missing keys read back as
     * [defaultValue]; elements that fail to deserialize are skipped.
     *
     * @param T The type of each element in the set.
     * @param key The preference key.
     * @param defaultValue The value used when the key is absent.
     * @param serializer Converts each element to its stored [String] representation.
     * @param deserializer Converts each stored [String] back to an element.
     */
    public fun <T> serializedSet(
        key: String,
        defaultValue: Set<T> = emptySet(),
        serializer: (T) -> String,
        deserializer: (String) -> T,
    ): BatchPref<Set<T>> = register(
        BatchSetPref(key, defaultValue, serializer, deserializer),
    )

    /**
     * Declares a preference for a [List] of custom objects stored as one JSON array string, where
     * each element is individually converted to and from a [String]. Missing keys and malformed
     * arrays read back as [defaultValue]; elements that fail to deserialize are skipped.
     *
     * @param T The type of each element in the list.
     * @param key The preference key.
     * @param defaultValue The value used when the key is absent or the array cannot be parsed.
     * @param serializer Converts each element to its stored [String] representation.
     * @param deserializer Converts each stored [String] back to an element.
     */
    public fun <T> serializedList(
        key: String,
        defaultValue: List<T> = emptyList(),
        serializer: (T) -> String,
        deserializer: (String) -> T,
    ): BatchPref<List<T>> = register(
        BatchCustomPref(
            key = key,
            defaultValue = defaultValue,
            serializer = { list -> serializeList(list, serializer) },
            deserializer = { str -> deserializeList(str, deserializer) },
        ),
    )

    /**
     * Declares a preference for a custom object encoded as JSON via [kotlinx.serialization].
     * Missing keys and decode failures read back as [defaultValue].
     *
     * @param T The type of the custom object.
     * @param key The preference key.
     * @param defaultValue The value used when the key is absent or deserialization fails.
     * @param serializer The [KSerializer] for [T].
     * @param json The [Json] configuration to use; `null` falls back to
     *   [PreferenceDefaults.defaultJson].
     */
    public fun <T> kserialized(
        key: String,
        defaultValue: T,
        serializer: KSerializer<T>,
        json: Json? = null,
    ): BatchPref<T> {
        val jsonInstance = json ?: PreferenceDefaults.defaultJson
        return register(
            BatchCustomPref(
                key = key,
                defaultValue = defaultValue,
                serializer = { jsonInstance.encodeToString(serializer, it) },
                deserializer = { jsonInstance.decodeFromString(serializer, it) },
            ),
        )
    }

    /**
     * Declares a preference for a custom object encoded as JSON, inferring the [KSerializer] from
     * [T]. Missing keys and decode failures read back as [defaultValue].
     *
     * @param T The type of the custom object.
     * @param key The preference key.
     * @param defaultValue The value used when the key is absent or deserialization fails.
     * @param json The [Json] configuration to use; `null` falls back to
     *   [PreferenceDefaults.defaultJson].
     */
    public inline fun <reified T> kserialized(
        key: String,
        defaultValue: T,
        json: Json? = null,
    ): BatchPref<T> = kserialized(key, defaultValue, serializer<T>(), json)

    /**
     * Declares a preference for a [Set] of custom objects encoded per-element as JSON and stored in
     * a string-set entry. Missing keys read back as [defaultValue]; elements that fail to decode
     * are skipped.
     *
     * @param T The type of each element in the set.
     * @param key The preference key.
     * @param defaultValue The value used when the key is absent.
     * @param serializer The [KSerializer] for each element.
     * @param json The [Json] configuration to use; `null` falls back to
     *   [PreferenceDefaults.defaultJson].
     */
    public fun <T> kserializedSet(
        key: String,
        defaultValue: Set<T> = emptySet(),
        serializer: KSerializer<T>,
        json: Json? = null,
    ): BatchPref<Set<T>> {
        val jsonInstance = json ?: PreferenceDefaults.defaultJson
        return register(
            BatchSetPref(
                key = key,
                defaultValue = defaultValue,
                elementSerializer = { jsonInstance.encodeToString(serializer, it) },
                elementDeserializer = { jsonInstance.decodeFromString(serializer, it) },
            ),
        )
    }

    /**
     * Declares a preference for a [Set] of custom objects encoded per-element as JSON, inferring
     * the [KSerializer] from [T]. Missing keys read back as [defaultValue]; elements that fail to
     * decode are skipped.
     *
     * @param T The type of each element in the set.
     * @param key The preference key.
     * @param defaultValue The value used when the key is absent.
     * @param json The [Json] configuration to use; `null` falls back to
     *   [PreferenceDefaults.defaultJson].
     */
    public inline fun <reified T> kserializedSet(
        key: String,
        defaultValue: Set<T> = emptySet(),
        json: Json? = null,
    ): BatchPref<Set<T>> = kserializedSet(key, defaultValue, serializer<T>(), json)

    /**
     * Declares a preference for a [List] of custom objects encoded as one JSON array string via
     * [kotlinx.serialization]. Missing keys and decode failures read back as [defaultValue].
     *
     * @param T The type of each element in the list.
     * @param key The preference key.
     * @param defaultValue The value used when the key is absent or deserialization fails.
     * @param serializer The [KSerializer] for each element.
     * @param json The [Json] configuration to use; `null` falls back to
     *   [PreferenceDefaults.defaultJson].
     */
    public fun <T> kserializedList(
        key: String,
        defaultValue: List<T> = emptyList(),
        serializer: KSerializer<T>,
        json: Json? = null,
    ): BatchPref<List<T>> {
        val jsonInstance = json ?: PreferenceDefaults.defaultJson
        val listSerializer = ListSerializer(serializer)
        return register(
            BatchCustomPref(
                key = key,
                defaultValue = defaultValue,
                serializer = { jsonInstance.encodeToString(listSerializer, it) },
                deserializer = { jsonInstance.decodeFromString(listSerializer, it) },
            ),
        )
    }

    /**
     * Declares a preference for a [List] of custom objects encoded as one JSON array string,
     * inferring the [KSerializer] from [T]. Missing keys and decode failures read back as
     * [defaultValue].
     *
     * @param T The type of each element in the list.
     * @param key The preference key.
     * @param defaultValue The value used when the key is absent or deserialization fails.
     * @param json The [Json] configuration to use; `null` falls back to
     *   [PreferenceDefaults.defaultJson].
     */
    public inline fun <reified T> kserializedList(
        key: String,
        defaultValue: List<T> = emptyList(),
        json: Json? = null,
    ): BatchPref<List<T>> = kserializedList(key, defaultValue, serializer<T>(), json)

    /**
     * Declares a nullable preference for a custom object converted to and from a [String] by the
     * supplied functions. Missing keys and decode failures read back as `null`; writing `null`
     * removes the key.
     *
     * @param T The non-null type of the custom object.
     * @param key The preference key.
     * @param serializer Converts [T] to its stored [String] representation.
     * @param deserializer Converts a stored [String] back to [T].
     */
    public fun <T : Any> nullableSerialized(
        key: String,
        serializer: (T) -> String,
        deserializer: (String) -> T,
    ): BatchPref<T?> = register(
        BatchNullableCustomPref(key, serializer, deserializer),
    )

    /**
     * Declares a nullable preference for a custom object encoded as JSON via
     * [kotlinx.serialization]. Missing keys and decode failures read back as `null`; writing
     * `null` removes the key.
     *
     * @param T The non-null type of the custom object.
     * @param key The preference key.
     * @param serializer The [KSerializer] for [T].
     * @param json The [Json] configuration to use; `null` falls back to
     *   [PreferenceDefaults.defaultJson].
     */
    public fun <T : Any> nullableKserialized(
        key: String,
        serializer: KSerializer<T>,
        json: Json? = null,
    ): BatchPref<T?> {
        val jsonInstance = json ?: PreferenceDefaults.defaultJson
        return register(
            BatchNullableCustomPref(
                key = key,
                serializer = { jsonInstance.encodeToString(serializer, it) },
                deserializer = { jsonInstance.decodeFromString(serializer, it) },
            ),
        )
    }

    /**
     * Declares a nullable preference for a custom object encoded as JSON, inferring the
     * [KSerializer] from [T]. Missing keys and decode failures read back as `null`; writing `null`
     * removes the key.
     *
     * @param T The non-null type of the custom object.
     * @param key The preference key.
     * @param json The [Json] configuration to use; `null` falls back to
     *   [PreferenceDefaults.defaultJson].
     */
    public inline fun <reified T : Any> nullableKserialized(
        key: String,
        json: Json? = null,
    ): BatchPref<T?> = nullableKserialized(key, serializer<T>(), json)

    /**
     * Declares a nullable preference for a [List] of custom objects stored as one JSON array
     * string, where each element is individually converted to and from a [String]. Missing keys
     * and malformed arrays read back as `null`; writing `null` removes the key.
     *
     * @param T The type of each element in the list.
     * @param key The preference key.
     * @param serializer Converts each element to its stored [String] representation.
     * @param deserializer Converts each stored [String] back to an element.
     */
    public fun <T> nullableSerializedList(
        key: String,
        serializer: (T) -> String,
        deserializer: (String) -> T,
    ): BatchPref<List<T>?> = register(
        BatchNullableCustomPref(
            key = key,
            serializer = { list -> serializeList(list, serializer) },
            deserializer = { str -> deserializeList(str, deserializer) },
        ),
    )

    /**
     * Declares a nullable preference for a [List] of custom objects encoded as one JSON array
     * string via [kotlinx.serialization]. Missing keys and decode failures read back as `null`;
     * writing `null` removes the key.
     *
     * @param T The type of each element in the list.
     * @param key The preference key.
     * @param serializer The [KSerializer] for each element.
     * @param json The [Json] configuration to use; `null` falls back to
     *   [PreferenceDefaults.defaultJson].
     */
    public fun <T> nullableKserializedList(
        key: String,
        serializer: KSerializer<T>,
        json: Json? = null,
    ): BatchPref<List<T>?> {
        val jsonInstance = json ?: PreferenceDefaults.defaultJson
        val listSerializer = ListSerializer(serializer)
        return register(
            BatchNullableCustomPref(
                key = key,
                serializer = { jsonInstance.encodeToString(listSerializer, it) },
                deserializer = { jsonInstance.decodeFromString(listSerializer, it) },
            ),
        )
    }

    /**
     * Declares a nullable preference for a [List] of custom objects encoded as one JSON array
     * string, inferring the [KSerializer] from [T]. Missing keys and decode failures read back as
     * `null`; writing `null` removes the key.
     *
     * @param T The type of each element in the list.
     * @param key The preference key.
     * @param json The [Json] configuration to use; `null` falls back to
     *   [PreferenceDefaults.defaultJson].
     */
    public inline fun <reified T> nullableKserializedList(
        key: String,
        json: Json? = null,
    ): BatchPref<List<T>?> = nullableKserializedList(key, serializer<T>(), json)

    /**
     * Declares a preference for storing an enum value by [Enum.name]. Unknown stored names read
     * back as [defaultValue].
     *
     * @param E The enum type.
     * @param key The preference key.
     * @param defaultValue The value used when the key is absent or the stored name is unknown.
     */
    public inline fun <reified E : Enum<E>> enum(key: String, defaultValue: E): BatchPref<E> =
        internalBatchEnum(key, defaultValue)

    /**
     * Declares a preference for storing a [Set] of enum values by [Enum.name]. Unknown stored
     * names are skipped when the set is read.
     *
     * @param E The enum type.
     * @param key The preference key.
     * @param defaultValue The value used when the key is absent.
     */
    public inline fun <reified E : Enum<E>> enumSet(
        key: String,
        defaultValue: Set<E> = emptySet(),
    ): BatchPref<Set<E>> = internalBatchEnumSet(key, defaultValue)

    /**
     * Declares a nullable preference for storing an enum value by [Enum.name]. Missing keys and
     * unknown stored names both read back as `null`.
     *
     * @param E The enum type.
     * @param key The preference key.
     */
    public inline fun <reified E : Enum<E>> nullableEnum(key: String): BatchPref<E?> =
        internalBatchNullableEnum(key)

    /**
     * Registers an already-declared [BatchPref] handle in this batch.
     *
     * @param T The preference value type.
     * @param pref The handle to register.
     * @return The same [pref] handle.
     */
    public fun <T> add(pref: BatchPref<T>): BatchPref<T> = register(pref)

    /**
     * Registers an existing library-created [Preference] in this batch, letting preferences that
     * were built through [io.github.arthurkun.generic.datastore.preferences.PreferencesDatastore]
     * factories join batch operations.
     *
     * @param T The preference value type.
     * @param preference The preference to register.
     * @return A [BatchPref] handle that forwards to [preference].
     * @throws IllegalStateException If [preference] was not created by this library.
     */
    public fun <T> add(preference: Preference<T>): BatchPref<T> =
        register(PreferenceBatchAdapter(preference))

    /** Builds the immutable [PreferenceBatch] in declaration order. */
    public fun build(): PreferenceBatch = PreferenceBatch(prefs.toList())
}

/**
 * Declares a batch of preferences and returns the immutable [PreferenceBatch].
 *
 * The inline batch operations on
 * [io.github.arthurkun.generic.datastore.preferences.PreferencesDatastore] accept the same
 * declaration block directly, so pre-building is only needed when handles must be shared:
 *
 * ```kotlin
 * datastore.batchWrite {
 *     val volume = int("volume", 50)
 *     set(volume, 30)
 * }
 * ```
 */
public fun prefBatch(block: PrefBuilder.() -> Unit): PreferenceBatch =
    PrefBuilder().apply(block).build()

@PublishedApi
internal inline fun <reified E : Enum<E>> PrefBuilder.internalBatchEnum(
    key: String,
    defaultValue: E,
): BatchPref<E> = serialized(
    key = key,
    defaultValue = defaultValue,
    serializer = { it.name },
    deserializer = { enumValueOf(it) },
)

@PublishedApi
internal inline fun <reified E : Enum<E>> PrefBuilder.internalBatchEnumSet(
    key: String,
    defaultValue: Set<E>,
): BatchPref<Set<E>> = serializedSet(
    key = key,
    defaultValue = defaultValue,
    serializer = { it.name },
    deserializer = { enumValueOf(it) },
)

@PublishedApi
internal inline fun <reified E : Enum<E>> PrefBuilder.internalBatchNullableEnum(
    key: String,
): BatchPref<E?> = nullableSerialized(
    key = key,
    serializer = { it.name },
    deserializer = { enumValueOf(it) },
)

/**
 * Builds the immutable [PreferenceBatch] for an inline batch declaration.
 *
 * Backs every `declare: PrefBuilder.() -> Unit` operation on
 * [io.github.arthurkun.generic.datastore.preferences.PreferencesDatastore].
 */
internal fun buildBatch(declare: PrefBuilder.() -> Unit): PreferenceBatch =
    PrefBuilder().apply(declare).build()
