package io.github.arthurkun.generic.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import io.github.arthurkun.generic.datastore.GenericPreference.BooleanPrimitive
import io.github.arthurkun.generic.datastore.GenericPreference.FloatPrimitive
import io.github.arthurkun.generic.datastore.GenericPreference.IntPrimitive
import io.github.arthurkun.generic.datastore.GenericPreference.LongPrimitive
import io.github.arthurkun.generic.datastore.GenericPreference.ObjectPrimitive
import io.github.arthurkun.generic.datastore.GenericPreference.StringPrimitive
import io.github.arthurkun.generic.datastore.GenericPreference.StringSetPrimitive

@Suppress("unused")
class GenericPreferenceDatastore(
    private val datastore: DataStore<Preferences>
) : PreferenceDatastore {
    override fun string(key: String, defaultValue: String): Prefs<String> =
        PrefsImpl(
            StringPrimitive(
                datastore = datastore,
                preferencesKey = stringPreferencesKey(key),
                key = key,
                defaultValue = defaultValue
            )
        )

    override fun long(key: String, defaultValue: Long) : Prefs<Long> =
        PrefsImpl(
            LongPrimitive(
                datastore = datastore,
                preferencesKey = longPreferencesKey(key),
                key = key,
                defaultValue = defaultValue
            )
        )

    override fun int(key: String, defaultValue: Int): Prefs<Int> =
        PrefsImpl(
            IntPrimitive(
                datastore = datastore,
                preferencesKey = intPreferencesKey(key),
                key = key,
                defaultValue = defaultValue
            )
        )


    override fun float(key: String, defaultValue: Float) : Prefs<Float> =
        PrefsImpl(
            FloatPrimitive(
                datastore = datastore,
                preferencesKey = floatPreferencesKey(key),
                key = key,
                defaultValue = defaultValue
            )
        )

    override fun bool(key: String, defaultValue: Boolean) : Prefs<Boolean> =
        PrefsImpl(
            BooleanPrimitive(
                datastore = datastore,
                preferencesKey = booleanPreferencesKey(key),
                key = key,
                defaultValue = defaultValue
            )
        )

    override fun stringSet(
        key: String,
        defaultValue: Set<String>,
    ) : Prefs<Set<String>> =
        PrefsImpl(
            StringSetPrimitive(
                datastore = datastore,
                preferencesKey = stringSetPreferencesKey(key),
                key = key,
                defaultValue = defaultValue
            )
        )


    override fun <T> serialized(
        key: String,
        defaultValue: T,
        serializer: (T) -> String,
        deserializer: (String) -> T,
    ) : Prefs<T> {
        return PrefsImpl(
            ObjectPrimitive(
                datastore = datastore,
                key = key,
                defaultValue = defaultValue,
                serializer = serializer,
                deserializer = deserializer,
            )
        )
    }

}