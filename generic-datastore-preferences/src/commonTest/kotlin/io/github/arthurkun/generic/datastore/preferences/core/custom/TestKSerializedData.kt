package io.github.arthurkun.generic.datastore.preferences.core.custom

import kotlinx.serialization.Serializable

@Serializable
data class KSerUser(
    val name: String = "",
    val age: Int = 0,
)

@Serializable
data class KSerAddress(
    val street: String = "",
    val city: String = "",
    val zip: String = "",
)
