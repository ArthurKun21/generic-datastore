package io.github.arthurkun.generic.datastore.preferences.core

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.byteArrayPreferencesKey

/**
 * [GenericPreferenceItem] for a [ByteArray] preference stored with `byteArrayPreferencesKey`.
 */
internal class BytesPrimitive(
    datastore: DataStore<Preferences>,
    key: String,
    defaultValue: ByteArray,
) : GenericPreferenceItem<ByteArray>(
    datastore = datastore,
    key = key,
    defaultValue = defaultValue,
    preferences = byteArrayPreferencesKey(key),
)
