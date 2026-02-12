package io.github.arthurkun.generic.datastore.proto.core.customSet

import io.github.arthurkun.generic.datastore.proto.ProtoDatastore
import io.github.arthurkun.generic.datastore.proto.ProtoPreference
import io.github.arthurkun.generic.datastore.proto.core.custom.safeDeserialize

internal fun <T, F> ProtoDatastore<T>.serializedSetFieldInternal(
    key: String,
    defaultValue: Set<F>,
    serializer: (F) -> String,
    deserializer: (String) -> F,
    getter: (T) -> Set<String>,
    updater: (T, Set<String>) -> T,
): ProtoPreference<Set<F>> = field(
    key = key,
    defaultValue = defaultValue,
    getter = { proto ->
        getter(proto).mapNotNull { raw ->
            safeDeserialize<F?>(raw, null) { deserializer(it) }
        }.toSet()
    },
    updater = { proto, value ->
        updater(proto, value.map { serializer(it) }.toSet())
    },
)
