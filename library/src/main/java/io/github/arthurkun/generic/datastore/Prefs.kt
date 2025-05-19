package io.github.arthurkun.generic.datastore

import kotlin.reflect.KProperty

interface Prefs<T> : Preference<T> {
    suspend fun getValue(thisRef: Any, property: KProperty<*>): T

    suspend fun setValue(thisRef: Any, property: KProperty<*>, value: T)

    suspend fun resetToDefault()
}

internal class PrefsImpl<T>(private val pref: Preference<T>) :
    Prefs<T>,
    Preference<T> by pref {
    override suspend fun getValue(thisRef: Any, property: KProperty<*>) = pref.get()

    override suspend fun setValue(thisRef: Any, property: KProperty<*>, value: T) =
        pref.set(value)

    override suspend fun resetToDefault() = pref.set(pref.defaultValue())
}