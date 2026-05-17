package io.github.arthurkun.generic.datastore.proto.custom.optional

import androidx.datastore.core.DataStore
import io.github.arthurkun.generic.datastore.proto.custom.ProtoSerialFieldPreference
import io.github.arthurkun.generic.datastore.proto.custom.core.safeDeserialize

internal fun <T, F : Any> nullableSerializedFieldInternal(
    datastore: DataStore<T>,
    key: String,
    serializer: (F) -> String,
    deserializer: (String) -> F,
    getter: (T) -> String?,
    updater: (T, String?) -> T,
    defaultProtoValue: T,
): ProtoSerialFieldPreference<T, F?> = ProtoSerialFieldPreference(
    datastore = datastore,
    key = key,
    defaultValue = null,
    getter = { proto ->
        val raw = getter(proto)
        raw?.let { safeDeserialize<F?>(it, null) { s -> deserializer(s) } }
    },
    updater = { proto, value ->
        updater(proto, value?.let { serializer(it) })
    },
    defaultProtoValue = defaultProtoValue,
)
