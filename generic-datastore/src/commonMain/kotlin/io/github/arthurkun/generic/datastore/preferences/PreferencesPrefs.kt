package io.github.arthurkun.generic.datastore.preferences

import io.github.arthurkun.generic.datastore.core.Prefs

/**
 * A marker interface for [io.github.arthurkun.generic.datastore.core.Prefs] instances backed by a Preferences DataStore.
 *
 * This interface restricts certain extension functions (such as [io.github.arthurkun.generic.datastore.preferences.utils.map] and [io.github.arthurkun.generic.datastore.preferences.utils.mapIO])
 * to preferences-backed implementations only, preventing their use with Proto DataStore.
 *
 * @param T The type of the preference value.
 */
public interface PreferencesPrefs<T> : Prefs<T>
