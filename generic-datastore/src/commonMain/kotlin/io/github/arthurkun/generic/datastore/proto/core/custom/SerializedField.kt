package io.github.arthurkun.generic.datastore.proto.core.custom

import io.github.arthurkun.generic.datastore.proto.ProtoDatastore
import io.github.arthurkun.generic.datastore.proto.ProtoPreference

internal fun <T, F> ProtoDatastore<T>.serializedFieldInternal(
    key: String,
    defaultValue: F,
    serializer: (F) -> String,
    deserializer: (String) -> F,
    getter: (T) -> String,
    updater: (T, String) -> T,
): ProtoPreference<F> = field(
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
)
