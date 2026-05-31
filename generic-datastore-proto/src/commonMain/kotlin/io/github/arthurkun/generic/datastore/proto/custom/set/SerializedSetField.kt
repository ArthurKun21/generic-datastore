package io.github.arthurkun.generic.datastore.proto.custom.set

import androidx.datastore.core.DataStore
import io.github.arthurkun.generic.datastore.proto.custom.ProtoSerialFieldPreference
import kotlin.coroutines.cancellation.CancellationException

internal fun <T, F> serializedSetFieldInternal(
    datastore: DataStore<T>,
    key: String,
    defaultValue: Set<F>,
    serializer: (F) -> String,
    deserializer: (String) -> F,
    getter: (T) -> Set<String>,
    updater: (T, Set<String>) -> T,
    defaultProtoValue: T,
): ProtoSerialFieldPreference<T, Set<F>> = ProtoSerialFieldPreference(
    datastore = datastore,
    key = key,
    defaultValue = defaultValue,
    getter = { proto ->
        val elements = mutableSetOf<F>()
        getter(proto).forEach { raw ->
            try {
                elements.add(deserializer(raw))
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                // Skip only elements that failed to deserialize.
            }
        }
        elements
    },
    updater = { proto, value ->
        updater(proto, value.map { serializer(it) }.toSet())
    },
    defaultProtoValue = defaultProtoValue,
)
