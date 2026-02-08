package io.github.arthurkun.generic.datastore.preferences.default

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.floatPreferencesKey

/**
 * A [GenericPreferenceItem] for storing [Float] values.
 * @param datastore The [DataStore<Preferences>] instance used for storing and retrieving preferences.
 * @param key The unique String key used to identify this preference within the DataStore.
 * @param defaultValue The default value to use if the preference is not set or on retrieval error.
 */
internal class FloatPrimitive(
    datastore: DataStore<Preferences>,
    key: String,
    defaultValue: Float,
) : GenericPreferenceItem<Float>(
    datastore = datastore,
    key = key,
    defaultValue = defaultValue,
    preferences = floatPreferencesKey(key),
)
