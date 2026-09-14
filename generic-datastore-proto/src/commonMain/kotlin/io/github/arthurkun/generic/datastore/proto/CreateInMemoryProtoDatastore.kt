@file:Suppress("unused")

package io.github.arthurkun.generic.datastore.proto

import androidx.datastore.core.okio.OkioSerializer
import io.github.arthurkun.generic.datastore.core.InMemoryDataStore
import io.github.arthurkun.generic.datastore.core.InternalGenericDatastoreApi
import io.github.arthurkun.generic.datastore.core.PreferenceDefaults
import kotlinx.serialization.json.Json

/**
 * Creates a [ProtoDatastore] backed entirely by memory instead of a file.
 *
 * Intended for unit tests and previews: the proto message lives for as long as the returned
 * instance, and `close()` is a no-op. Migrations and file corruption handling do not apply —
 * there is no file. [exportAsByteArray] is unsupported (it reads the datastore file), while
 * [ProtoDatastore.importFromByteArray] works because it only performs an in-memory update.
 *
 * ```kotlin
 * val datastore = createInMemoryProtoDatastore(
 *     serializer = MyMessageSerializer,
 *     defaultValue = MyMessage(),
 * )
 * ```
 *
 * @param T The proto message type.
 * @param serializer The [OkioSerializer] for [T]; used by [ProtoDatastore.importFromByteArray].
 * @param defaultValue The default value for the proto message.
 * @param defaultJson The fallback [Json] instance for Kotlin-serialization-backed fields.
 * @return A [GenericProtoDatastore] with no on-disk storage.
 */
@OptIn(InternalGenericDatastoreApi::class)
public fun <T> createInMemoryProtoDatastore(
    serializer: OkioSerializer<T>,
    defaultValue: T,
    defaultJson: Json = PreferenceDefaults.defaultJson,
): GenericProtoDatastore<T> = GenericProtoDatastore(
    datastore = InMemoryDataStore(defaultValue),
    defaultValue = defaultValue,
    serializer = serializer,
    defaultJson = defaultJson,
)
