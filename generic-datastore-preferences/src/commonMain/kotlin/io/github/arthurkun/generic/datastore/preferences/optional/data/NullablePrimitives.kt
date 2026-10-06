package io.github.arthurkun.generic.datastore.preferences.optional.data

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
 * [NullableGenericPreferenceItem] for a nullable [Boolean] stored with `booleanPreferencesKey`.
 */
internal class NullableBooleanPrimitive(
    datastore: DataStore<Preferences>,
    key: String,
) : NullableGenericPreferenceItem<Boolean>(
    datastore = datastore,
    key = key,
    preferences = booleanPreferencesKey(key),
)

/**
 * [NullableGenericPreferenceItem] for a nullable [Double] stored with `doublePreferencesKey`.
 */
internal class NullableDoublePrimitive(
    datastore: DataStore<Preferences>,
    key: String,
) : NullableGenericPreferenceItem<Double>(
    datastore = datastore,
    key = key,
    preferences = doublePreferencesKey(key),
)

/**
 * [NullableGenericPreferenceItem] for a nullable [Float] stored with `floatPreferencesKey`.
 */
internal class NullableFloatPrimitive(
    datastore: DataStore<Preferences>,
    key: String,
) : NullableGenericPreferenceItem<Float>(
    datastore = datastore,
    key = key,
    preferences = floatPreferencesKey(key),
)

/**
 * [NullableGenericPreferenceItem] for a nullable [Int] stored with `intPreferencesKey`.
 */
internal class NullableIntPrimitive(
    datastore: DataStore<Preferences>,
    key: String,
) : NullableGenericPreferenceItem<Int>(
    datastore = datastore,
    key = key,
    preferences = intPreferencesKey(key),
)

/**
 * [NullableGenericPreferenceItem] for a nullable [Long] stored with `longPreferencesKey`.
 */
internal class NullableLongPrimitive(
    datastore: DataStore<Preferences>,
    key: String,
) : NullableGenericPreferenceItem<Long>(
    datastore = datastore,
    key = key,
    preferences = longPreferencesKey(key),
)

/**
 * [NullableGenericPreferenceItem] for a nullable [String] stored with `stringPreferencesKey`.
 */
internal class NullableStringPrimitive(
    datastore: DataStore<Preferences>,
    key: String,
) : NullableGenericPreferenceItem<String>(
    datastore = datastore,
    key = key,
    preferences = stringPreferencesKey(key),
)

/**
 * [NullableGenericPreferenceItem] for a nullable `Set<String>` stored with
 * `stringSetPreferencesKey`.
 */
internal class NullableStringSetPrimitive(
    datastore: DataStore<Preferences>,
    key: String,
) : NullableGenericPreferenceItem<Set<String>>(
    datastore = datastore,
    key = key,
    preferences = stringSetPreferencesKey(key),
)


