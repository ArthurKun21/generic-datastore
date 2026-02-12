package io.github.arthurkun.generic.datastore.proto.core.custom

import io.github.arthurkun.generic.datastore.proto.ProtoDatastore
import io.github.arthurkun.generic.datastore.proto.ProtoPreference

internal fun <T, F : Enum<F>> ProtoDatastore<T>.enumFieldInternal(
    key: String,
    defaultValue: F,
    enumValues: Array<F>,
    getter: (T) -> String,
    updater: (T, String) -> T,
): ProtoPreference<F> = field(
    key = key,
    defaultValue = defaultValue,
    getter = { proto ->
        val raw = getter(proto)
        safeDeserialize(raw, defaultValue) { name ->
            enumValues.first { it.name == name }
        }
    },
    updater = { proto, value ->
        updater(proto, value.name)
    },
)
