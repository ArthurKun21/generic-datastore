package io.github.arthurkun.generic.datastore.app.domain

import io.github.arthurkun.generic.datastore.GenericPreferenceDatastore
import io.github.arthurkun.generic.datastore.enum
import io.github.arthurkun.generic.datastore.mapIO
import kotlin.time.Instant

class PreferenceStore(
    private val datastore: GenericPreferenceDatastore,
) {

    val theme = datastore.enum(
        "theme",
        defaultValue = Theme.SYSTEM,
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

    suspend fun exportPreferences() = datastore.export()

    suspend fun importPreferences(data: Map<String, Any>) = datastore.import(data)
}
