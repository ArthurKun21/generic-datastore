package io.github.arthurkun.generic.datastore.proto.core.customSet

import io.github.arthurkun.generic.datastore.proto.ProtoDatastore
import io.github.arthurkun.generic.datastore.proto.ProtoPreference
import io.github.arthurkun.generic.datastore.proto.core.custom.resolveJson
import io.github.arthurkun.generic.datastore.proto.core.custom.safeDeserialize
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json

internal fun <T, F> ProtoDatastore<T>.kserializedSetFieldInternal(
    key: String,
    defaultValue: Set<F>,
    serializer: KSerializer<F>,
    json: Json?,
    getter: (T) -> Set<String>,
    updater: (T, Set<String>) -> T,
): ProtoPreference<Set<F>> {
    val jsonInstance = resolveJson(json)
    return field(
        key = key,
        defaultValue = defaultValue,
        getter = { proto ->
            getter(proto).mapNotNull { raw ->
                safeDeserialize<F?>(raw, null) { jsonInstance.decodeFromString(serializer, it) }
            }.toSet()
        },
        updater = { proto, value ->
            updater(proto, value.map { jsonInstance.encodeToString(serializer, it) }.toSet())
        },
    )
}
