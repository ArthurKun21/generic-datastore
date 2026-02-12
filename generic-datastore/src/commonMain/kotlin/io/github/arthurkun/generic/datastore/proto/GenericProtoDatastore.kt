@file:Suppress("unused")

package io.github.arthurkun.generic.datastore.proto

import androidx.datastore.core.DataStore
import io.github.arthurkun.generic.datastore.core.PreferenceDefaults
import io.github.arthurkun.generic.datastore.proto.core.GenericProtoPreferenceItem
import io.github.arthurkun.generic.datastore.proto.core.ProtoFieldPreference
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import kotlin.coroutines.cancellation.CancellationException

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
    ): ProtoPreference<F> = field(
        key = key,
        defaultValue = defaultValue,
        getter = { proto ->
            val raw = getter(proto)
            safeDeserialize(raw, defaultValue) { name ->
                enumValues.first { it.name == name }
            }
        },
        updater = { proto, value ->
            updater(proto, value.name)
        },
    )

    override fun <F : Enum<F>> nullableEnumField(
        key: String,
        enumValues: Array<F>,
        getter: (T) -> String?,
        updater: (T, String?) -> T,
    ): ProtoPreference<F?> = field(
        key = key,
        defaultValue = null,
        getter = { proto ->
            val raw = getter(proto)
            raw?.let { safeDeserialize<F?>(it, null) { name -> enumValues.first { e -> e.name == name } } }
        },
        updater = { proto, value ->
            updater(proto, value?.name)
        },
    )

    override fun <F : Enum<F>> enumSetField(
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

    // --- KSerialized fields ---

    override fun <F> kserializedField(
        key: String,
        defaultValue: F,
        serializer: KSerializer<F>,
        json: Json?,
        getter: (T) -> String,
        updater: (T, String) -> T,
    ): ProtoPreference<F> {
        val jsonInstance = json ?: PreferenceDefaults.defaultJson
        return field(
            key = key,
            defaultValue = defaultValue,
            getter = { proto ->
                val raw = getter(proto)
                if (raw.isBlank()) {
                    defaultValue
                } else {
                    safeDeserialize(raw, defaultValue) { jsonInstance.decodeFromString(serializer, it) }
                }
            },
            updater = { proto, value ->
                updater(proto, jsonInstance.encodeToString(serializer, value))
            },
        )
    }

    override fun <F : Any> nullableKserializedField(
        key: String,
        serializer: KSerializer<F>,
        json: Json?,
        getter: (T) -> String?,
        updater: (T, String?) -> T,
    ): ProtoPreference<F?> {
        val jsonInstance = json ?: PreferenceDefaults.defaultJson
        return field(
            key = key,
            defaultValue = null,
            getter = { proto ->
                val raw = getter(proto)
                raw?.let { safeDeserialize<F?>(it, null) { s -> jsonInstance.decodeFromString(serializer, s) } }
            },
            updater = { proto, value ->
                updater(proto, value?.let { jsonInstance.encodeToString(serializer, it) })
            },
        )
    }

    override fun <F> kserializedListField(
        key: String,
        defaultValue: List<F>,
        serializer: KSerializer<F>,
        json: Json?,
        getter: (T) -> String,
        updater: (T, String) -> T,
    ): ProtoPreference<List<F>> {
        val jsonInstance = json ?: PreferenceDefaults.defaultJson
        val listSerializer = ListSerializer(serializer)
        return field(
            key = key,
            defaultValue = defaultValue,
            getter = { proto ->
                val raw = getter(proto)
                if (raw.isBlank()) {
                    defaultValue
                } else {
                    safeDeserialize(raw, defaultValue) { jsonInstance.decodeFromString(listSerializer, it) }
                }
            },
            updater = { proto, value ->
                updater(proto, jsonInstance.encodeToString(listSerializer, value))
            },
        )
    }

    override fun <F> nullableKserializedListField(
        key: String,
        serializer: KSerializer<F>,
        json: Json?,
        getter: (T) -> String?,
        updater: (T, String?) -> T,
    ): ProtoPreference<List<F>?> {
        val jsonInstance = json ?: PreferenceDefaults.defaultJson
        val listSerializer = ListSerializer(serializer)
        return field(
            key = key,
            defaultValue = null,
            getter = { proto ->
                val raw = getter(proto)
                raw?.let {
                    safeDeserialize<List<F>?>(it, null) { s -> jsonInstance.decodeFromString(listSerializer, s) }
                }
            },
            updater = { proto, value ->
                updater(proto, value?.let { jsonInstance.encodeToString(listSerializer, it) })
            },
        )
    }

    override fun <F> kserializedSetField(
        key: String,
        defaultValue: Set<F>,
        serializer: KSerializer<F>,
        json: Json?,
        getter: (T) -> Set<String>,
        updater: (T, Set<String>) -> T,
    ): ProtoPreference<Set<F>> {
        val jsonInstance = json ?: PreferenceDefaults.defaultJson
        return field(
            key = key,
            defaultValue = defaultValue,
            getter = { proto ->
                getter(proto).mapNotNull { raw ->
                    safeDeserialize<F?>(raw, null) { jsonInstance.decodeFromString(serializer, it) }
                }.toSet()
            },
            updater = { proto, value ->
                updater(proto, value.map { jsonInstance.encodeToString(serializer, it) }.toSet())
            },
        )
    }

    // --- Serialized fields (caller-provided functions) ---

    override fun <F> serializedField(
        key: String,
        defaultValue: F,
        serializer: (F) -> String,
        deserializer: (String) -> F,
        getter: (T) -> String,
        updater: (T, String) -> T,
    ): ProtoPreference<F> = field(
        key = key,
        defaultValue = defaultValue,
        getter = { proto ->
            val raw = getter(proto)
            if (raw.isBlank()) {
                defaultValue
            } else {
                safeDeserialize(raw, defaultValue) { deserializer(it) }
            }
        },
        updater = { proto, value ->
            updater(proto, serializer(value))
        },
    )

    override fun <F : Any> nullableSerializedField(
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

    override fun <F> serializedListField(
        key: String,
        defaultValue: List<F>,
        elementSerializer: (F) -> String,
        elementDeserializer: (String) -> F,
        getter: (T) -> String,
        updater: (T, String) -> T,
    ): ProtoPreference<List<F>> {
        val jsonInstance = PreferenceDefaults.defaultJson
        return field(
            key = key,
            defaultValue = defaultValue,
            getter = { proto ->
                val raw = getter(proto)
                if (raw.isBlank()) {
                    defaultValue
                } else {
                    safeDeserialize(raw, defaultValue) { rawStr ->
                        val jsonArray = jsonInstance.decodeFromString<List<String>>(rawStr)
                        jsonArray.map { elementDeserializer(it) }
                    }
                }
            },
            updater = { proto, value ->
                val jsonArray = value.map { elementSerializer(it) }
                updater(proto, jsonInstance.encodeToString(jsonArray))
            },
        )
    }

    override fun <F> nullableSerializedListField(
        key: String,
        elementSerializer: (F) -> String,
        elementDeserializer: (String) -> F,
        getter: (T) -> String?,
        updater: (T, String?) -> T,
    ): ProtoPreference<List<F>?> {
        val jsonInstance = PreferenceDefaults.defaultJson
        return field(
            key = key,
            defaultValue = null,
            getter = { proto ->
                val raw = getter(proto)
                raw?.let {
                    safeDeserialize<List<F>?>(it, null) { rawStr ->
                        val jsonArray = jsonInstance.decodeFromString<List<String>>(rawStr)
                        jsonArray.map { elem -> elementDeserializer(elem) }
                    }
                }
            },
            updater = { proto, value ->
                updater(
                    proto,
                    value?.let { list ->
                        val jsonArray = list.map { elementSerializer(it) }
                        jsonInstance.encodeToString(jsonArray)
                    },
                )
            },
        )
    }

    override fun <F> serializedSetField(
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
}

private inline fun <T> safeDeserialize(
    raw: String,
    fallback: T,
    deserialize: (String) -> T,
): T = try {
    deserialize(raw)
} catch (e: CancellationException) {
    throw e
} catch (_: Exception) {
    fallback
}
