package io.github.arthurkun.generic.datastore.preferences

import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import io.github.arthurkun.generic.datastore.core.BasePreference
import io.github.arthurkun.generic.datastore.core.DelegatedPreference
import io.github.arthurkun.generic.datastore.preferences.batch.PreferencesAccessor
import kotlin.reflect.KProperty

/**
 * A sealed [DelegatedPreference] backed by a Preferences DataStore.
 *
 * Only this library can create implementations of [Preference]. This keeps batch APIs limited to
 * library-owned preferences while still allowing all preference wrappers produced by this module,
 * such as mapped preferences, to participate in batch reads and writes.
 *
 * @param T The type of the preference value.
 */
public sealed interface Preference<T> : DelegatedPreference<T>

/**
 * Internal implementation of the [Preference] interface.
 *
 * This class delegates the [BasePreference] functionalities to the provided [pref]
 * instance and implements the property delegation methods.
 *
 * @param T The type of the preference value.
 * @property pref The underlying [BasePreference] instance.
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
