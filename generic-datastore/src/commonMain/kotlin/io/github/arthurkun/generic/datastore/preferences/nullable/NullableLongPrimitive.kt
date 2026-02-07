package io.github.arthurkun.generic.datastore.preferences.nullable

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.longPreferencesKey

internal class NullableLongPrimitive(
    datastore: DataStore<Preferences>,
    key: String,
) : NullableGenericPreferenceItem<Long>(
    datastore = datastore,
    key = key,
    preferences = longPreferencesKey(key),
)
