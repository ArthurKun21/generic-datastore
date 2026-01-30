@file:Suppress("unused")

package io.github.arthurkun.generic.datastore

import kotlinx.serialization.json.JsonElement
import io.github.arthurkun.generic.datastore.core.toJsonElement as coreToJsonElement
import io.github.arthurkun.generic.datastore.core.toJsonMap as coreToJsonMap

/**
 * Extension for backwards compatibility.
 * @see io.github.arthurkun.generic.datastore.core.toJsonElement
 */
@Deprecated(
    message = "Moved to core package",
    replaceWith = ReplaceWith(
        "toJsonElement()",
        "io.github.arthurkun.generic.datastore.core.toJsonElement",
    ),
    level = DeprecationLevel.WARNING,
)
fun Any?.toJsonElement(): JsonElement = coreToJsonElement()

/**
 * Extension for backwards compatibility.
 * @see io.github.arthurkun.generic.datastore.core.toJsonMap
 */
@Deprecated(
    message = "Moved to core package",
    replaceWith = ReplaceWith(
        "toJsonMap()",
        "io.github.arthurkun.generic.datastore.core.toJsonMap",
    ),
    level = DeprecationLevel.WARNING,
)
fun String.toJsonMap(): Map<String, Any?> = coreToJsonMap()
