@file:Suppress("unused")

package io.github.arthurkun.generic.datastore

import io.github.arthurkun.generic.datastore.preferences.DatastorePreferenceItem
import io.github.arthurkun.generic.datastore.preferences.utils.map as coreMap
import io.github.arthurkun.generic.datastore.preferences.utils.mapIO as coreMapIO

/**
 * Extension for backwards compatibility.
 * @see io.github.arthurkun.generic.datastore.preferences.utils.mapIO
 */
@Deprecated(
    message = "Moved to core package",
    replaceWith = ReplaceWith(
        "mapIO(convert, reverse)",
        "io.github.arthurkun.generic.datastore.core.mapIO",
    ),
    level = DeprecationLevel.WARNING,
)
public fun <T, R> DatastorePreferenceItem<T>.mapIO(
    convert: (T) -> R,
    reverse: (R) -> T,
): DatastorePreferenceItem<R> = coreMapIO(convert, reverse)

/**
 * Extension for backwards compatibility.
 * @see io.github.arthurkun.generic.datastore.preferences.utils.map
 */
@Deprecated(
    message = "Moved to core package",
    replaceWith = ReplaceWith(
        "map(defaultValue, convert, reverse)",
        "io.github.arthurkun.generic.datastore.core.map",
    ),
    level = DeprecationLevel.WARNING,
)
public fun <T, R> DatastorePreferenceItem<T>.map(
    defaultValue: R,
    convert: (T) -> R,
    reverse: (R) -> T,
): DatastorePreferenceItem<R> = coreMap(defaultValue, convert, reverse)
