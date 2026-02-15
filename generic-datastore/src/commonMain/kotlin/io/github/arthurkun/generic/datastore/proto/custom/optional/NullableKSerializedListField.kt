package io.github.arthurkun.generic.datastore.proto.custom.optional

import androidx.datastore.core.DataStore
import io.github.arthurkun.generic.datastore.proto.custom.ProtoSerialFieldPreference
import io.github.arthurkun.generic.datastore.proto.custom.core.safeDeserialize
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

internal fun <T, F> nullableKserializedListFieldInternal(
    datastore: DataStore<T>,
    key: String,
    serializer: KSerializer<F>,
    json: Json,
    getter: (T) -> String?,
    updater: (T, String?) -> T,
    defaultProtoValue: T,
): ProtoSerialFieldPreference<T, List<F>?> {
    val listSerializer = ListSerializer(serializer)
    return ProtoSerialFieldPreference(
        datastore = datastore,
        key = key,
        defaultValue = null,
        getter = { proto ->
            val raw = getter(proto)
            raw?.let { safeDeserialize<List<F>?>(it, null) { s -> json.decodeFromString(listSerializer, s) } }
        },
        updater = { proto, value ->
            updater(proto, value?.let { json.encodeToString(listSerializer, it) })
        },
        defaultProtoValue = defaultProtoValue,
    )
}
