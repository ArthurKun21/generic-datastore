package io.github.arthurkun.generic.datastore

import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey

internal fun <T> preferencesKey(
    key: String,
    defaultValue: T,
): Preferences.Key<T> = when (defaultValue) {
    is String -> stringPreferencesKey(key)
    is Long -> longPreferencesKey(key)
    is Int -> intPreferencesKey(key)
    is Float -> floatPreferencesKey(key)
    is Boolean -> booleanPreferencesKey(key)
    is Set<*> -> stringSetPreferencesKey(key)
    else -> stringPreferencesKey(key)
} as Preferences.Key<T>