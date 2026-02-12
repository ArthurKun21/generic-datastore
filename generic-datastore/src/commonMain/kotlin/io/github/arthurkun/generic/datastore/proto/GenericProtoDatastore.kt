@file:Suppress("unused")

package io.github.arthurkun.generic.datastore.proto

import androidx.datastore.core.DataStore
import io.github.arthurkun.generic.datastore.core.PreferenceDefaults
import io.github.arthurkun.generic.datastore.proto.core.GenericProtoPreferenceItem
import io.github.arthurkun.generic.datastore.proto.core.ProtoFieldPreference
import io.github.arthurkun.generic.datastore.proto.custom.core.enumFieldInternal
import io.github.arthurkun.generic.datastore.proto.custom.core.kserializedFieldInternal
import io.github.arthurkun.generic.datastore.proto.custom.core.kserializedListFieldInternal
import io.github.arthurkun.generic.datastore.proto.custom.core.serializedFieldInternal
import io.github.arthurkun.generic.datastore.proto.custom.core.serializedListFieldInternal
import io.github.arthurkun.generic.datastore.proto.custom.optional.nullableEnumFieldInternal
import io.github.arthurkun.generic.datastore.proto.custom.optional.nullableKserializedFieldInternal
import io.github.arthurkun.generic.datastore.proto.custom.optional.nullableKserializedListFieldInternal
import io.github.arthurkun.generic.datastore.proto.custom.optional.nullableSerializedFieldInternal
import io.github.arthurkun.generic.datastore.proto.custom.optional.nullableSerializedListFieldInternal
import io.github.arthurkun.generic.datastore.proto.custom.set.enumSetFieldInternal
import io.github.arthurkun.generic.datastore.proto.custom.set.kserializedSetFieldInternal
import io.github.arthurkun.generic.datastore.proto.custom.set.serializedSetFieldInternal
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
        defaultValue: F,
        enumValues: Array<F>,
        getter: (T) -> String,
        updater: (T, String) -> T,
    ): ProtoPreference<F> = ProtoFieldPrefs(
        enumFieldInternal(
            datastore = this.datastore,
            key = key,
            defaultValue = defaultValue,
            enumValues = enumValues,
            getter = getter,
            updater = updater,
            defaultProtoValue = this.defaultValue,
        )
    )

    override fun <F : Enum<F>> nullableEnumField(

        enumValues: Array<F>,
        getter: (T) -> String?,
        updater: (T, String?) -> T,
    ): ProtoPreference<F?> = ProtoFieldPrefs(
        nullableEnumFieldInternal(
            datastore = this.datastore,
            key = key,
            enumValues = enumValues,
            getter = getter,
            updater = updater,
            defaultProtoValue = this.defaultValue,
        )
    )

    override fun <F : Enum<F>> enumSetField(

        defaultValue: Set<F>,
        enumValues: Array<F>,
        getter: (T) -> Set<String>,
        updater: (T, Set<String>) -> T,
    ): ProtoPreference<Set<F>> = ProtoFieldPrefs(
        enumSetFieldInternal(
            datastore = this.datastore,
            key = key,
            defaultValue = defaultValue,
            enumValues = enumValues,
            getter = getter,
            updater = updater,
            defaultProtoValue = this.defaultValue,
        )
    )

    // --- KSerialized fields ---

    override fun <F> kserializedField(

        defaultValue: F,
        serializer: KSerializer<F>,
        json: Json?,
        getter: (T) -> String,
        updater: (T, String) -> T,
    ): ProtoPreference<F> = ProtoFieldPrefs(
        kserializedFieldInternal(
            datastore = this.datastore,
            key = key,
            defaultValue = defaultValue,
            serializer = serializer,
            json = json ?: defaultJson,
            getter = getter,
            updater = updater,
            defaultProtoValue = this.defaultValue,
        )
    )

    override fun <F : Any> nullableKserializedField(

        serializer: KSerializer<F>,
        json: Json?,
        getter: (T) -> String?,
        updater: (T, String?) -> T,
    ): ProtoPreference<F?> = ProtoFieldPrefs(
        nullableKserializedFieldInternal(
            datastore = this.datastore,
            key = key,
            serializer = serializer,
            json = json ?: defaultJson,
            getter = getter,
            updater = updater,
            defaultProtoValue = this.defaultValue,
        )
    )

    override fun <F> kserializedListField(

        defaultValue: List<F>,
        serializer: KSerializer<F>,
        json: Json?,
        getter: (T) -> String,
        updater: (T, String) -> T,
    ): ProtoPreference<List<F>> = ProtoFieldPrefs(
        kserializedListFieldInternal(
            datastore = this.datastore,
            key = key,
            defaultValue = defaultValue,
            serializer = serializer,
            json = json ?: defaultJson,
            getter = getter,
            updater = updater,
            defaultProtoValue = this.defaultValue,
        )
    )

    override fun <F> nullableKserializedListField(

        serializer: KSerializer<F>,
        json: Json?,
        getter: (T) -> String?,
        updater: (T, String?) -> T,
    ): ProtoPreference<List<F>?> = ProtoFieldPrefs(
        nullableKserializedListFieldInternal(
            datastore = this.datastore,
            key = key,
            serializer = serializer,
            json = json ?: defaultJson,
            getter = getter,
            updater = updater,
            defaultProtoValue = this.defaultValue,
        )
    )

    override fun <F> kserializedSetField(

        defaultValue: Set<F>,
        serializer: KSerializer<F>,
        json: Json?,
        getter: (T) -> Set<String>,
        updater: (T, Set<String>) -> T,
    ): ProtoPreference<Set<F>> = ProtoFieldPrefs(
        kserializedSetFieldInternal(
            datastore = this.datastore,
            key = key,
            defaultValue = defaultValue,
            serializer = serializer,
            json = json ?: defaultJson,
            getter = getter,
            updater = updater,
            defaultProtoValue = this.defaultValue,
        )
    )

    // --- Serialized fields (caller-provided functions) ---

    override fun <F> serializedField(

        defaultValue: F,
        serializer: (F) -> String,
        deserializer: (String) -> F,
        getter: (T) -> String,
        updater: (T, String) -> T,
    ): ProtoPreference<F> = ProtoFieldPrefs(
        serializedFieldInternal(
            datastore = this.datastore,
            key = key,
            defaultValue = defaultValue,
            serializer = serializer,
            deserializer = deserializer,
            getter = getter,
            updater = updater,
            defaultProtoValue = this.defaultValue,
        )
    )

    override fun <F : Any> nullableSerializedField(

        serializer: (F) -> String,
        deserializer: (String) -> F,
        getter: (T) -> String?,
        updater: (T, String?) -> T,
    ): ProtoPreference<F?> = ProtoFieldPrefs(
        nullableSerializedFieldInternal(
            datastore = this.datastore,
            key = key,
            serializer = serializer,
            deserializer = deserializer,
            getter = getter,
            updater = updater,
            defaultProtoValue = this.defaultValue,
        )
    )

    override fun <F> serializedListField(

        defaultValue: List<F>,
        elementSerializer: (F) -> String,
        elementDeserializer: (String) -> F,
        getter: (T) -> String,
        updater: (T, String) -> T,
    ): ProtoPreference<List<F>> = ProtoFieldPrefs(
        serializedListFieldInternal(
            datastore = this.datastore,
            key = key,
            defaultValue = defaultValue,
            elementSerializer = elementSerializer,
            elementDeserializer = elementDeserializer,
            getter = getter,
            updater = updater,
            defaultProtoValue = this.defaultValue,
        )
    )

    override fun <F> nullableSerializedListField(

        elementSerializer: (F) -> String,
        elementDeserializer: (String) -> F,
        getter: (T) -> String?,
        updater: (T, String?) -> T,
    ): ProtoPreference<List<F>?> = ProtoFieldPrefs(
        nullableSerializedListFieldInternal(
            datastore = this.datastore,
            key = key,
            elementSerializer = elementSerializer,
            elementDeserializer = elementDeserializer,
            getter = getter,
            updater = updater,
            defaultProtoValue = this.defaultValue,
        )
    )

    override fun <F> serializedSetField(
        defaultValue: Set<F>,
        serializer: (F) -> String,
        deserializer: (String) -> F,
        getter: (T) -> Set<String>,
        updater: (T, Set<String>) -> T,
    ): ProtoPreference<Set<F>> = ProtoFieldPrefs(
        serializedSetFieldInternal(
            datastore = this.datastore,
            key = key,
            defaultValue = defaultValue,
            serializer = serializer,
            deserializer = deserializer,
            getter = getter,
            updater = updater,
            defaultProtoValue = this.defaultValue,
        )
    )
}
