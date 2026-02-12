package io.github.arthurkun.generic.datastore.proto.core.custom

import io.github.arthurkun.generic.datastore.proto.ProtoDatastore
import io.github.arthurkun.generic.datastore.proto.ProtoPreference
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

internal fun <T, F> ProtoDatastore<T>.kserializedListFieldInternal(
    key: String,
    defaultValue: List<F>,
    serializer: KSerializer<F>,
    json: Json,
    getter: (T) -> String,
    updater: (T, String) -> T,
): ProtoPreference<List<F>> {
    val listSerializer = ListSerializer(serializer)
    return field(
        key = key,
        defaultValue = defaultValue,
        getter = { proto ->
            val raw = getter(proto)
            if (raw.isBlank()) {
                defaultValue
            } else {
                safeDeserialize(raw, defaultValue) { json.decodeFromString(listSerializer, it) }
            }
        },
        updater = { proto, value ->
            updater(proto, json.encodeToString(listSerializer, value))
        },
    )
}
