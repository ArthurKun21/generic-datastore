@file:Suppress("unused")

package io.github.arthurkun.generic.datastore

import io.github.arthurkun.generic.datastore.preferences.GenericPreferencesDatastore

/**
 * Type alias for backwards compatibility.
 * @see io.github.arthurkun.generic.datastore.preferences.GenericPreferencesDatastore
 */
@Deprecated(
    message = "Moved to preferences package and renamed to GenericPreferencesDatastore",
    replaceWith = ReplaceWith(
        "GenericPreferencesDatastore",
        "io.github.arthurkun.generic.datastore.preferences.GenericPreferencesDatastore",
    ),
    level = DeprecationLevel.HIDDEN,
)
typealias GenericPreferenceDatastore = GenericPreferencesDatastore
