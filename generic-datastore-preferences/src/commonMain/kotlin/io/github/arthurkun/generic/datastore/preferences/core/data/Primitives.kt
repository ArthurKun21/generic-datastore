package io.github.arthurkun.generic.datastore.preferences.core.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey

/**
 * [GenericPreferenceItem] for a [Boolean] preference stored with `booleanPreferencesKey`.
 */
internal class BooleanPrimitive(
    datastore: DataStore<Preferences>,
    key: String,
    defaultValue: Boolean,
) : GenericPreferenceItem<Boolean>(
    datastore = datastore,
    key = key,
    defaultValue = defaultValue,
    preferences = booleanPreferencesKey(key),
)

/**
 * [GenericPreferenceItem] for a [Double] preference stored with `doublePreferencesKey`.
 */
internal class DoublePrimitive(
    datastore: DataStore<Preferences>,
    key: String,
    defaultValue: Double,
) : GenericPreferenceItem<Double>(
    datastore = datastore,
    key = key,
    defaultValue = defaultValue,
    preferences = doublePreferencesKey(key),
)

/**
 * [GenericPreferenceItem] for a [Float] preference stored with `floatPreferencesKey`.
 */
internal class FloatPrimitive(
    datastore: DataStore<Preferences>,
    key: String,
    defaultValue: Float,
) : GenericPreferenceItem<Float>(
    datastore = datastore,
    key = key,
    defaultValue = defaultValue,
    preferences = floatPreferencesKey(key),
)

/**
 * [GenericPreferenceItem] for an [Int] preference stored with `intPreferencesKey`.
 */
internal class IntPrimitive(
    datastore: DataStore<Preferences>,
    key: String,
    defaultValue: Int,
) : GenericPreferenceItem<Int>(
    datastore = datastore,
    key = key,
    defaultValue = defaultValue,
    preferences = intPreferencesKey(key),
)

/**
 * [GenericPreferenceItem] for a [Long] preference stored with `longPreferencesKey`.
 */
internal class LongPrimitive(
    datastore: DataStore<Preferences>,
    key: String,
    defaultValue: Long,
) : GenericPreferenceItem<Long>(
    datastore = datastore,
    key = key,
    defaultValue = defaultValue,
    preferences = longPreferencesKey(key),
)


/**
 * [GenericPreferenceItem] for a [String] preference stored with `stringPreferencesKey`.
 */
internal class StringPrimitive(
    datastore: DataStore<Preferences>,
    key: String,
    defaultValue: String,
) : GenericPreferenceItem<String>(
    datastore = datastore,
    key = key,
    defaultValue = defaultValue,
    preferences = stringPreferencesKey(key),
)

/**
 * [GenericPreferenceItem] for a `Set<String>` preference stored with `stringSetPreferencesKey`.
 */
internal class StringSetPrimitive(
    datastore: DataStore<Preferences>,
    key: String,
    defaultValue: Set<String>,
) : GenericPreferenceItem<Set<String>>(
    datastore = datastore,
    key = key,
    defaultValue = defaultValue,
    preferences = stringSetPreferencesKey(key),
)
