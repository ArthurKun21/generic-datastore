package io.github.arthurkun.generic.datastore.preferences.optional

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey

/**
 * A [NullableGenericPreferenceItem] for storing nullable [Boolean] values.
 *
 * @param datastore The [DataStore] instance used for storing and retrieving preferences.
 * @param key The unique String key used to identify this preference within the DataStore.
 */
internal class NullableBooleanPrimitive(
    datastore: DataStore<Preferences>,
    key: String,
) : NullableGenericPreferenceItem<Boolean>(
    datastore = datastore,
    key = key,
    preferences = booleanPreferencesKey(key),
)
