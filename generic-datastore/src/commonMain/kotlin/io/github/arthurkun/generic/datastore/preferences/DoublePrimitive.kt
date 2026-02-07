package io.github.arthurkun.generic.datastore.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.doublePreferencesKey

/**
 * A [GenericPreferenceItem] for storing [Double] values.
 * @param datastore The [DataStore<Preferences>] instance used for storing and retrieving preferences.
 * @param key The unique String key used to identify this preference within the DataStore.
 * @param defaultValue The default value to use if the preference is not set or on retrieval error.
 */
internal class DoublePrimitive(
    datastore: DataStore<Preferences>,
    key: String,
    defaultValue: Double,
) : GenericPreferenceItem<Double>(
    datastore = datastore,
    key = key,
    defaultValue = defaultValue,
    preferences = doublePreferencesKey(key),
)
