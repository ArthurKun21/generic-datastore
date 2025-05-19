package io.github.arthurkun.generic.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map


/**
 * A GenericPreference for custom Object values.
 *
 * @param T The type of the custom object.
 * @param deserializer A function to deserialize the String representation back to the object.
 */
class ObjectPrimitive<T>(
    datastore: DataStore<Preferences>,
    key: String,
    default: T,
    val serializer: (T) -> String,
    val deserializer: (String) -> T,
) : GenericPreference<T>(
    datastore = datastore,
    key = key,
    defaultValue = default,
    preferences = stringPreferencesKey(key) as Preferences.Key<T>,
) {
    private val stringPrefKey = stringPreferencesKey(key)

    override suspend fun get(): T = datastore
        .data
        .map { prefs ->
            prefs[stringPrefKey]?.let { deserializer(it) } ?: defaultValue
        }
        .first()

    override suspend fun set(value: T) {
        datastore.edit { prefs ->
            prefs[stringPrefKey] = serializer(value)
        }
    }

    override suspend fun delete() {
        datastore.edit { prefs ->
            prefs.remove(stringPrefKey)
        }
    }

    override fun asFlow(): Flow<T> {
        return datastore.data.map { prefs ->
            prefs[stringPrefKey]?.let { deserializer(it) } ?: defaultValue
        }
    }
}