package io.github.arthurkun.generic.datastore.preferences.optional

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.byteArrayPreferencesKey

/**
 * [NullableGenericPreferenceItem] for a nullable [ByteArray] stored with `byteArrayPreferencesKey`.
 */
internal class NullableBytesPrimitive(
    datastore: DataStore<Preferences>,
    key: String,
) : NullableGenericPreferenceItem<ByteArray>(
    datastore = datastore,
    key = key,
    preferences = byteArrayPreferencesKey(key),
)
