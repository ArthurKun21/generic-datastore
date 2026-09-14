@file:Suppress("unused")

package io.github.arthurkun.generic.datastore.proto

import androidx.datastore.core.DataStore
import androidx.datastore.core.okio.OkioSerializer
import io.github.arthurkun.generic.datastore.core.InternalGenericDatastoreApi
import io.github.arthurkun.generic.datastore.core.PreferenceDefaults
import io.github.arthurkun.generic.datastore.proto.backup.ProtoBackupCreator
import io.github.arthurkun.generic.datastore.proto.backup.ProtoBackupRestorer
import io.github.arthurkun.generic.datastore.proto.batch.ProtoUpdateScope
import io.github.arthurkun.generic.datastore.proto.batch.ProtoWriteScope
import io.github.arthurkun.generic.datastore.proto.core.GenericProtoPreferenceItem
import io.github.arthurkun.generic.datastore.proto.custom.ProtoSerialFieldPreference
import io.github.arthurkun.generic.datastore.proto.custom.core.enumFieldInternal
import io.github.arthurkun.generic.datastore.proto.custom.core.kserializedFieldInternal
import io.github.arthurkun.generic.datastore.proto.custom.core.kserializedListFieldInternal
import io.github.arthurkun.generic.datastore.proto.custom.core.kserializedMapFieldInternal
import io.github.arthurkun.generic.datastore.proto.custom.core.serializedFieldInternal
import io.github.arthurkun.generic.datastore.proto.custom.core.serializedListFieldInternal
import io.github.arthurkun.generic.datastore.proto.custom.core.serializedMapFieldInternal
import io.github.arthurkun.generic.datastore.proto.custom.optional.nullableEnumFieldInternal
import io.github.arthurkun.generic.datastore.proto.custom.optional.nullableKserializedFieldInternal
import io.github.arthurkun.generic.datastore.proto.custom.optional.nullableKserializedListFieldInternal
import io.github.arthurkun.generic.datastore.proto.custom.optional.nullableKserializedMapFieldInternal
import io.github.arthurkun.generic.datastore.proto.custom.optional.nullableSerializedFieldInternal
import io.github.arthurkun.generic.datastore.proto.custom.optional.nullableSerializedListFieldInternal
import io.github.arthurkun.generic.datastore.proto.custom.optional.nullableSerializedMapFieldInternal
import io.github.arthurkun.generic.datastore.proto.custom.set.enumSetFieldInternal
import io.github.arthurkun.generic.datastore.proto.custom.set.kserializedSetFieldInternal
import io.github.arthurkun.generic.datastore.proto.custom.set.serializedSetFieldInternal
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import okio.Path
import kotlin.concurrent.Volatile

/**
 * A DataStore implementation for Proto DataStore.
 *
 * This class wraps a [DataStore<T>] instance for typed proto messages.
 *
 * Direct construction is a low-level wiring API. Prefer [createProtoDatastore] unless you already
 * own the underlying [DataStore<T>].
 *
 * @param T The proto message type.
 * @param datastore The underlying [DataStore<T>] instance.
 * @param defaultValue The default value for the proto message.
 * @param ownedScope The scope owned by this wrapper when it creates the underlying [DataStore].
 * @param serializer The serializer used by byte-array restore for factory-created datastores.
 * @param path The resolved datastore file path used by byte-array backup for factory-created datastores.
 */
public class GenericProtoDatastore<T> @InternalGenericDatastoreApi constructor(
    internal val datastore: DataStore<T>,
    private val defaultValue: T,
    private val key: String = "proto_datastore",
    private val defaultJson: Json = PreferenceDefaults.defaultJson,
    private val onDecodeFailure: ((String, Throwable) -> Unit)? = null,
    private val ownedScope: CoroutineScope? = null,
    private val serializer: OkioSerializer<T>? = null,
    private val path: Path? = null,
) : ProtoDatastore<T> {

    private val cachedData: ProtoPreference<T> by lazy {
        GenericProtoPreferenceItem(
            datastore = datastore,
            defaultValue = defaultValue,
            key = key,
        )
    }

    // Copy-on-write cache: writers publish a fresh immutable map, so readers never see a torn
    // structure. Two concurrent first-time writers of different names may drop one entry; the
    // next `cached` call for that name rebuilds it.
    @Volatile
    private var fieldCache: Map<String, ProtoPreference<*>> = emptyMap()

    override fun <F> cached(name: String, factory: () -> ProtoPreference<F>): ProtoPreference<F> {
        require(name.isNotBlank()) { "Cache name cannot be blank." }
        @Suppress("UNCHECKED_CAST")
        fieldCache[name]?.let { return it as ProtoPreference<F> }
        val built = factory()
        @Suppress("UNCHECKED_CAST")
        fieldCache = fieldCache + (name to built as ProtoPreference<*>)
        return built
    }

    override fun data(): ProtoPreference<T> = cachedData

    override fun close() {
        runBlocking {
            ownedScope?.coroutineContext?.get(Job)?.cancelAndJoin()
        }
    }

    override suspend fun exportAsByteArray(): ByteArray {
        val resolvedPath = path
            ?: throw UnsupportedOperationException("Byte-array backup requires a resolved datastore path.")
        datastore.data.first()
        return ProtoBackupCreator(resolvedPath).exportAsByteArray()
    }

    override suspend fun importFromByteArray(data: ByteArray) {
        val resolvedSerializer = serializer
            ?: throw UnsupportedOperationException("Byte-array restore requires an OkioSerializer.")
        ProtoBackupRestorer(
            datastore = datastore,
            serializer = resolvedSerializer,
        ).importFromByteArray(data)
    }

    override fun <F> field(
        defaultValue: F,
        getter: (T) -> F,
        updater: (T, F) -> T,
    ): ProtoPreference<F> = ProtoFieldPrefs(
        ProtoSerialFieldPreference(
            datastore = datastore,
            key = key,
            defaultValue = defaultValue,
            getter = getter,
            updater = updater,
            defaultProtoValue = this.defaultValue,
            onDecodeFailure = onDecodeFailure,
        ),
    )

    override fun <F : Any> nullableField(
        getter: (T) -> F?,
        updater: (T, F?) -> T,
    ): ProtoPreference<F?> = ProtoFieldPrefs(
        ProtoSerialFieldPreference(
            datastore = datastore,
            key = key,
            defaultValue = null,
            getter = getter,
            updater = updater,
            defaultProtoValue = this.defaultValue,
            onDecodeFailure = onDecodeFailure,
        ),
    )

    // --- Batch operations ---

    override suspend fun batchWrite(block: ProtoWriteScope<T>.() -> Unit) {
        batchUpdate(block)
    }

    override suspend fun batchUpdate(block: ProtoUpdateScope<T>.() -> Unit) {
        withContext(Dispatchers.IO) {
            datastore.updateData { current ->
                ProtoUpdateScope(datastore, current).apply(block).currentProto()
            }
        }
    }

    override fun batchWriteBlocking(block: ProtoWriteScope<T>.() -> Unit): Unit =
        runBlocking { batchWrite(block) }

    override fun batchUpdateBlocking(block: ProtoUpdateScope<T>.() -> Unit): Unit =
        runBlocking { batchUpdate(block) }

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
            onDecodeFailure = onDecodeFailure,
        ),
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
            onDecodeFailure = onDecodeFailure,
        ),
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
            onDecodeFailure = onDecodeFailure,
        ),
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
            onDecodeFailure = onDecodeFailure,
        ),
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
            onDecodeFailure = onDecodeFailure,
        ),
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
            onDecodeFailure = onDecodeFailure,
        ),
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
            onDecodeFailure = onDecodeFailure,
        ),
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
            onDecodeFailure = onDecodeFailure,
        ),
    )

    override fun <K, V> kserializedMapField(
        defaultValue: Map<K, V>,
        keySerializer: KSerializer<K>,
        valueSerializer: KSerializer<V>,
        json: Json?,
        getter: (T) -> String,
        updater: (T, String) -> T,
    ): ProtoPreference<Map<K, V>> = ProtoFieldPrefs(
        kserializedMapFieldInternal(
            datastore = this.datastore,
            key = key,
            defaultValue = defaultValue,
            keySerializer = keySerializer,
            valueSerializer = valueSerializer,
            json = json ?: defaultJson,
            getter = getter,
            updater = updater,
            defaultProtoValue = this.defaultValue,
            onDecodeFailure = onDecodeFailure,
        ),
    )

    override fun <K, V> nullableKserializedMapField(
        keySerializer: KSerializer<K>,
        valueSerializer: KSerializer<V>,
        json: Json?,
        getter: (T) -> String?,
        updater: (T, String?) -> T,
    ): ProtoPreference<Map<K, V>?> = ProtoFieldPrefs(
        nullableKserializedMapFieldInternal(
            datastore = this.datastore,
            key = key,
            keySerializer = keySerializer,
            valueSerializer = valueSerializer,
            json = json ?: defaultJson,
            getter = getter,
            updater = updater,
            defaultProtoValue = this.defaultValue,
            onDecodeFailure = onDecodeFailure,
        ),
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
            onDecodeFailure = onDecodeFailure,
        ),
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
            onDecodeFailure = onDecodeFailure,
        ),
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
            json = defaultJson,
        ),
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
            json = defaultJson,
        ),
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
            onDecodeFailure = onDecodeFailure,
        ),
    )

    override fun <K, V> serializedMapField(
        defaultValue: Map<K, V>,
        keySerializer: (K) -> String,
        keyDeserializer: (String) -> K,
        valueSerializer: (V) -> String,
        valueDeserializer: (String) -> V,
        getter: (T) -> String,
        updater: (T, String) -> T,
    ): ProtoPreference<Map<K, V>> = ProtoFieldPrefs(
        serializedMapFieldInternal(
            datastore = this.datastore,
            key = key,
            defaultValue = defaultValue,
            keySerializer = keySerializer,
            keyDeserializer = keyDeserializer,
            valueSerializer = valueSerializer,
            valueDeserializer = valueDeserializer,
            getter = getter,
            updater = updater,
            defaultProtoValue = this.defaultValue,
            json = defaultJson,
            onDecodeFailure = onDecodeFailure,
        ),
    )

    override fun <K, V> nullableSerializedMapField(
        keySerializer: (K) -> String,
        keyDeserializer: (String) -> K,
        valueSerializer: (V) -> String,
        valueDeserializer: (String) -> V,
        getter: (T) -> String?,
        updater: (T, String?) -> T,
    ): ProtoPreference<Map<K, V>?> = ProtoFieldPrefs(
        nullableSerializedMapFieldInternal(
            datastore = this.datastore,
            key = key,
            keySerializer = keySerializer,
            keyDeserializer = keyDeserializer,
            valueSerializer = valueSerializer,
            valueDeserializer = valueDeserializer,
            getter = getter,
            updater = updater,
            defaultProtoValue = this.defaultValue,
            json = defaultJson,
            onDecodeFailure = onDecodeFailure,
        ),
    )
}
