@file:Suppress("unused")
@file:OptIn(io.github.arthurkun.generic.datastore.core.InternalGenericDatastoreApi::class)

package io.github.arthurkun.generic.datastore.preferences

import androidx.datastore.core.DataMigration
import androidx.datastore.core.DataStore
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.core.okio.OkioStorage
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.PreferencesSerializer
import io.github.arthurkun.generic.datastore.core.PreferenceDefaults
import io.github.arthurkun.generic.datastore.core.createDatastoreScope
import io.github.arthurkun.generic.datastore.core.systemFileSystem
import kotlinx.coroutines.CoroutineScope
import kotlinx.serialization.json.Json
import okio.Path.Companion.toPath
import kotlin.coroutines.CoroutineContext
import kotlin.jvm.JvmName
import kotlinx.io.files.Path as KotlinxIoPath

/**
 * Creates a [GenericPreferencesDatastore] from a path producer that returns a [String].
 *
 * Use this overload when your calling code naturally works with string paths. The produced path is
 * converted to [okio.Path] before creating the underlying DataStore.
 *
 * Example:
 * ```kotlin
 * val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
 * val datastore = createPreferencesDatastore(scope = scope) {
 *     "C:/app/settings.preferences_pb"
 * }
 * ```
 *
 * @param corruptionHandler An optional [ReplaceFileCorruptionHandler] to handle data corruption.
 * @param migrations A list of [DataMigration] to apply when the DataStore is created.
 * @param scope Optional parent [CoroutineScope] for DataStore operations. The returned
 * [GenericPreferencesDatastore] owns a child scope and cancels it from [GenericPreferencesDatastore.close].
 * Pass a custom scope to tie the datastore to a parent lifecycle.
 * @param defaultJson The fallback [Json] instance for Kotlin-serialization-backed preferences.
 * @param producePath A lambda that returns the full file path as a [String].
 * @return A new [GenericPreferencesDatastore] instance.
 */
public fun createPreferencesDatastore(
    corruptionHandler: ReplaceFileCorruptionHandler<Preferences>? = null,
    migrations: List<DataMigration<Preferences>> = emptyList(),
    scope: CoroutineScope? = null,
    defaultJson: Json = PreferenceDefaults.defaultJson,
    producePath: () -> String,
): GenericPreferencesDatastore {
    val datastoreScope = createDatastoreScope(scope)
    val datastore = createPreferencesDataStore(
        corruptionHandler = corruptionHandler,
        migrations = migrations,
        context = datastoreScope.coroutineContext,
        producePath = { producePath().toPath() },
    )
    return GenericPreferencesDatastore(
        datastore = datastore,
        defaultJson = defaultJson,
        ownedScope = datastoreScope,
    )
}

/**
 * Creates a [GenericPreferencesDatastore] from an [okio.Path] producer.
 *
 * Use this overload when your application already uses Okio paths and you want to avoid string
 * conversion at the call site.
 *
 * @param corruptionHandler An optional [ReplaceFileCorruptionHandler] to handle data corruption.
 * @param migrations A list of [DataMigration] to apply when the DataStore is created.
 * @param scope Optional parent [CoroutineScope] for DataStore operations. The returned
 * [GenericPreferencesDatastore] owns a child scope and cancels it from [GenericPreferencesDatastore.close].
 * Pass a custom scope to tie the datastore to a parent lifecycle.
 * @param defaultJson The fallback [Json] instance for Kotlin-serialization-backed preferences.
 * @param produceOkioPath A lambda that returns the file path as an [okio.Path].
 * @return A new [GenericPreferencesDatastore] instance.
 */
@JvmName("createPreferencesDatastoreWithOkioPath")
public fun createPreferencesDatastore(
    corruptionHandler: ReplaceFileCorruptionHandler<Preferences>? = null,
    migrations: List<DataMigration<Preferences>> = emptyList(),
    scope: CoroutineScope? = null,
    defaultJson: Json = PreferenceDefaults.defaultJson,
    produceOkioPath: () -> okio.Path,
): GenericPreferencesDatastore {
    val datastoreScope = createDatastoreScope(scope)
    val datastore = createPreferencesDataStore(
        corruptionHandler = corruptionHandler,
        migrations = migrations,
        context = datastoreScope.coroutineContext,
        producePath = produceOkioPath,
    )
    return GenericPreferencesDatastore(
        datastore = datastore,
        defaultJson = defaultJson,
        ownedScope = datastoreScope,
    )
}

/**
 * Creates a [GenericPreferencesDatastore] from a [KotlinxIoPath] producer.
 *
 * This overload is useful in code that already uses `kotlinx-io` paths. The produced path is
 * converted to [okio.Path] before the datastore is created.
 *
 * @param corruptionHandler An optional [ReplaceFileCorruptionHandler] to handle data corruption.
 * @param migrations A list of [DataMigration] to apply when the DataStore is created.
 * @param scope Optional parent [CoroutineScope] for DataStore operations. The returned
 * [GenericPreferencesDatastore] owns a child scope and cancels it from [GenericPreferencesDatastore.close].
 * Pass a custom scope to tie the datastore to a parent lifecycle.
 * @param defaultJson The fallback [Json] instance for Kotlin-serialization-backed preferences.
 * @param produceKotlinxIoPath A lambda that returns the file path as a [KotlinxIoPath].
 * @return A new [GenericPreferencesDatastore] instance.
 */
@JvmName("createPreferencesDatastoreWithKotlinxIoPath")
public fun createPreferencesDatastore(
    corruptionHandler: ReplaceFileCorruptionHandler<Preferences>? = null,
    migrations: List<DataMigration<Preferences>> = emptyList(),
    scope: CoroutineScope? = null,
    defaultJson: Json = PreferenceDefaults.defaultJson,
    produceKotlinxIoPath: () -> KotlinxIoPath,
): GenericPreferencesDatastore {
    val datastoreScope = createDatastoreScope(scope)
    val datastore = createPreferencesDataStore(
        corruptionHandler = corruptionHandler,
        migrations = migrations,
        context = datastoreScope.coroutineContext,
        producePath = { produceKotlinxIoPath().toString().toPath() },
    )
    return GenericPreferencesDatastore(
        datastore = datastore,
        defaultJson = defaultJson,
        ownedScope = datastoreScope,
    )
}

/**
 * Creates a [GenericPreferencesDatastore] from a directory path plus a [fileName].
 *
 * Use this overload when the directory location is dynamic but the datastore file name is stable.
 * The [fileName] is appended to the directory returned by [producePath].
 *
 * @param fileName The name of the DataStore file, such as `settings.preferences_pb`.
 * @param corruptionHandler An optional [ReplaceFileCorruptionHandler] to handle data corruption.
 * @param migrations A list of [DataMigration] to apply when the DataStore is created.
 * @param scope Optional parent [CoroutineScope] for DataStore operations. The returned
 * [GenericPreferencesDatastore] owns a child scope and cancels it from [GenericPreferencesDatastore.close].
 * Pass a custom scope to tie the datastore to a parent lifecycle.
 * @param defaultJson The fallback [Json] instance for Kotlin-serialization-backed preferences.
 * @param producePath A lambda that returns the directory path as a [String].
 * @return A new [GenericPreferencesDatastore] instance.
 */
public fun createPreferencesDatastore(
    fileName: String,
    corruptionHandler: ReplaceFileCorruptionHandler<Preferences>? = null,
    migrations: List<DataMigration<Preferences>> = emptyList(),
    scope: CoroutineScope? = null,
    defaultJson: Json = PreferenceDefaults.defaultJson,
    producePath: () -> String,
): GenericPreferencesDatastore {
    val datastoreScope = createDatastoreScope(scope)
    val datastore = createPreferencesDataStore(
        corruptionHandler = corruptionHandler,
        migrations = migrations,
        context = datastoreScope.coroutineContext,
        producePath = { producePath().toPath() / fileName },
    )
    return GenericPreferencesDatastore(
        datastore = datastore,
        defaultJson = defaultJson,
        ownedScope = datastoreScope,
    )
}

private fun createPreferencesDataStore(
    corruptionHandler: ReplaceFileCorruptionHandler<Preferences>?,
    migrations: List<DataMigration<Preferences>>,
    context: CoroutineContext,
    producePath: () -> okio.Path,
): DataStore<Preferences> {
    val builder = DataStore.Builder(
        storage = OkioStorage(
            fileSystem = systemFileSystem,
            serializer = PreferencesSerializer,
            producePath = producePath,
        ),
        context = context,
    )
    builder.apply {
        addMigrations(migrations)
        if (corruptionHandler != null) {
            setCorruptionHandler(corruptionHandler)
        }
    }

    return builder.build()
}
