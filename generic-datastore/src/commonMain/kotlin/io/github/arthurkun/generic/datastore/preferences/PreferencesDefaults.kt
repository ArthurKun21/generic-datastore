package io.github.arthurkun.generic.datastore.preferences

import kotlinx.serialization.json.Json

object PreferencesDefaults {
    val json: Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }
}
