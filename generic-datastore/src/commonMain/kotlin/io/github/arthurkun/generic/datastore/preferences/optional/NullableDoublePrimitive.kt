package io.github.arthurkun.generic.datastore.preferences.optional

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.doublePreferencesKey

/**
 * A [NullableGenericPreferenceItem] for storing nullable [Double] values.
 *
 * @param datastore The [DataStore] instance used for storing and retrieving preferences.
 * @param key The unique String key used to identify this preference within the DataStore.
 */
internal class NullableDoublePrimitive(
    datastore: DataStore<Preferences>,
    key: String,
) : NullableGenericPreferenceItem<Double>(
    datastore = datastore,
    key = key,
    preferences = doublePreferencesKey(key),
)
