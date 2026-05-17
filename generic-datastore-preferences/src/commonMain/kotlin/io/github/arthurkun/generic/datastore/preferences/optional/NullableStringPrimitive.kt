package io.github.arthurkun.generic.datastore.preferences.optional

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey

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
