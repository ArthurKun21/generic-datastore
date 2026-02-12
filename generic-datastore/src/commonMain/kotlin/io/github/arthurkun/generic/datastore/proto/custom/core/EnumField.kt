package io.github.arthurkun.generic.datastore.proto.custom.core

import io.github.arthurkun.generic.datastore.core.BasePreference
import io.github.arthurkun.generic.datastore.proto.ProtoDatastore
import io.github.arthurkun.generic.datastore.proto.core.custom.safeDeserialize
import io.github.arthurkun.generic.datastore.proto.custom.ProtoSerialFieldPreference

internal fun <P, T, F : Enum<F>> enumFieldInternal(
    datastore: ProtoDatastore<P>,
    key: String,
    defaultValue: F,
    enumValues: Array<F>,
    getter: (T) -> String,
    updater: (T, String) -> T,
): BasePreference<F> = ProtoSerialFieldPreference(
    datastore = datastore,
    key = key,
    defaultValue = defaultValue,
    getter = { proto ->
        val raw = getter(proto)
        safeDeserialize<F>(raw, defaultValue) { name -> enumValues.first { e -> e.name == name } }
    },
    updater = { proto, value ->
        updater(proto, value.name)
    },
)
