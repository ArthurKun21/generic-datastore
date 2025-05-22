package io.github.arthurkun.generic.datastore.app.domain

import io.github.arthurkun.generic.datastore.GenericPreferenceDatastore
import io.github.arthurkun.generic.datastore.enum

class PreferenceStore(
    datastore: GenericPreferenceDatastore,
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
}