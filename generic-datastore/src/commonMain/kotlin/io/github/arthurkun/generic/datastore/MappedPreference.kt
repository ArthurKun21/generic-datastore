@file:Suppress("unused")

package io.github.arthurkun.generic.datastore

import io.github.arthurkun.generic.datastore.core.PreferencesPrefs
import io.github.arthurkun.generic.datastore.core.map as coreMap
import io.github.arthurkun.generic.datastore.core.mapIO as coreMapIO

/**
 * Extension for backwards compatibility.
 * @see io.github.arthurkun.generic.datastore.core.mapIO
 */
@Deprecated(
    message = "Moved to core package",
    replaceWith = ReplaceWith(
        "mapIO(convert, reverse)",
        "io.github.arthurkun.generic.datastore.core.mapIO",
    ),
    level = DeprecationLevel.WARNING,
)
public fun <T, R> PreferencesPrefs<T>.mapIO(
    convert: (T) -> R,
    reverse: (R) -> T,
): PreferencesPrefs<R> = coreMapIO(convert, reverse)

/**
 * Extension for backwards compatibility.
 * @see io.github.arthurkun.generic.datastore.core.map
 */
@Deprecated(
    message = "Moved to core package",
    replaceWith = ReplaceWith(
        "map(defaultValue, convert, reverse)",
        "io.github.arthurkun.generic.datastore.core.map",
    ),
    level = DeprecationLevel.WARNING,
)
public fun <T, R> PreferencesPrefs<T>.map(
    defaultValue: R,
    convert: (T) -> R,
    reverse: (R) -> T,
): PreferencesPrefs<R> = coreMap(defaultValue, convert, reverse)
