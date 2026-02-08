package io.github.arthurkun.generic.datastore.preferences.nullable

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.intPreferencesKey

/**
 * A [NullableGenericPreferenceItem] for storing nullable [Int] values.
 *
 * @param datastore The [DataStore] instance used for storing and retrieving preferences.
 * @param key The unique String key used to identify this preference within the DataStore.
 */
internal class NullableIntPrimitive(
    datastore: DataStore<Preferences>,
    key: String,
) : NullableGenericPreferenceItem<Int>(
    datastore = datastore,
    key = key,
    preferences = intPreferencesKey(key),
)
