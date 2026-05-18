package io.github.arthurkun.generic.datastore.preferences.core

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.floatPreferencesKey

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
