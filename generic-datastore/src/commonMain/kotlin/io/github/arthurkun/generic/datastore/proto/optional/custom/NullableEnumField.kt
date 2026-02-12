package io.github.arthurkun.generic.datastore.proto.optional.custom

import io.github.arthurkun.generic.datastore.proto.ProtoDatastore
import io.github.arthurkun.generic.datastore.proto.ProtoPreference
import io.github.arthurkun.generic.datastore.proto.core.custom.safeDeserialize

internal fun <T, F : Enum<F>> ProtoDatastore<T>.nullableEnumFieldInternal(
    key: String,
    enumValues: Array<F>,
    getter: (T) -> String?,
    updater: (T, String?) -> T,
): ProtoPreference<F?> = field(
    key = key,
    defaultValue = null,
    getter = { proto ->
        val raw = getter(proto)
        raw?.let { safeDeserialize<F?>(it, null) { name -> enumValues.first { e -> e.name == name } } }
    },
    updater = { proto, value ->
        updater(proto, value?.name)
    },
)
