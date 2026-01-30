package io.github.arthurkun.generic.datastore.app.domain

import io.github.arthurkun.generic.datastore.backup.BackupPreference
import io.github.arthurkun.generic.datastore.core.mapIO
import io.github.arthurkun.generic.datastore.preferences.GenericPreferencesDatastore
import io.github.arthurkun.generic.datastore.preferences.enum
import kotlin.time.Instant

class PreferenceStore(
    val datastore: GenericPreferencesDatastore,
) {

    val theme = datastore.enum(
        "theme",
        Theme.SYSTEM,
    )

    val text = datastore.string(
        "text",
        defaultValue = "Hello World!",
    )

    val num = datastore.int(
        "num",
        defaultValue = 0,
    )

    val bool = datastore.bool(
        "bool",
        defaultValue = false,
    )

    val customObject = datastore.serialized(
        key = "animal",
        defaultValue = Animal.Dog,
        serializer = { Animal.to(it) },
        deserializer = { Animal.from(it) },
    )

    val duration = datastore.long(
        key = "duration",
        defaultValue = 0L,
    ).mapIO(
        convert = {
            Instant.fromEpochMilliseconds(it)
        },
        reverse = {
            it.toEpochMilliseconds()
        },
    )

    /**
     * Example of using kserialized() with a @Serializable data class.
     * This automatically handles JSON serialization/deserialization.
     */
    val userProfile = datastore.kserialized(
        key = "user_profile",
        defaultValue = UserProfile(),
        serializer = UserProfile.serializer(),
    )

    suspend fun exportPreferences(): List<BackupPreference> = datastore.export()

    suspend fun importPreferences(backupPreferences: List<BackupPreference>) =
        datastore.import(backupPreferences)
}
