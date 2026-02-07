package io.github.arthurkun.generic.datastore.compose.app.domain

import io.github.arthurkun.generic.datastore.core.mapIO
import io.github.arthurkun.generic.datastore.preferences.GenericPreferencesDatastore
import io.github.arthurkun.generic.datastore.preferences.enum
import io.github.arthurkun.generic.datastore.preferences.enumSet
import io.github.arthurkun.generic.datastore.preferences.kserialized
import io.github.arthurkun.generic.datastore.preferences.kserializedSet
import kotlin.time.Instant

class PreferenceStore(
    private val datastore: GenericPreferencesDatastore,
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

    val userProfile = datastore.kserialized(
        key = "user_profile",
        defaultValue = UserProfile(name = "John", age = 25),
    )

    val animalSet = datastore.serializedSet(
        key = "animal_set",
        defaultValue = emptySet(),
        serializer = { Animal.to(it) },
        deserializer = { Animal.from(it) },
    )

    val userProfileSet = datastore.kserializedSet<UserProfile>(
        key = "user_profile_set",
        defaultValue = emptySet(),
    )

    val themeSet = datastore.enumSet<Theme>(
        key = "theme_set",
        defaultValue = emptySet(),
    )

    suspend fun exportPreferences() = datastore.export()

    suspend fun importPreferences(data: Map<String, Any>) = datastore.import(data)
}
