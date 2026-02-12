package io.github.arthurkun.generic.datastore.proto.core.custom

import io.github.arthurkun.generic.datastore.core.PreferenceDefaults
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.json.Json

internal fun resolveJson(json: Json?): Json = json ?: PreferenceDefaults.defaultJson

internal inline fun <T> safeDeserialize(
    raw: String,
    fallback: T,
    deserialize: (String) -> T,
): T = try {
    deserialize(raw)
} catch (e: CancellationException) {
    throw e
} catch (_: Exception) {
    fallback
}
