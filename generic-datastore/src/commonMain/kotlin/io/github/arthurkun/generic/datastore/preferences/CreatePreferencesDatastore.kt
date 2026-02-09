@file:Suppress("unused")

package io.github.arthurkun.generic.datastore.preferences

import androidx.datastore.core.DataMigration
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import io.github.arthurkun.generic.datastore.core.PreferenceDefaults
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import kotlinx.serialization.json.Json
import okio.Path.Companion.toPath
import kotlin.jvm.JvmName
import kotlinx.io.files.Path as KotlinxIoPath

/**
 * Creates a [GenericPreferencesDatastore] using a path producer that returns a [String].
 *
 * This follows the same pattern as [PreferenceDataStoreFactory.createWithPath] from AndroidX,
 * converting the produced path string to an [okio.Path].
 *
 * @param corruptionHandler An optional [ReplaceFileCorruptionHandler] to handle data corruption.
 * @param migrations A list of [DataMigration] to apply when the DataStore is created.
 * @param scope The [CoroutineScope] to use for DataStore operations. Defaults to an unmanaged
 *   `CoroutineScope(Dispatchers.IO + SupervisorJob())`, which is suitable for application-level
 *   singletons. To control the DataStore lifecycle (e.g., in a scoped component or for testing),
 *   pass a custom scope and cancel it when no longer needed:
 *   ```
 *   val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
 *   val datastore = createPreferencesDatastore(scope = scope) { "/path/to/file" }
 *   // When done:
 *   scope.cancel()
 *   ```
 * @param defaultJson The default [Json] instance to use for Kotlin Serialization-based preferences.
 * @param producePath A lambda that returns the full file path as a [String].
 * @return A new [GenericPreferencesDatastore] instance.
 */
public fun createPreferencesDatastore(
    corruptionHandler: ReplaceFileCorruptionHandler<Preferences>? = null,
    migrations: List<DataMigration<Preferences>> = emptyList(),
    scope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
    defaultJson: Json = PreferenceDefaults.defaultJson,
    producePath: () -> String,
): GenericPreferencesDatastore {
    val datastore = PreferenceDataStoreFactory.createWithPath(
        corruptionHandler = corruptionHandler,
        migrations = migrations,
        scope = scope,
        produceFile = { producePath().toPath() },
    )
    return GenericPreferencesDatastore(
        datastore = datastore,
        defaultJson = defaultJson,
    )
}

/**
 * Creates a [GenericPreferencesDatastore] using an [okio.Path] producer.
 *
 * @param corruptionHandler An optional [ReplaceFileCorruptionHandler] to handle data corruption.
 * @param migrations A list of [DataMigration] to apply when the DataStore is created.
 * @param scope The [CoroutineScope] to use for DataStore operations. Defaults to an unmanaged
 *   `CoroutineScope(Dispatchers.IO + SupervisorJob())`, which is suitable for application-level
 *   singletons. To control the DataStore lifecycle (e.g., in a scoped component or for testing),
 *   pass a custom scope and cancel it when no longer needed:
 *   ```
 *   val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
 *   val datastore = createPreferencesDatastore(scope = scope) { "/path/to/file" }
 *   // When done:
 *   scope.cancel()
 *   ```
 * @param defaultJson The default [Json] instance to use for Kotlin Serialization-based preferences.
 * @param produceOkioPath A lambda that returns the file path as an [okio.Path].
 * @return A new [GenericPreferencesDatastore] instance.
 */
@JvmName("createPreferencesDatastoreWithOkioPath")
public fun createPreferencesDatastore(
    corruptionHandler: ReplaceFileCorruptionHandler<Preferences>? = null,
    migrations: List<DataMigration<Preferences>> = emptyList(),
    scope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
    defaultJson: Json = PreferenceDefaults.defaultJson,
    produceOkioPath: () -> okio.Path,
): GenericPreferencesDatastore {
    val datastore = PreferenceDataStoreFactory.createWithPath(
        corruptionHandler = corruptionHandler,
        migrations = migrations,
        scope = scope,
        produceFile = produceOkioPath,
    )
    return GenericPreferencesDatastore(
        datastore = datastore,
        defaultJson = defaultJson,
    )
}

/**
 * Creates a [GenericPreferencesDatastore] using a [kotlinx.io.files.Path] producer.
 *
 * @param corruptionHandler An optional [ReplaceFileCorruptionHandler] to handle data corruption.
 * @param migrations A list of [DataMigration] to apply when the DataStore is created.
 * @param scope The [CoroutineScope] to use for DataStore operations. Defaults to an unmanaged
 *   `CoroutineScope(Dispatchers.IO + SupervisorJob())`, which is suitable for application-level
 *   singletons. To control the DataStore lifecycle (e.g., in a scoped component or for testing),
 *   pass a custom scope and cancel it when no longer needed:
 *   ```
 *   val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
 *   val datastore = createPreferencesDatastore(scope = scope) { "/path/to/file" }
 *   // When done:
 *   scope.cancel()
 *   ```
 * @param defaultJson The default [Json] instance to use for Kotlin Serialization-based preferences.
 * @param produceKotlinxIoPath A lambda that returns the file path as a [kotlinx.io.files.Path].
 * @return A new [GenericPreferencesDatastore] instance.
 */
@JvmName("createPreferencesDatastoreWithKotlinxIoPath")
public fun createPreferencesDatastore(
    corruptionHandler: ReplaceFileCorruptionHandler<Preferences>? = null,
    migrations: List<DataMigration<Preferences>> = emptyList(),
    scope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
    defaultJson: Json = PreferenceDefaults.defaultJson,
    produceKotlinxIoPath: () -> KotlinxIoPath,
): GenericPreferencesDatastore {
    val datastore = PreferenceDataStoreFactory.createWithPath(
        corruptionHandler = corruptionHandler,
        migrations = migrations,
        scope = scope,
        produceFile = { produceKotlinxIoPath().toString().toPath() },
    )
    return GenericPreferencesDatastore(
        datastore = datastore,
        defaultJson = defaultJson,
    )
}

/**
 * Creates a [GenericPreferencesDatastore] using a directory path and file name.
 *
 * The [fileName] will be appended to the path produced by [producePath].
 *
 * @param fileName The name of the DataStore file (e.g., "settings.preferences_pb").
 * @param corruptionHandler An optional [ReplaceFileCorruptionHandler] to handle data corruption.
 * @param migrations A list of [DataMigration] to apply when the DataStore is created.
 * @param scope The [CoroutineScope] to use for DataStore operations. Defaults to an unmanaged
 *   `CoroutineScope(Dispatchers.IO + SupervisorJob())`, which is suitable for application-level
 *   singletons. To control the DataStore lifecycle (e.g., in a scoped component or for testing),
 *   pass a custom scope and cancel it when no longer needed:
 *   ```
 *   val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
 *   val datastore = createPreferencesDatastore(scope = scope) { "/path/to/file" }
 *   // When done:
 *   scope.cancel()
 *   ```
 * @param defaultJson The default [Json] instance to use for Kotlin Serialization-based preferences.
 * @param producePath A lambda that returns the directory path as a [String].
 * @return A new [GenericPreferencesDatastore] instance.
 */
public fun createPreferencesDatastore(
    fileName: String,
    corruptionHandler: ReplaceFileCorruptionHandler<Preferences>? = null,
    migrations: List<DataMigration<Preferences>> = emptyList(),
    scope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
    defaultJson: Json = PreferenceDefaults.defaultJson,
    producePath: () -> String,
): GenericPreferencesDatastore {
    val datastore = PreferenceDataStoreFactory.createWithPath(
        corruptionHandler = corruptionHandler,
        migrations = migrations,
        scope = scope,
        produceFile = { producePath().toPath() / fileName },
    )
    return GenericPreferencesDatastore(
        datastore = datastore,
        defaultJson = defaultJson,
    )
}
