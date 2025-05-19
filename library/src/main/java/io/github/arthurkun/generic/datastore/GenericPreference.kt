package io.github.arthurkun.generic.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

sealed class GenericPreference<T>(
    private val datastore: DataStore<Preferences>,
    private val key: String,
    private val defaultValue: T,
    private val preferences: Preferences.Key<T> = preferencesKey(key, defaultValue),
): Preference<T> {
    abstract suspend fun read(
        datastore: DataStore<Preferences>,
        key: String,
        defaultValue: T,
    ): T

    abstract suspend fun write(
        datastore: DataStore<Preferences>,
        key: String,
        value: T
    )

    override fun key(): String = key

    override suspend fun get(): T {
        return datastore
            .data
            .map { ds ->
                ds[preferences] ?: defaultValue
            }
            .first()
    }

    override suspend fun set(value: T) {
        datastore.edit { ds ->
            ds[preferences] = value
        }
    }

    override suspend fun delete() {
        datastore.edit { ds ->
            ds.remove(preferences)
        }
    }

    override fun defaultValue(): T {
        return defaultValue
    }

    override fun asFlow(): Flow<T> {
        return return datastore
            .data
            .map { ds ->
                ds[preferences] ?: defaultValue
            }
    }

    override suspend fun stateIn(scope: CoroutineScope): StateFlow<T> {
        return asFlow().stateIn(scope, SharingStarted.Companion.Eagerly, get())
    }

    class StringPrimitive(
        datastore: DataStore<Preferences>,
        preferencesKey: Preferences.Key<String>,
        key: String,
        defaultValue: String,
    ) : GenericPreference<String>(
        datastore = datastore,
        key = key,
        defaultValue = defaultValue,
        preferences = preferencesKey,
    ) {
        override suspend fun read(
            datastore: DataStore<Preferences>,
            key: String,
            defaultValue: String
        ): String = get()

        override suspend fun write(
            datastore: DataStore<Preferences>,
            key: String,
            value: String
        ) = set(value)

    }

    class LongPrimitive(
        datastore: DataStore<Preferences>,
        preferencesKey: Preferences.Key<Long>,
        key: String,
        defaultValue: Long,
    ) : GenericPreference<Long>(
        datastore = datastore,
        key = key,
        preferences = preferencesKey,
        defaultValue = defaultValue,
    ) {
        override suspend fun read(
            datastore: DataStore<Preferences>,
            key: String,
            defaultValue: Long
        ): Long = get()

        override suspend fun write(
            datastore: DataStore<Preferences>,
            key: String,
            value: Long
        ) = set(value)
    }

    class IntPrimitive(
        datastore: DataStore<Preferences>,
        preferencesKey: Preferences.Key<Int>,
        key: String,
        defaultValue: Int,
    ) : GenericPreference<Int>(
        datastore = datastore,
        key = key,
        preferences = preferencesKey,
        defaultValue = defaultValue,
    ) {
        override suspend fun read(
            datastore: DataStore<Preferences>,
            key: String,
            defaultValue: Int
        ): Int = get()

        override suspend fun write(
            datastore: DataStore<Preferences>,
            key: String,
            value: Int
        ) = set(value)
    }

    class FloatPrimitive(
        datastore: DataStore<Preferences>,
        preferencesKey: Preferences.Key<Float>,
        key: String,
        defaultValue: Float,
    ) : GenericPreference<Float>(
        datastore = datastore,
        key = key,
        preferences = preferencesKey,
        defaultValue = defaultValue,
    ) {
        override suspend fun read(
            datastore: DataStore<Preferences>,
            key: String,
            defaultValue: Float
        ): Float = get()

        override suspend fun write(
            datastore: DataStore<Preferences>,
            key: String,
            value: Float
        ) = set(value)
    }

    class BooleanPrimitive(
        datastore: DataStore<Preferences>,
        preferencesKey: Preferences.Key<Boolean>,
        key: String,
        defaultValue: Boolean,
    ) : GenericPreference<Boolean>(
        datastore = datastore,
        key = key,
        preferences = preferencesKey,
        defaultValue = defaultValue,
    ) {
        override suspend fun read(
            datastore: DataStore<Preferences>,
            key: String,
            defaultValue: Boolean
        ): Boolean = get()

        override suspend fun write(
            datastore: DataStore<Preferences>,
            key: String,
            value: Boolean
        ) = set(value)
    }

    class StringSetPrimitive(
        datastore: DataStore<Preferences>,
        preferencesKey: Preferences.Key<Set<String>>,
        key: String,
        defaultValue: Set<String>,
    ) : GenericPreference<Set<String>>(
        datastore = datastore,
        key = key,
        preferences = preferencesKey,
        defaultValue = defaultValue,
    ) {
        override suspend fun read(
            datastore: DataStore<Preferences>,
            key: String,
            defaultValue: Set<String>
        ): Set<String> = get()

        override suspend fun write(
            datastore: DataStore<Preferences>,
            key: String,
            value: Set<String>
        ) = set(value)
    }

    class ObjectPrimitive<T>(
        datastore: DataStore<Preferences>,
        key: String,
        defaultValue: T,
        val serializer: (T) -> String,
        val deserializer: (String) -> T,
    ) : GenericPreference<T>(
        datastore = datastore,
        key = key,
        defaultValue = defaultValue,
    ) {
        override suspend fun read(
            datastore: DataStore<Preferences>,
            key: String,
            defaultValue: T
        ): T = datastore
            .data
            .map { ds ->
                val prefs = stringPreferencesKey(key)
                ds[prefs]?.let { deserializer(it) } ?: defaultValue
            }
            .first()

        override suspend fun write(
            datastore: DataStore<Preferences>,
            key: String,
            value: T
        ) {
            datastore.edit { ds ->
                val prefs = stringPreferencesKey(key)
                ds[prefs] = serializer(value)
            }
        }
    }
}