package io.github.arthurkun.generic.datastore.preferences.nullable

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.floatPreferencesKey

internal class NullableFloatPrimitive(
    datastore: DataStore<Preferences>,
    key: String,
) : NullableGenericPreferenceItem<Float>(
    datastore = datastore,
    key = key,
    preferences = floatPreferencesKey(key),
)
