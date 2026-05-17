package io.github.arthurkun.generic.datastore.proto.custom.core

import kotlinx.coroutines.CancellationException

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
