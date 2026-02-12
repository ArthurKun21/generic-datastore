@file:Suppress("unused")

package io.github.arthurkun.generic.datastore.proto

import androidx.datastore.core.DataStore
import io.github.arthurkun.generic.datastore.core.PreferenceDefaults
import io.github.arthurkun.generic.datastore.proto.core.GenericProtoPreferenceItem
import io.github.arthurkun.generic.datastore.proto.core.ProtoFieldPreference
import io.github.arthurkun.generic.datastore.proto.core.custom.enumFieldInternal
import io.github.arthurkun.generic.datastore.proto.core.custom.kserializedFieldInternal
import io.github.arthurkun.generic.datastore.proto.core.custom.kserializedListFieldInternal
import io.github.arthurkun.generic.datastore.proto.core.custom.serializedFieldInternal
import io.github.arthurkun.generic.datastore.proto.core.custom.serializedListFieldInternal
import io.github.arthurkun.generic.datastore.proto.core.customSet.enumSetFieldInternal
import io.github.arthurkun.generic.datastore.proto.core.customSet.kserializedSetFieldInternal
import io.github.arthurkun.generic.datastore.proto.core.customSet.serializedSetFieldInternal
import io.github.arthurkun.generic.datastore.proto.optional.custom.nullableEnumFieldInternal
import io.github.arthurkun.generic.datastore.proto.optional.custom.nullableKserializedFieldInternal
import io.github.arthurkun.generic.datastore.proto.optional.custom.nullableKserializedListFieldInternal
import io.github.arthurkun.generic.datastore.proto.optional.custom.nullableSerializedFieldInternal
import io.github.arthurkun.generic.datastore.proto.optional.custom.nullableSerializedListFieldInternal
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json

/**
 * A DataStore implementation for Proto DataStore.
 *
 * This class wraps a [DataStore<T>] instance for typed proto messages.
 *
 * @param T The proto message type.
 * @param datastore The underlying [DataStore<T>] instance.
 * @param defaultValue The default value for the proto message.
 */
public class GenericProtoDatastore<T>(
    internal val datastore: DataStore<T>,
    private val defaultValue: T,
    private val key: String = "proto_datastore",
    private val defaultJson: Json = PreferenceDefaults.defaultJson,
) : ProtoDatastore<T> {

    override fun data(): ProtoPreference<T> {
        return GenericProtoPreferenceItem(
            datastore = datastore,
            defaultValue = defaultValue,
            key = key,
        )
    }

    override fun <F> field(
        key: String,
        defaultValue: F,
        getter: (T) -> F,
        updater: (T, F) -> T,
    ): ProtoPreference<F> = ProtoFieldPrefs(
        ProtoFieldPreference(
            datastore = datastore,
            key = key,
            defaultValue = defaultValue,
            getter = getter,
            updater = updater,
            defaultProtoValue = this.defaultValue,
        ),
    )

    // --- Enum fields ---

    override fun <F : Enum<F>> enumField(
        key: String,
        defaultValue: F,
        enumValues: Array<F>,
        getter: (T) -> String,
        updater: (T, String) -> T,
    ): ProtoPreference<F> = enumFieldInternal(
        key = key,
        defaultValue = defaultValue,
        enumValues = enumValues,
        getter = getter,
        updater = updater,
    )

    override fun <F : Enum<F>> nullableEnumField(
        key: String,
        enumValues: Array<F>,
        getter: (T) -> String?,
        updater: (T, String?) -> T,
    ): ProtoPreference<F?> = nullableEnumFieldInternal(
        key = key,
        enumValues = enumValues,
        getter = getter,
        updater = updater,
    )

    override fun <F : Enum<F>> enumSetField(
        key: String,
        defaultValue: Set<F>,
        enumValues: Array<F>,
        getter: (T) -> Set<String>,
        updater: (T, Set<String>) -> T,
    ): ProtoPreference<Set<F>> = enumSetFieldInternal(
        key = key,
        defaultValue = defaultValue,
        enumValues = enumValues,
        getter = getter,
        updater = updater,
    )

    // --- KSerialized fields ---

    override fun <F> kserializedField(
        key: String,
        defaultValue: F,
        serializer: KSerializer<F>,
        json: Json?,
        getter: (T) -> String,
        updater: (T, String) -> T,
    ): ProtoPreference<F> = kserializedFieldInternal(
        key = key,
        defaultValue = defaultValue,
        serializer = serializer,
        json = json ?: defaultJson,
        getter = getter,
        updater = updater,
    )

    override fun <F : Any> nullableKserializedField(
        key: String,
        serializer: KSerializer<F>,
        json: Json?,
        getter: (T) -> String?,
        updater: (T, String?) -> T,
    ): ProtoPreference<F?> = nullableKserializedFieldInternal(
        key = key,
        serializer = serializer,
        json = json ?: defaultJson,
        getter = getter,
        updater = updater,
    )

    override fun <F> kserializedListField(
        key: String,
        defaultValue: List<F>,
        serializer: KSerializer<F>,
        json: Json?,
        getter: (T) -> String,
        updater: (T, String) -> T,
    ): ProtoPreference<List<F>> = kserializedListFieldInternal(
        key = key,
        defaultValue = defaultValue,
        serializer = serializer,
        json = json ?: defaultJson,
        getter = getter,
        updater = updater,
    )

    override fun <F> nullableKserializedListField(
        key: String,
        serializer: KSerializer<F>,
        json: Json?,
        getter: (T) -> String?,
        updater: (T, String?) -> T,
    ): ProtoPreference<List<F>?> = nullableKserializedListFieldInternal(
        key = key,
        serializer = serializer,
        json = json ?: defaultJson,
        getter = getter,
        updater = updater,
    )

    override fun <F> kserializedSetField(
        key: String,
        defaultValue: Set<F>,
        serializer: KSerializer<F>,
        json: Json?,
        getter: (T) -> Set<String>,
        updater: (T, Set<String>) -> T,
    ): ProtoPreference<Set<F>> = kserializedSetFieldInternal(
        key = key,
        defaultValue = defaultValue,
        serializer = serializer,
        json = json ?: defaultJson,
        getter = getter,
        updater = updater,
    )

    // --- Serialized fields (caller-provided functions) ---

    override fun <F> serializedField(
        key: String,
        defaultValue: F,
        serializer: (F) -> String,
        deserializer: (String) -> F,
        getter: (T) -> String,
        updater: (T, String) -> T,
    ): ProtoPreference<F> = serializedFieldInternal(
        key = key,
        defaultValue = defaultValue,
        serializer = serializer,
        deserializer = deserializer,
        getter = getter,
        updater = updater,
    )

    override fun <F : Any> nullableSerializedField(
        key: String,
        serializer: (F) -> String,
        deserializer: (String) -> F,
        getter: (T) -> String?,
        updater: (T, String?) -> T,
    ): ProtoPreference<F?> = nullableSerializedFieldInternal(
        key = key,
        serializer = serializer,
        deserializer = deserializer,
        getter = getter,
        updater = updater,
    )

    override fun <F> serializedListField(
        key: String,
        defaultValue: List<F>,
        elementSerializer: (F) -> String,
        elementDeserializer: (String) -> F,
        getter: (T) -> String,
        updater: (T, String) -> T,
    ): ProtoPreference<List<F>> = serializedListFieldInternal(
        key = key,
        defaultValue = defaultValue,
        elementSerializer = elementSerializer,
        elementDeserializer = elementDeserializer,
        getter = getter,
        updater = updater,
    )

    override fun <F> nullableSerializedListField(
        key: String,
        elementSerializer: (F) -> String,
        elementDeserializer: (String) -> F,
        getter: (T) -> String?,
        updater: (T, String?) -> T,
    ): ProtoPreference<List<F>?> = nullableSerializedListFieldInternal(
        key = key,
        elementSerializer = elementSerializer,
        elementDeserializer = elementDeserializer,
        getter = getter,
        updater = updater,
    )

    override fun <F> serializedSetField(
        key: String,
        defaultValue: Set<F>,
        serializer: (F) -> String,
        deserializer: (String) -> F,
        getter: (T) -> Set<String>,
        updater: (T, Set<String>) -> T,
    ): ProtoPreference<Set<F>> = serializedSetFieldInternal(
        key = key,
        defaultValue = defaultValue,
        serializer = serializer,
        deserializer = deserializer,
        getter = getter,
        updater = updater,
    )
}
