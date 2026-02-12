package io.github.arthurkun.generic.datastore.proto.custom.set

import androidx.datastore.core.DataStore
import io.github.arthurkun.generic.datastore.proto.custom.ProtoSerialFieldPreference
import io.github.arthurkun.generic.datastore.proto.custom.core.safeDeserialize

internal fun <T, F : Enum<F>> enumSetFieldInternal(
    datastore: DataStore<T>,
    key: String,
    defaultValue: Set<F>,
    enumValues: Array<F>,
    getter: (T) -> Set<String>,
    updater: (T, Set<String>) -> T,
    defaultProtoValue: T,
): ProtoSerialFieldPreference<T, Set<F>> = ProtoSerialFieldPreference(
    datastore = datastore,
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
    defaultProtoValue = defaultProtoValue,
)
