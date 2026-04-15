package io.github.arthurkun.generic.datastore.preferences

import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import io.github.arthurkun.generic.datastore.core.BasePreference
import io.github.arthurkun.generic.datastore.preferences.batch.PreferencesAccessor
import kotlin.reflect.KProperty

/**
 * Internal adapter that exposes a [BasePreference] as a public [Preference].
 *
 * The wrapper preserves property delegation and batch access support while hiding the concrete
 * primitive or serializer-backed implementation classes from the public API.
 *
 * @param T The exposed value type.
 * @property pref The underlying [BasePreference] implementation.
 */
internal class PreferenceImpl<T>(
    private val pref: BasePreference<T>,
) : Preference<T>,
    BasePreference<T> by pref,
    PreferencesAccessor<T> {

    override fun getValue(thisRef: Any?, property: KProperty<*>): T = pref.getBlocking()

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) = pref.setBlocking(value)

    override fun resetToDefaultBlocking() = pref.setBlocking(pref.defaultValue)

    @Suppress("UNCHECKED_CAST")
    override fun readFrom(preferences: Preferences): T =
        (pref as PreferencesAccessor<T>).readFrom(preferences)

    @Suppress("UNCHECKED_CAST")
    override fun writeInto(mutablePreferences: MutablePreferences, value: T) =
        (pref as PreferencesAccessor<T>).writeInto(mutablePreferences, value)

    @Suppress("UNCHECKED_CAST")
    override fun removeFrom(mutablePreferences: MutablePreferences) =
        (pref as PreferencesAccessor<T>).removeFrom(mutablePreferences)
}
