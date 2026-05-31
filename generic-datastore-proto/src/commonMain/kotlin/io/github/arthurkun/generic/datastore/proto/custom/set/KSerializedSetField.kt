package io.github.arthurkun.generic.datastore.proto.custom.set

import androidx.datastore.core.DataStore
import io.github.arthurkun.generic.datastore.proto.custom.ProtoSerialFieldPreference
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlin.coroutines.cancellation.CancellationException

internal fun <T, F> kserializedSetFieldInternal(
    datastore: DataStore<T>,
    key: String,
    defaultValue: Set<F>,
    serializer: KSerializer<F>,
    json: Json,
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
                elements.add(json.decodeFromString(serializer, raw))
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                // Skip only elements that failed to deserialize.
            }
        }
        elements
    },
    updater = { proto, value ->
        updater(proto, value.map { json.encodeToString(serializer, it) }.toSet())
    },
    defaultProtoValue = defaultProtoValue,
)
