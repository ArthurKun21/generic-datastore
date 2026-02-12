package io.github.arthurkun.generic.datastore.proto.optional.custom

import io.github.arthurkun.generic.datastore.proto.ProtoDatastore
import io.github.arthurkun.generic.datastore.proto.ProtoPreference
import io.github.arthurkun.generic.datastore.proto.core.custom.safeDeserialize
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

internal fun <T, F> ProtoDatastore<T>.nullableKserializedListFieldInternal(
    key: String,
    serializer: KSerializer<F>,
    json: Json,
    getter: (T) -> String?,
    updater: (T, String?) -> T,
): ProtoPreference<List<F>?> {
    val listSerializer = ListSerializer(serializer)
    return field(
        key = key,
        defaultValue = null,
        getter = { proto ->
            val raw = getter(proto)
            raw?.let { safeDeserialize<List<F>?>(it, null) { s -> json.decodeFromString(listSerializer, s) } }
        },
        updater = { proto, value ->
            updater(proto, value?.let { json.encodeToString(listSerializer, it) })
        },
    )
}
