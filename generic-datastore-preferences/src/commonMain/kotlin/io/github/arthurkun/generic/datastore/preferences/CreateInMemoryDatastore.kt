@file:Suppress("unused")

package io.github.arthurkun.generic.datastore.preferences

import androidx.datastore.preferences.core.emptyPreferences
import io.github.arthurkun.generic.datastore.core.InMemoryDataStore
import io.github.arthurkun.generic.datastore.core.InternalGenericDatastoreApi
import io.github.arthurkun.generic.datastore.core.PreferenceDefaults
import kotlinx.serialization.json.Json

/**
 * Creates a [PreferencesDatastore] backed entirely by memory instead of a file.
 *
 * Intended for unit tests, previews, and short-lived state: the stored preferences live for as
 * long as the returned instance, and `close()` is a no-op. Migrations and file corruption
 * handling do not apply — there is no file.
 *
 * ```kotlin
 * val datastore = createInMemoryPreferencesDatastore()
 * val name = datastore.string("name", "")
 * ```
 *
 * @param defaultJson The fallback [Json] instance for Kotlin-serialization-backed preferences.
 * @return A [GenericPreferencesDatastore] with no on-disk storage.
 */
@OptIn(InternalGenericDatastoreApi::class)
public fun createInMemoryPreferencesDatastore(
    defaultJson: Json = PreferenceDefaults.defaultJson,
): GenericPreferencesDatastore = GenericPreferencesDatastore(
    datastore = InMemoryDataStore(emptyPreferences()),
    defaultJson = defaultJson,
)
