@file:Suppress("unused")

package io.github.arthurkun.generic.datastore

import io.github.arthurkun.generic.datastore.core.Preference as CorePreference

/**
 * Type alias for backwards compatibility.
 * @see io.github.arthurkun.generic.datastore.core.Preference
 */
@Deprecated(
    message = "Moved to core package",
    replaceWith = ReplaceWith(
        "Preference",
        "io.github.arthurkun.generic.datastore.core.Preference",
    ),
    level = DeprecationLevel.WARNING,
)
typealias Preference<T> = CorePreference<T>
