@file:Suppress("unused")

package io.github.arthurkun.generic.datastore

import kotlinx.serialization.json.JsonElement
import io.github.arthurkun.generic.datastore.preferences.toJsonElement as coreToJsonElement
import io.github.arthurkun.generic.datastore.preferences.toJsonMap as coreToJsonMap

/**
 * Extension for backwards compatibility.
 * @see io.github.arthurkun.generic.datastore.preferences.toJsonElement
 */
@Deprecated(
    message = "Moved to new package",
    replaceWith = ReplaceWith(
        "toJsonElement()",
        "io.github.arthurkun.generic.datastore.preferences.toJsonElement",
    ),
    level = DeprecationLevel.WARNING,
)
public fun Any?.toJsonElement(): JsonElement = coreToJsonElement()

/**
 * Extension for backwards compatibility.
 * @see io.github.arthurkun.generic.datastore.preferences.toJsonMap
 */
@Deprecated(
    message = "Moved to new package",
    replaceWith = ReplaceWith(
        "toJsonMap()",
        "io.github.arthurkun.generic.datastore.preferences.toJsonMap",
    ),
    level = DeprecationLevel.WARNING,
)
public fun String.toJsonMap(): Map<String, Any> = coreToJsonMap()
