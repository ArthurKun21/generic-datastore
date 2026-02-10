package io.github.arthurkun.generic.datastore.core

/**
 * A marker interface for [Prefs] instances backed by a Preferences DataStore.
 *
 * This interface restricts certain extension functions (such as [map] and [mapIO])
 * to preferences-backed implementations only, preventing their use with Proto DataStore.
 *
 * @param T The type of the preference value.
 */
public interface PreferencesPrefs<T> : Prefs<T>
