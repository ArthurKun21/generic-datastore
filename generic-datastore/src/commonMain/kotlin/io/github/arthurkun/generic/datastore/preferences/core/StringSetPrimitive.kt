package io.github.arthurkun.generic.datastore.preferences.core

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringSetPreferencesKey

/**
 * A [GenericPreferenceItem] for storing [Set] of [String] values.
 * @param datastore The [DataStore<Preferences>] instance used for storing and retrieving preferences.
 * @param key The unique String key used to identify this preference within the DataStore.
 * @param defaultValue The default value to use if the preference is not set or on retrieval error.
 */
internal class StringSetPrimitive(
    datastore: DataStore<Preferences>,
    key: String,
    defaultValue: Set<String>,
) : GenericPreferenceItem<Set<String>>(
    datastore = datastore,
    key = key,
    defaultValue = defaultValue,
    preferences = stringSetPreferencesKey(key),
)
