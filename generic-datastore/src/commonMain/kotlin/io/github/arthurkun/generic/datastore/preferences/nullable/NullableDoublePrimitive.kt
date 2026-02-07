package io.github.arthurkun.generic.datastore.preferences.nullable

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.doublePreferencesKey

internal class NullableDoublePrimitive(
    datastore: DataStore<Preferences>,
    key: String,
) : NullableGenericPreferenceItem<Double>(
    datastore = datastore,
    key = key,
    preferences = doublePreferencesKey(key),
)
