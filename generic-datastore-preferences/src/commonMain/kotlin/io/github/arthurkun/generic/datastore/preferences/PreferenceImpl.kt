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
 * @property inMemoryStorage Whether [pref] is backed by the in-memory store. In-memory
 * preferences are excluded from batch operations because batch reads and writes operate on
 * disk [Preferences] snapshots.
 */
internal class PreferenceImpl<T>(
    private val pref: BasePreference<T>,
    internal val inMemoryStorage: Boolean = false,
) : Preference<T>,
    BasePreference<T> by pref,
    PreferencesAccessor<T> {

    override fun getValue(thisRef: Any?, property: KProperty<*>): T = pref.getBlocking()

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T): Unit = pref.setBlocking(value)

    override fun resetToDefaultBlocking(): Unit = pref.setBlocking(pref.defaultValue)

    @Suppress("UNCHECKED_CAST")
    override fun readFrom(preferences: Preferences): T {
        assertBatchSupported()
        return (pref as PreferencesAccessor<T>).readFrom(preferences)
    }

    @Suppress("UNCHECKED_CAST")
    override fun writeInto(mutablePreferences: MutablePreferences, value: T) {
        assertBatchSupported()
        (pref as PreferencesAccessor<T>).writeInto(mutablePreferences, value)
    }

    @Suppress("UNCHECKED_CAST")
    override fun removeFrom(mutablePreferences: MutablePreferences) {
        assertBatchSupported()
        (pref as PreferencesAccessor<T>).removeFrom(mutablePreferences)
    }

    private fun assertBatchSupported() {
        check(!inMemoryStorage) {
            "In-memory preferences do not participate in batch operations."
        }
    }
}
