package io.github.arthurkun.generic.datastore.proto.core.customSet

import io.github.arthurkun.generic.datastore.proto.ProtoDatastore
import io.github.arthurkun.generic.datastore.proto.ProtoPreference
import io.github.arthurkun.generic.datastore.proto.core.custom.safeDeserialize

internal fun <T, F : Enum<F>> ProtoDatastore<T>.enumSetFieldInternal(
    key: String,
    defaultValue: Set<F>,
    enumValues: Array<F>,
    getter: (T) -> Set<String>,
    updater: (T, Set<String>) -> T,
): ProtoPreference<Set<F>> = field(
    key = key,
    defaultValue = defaultValue,
    getter = { proto ->
        getter(proto).mapNotNull { raw ->
            safeDeserialize<F?>(raw, null) { name -> enumValues.first { it.name == name } }
        }.toSet()
    },
    updater = { proto, value ->
        updater(proto, value.map { it.name }.toSet())
    },
)
