package io.github.arthurkun.generic.datastore.preferences.optional

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey

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
