package io.github.arthurkun.generic.datastore.proto.custom.core

import androidx.datastore.core.DataStore
import io.github.arthurkun.generic.datastore.proto.custom.ProtoSerialFieldPreference
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

internal fun <T, F> kserializedListFieldInternal(
    datastore: DataStore<T>,
    key: String,
    defaultValue: List<F>,
    serializer: KSerializer<F>,
    json: Json,
    getter: (T) -> String,
    updater: (T, String) -> T,
    defaultProtoValue: T,
): ProtoSerialFieldPreference<T, List<F>> {
    val listSerializer = ListSerializer(serializer)
    return ProtoSerialFieldPreference(
        datastore = datastore,
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
        defaultProtoValue = defaultProtoValue,
    )
}
