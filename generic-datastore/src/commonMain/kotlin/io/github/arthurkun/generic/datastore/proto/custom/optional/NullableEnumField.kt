package io.github.arthurkun.generic.datastore.proto.custom.optional

import androidx.datastore.core.DataStore
import io.github.arthurkun.generic.datastore.proto.custom.ProtoSerialFieldPreference

internal fun <T, F : Enum<F>> nullableEnumFieldInternal(
    datastore: DataStore<T>,
    key: String,
    enumValues: Array<F>,
    getter: (T) -> String?,
    updater: (T, String?) -> T,
    defaultProtoValue: T,
): ProtoSerialFieldPreference<T, F?> = ProtoSerialFieldPreference(
    datastore = datastore,
    key = key,
    defaultValue = null,
    getter = { proto ->
        val raw = getter(proto)
        raw?.let { name -> enumValues.firstOrNull { e -> e.name == name } }
    },
    updater = { proto, value ->
        updater(proto, value?.name)
    },
    defaultProtoValue = defaultProtoValue,
)
