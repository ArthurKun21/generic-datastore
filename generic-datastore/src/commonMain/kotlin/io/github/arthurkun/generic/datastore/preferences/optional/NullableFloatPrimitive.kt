package io.github.arthurkun.generic.datastore.preferences.optional

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.floatPreferencesKey

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
