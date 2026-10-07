package io.github.arthurkun.generic.datastore.preferences.core.custom

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

/**
 * [CustomGenericPreferenceItem] for a custom value stored as a single [Int] entry using
 * caller-supplied serializers.
 *
 * Missing keys and decode failures fall back to `defaultValue`.
 */
internal class SerializedAsIntPrimitive<T>(
    datastore: DataStore<Preferences>,
    key: String,
    defaultValue: T,
    serializer: (T) -> Int,
    deserializer: (Int) -> T,
    ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : CustomGenericPreferenceItem<T, Int>(
    datastore = datastore,
    key = key,
    defaultValue = defaultValue,
    preferences = intPreferencesKey(key),
    serializer = serializer,
    deserializer = deserializer,
    ioDispatcher = ioDispatcher,
)

/**
 * [CustomGenericPreferenceItem] for a custom value stored as a single [Long] entry using
 * caller-supplied serializers.
 *
 * Missing keys and decode failures fall back to `defaultValue`.
 */
internal class SerializedAsLongPrimitive<T>(
    datastore: DataStore<Preferences>,
    key: String,
    defaultValue: T,
    serializer: (T) -> Long,
    deserializer: (Long) -> T,
    ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : CustomGenericPreferenceItem<T, Long>(
    datastore = datastore,
    key = key,
    defaultValue = defaultValue,
    preferences = longPreferencesKey(key),
    serializer = serializer,
    deserializer = deserializer,
    ioDispatcher = ioDispatcher,
)

/**
 * [CustomGenericPreferenceItem] for a custom value stored as a single [Float] entry using
 * caller-supplied serializers.
 *
 * Missing keys and decode failures fall back to `defaultValue`.
 */
internal class SerializedAsFloatPrimitive<T>(
    datastore: DataStore<Preferences>,
    key: String,
    defaultValue: T,
    serializer: (T) -> Float,
    deserializer: (Float) -> T,
    ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : CustomGenericPreferenceItem<T, Float>(
    datastore = datastore,
    key = key,
    defaultValue = defaultValue,
    preferences = floatPreferencesKey(key),
    serializer = serializer,
    deserializer = deserializer,
    ioDispatcher = ioDispatcher,
)

/**
 * [CustomGenericPreferenceItem] for a custom value stored as a single [Double] entry using
 * caller-supplied serializers.
 *
 * Missing keys and decode failures fall back to `defaultValue`.
 */
internal class SerializedAsDoublePrimitive<T>(
    datastore: DataStore<Preferences>,
    key: String,
    defaultValue: T,
    serializer: (T) -> Double,
    deserializer: (Double) -> T,
    ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : CustomGenericPreferenceItem<T, Double>(
    datastore = datastore,
    key = key,
    defaultValue = defaultValue,
    preferences = doublePreferencesKey(key),
    serializer = serializer,
    deserializer = deserializer,
    ioDispatcher = ioDispatcher,
)
