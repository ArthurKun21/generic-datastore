package io.github.arthurkun.generic.datastore.proto.custom.optional

import androidx.datastore.core.DataStore
import io.github.arthurkun.generic.datastore.proto.custom.ProtoSerialFieldPreference
import io.github.arthurkun.generic.datastore.proto.custom.core.safeDeserialize
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json

internal fun <T, F : Any> nullableKserializedFieldInternal(
    datastore: DataStore<T>,
    key: String,
    serializer: KSerializer<F>,
    json: Json,
    getter: (T) -> String?,
    updater: (T, String?) -> T,
    defaultProtoValue: T,
): ProtoSerialFieldPreference<T, F?> = ProtoSerialFieldPreference(
    datastore = datastore,
    key = key,
    defaultValue = null,
    getter = { proto ->
        val raw = getter(proto)
        raw?.let { safeDeserialize<F?>(it, null) { s -> json.decodeFromString(serializer, s) } }
    },
    updater = { proto, value ->
        updater(proto, value?.let { json.encodeToString(serializer, it) })
    },
    defaultProtoValue = defaultProtoValue,
)
