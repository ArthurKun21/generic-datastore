package io.github.arthurkun.generic.datastore

interface PreferenceDatastore {
    fun string(key: String, defaultValue: String = ""): Prefs<String>

    fun long(key: String, defaultValue: Long = 0) : Prefs<Long>

    fun int(key: String, defaultValue: Int = 0) : Prefs<Int>

    fun float(key: String, defaultValue: Float = 0f) : Prefs<Float>

    fun bool(key: String, defaultValue: Boolean = false) : Prefs<Boolean>

    fun stringSet(key: String, defaultValue: Set<String> = emptySet()) : Prefs<Set<String>>

    fun <T> serialized(
        key: String,
        defaultValue: T,
        serializer: (T) -> String,
        deserializer: (String) -> T,
    ) : Prefs<T>
}