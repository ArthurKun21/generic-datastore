@file:Suppress("unused")

package io.github.arthurkun.generic.datastore

import kotlinx.serialization.json.JsonElement
import io.github.arthurkun.generic.datastore.preferences.backup.toJsonElement as coreToJsonElement
import io.github.arthurkun.generic.datastore.preferences.backup.toJsonMap as coreToJsonMap

/**
 * Extension for backwards compatibility.
 * @see io.github.arthurkun.generic.datastore.preferences.backup.toJsonElement
 */
@Deprecated(
    message = "Moved to core package",
    replaceWith = ReplaceWith(
        "toJsonElement()",
        "io.github.arthurkun.generic.datastore.preferences.backup.toJsonElement",
    ),
    level = DeprecationLevel.WARNING,
)
public fun Any?.toJsonElement(): JsonElement = coreToJsonElement()

/**
 * Extension for backwards compatibility.
 * @see io.github.arthurkun.generic.datastore.preferences.backup.toJsonMap
 */
@Deprecated(
    message = "Moved to core package",
    replaceWith = ReplaceWith(
        "toJsonMap()",
        "io.github.arthurkun.generic.datastore.preferences.backup.toJsonMap",
    ),
    level = DeprecationLevel.WARNING,
)
public fun String.toJsonMap(): Map<String, Any> = coreToJsonMap()
