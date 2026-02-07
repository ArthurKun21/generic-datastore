package io.github.arthurkun.generic.datastore.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.intPreferencesKey

/**
 * A [GenericPreferenceItem] for storing [Int] values.
 * @param datastore The [DataStore<Preferences>] instance used for storing and retrieving preferences.
 * @param key The unique String key used to identify this preference within the DataStore.
 * @param defaultValue The default value to use if the preference is not set or on retrieval error.
 */
internal class IntPrimitive(
    datastore: DataStore<Preferences>,
    key: String,
    defaultValue: Int,
) : GenericPreferenceItem<Int>(
    datastore = datastore,
    key = key,
    defaultValue = defaultValue,
    preferences = intPreferencesKey(key),
)
