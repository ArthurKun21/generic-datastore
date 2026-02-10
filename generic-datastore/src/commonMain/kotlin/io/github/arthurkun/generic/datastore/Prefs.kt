@file:Suppress("unused")

package io.github.arthurkun.generic.datastore

import io.github.arthurkun.generic.datastore.core.DelegatedPreference as CorePrefs

/**
 * Type alias for backwards compatibility.
 * @see io.github.arthurkun.generic.datastore.core.DelegatedPreference
 */
@Deprecated(
    message = "Moved to core package",
    replaceWith = ReplaceWith(
        "Prefs",
        "io.github.arthurkun.generic.datastore.core.Prefs",
    ),
    level = DeprecationLevel.WARNING,
)
public typealias Prefs<T> = CorePrefs<T>
