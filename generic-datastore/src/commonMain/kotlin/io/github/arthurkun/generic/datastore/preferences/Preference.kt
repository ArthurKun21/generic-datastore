package io.github.arthurkun.generic.datastore.preferences

import io.github.arthurkun.generic.datastore.core.DelegatedPreference

/**
 * A [DelegatedPreference] backed by a Preferences DataStore entry.
 *
 * This marker type keeps Preferences-specific helpers scoped to the Preferences API surface.
 * Extensions such as [map], [mapIO], [toggle], and the batch helpers only apply to this subtype,
 * which prevents accidentally using them with Proto-backed preferences.
 *
 * @param T The exposed value type of the preference.
 */
public interface Preference<T> : DelegatedPreference<T>
