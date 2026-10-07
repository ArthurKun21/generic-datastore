package io.github.arthurkun.generic.datastore.preferences.batch

import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import io.github.arthurkun.generic.datastore.preferences.Preference
import io.github.arthurkun.generic.datastore.preferences.utils.decodeEnum
import io.github.arthurkun.generic.datastore.preferences.utils.deserializeOrDefault
import io.github.arthurkun.generic.datastore.preferences.utils.deserializeOrNull
import io.github.arthurkun.generic.datastore.preferences.utils.deserializeSet

/**
 * A lightweight, typed declaration of one preference inside a [PreferenceBatch].
 *
 * A `BatchPref` carries only the [key], the [defaultValue], and the storage strategy — it holds no
 * datastore reference. Declare instances with [prefBatch] and consume them with the batch
 * operations on [io.github.arthurkun.generic.datastore.preferences.PreferencesDatastore]
 * (`batchRead`, `batchReadFlow`, `batchWrite`, `batchUpdate`, `batchDelete`).
 *
 * Instances use value equality on key, default, and storage strategy (the concrete subclass),
 * so the same declaration can be safely compared across snapshots.
 *
 * Note: the (de)serializer lambdas are behavior, not state, and are intentionally excluded from
 * equality. Two handles with the same key, default, and storage kind compare equal even if they
 * were built with different serializer functions. Always reuse the declared handle (or an equal
 * declaration with identical (de)serializers) when reading a snapshot — do not mix different
 * serializers under the same key.
 *
 * @param T The preference value type (`T?` for nullable declarations).
 * @property key The unique preference key.
 * @property defaultValue The value used when the key is absent (always `null` for nullable
 *   declarations).
 */
public sealed class BatchPref<T>(
    public val key: String,
    public val defaultValue: T,
) {

    init {
        require(key.isNotBlank()) { "Batch preference key cannot be blank." }
    }

    /** Reads this preference's value from a [Preferences] snapshot, applying the fallback rules. */
    internal abstract fun readFrom(preferences: Preferences): T

    /** Writes [value] into the ongoing transaction state; `null` removes the key for nullable types. */
    internal abstract fun writeTo(mutablePreferences: MutablePreferences, value: T)

    /** Removes this preference's key from the ongoing transaction state. */
    internal abstract fun removeFrom(mutablePreferences: MutablePreferences)

    final override fun equals(other: Any?): Boolean =
        other is BatchPref<*> &&
            other::class == this::class &&
            other.key == key &&
            other.defaultValue == defaultValue

    final override fun hashCode(): Int {
        var result = this::class.hashCode()
        result = 31 * result + key.hashCode()
        result = 31 * result + (defaultValue?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String = "BatchPref(key=$key, defaultValue=$defaultValue)"
}

/**
 * Non-null preference stored under a native typed [Preferences.Key] (`intPreferencesKey`,
 * `stringSetPreferencesKey`, ...). Missing keys read back as [BatchPref.defaultValue].
 */
internal class BatchTypedPref<T>(
    private val typedKey: Preferences.Key<T>,
    key: String,
    defaultValue: T,
) : BatchPref<T>(key, defaultValue) {

    override fun readFrom(preferences: Preferences): T = preferences[typedKey] ?: defaultValue

    override fun writeTo(mutablePreferences: MutablePreferences, value: T) {
        mutablePreferences[typedKey] = value
    }

    override fun removeFrom(mutablePreferences: MutablePreferences) {
        mutablePreferences.remove(typedKey)
    }
}

/**
 * Nullable preference stored under a native typed [Preferences.Key]. Missing keys read back as
 * `null`, and writing `null` removes the key.
 */
internal class BatchNullableTypedPref<T : Any>(
    private val typedKey: Preferences.Key<T>,
    key: String,
) : BatchPref<T?>(key, null) {

    override fun readFrom(preferences: Preferences): T? = preferences[typedKey]

    override fun writeTo(mutablePreferences: MutablePreferences, value: T?) {
        if (value == null) {
            mutablePreferences.remove(typedKey)
        } else {
            mutablePreferences[typedKey] = value
        }
    }

    override fun removeFrom(mutablePreferences: MutablePreferences) {
        mutablePreferences.remove(typedKey)
    }
}

/**
 * Non-null preference stored as a single string entry produced by a custom [serializer].
 *
 * Missing keys and decode failures read back as [BatchPref.defaultValue]. Also backs list
 * declarations: the serializer/deserializer lambdas implement the list storage format.
 */
internal class BatchCustomPref<T>(
    key: String,
    defaultValue: T,
    private val serializer: (T) -> String,
    private val deserializer: (String) -> T,
) : BatchPref<T>(key, defaultValue) {

    private val stringKey = stringPreferencesKey(key)

    override fun readFrom(preferences: Preferences): T =
        preferences[stringKey]?.let { deserializeOrDefault(it, defaultValue, deserializer) }
            ?: defaultValue

    override fun writeTo(mutablePreferences: MutablePreferences, value: T) {
        mutablePreferences[stringKey] = serializer(value)
    }

    override fun removeFrom(mutablePreferences: MutablePreferences) {
        mutablePreferences.remove(stringKey)
    }
}

/**
 * Nullable preference stored as a single string entry produced by a custom [serializer].
 *
 * Missing keys and decode failures both read back as `null`. Writing `null` removes the key.
 */
internal class BatchNullableCustomPref<T : Any>(
    key: String,
    private val serializer: (T) -> String,
    private val deserializer: (String) -> T,
) : BatchPref<T?>(key, null) {

    private val stringKey = stringPreferencesKey(key)

    override fun readFrom(preferences: Preferences): T? =
        preferences[stringKey]?.let { deserializeOrNull(it, deserializer) }

    override fun writeTo(mutablePreferences: MutablePreferences, value: T?) {
        if (value == null) {
            mutablePreferences.remove(stringKey)
        } else {
            mutablePreferences[stringKey] = serializer(value)
        }
    }

    override fun removeFrom(mutablePreferences: MutablePreferences) {
        mutablePreferences.remove(stringKey)
    }
}

/**
 * Non-null custom preference stored under a native typed [Preferences.Key] (`intPreferencesKey`,
 * `longPreferencesKey`, ...), where the value is converted to and from the stored primitive by the
 * supplied functions. Missing keys and decode failures read back as [BatchPref.defaultValue].
 *
 * Concrete subclasses bind one storage strategy per primitive type so that two handles with the
 * same key and default but different storage kinds are never considered equal.
 */
internal abstract class BatchCustomPrimitivePref<T, S>(
    key: String,
    defaultValue: T,
    private val typedKey: Preferences.Key<S>,
    private val serializer: (T) -> S,
    private val deserializer: (S) -> T,
) : BatchPref<T>(key, defaultValue) {

    override fun readFrom(preferences: Preferences): T =
        preferences[typedKey]?.let { deserializeOrDefault(it, defaultValue, deserializer) }
            ?: defaultValue

    override fun writeTo(mutablePreferences: MutablePreferences, value: T) {
        mutablePreferences[typedKey] = serializer(value)
    }

    override fun removeFrom(mutablePreferences: MutablePreferences) {
        mutablePreferences.remove(typedKey)
    }
}

/** [BatchCustomPrimitivePref] storing the serialized form as an `Int`. */
internal class BatchSerializedAsIntPref<T>(
    key: String,
    defaultValue: T,
    serializer: (T) -> Int,
    deserializer: (Int) -> T,
) : BatchCustomPrimitivePref<T, Int>(key, defaultValue, intPreferencesKey(key), serializer, deserializer)

/** [BatchCustomPrimitivePref] storing the serialized form as a `Long`. */
internal class BatchSerializedAsLongPref<T>(
    key: String,
    defaultValue: T,
    serializer: (T) -> Long,
    deserializer: (Long) -> T,
) : BatchCustomPrimitivePref<T, Long>(key, defaultValue, longPreferencesKey(key), serializer, deserializer)

/** [BatchCustomPrimitivePref] storing the serialized form as a `Float`. */
internal class BatchSerializedAsFloatPref<T>(
    key: String,
    defaultValue: T,
    serializer: (T) -> Float,
    deserializer: (Float) -> T,
) : BatchCustomPrimitivePref<T, Float>(key, defaultValue, floatPreferencesKey(key), serializer, deserializer)

/** [BatchCustomPrimitivePref] storing the serialized form as a `Double`. */
internal class BatchSerializedAsDoublePref<T>(
    key: String,
    defaultValue: T,
    serializer: (T) -> Double,
    deserializer: (Double) -> T,
) : BatchCustomPrimitivePref<T, Double>(key, defaultValue, doublePreferencesKey(key), serializer, deserializer)

/**
 * Nullable custom preference stored under a native typed [Preferences.Key]. Missing keys and decode
 * failures both read back as `null`. Writing `null` removes the key.
 *
 * Concrete subclasses bind one storage strategy per primitive type so that two handles with the
 * same key and default but different storage kinds are never considered equal.
 */
internal abstract class BatchNullableCustomPrimitivePref<T : Any, S>(
    key: String,
    private val typedKey: Preferences.Key<S>,
    private val serializer: (T) -> S,
    private val deserializer: (S) -> T,
) : BatchPref<T?>(key, null) {

    override fun readFrom(preferences: Preferences): T? =
        preferences[typedKey]?.let { deserializeOrNull(it, deserializer) }

    override fun writeTo(mutablePreferences: MutablePreferences, value: T?) {
        if (value == null) {
            mutablePreferences.remove(typedKey)
        } else {
            mutablePreferences[typedKey] = serializer(value)
        }
    }

    override fun removeFrom(mutablePreferences: MutablePreferences) {
        mutablePreferences.remove(typedKey)
    }
}

/** [BatchNullableCustomPrimitivePref] storing the serialized form as an `Int`. */
internal class BatchNullableSerializedAsIntPref<T : Any>(
    key: String,
    serializer: (T) -> Int,
    deserializer: (Int) -> T,
) : BatchNullableCustomPrimitivePref<T, Int>(key, intPreferencesKey(key), serializer, deserializer)

/** [BatchNullableCustomPrimitivePref] storing the serialized form as a `Long`. */
internal class BatchNullableSerializedAsLongPref<T : Any>(
    key: String,
    serializer: (T) -> Long,
    deserializer: (Long) -> T,
) : BatchNullableCustomPrimitivePref<T, Long>(key, longPreferencesKey(key), serializer, deserializer)

/** [BatchNullableCustomPrimitivePref] storing the serialized form as a `Float`. */
internal class BatchNullableSerializedAsFloatPref<T : Any>(
    key: String,
    serializer: (T) -> Float,
    deserializer: (Float) -> T,
) : BatchNullableCustomPrimitivePref<T, Float>(key, floatPreferencesKey(key), serializer, deserializer)

/** [BatchNullableCustomPrimitivePref] storing the serialized form as a `Double`. */
internal class BatchNullableSerializedAsDoublePref<T : Any>(
    key: String,
    serializer: (T) -> Double,
    deserializer: (Double) -> T,
) : BatchNullableCustomPrimitivePref<T, Double>(key, doublePreferencesKey(key), serializer, deserializer)

/**
 * Non-null set preference stored in a string-set entry, where every element is individually
 * (de)serialized. Missing keys read back as [BatchPref.defaultValue]; elements that fail to
 * deserialize are skipped.
 */
internal class BatchSetPref<T>(
    key: String,
    defaultValue: Set<T>,
    private val elementSerializer: (T) -> String,
    private val elementDeserializer: (String) -> T,
) : BatchPref<Set<T>>(key, defaultValue) {

    private val stringSetKey = stringSetPreferencesKey(key)

    override fun readFrom(preferences: Preferences): Set<T> =
        preferences[stringSetKey]?.let { deserializeSet(it, elementDeserializer) } ?: defaultValue

    override fun writeTo(mutablePreferences: MutablePreferences, value: Set<T>) {
        mutablePreferences[stringSetKey] = value.map(elementSerializer).toSet()
    }

    override fun removeFrom(mutablePreferences: MutablePreferences) {
        mutablePreferences.remove(stringSetKey)
    }
}

/**
 * [BatchPref] that forwards raw access to an existing library-created [Preference] through its
 * [PreferencesAccessor] implementation, letting already-built preferences join a batch.
 */
internal class PreferenceBatchAdapter<T>(
    private val preference: Preference<T>,
) : BatchPref<T>(preference.key(), preference.defaultValue) {

    @Suppress("UNCHECKED_CAST")
    private val accessor: PreferencesAccessor<T> = preference as? PreferencesAccessor<T>
        ?: throw IllegalStateException(
            "Batch operations only support preferences created by this library",
        )

    override fun readFrom(preferences: Preferences): T = accessor.readFrom(preferences)

    override fun writeTo(mutablePreferences: MutablePreferences, value: T) {
        accessor.writeInto(mutablePreferences, value)
    }

    override fun removeFrom(mutablePreferences: MutablePreferences) {
        accessor.removeFrom(mutablePreferences)
    }
}

/**
 * Nullable set preference stored in a string-set entry, where every element is individually
 * (de)serialized. Missing keys read back as `null`; elements that fail to deserialize are skipped,
 * so a fully undecodable entry reads back as an empty set. Writing `null` removes the key.
 */
internal class BatchNullableSetPref<T : Any>(
    key: String,
    private val elementSerializer: (T) -> String,
    private val elementDeserializer: (String) -> T,
) : BatchPref<Set<T>?>(key, null) {

    private val stringSetKey = stringSetPreferencesKey(key)

    override fun readFrom(preferences: Preferences): Set<T>? =
        preferences[stringSetKey]?.let { deserializeSet(it, elementDeserializer) }

    override fun writeTo(mutablePreferences: MutablePreferences, value: Set<T>?) {
        if (value == null) {
            mutablePreferences.remove(stringSetKey)
        } else {
            mutablePreferences[stringSetKey] = value.map(elementSerializer).toSet()
        }
    }

    override fun removeFrom(mutablePreferences: MutablePreferences) {
        mutablePreferences.remove(stringSetKey)
    }
}

/**
 * Enum preference stored in a single string entry holding [Enum.name]. Missing keys and names that
 * no longer match any constant read back as [BatchPref.defaultValue].
 */
internal class BatchEnumPref<T : Enum<T>>(
    key: String,
    defaultValue: T,
    private val enumValues: Array<T>,
) : BatchPref<T>(key, defaultValue) {

    private val stringKey = stringPreferencesKey(key)

    override fun readFrom(preferences: Preferences): T =
        preferences[stringKey]?.let { deserializeOrDefault(it, defaultValue) { decodeEnum(enumValues, it) } }
            ?: defaultValue

    override fun writeTo(mutablePreferences: MutablePreferences, value: T) {
        mutablePreferences[stringKey] = value.name
    }

    override fun removeFrom(mutablePreferences: MutablePreferences) {
        mutablePreferences.remove(stringKey)
    }
}

/**
 * Nullable enum preference stored in a single string entry holding [Enum.name]. Missing keys and
 * names that no longer match any constant both read back as `null`. Writing `null` removes the key.
 */
internal class BatchNullableEnumPref<T : Enum<T>>(
    key: String,
    private val enumValues: Array<T>,
) : BatchPref<T?>(key, null) {

    private val stringKey = stringPreferencesKey(key)

    override fun readFrom(preferences: Preferences): T? =
        preferences[stringKey]?.let { deserializeOrNull(it) { decodeEnum(enumValues, it) } }

    override fun writeTo(mutablePreferences: MutablePreferences, value: T?) {
        if (value == null) {
            mutablePreferences.remove(stringKey)
        } else {
            mutablePreferences[stringKey] = value.name
        }
    }

    override fun removeFrom(mutablePreferences: MutablePreferences) {
        mutablePreferences.remove(stringKey)
    }
}

/**
 * Enum set preference stored in a string-set entry holding [Enum.name] per element. Missing keys
 * read back as [BatchPref.defaultValue]; names that no longer match any constant are skipped.
 */
internal class BatchEnumSetPref<T : Enum<T>>(
    key: String,
    defaultValue: Set<T>,
    private val enumValues: Array<T>,
) : BatchPref<Set<T>>(key, defaultValue) {

    private val stringSetKey = stringSetPreferencesKey(key)

    override fun readFrom(preferences: Preferences): Set<T> =
        preferences[stringSetKey]?.let { deserializeSet(it) { decodeEnum(enumValues, it) } }
            ?: defaultValue

    override fun writeTo(mutablePreferences: MutablePreferences, value: Set<T>) {
        mutablePreferences[stringSetKey] = value.map { it.name }.toSet()
    }

    override fun removeFrom(mutablePreferences: MutablePreferences) {
        mutablePreferences.remove(stringSetKey)
    }
}

/**
 * Nullable enum set preference stored in a string-set entry holding [Enum.name] per element.
 * Missing keys read back as `null`; unknown names are skipped, so a fully unknown entry reads back
 * as an empty set. Writing `null` removes the key.
 */
internal class BatchNullableEnumSetPref<T : Enum<T>>(
    key: String,
    private val enumValues: Array<T>,
) : BatchPref<Set<T>?>(key, null) {

    private val stringSetKey = stringSetPreferencesKey(key)

    override fun readFrom(preferences: Preferences): Set<T>? =
        preferences[stringSetKey]?.let { deserializeSet(it) { decodeEnum(enumValues, it) } }

    override fun writeTo(mutablePreferences: MutablePreferences, value: Set<T>?) {
        if (value == null) {
            mutablePreferences.remove(stringSetKey)
        } else {
            mutablePreferences[stringSetKey] = value.map { it.name }.toSet()
        }
    }

    override fun removeFrom(mutablePreferences: MutablePreferences) {
        mutablePreferences.remove(stringSetKey)
    }
}
