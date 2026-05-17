package io.github.arthurkun.generic.datastore.proto.custom.core

import androidx.datastore.core.DataStore
import io.github.arthurkun.generic.datastore.proto.custom.ProtoSerialFieldPreference
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json

internal fun <T, F> kserializedFieldInternal(
    datastore: DataStore<T>,
    key: String,
    defaultValue: F,
    serializer: KSerializer<F>,
    json: Json,
    getter: (T) -> String,
    updater: (T, String) -> T,
    defaultProtoValue: T,
): ProtoSerialFieldPreference<T, F> = ProtoSerialFieldPreference(
    datastore = datastore,
    key = key,
    defaultValue = defaultValue,
    getter = { proto ->
        val raw = getter(proto)
        if (raw.isBlank()) {
            defaultValue
        } else {
            safeDeserialize(raw, defaultValue) { json.decodeFromString(serializer, it) }
        }
    },
    updater = { proto, value ->
        updater(proto, json.encodeToString(serializer, value))
    },
    defaultProtoValue = defaultProtoValue,
)
