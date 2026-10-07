package io.github.arthurkun.generic.datastore.preferences.optional.custom

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import io.github.arthurkun.generic.datastore.preferences.utils.deserializeList
import io.github.arthurkun.generic.datastore.preferences.utils.serializeList
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

/**
 * [NullableCustomGenericPreferenceItem] for a nullable string-backed custom value using
 * caller-supplied serializers.
 */
internal class NullableSerializedPrimitive<T : Any>(
    datastore: DataStore<Preferences>,
    key: String,
    serializer: (T) -> String,
    deserializer: (String) -> T,
    ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : NullableCustomGenericPreferenceItem<T, String>(
    datastore = datastore,
    key = key,
    preferences = stringPreferencesKey(key),
    serializer = serializer,
    deserializer = deserializer,
    ioDispatcher = ioDispatcher,
)

/**
 * [NullableCustomGenericPreferenceItem] that stores a nullable [List] inside one JSON array
 * string using caller-supplied element serializers.
 */
internal class NullableSerializedListPrimitive<T>(
    datastore: DataStore<Preferences>,
    key: String,
    elementSerializer: (T) -> String,
    elementDeserializer: (String) -> T,
    ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : NullableCustomGenericPreferenceItem<List<T>, String>(
    datastore = datastore,
    key = key,
    preferences = stringPreferencesKey(key),
    serializer = { list -> serializeList(list, elementSerializer) },
    deserializer = { str -> deserializeList(str, elementDeserializer) },
    ioDispatcher = ioDispatcher,
)

/**
 * [NullableCustomGenericPreferenceItem] for a nullable custom value stored as a single [Int]
 * entry using caller-supplied serializers.
 *
 * Missing keys and decode failures both read back as `null`; writing `null` removes the key.
 */
internal class NullableSerializedAsIntPrimitive<T : Any>(
    datastore: DataStore<Preferences>,
    key: String,
    serializer: (T) -> Int,
    deserializer: (Int) -> T,
    ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : NullableCustomGenericPreferenceItem<T, Int>(
    datastore = datastore,
    key = key,
    preferences = intPreferencesKey(key),
    serializer = serializer,
    deserializer = deserializer,
    ioDispatcher = ioDispatcher,
)

/**
 * [NullableCustomGenericPreferenceItem] for a nullable custom value stored as a single [Long]
 * entry using caller-supplied serializers.
 *
 * Missing keys and decode failures both read back as `null`; writing `null` removes the key.
 */
internal class NullableSerializedAsLongPrimitive<T : Any>(
    datastore: DataStore<Preferences>,
    key: String,
    serializer: (T) -> Long,
    deserializer: (Long) -> T,
    ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : NullableCustomGenericPreferenceItem<T, Long>(
    datastore = datastore,
    key = key,
    preferences = longPreferencesKey(key),
    serializer = serializer,
    deserializer = deserializer,
    ioDispatcher = ioDispatcher,
)

/**
 * [NullableCustomGenericPreferenceItem] for a nullable custom value stored as a single [Float]
 * entry using caller-supplied serializers.
 *
 * Missing keys and decode failures both read back as `null`; writing `null` removes the key.
 */
internal class NullableSerializedAsFloatPrimitive<T : Any>(
    datastore: DataStore<Preferences>,
    key: String,
    serializer: (T) -> Float,
    deserializer: (Float) -> T,
    ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : NullableCustomGenericPreferenceItem<T, Float>(
    datastore = datastore,
    key = key,
    preferences = floatPreferencesKey(key),
    serializer = serializer,
    deserializer = deserializer,
    ioDispatcher = ioDispatcher,
)

/**
 * [NullableCustomGenericPreferenceItem] for a nullable custom value stored as a single [Double]
 * entry using caller-supplied serializers.
 *
 * Missing keys and decode failures both read back as `null`; writing `null` removes the key.
 */
internal class NullableSerializedAsDoublePrimitive<T : Any>(
    datastore: DataStore<Preferences>,
    key: String,
    serializer: (T) -> Double,
    deserializer: (Double) -> T,
    ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : NullableCustomGenericPreferenceItem<T, Double>(
    datastore = datastore,
    key = key,
    preferences = doublePreferencesKey(key),
    serializer = serializer,
    deserializer = deserializer,
    ioDispatcher = ioDispatcher,
)
