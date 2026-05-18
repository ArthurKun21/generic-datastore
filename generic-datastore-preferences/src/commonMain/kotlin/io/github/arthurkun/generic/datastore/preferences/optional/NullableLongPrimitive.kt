package io.github.arthurkun.generic.datastore.preferences.optional

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.longPreferencesKey

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
