package io.github.arthurkun.generic.datastore.proto.optional.custom

import io.github.arthurkun.generic.datastore.proto.ProtoDatastore
import io.github.arthurkun.generic.datastore.proto.ProtoPreference
import io.github.arthurkun.generic.datastore.proto.core.custom.safeDeserialize

internal fun <T, F : Any> ProtoDatastore<T>.nullableSerializedFieldInternal(
    key: String,
    serializer: (F) -> String,
    deserializer: (String) -> F,
    getter: (T) -> String?,
    updater: (T, String?) -> T,
): ProtoPreference<F?> = field(
    key = key,
    defaultValue = null,
    getter = { proto ->
        val raw = getter(proto)
        raw?.let { safeDeserialize<F?>(it, null) { s -> deserializer(s) } }
    },
    updater = { proto, value ->
        updater(proto, value?.let { serializer(it) })
    },
)
