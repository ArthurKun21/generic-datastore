@file:Suppress("unused")

package io.github.arthurkun.generic.datastore.proto

import androidx.datastore.core.DataMigration
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.core.okio.OkioSerializer
import androidx.datastore.core.okio.OkioStorage
import io.github.arthurkun.generic.datastore.core.PreferenceDefaults
import io.github.arthurkun.generic.datastore.core.systemFileSystem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import kotlinx.serialization.json.Json
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
 * @param scope The [CoroutineScope] to use for DataStore operations. Defaults to an unmanaged
 *   `CoroutineScope(Dispatchers.IO + SupervisorJob())`, which is suitable for application-level
 *   singletons. To control the DataStore lifecycle (e.g., in a scoped component or for testing),
 *   pass a custom scope and cancel it when no longer needed:
 *   ```
 *   val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
 *   val datastore = createProtoDatastore(scope = scope, ...) { "/path/to/file" }
 *   // When done:
 *   scope.cancel()
 *   ```
 * @param defaultJson The default [Json] instance to use for Kotlin Serialization-based fields in this proto datastore.
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
    defaultJson: Json = PreferenceDefaults.defaultJson,
    producePath: () -> String,
): GenericProtoDatastore<T> {
    val datastore = DataStoreFactory.create(
        storage = OkioStorage(
            fileSystem = systemFileSystem,
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
        defaultJson = defaultJson,
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
 * @param scope The [CoroutineScope] to use for DataStore operations. Defaults to an unmanaged
 *   `CoroutineScope(Dispatchers.IO + SupervisorJob())`, which is suitable for application-level
 *   singletons. To control the DataStore lifecycle (e.g., in a scoped component or for testing),
 *   pass a custom scope and cancel it when no longer needed:
 *   ```
 *   val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
 *   val datastore = createProtoDatastore(scope = scope, ...) { "/path/to/file" }
 *   // When done:
 *   scope.cancel()
 *   ```
 * @param defaultJson The default [Json] instance to use for Kotlin Serialization-based fields in this proto datastore.
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
    defaultJson: Json = PreferenceDefaults.defaultJson,
    produceOkioPath: () -> okio.Path,
): GenericProtoDatastore<T> {
    val datastore = DataStoreFactory.create(
        storage = OkioStorage(
            fileSystem = systemFileSystem,
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
        defaultJson = defaultJson,
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
 * @param scope The [CoroutineScope] to use for DataStore operations. Defaults to an unmanaged
 *   `CoroutineScope(Dispatchers.IO + SupervisorJob())`, which is suitable for application-level
 *   singletons. To control the DataStore lifecycle (e.g., in a scoped component or for testing),
 *   pass a custom scope and cancel it when no longer needed:
 *   ```
 *   val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
 *   val datastore = createProtoDatastore(scope = scope, ...) { "/path/to/file" }
 *   // When done:
 *   scope.cancel()
 *   ```
 * @param defaultJson The default [Json] instance to use for Kotlin Serialization-based fields in this proto datastore.
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
    defaultJson: Json = PreferenceDefaults.defaultJson,
    produceKotlinxIoPath: () -> KotlinxIoPath,
): GenericProtoDatastore<T> {
    val datastore = DataStoreFactory.create(
        storage = OkioStorage(
            fileSystem = systemFileSystem,
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
        defaultJson = defaultJson,
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
 * @param scope The [CoroutineScope] to use for DataStore operations. Defaults to an unmanaged
 *   `CoroutineScope(Dispatchers.IO + SupervisorJob())`, which is suitable for application-level
 *   singletons. To control the DataStore lifecycle (e.g., in a scoped component or for testing),
 *   pass a custom scope and cancel it when no longer needed:
 *   ```
 *   val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
 *   val datastore = createProtoDatastore(scope = scope, ...) { "/path/to/file" }
 *   // When done:
 *   scope.cancel()
 *   ```
 * @param defaultJson The default [Json] instance to use for Kotlin Serialization-based fields in this proto datastore.
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
    defaultJson: Json = PreferenceDefaults.defaultJson,
    producePath: () -> String,
): GenericProtoDatastore<T> {
    val datastore = DataStoreFactory.create(
        storage = OkioStorage(
            fileSystem = systemFileSystem,
            serializer = serializer,
            producePath = { producePath().toPath() / fileName },
        ),
        corruptionHandler = corruptionHandler,
        migrations = migrations,
        scope = scope,
    )
    return GenericProtoDatastore(
        datastore = datastore,
        defaultValue = defaultValue,
        key = key,
        defaultJson = defaultJson,
    )
}
