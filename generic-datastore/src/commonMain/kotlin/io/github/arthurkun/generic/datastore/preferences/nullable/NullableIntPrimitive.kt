package io.github.arthurkun.generic.datastore.preferences.nullable

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.intPreferencesKey

internal class NullableIntPrimitive(
    datastore: DataStore<Preferences>,
    key: String,
) : NullableGenericPreferenceItem<Int>(
    datastore = datastore,
    key = key,
    preferences = intPreferencesKey(key),
)
