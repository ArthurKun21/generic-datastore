package io.github.arthurkun.generic.datastore.core

import kotlinx.serialization.json.Json

/**
 * Provides default configuration values used across the datastore library.
 */
public object PreferenceDefaults {
    /**
     * A default [Json] instance configured with:
     * - unknown key ignoring
     * - lenient parsing
     * - default value encoding
     * - trailing comma allowance
     * - comment allowance
     *
     * Used as the default serializer for [KSerializer]-based preference types.
     */
    public val defaultJson: Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
        allowTrailingComma = true
        allowComments = true
    }
}
