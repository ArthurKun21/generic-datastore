package io.github.arthurkun.generic.datastore.preferences.optional

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.intPreferencesKey

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
