package io.github.arthurkun.generic.datastore.proto.custom.core

import androidx.datastore.core.DataStore
import io.github.arthurkun.generic.datastore.proto.custom.ProtoSerialFieldPreference

internal fun <T, F> serializedFieldInternal(
    datastore: DataStore<T>,
    key: String,
    defaultValue: F,
    serializer: (F) -> String,
    deserializer: (String) -> F,
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
            safeDeserialize(raw, defaultValue) { deserializer(it) }
        }
    },
    updater = { proto, value ->
        updater(proto, serializer(value))
    },
    defaultProtoValue = defaultProtoValue,
)
