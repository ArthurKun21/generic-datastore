@file:Suppress("unused")

package io.github.arthurkun.generic.datastore

import io.github.arthurkun.generic.datastore.preferences.ObjectPrimitive as PreferencesObjectPrimitive

/**
 * Type alias for backwards compatibility.
 * @see io.github.arthurkun.generic.datastore.preferences.ObjectPrimitive
 */
@Deprecated(
    message = "Moved to preferences package",
    replaceWith = ReplaceWith(
        "ObjectPrimitive",
        "io.github.arthurkun.generic.datastore.preferences.ObjectPrimitive",
    ),
    level = DeprecationLevel.WARNING,
)
typealias ObjectPrimitive<T> = PreferencesObjectPrimitive<T>
