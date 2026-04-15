package io.github.arthurkun.generic.datastore.preferences.optional

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringSetPreferencesKey

/**
 * [NullableGenericPreferenceItem] for a nullable `Set<String>` stored with
 * `stringSetPreferencesKey`.
 */
internal class NullableStringSetPrimitive(
    datastore: DataStore<Preferences>,
    key: String,
) : NullableGenericPreferenceItem<Set<String>>(
    datastore = datastore,
    key = key,
    preferences = stringSetPreferencesKey(key),
)
