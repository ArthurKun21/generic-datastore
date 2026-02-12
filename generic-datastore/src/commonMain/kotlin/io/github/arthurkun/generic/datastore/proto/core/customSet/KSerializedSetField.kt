package io.github.arthurkun.generic.datastore.proto.core.customSet

import io.github.arthurkun.generic.datastore.proto.ProtoDatastore
import io.github.arthurkun.generic.datastore.proto.ProtoPreference
import io.github.arthurkun.generic.datastore.proto.core.custom.safeDeserialize
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json

internal fun <T, F> ProtoDatastore<T>.kserializedSetFieldInternal(
    key: String,
    defaultValue: Set<F>,
    serializer: KSerializer<F>,
    json: Json,
    getter: (T) -> Set<String>,
    updater: (T, Set<String>) -> T,
): ProtoPreference<Set<F>> {
    return field(
        key = key,
        defaultValue = defaultValue,
        getter = { proto ->
            getter(proto).mapNotNull { raw ->
                safeDeserialize<F?>(raw, null) { json.decodeFromString(serializer, it) }
            }.toSet()
        },
        updater = { proto, value ->
            updater(proto, value.map { json.encodeToString(serializer, it) }.toSet())
        },
    )
}
