@file:Suppress("unused")

package io.github.arthurkun.generic.datastore.proto

import androidx.datastore.core.DataMigration
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.core.okio.OkioSerializer
import androidx.datastore.core.okio.OkioStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import okio.FileSystem
import okio.Path.Companion.toPath
import kotlin.jvm.JvmName
import kotlinx.io.files.Path as KotlinxIoPath

/**
 * Creates a [GenericProtoDatastore] using an [OkioSerializer] and a path producer
 * that returns a [String].
 *
 * The user is responsible for providing the [OkioSerializer] and [defaultValue]
 * appropriate for the proto/typed DataStore schema.
 *
 * @param T The proto message or typed data type.
 * @param serializer The [OkioSerializer] for the type [T].
 * @param defaultValue The default value for the proto message.
 * @param key The key identifier for this proto datastore.
 * @param corruptionHandler An optional [ReplaceFileCorruptionHandler] to handle data corruption.
 * @param migrations A list of [DataMigration] to apply when the DataStore is created.
 * @param scope The [CoroutineScope] to use for DataStore operations.
 * @param producePath A lambda that returns the full file path as a [String].
 * @return A new [GenericProtoDatastore] instance.
 */
public fun <T> createProtoDatastore(
    serializer: OkioSerializer<T>,
    defaultValue: T,
    key: String = "proto_datastore",
    corruptionHandler: ReplaceFileCorruptionHandler<T>? = null,
    migrations: List<DataMigration<T>> = emptyList(),
    scope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
    producePath: () -> String,
): GenericProtoDatastore<T> {
    val datastore = DataStoreFactory.create(
        storage = OkioStorage(
            fileSystem = FileSystem.SYSTEM,
            serializer = serializer,
            producePath = { producePath().toPath() },
        ),
        corruptionHandler = corruptionHandler,
        migrations = migrations,
        scope = scope,
    )
    return GenericProtoDatastore(
        datastore = datastore,
        defaultValue = defaultValue,
        key = key,
    )
}

/**
 * Creates a [GenericProtoDatastore] using an [OkioSerializer] and an [okio.Path] producer.
 *
 * @param T The proto message or typed data type.
 * @param serializer The [OkioSerializer] for the type [T].
 * @param defaultValue The default value for the proto message.
 * @param key The key identifier for this proto datastore.
 * @param corruptionHandler An optional [ReplaceFileCorruptionHandler] to handle data corruption.
 * @param migrations A list of [DataMigration] to apply when the DataStore is created.
 * @param scope The [CoroutineScope] to use for DataStore operations.
 * @param produceOkioPath A lambda that returns the file path as an [okio.Path].
 * @return A new [GenericProtoDatastore] instance.
 */
@JvmName("createProtoDatastoreWithOkioPath")
public fun <T> createProtoDatastore(
    serializer: OkioSerializer<T>,
    defaultValue: T,
    key: String = "proto_datastore",
    corruptionHandler: ReplaceFileCorruptionHandler<T>? = null,
    migrations: List<DataMigration<T>> = emptyList(),
    scope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
    produceOkioPath: () -> okio.Path,
): GenericProtoDatastore<T> {
    val datastore = DataStoreFactory.create(
        storage = OkioStorage(
            fileSystem = FileSystem.SYSTEM,
            serializer = serializer,
            producePath = produceOkioPath,
        ),
        corruptionHandler = corruptionHandler,
        migrations = migrations,
        scope = scope,
    )
    return GenericProtoDatastore(
        datastore = datastore,
        defaultValue = defaultValue,
        key = key,
    )
}

/**
 * Creates a [GenericProtoDatastore] using an [OkioSerializer] and a [kotlinx.io.files.Path]
 * producer.
 *
 * @param T The proto message or typed data type.
 * @param serializer The [OkioSerializer] for the type [T].
 * @param defaultValue The default value for the proto message.
 * @param key The key identifier for this proto datastore.
 * @param corruptionHandler An optional [ReplaceFileCorruptionHandler] to handle data corruption.
 * @param migrations A list of [DataMigration] to apply when the DataStore is created.
 * @param scope The [CoroutineScope] to use for DataStore operations.
 * @param produceKotlinxIoPath A lambda that returns the file path as a [kotlinx.io.files.Path].
 * @return A new [GenericProtoDatastore] instance.
 */
@JvmName("createProtoDatastoreWithKotlinxIoPath")
public fun <T> createProtoDatastore(
    serializer: OkioSerializer<T>,
    defaultValue: T,
    key: String = "proto_datastore",
    corruptionHandler: ReplaceFileCorruptionHandler<T>? = null,
    migrations: List<DataMigration<T>> = emptyList(),
    scope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
    produceKotlinxIoPath: () -> KotlinxIoPath,
): GenericProtoDatastore<T> {
    val datastore = DataStoreFactory.create(
        storage = OkioStorage(
            fileSystem = FileSystem.SYSTEM,
            serializer = serializer,
            producePath = { produceKotlinxIoPath().toString().toPath() },
        ),
        corruptionHandler = corruptionHandler,
        migrations = migrations,
        scope = scope,
    )
    return GenericProtoDatastore(
        datastore = datastore,
        defaultValue = defaultValue,
        key = key,
    )
}

/**
 * Creates a [GenericProtoDatastore] using an [OkioSerializer], a directory path,
 * and a file name.
 *
 * The [fileName] will be appended to the path produced by [producePath].
 *
 * @param T The proto message or typed data type.
 * @param serializer The [OkioSerializer] for the type [T].
 * @param defaultValue The default value for the proto message.
 * @param fileName The name of the DataStore file.
 * @param key The key identifier for this proto datastore.
 * @param corruptionHandler An optional [ReplaceFileCorruptionHandler] to handle data corruption.
 * @param migrations A list of [DataMigration] to apply when the DataStore is created.
 * @param scope The [CoroutineScope] to use for DataStore operations.
 * @param producePath A lambda that returns the directory path as a [String].
 * @return A new [GenericProtoDatastore] instance.
 */
public fun <T> createProtoDatastore(
    serializer: OkioSerializer<T>,
    defaultValue: T,
    fileName: String,
    key: String = "proto_datastore",
    corruptionHandler: ReplaceFileCorruptionHandler<T>? = null,
    migrations: List<DataMigration<T>> = emptyList(),
    scope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
    producePath: () -> String,
): GenericProtoDatastore<T> {
    val datastore = DataStoreFactory.create(
        storage = OkioStorage(
            fileSystem = FileSystem.SYSTEM,
            serializer = serializer,
            producePath = { "${producePath()}/$fileName".toPath() },
        ),
        corruptionHandler = corruptionHandler,
        migrations = migrations,
        scope = scope,
    )
    return GenericProtoDatastore(
        datastore = datastore,
        defaultValue = defaultValue,
        key = key,
    )
}
