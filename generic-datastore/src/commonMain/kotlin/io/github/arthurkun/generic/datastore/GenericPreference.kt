@file:Suppress("unused")

package io.github.arthurkun.generic.datastore

import io.github.arthurkun.generic.datastore.preferences.GenericPreference as PreferencesGenericPreference

/**
 * Type alias for backwards compatibility.
 * @see io.github.arthurkun.generic.datastore.preferences.GenericPreference
 */
@Deprecated(
    message = "Moved to preferences package",
    replaceWith = ReplaceWith(
        "GenericPreference",
        "io.github.arthurkun.generic.datastore.preferences.GenericPreference",
    ),
    level = DeprecationLevel.HIDDEN,
)
internal typealias GenericPreference<T> = PreferencesGenericPreference<T>
