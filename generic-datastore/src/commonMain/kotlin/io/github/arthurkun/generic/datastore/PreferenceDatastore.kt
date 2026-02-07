@file:Suppress("unused")

package io.github.arthurkun.generic.datastore

import io.github.arthurkun.generic.datastore.preferences.PreferencesDatastore

/**
 * Type alias for backwards compatibility.
 * @see io.github.arthurkun.generic.datastore.preferences.PreferencesDatastore
 */
@Deprecated(
    message = "Moved to preferences package and renamed to PreferencesDatastore",
    replaceWith = ReplaceWith(
        "PreferencesDatastore",
        "io.github.arthurkun.generic.datastore.preferences.PreferencesDatastore",
    ),
    level = DeprecationLevel.WARNING,
)
public typealias PreferenceDatastore = PreferencesDatastore
