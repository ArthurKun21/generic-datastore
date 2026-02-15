package io.github.arthurkun.generic.datastore.proto.custom.core

import androidx.datastore.core.DataStore
import io.github.arthurkun.generic.datastore.proto.custom.ProtoSerialFieldPreference

internal fun <T, F : Enum<F>> enumFieldInternal(
    datastore: DataStore<T>,
    key: String,
    defaultValue: F,
    enumValues: Array<F>,
    getter: (T) -> String,
    updater: (T, String) -> T,
    defaultProtoValue: T,
): ProtoSerialFieldPreference<T, F> = ProtoSerialFieldPreference(
    datastore = datastore,
    key = key,
    defaultValue = defaultValue,
    getter = { proto ->
        val raw = getter(proto)
        enumValues.firstOrNull { e -> e.name == raw } ?: defaultValue
    },
    updater = { proto, value ->
        updater(proto, value.name)
    },
    defaultProtoValue = defaultProtoValue,
)
