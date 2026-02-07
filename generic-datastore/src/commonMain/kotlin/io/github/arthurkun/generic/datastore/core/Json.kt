package io.github.arthurkun.generic.datastore.core

import kotlinx.serialization.json.Json

public val defaultJson: Json = Json {
    ignoreUnknownKeys = true
    isLenient = true
    encodeDefaults = true
}
