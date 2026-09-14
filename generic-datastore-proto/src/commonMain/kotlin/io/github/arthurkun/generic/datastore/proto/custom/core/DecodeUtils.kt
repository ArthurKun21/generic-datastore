package io.github.arthurkun.generic.datastore.proto.custom.core

import kotlin.coroutines.cancellation.CancellationException

internal inline fun <T> safeDeserialize(
    raw: String,
    fallback: T,
    noinline onDecodeFailure: ((Throwable) -> Unit)? = null,
    deserialize: (String) -> T,
): T = try {
    deserialize(raw)
} catch (e: CancellationException) {
    throw e
} catch (e: Exception) {
    onDecodeFailure?.invoke(e)
    fallback
}
