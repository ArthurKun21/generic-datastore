package io.github.arthurkun.generic.datastore.compose.app.domain

import kotlinx.serialization.Serializable

@Serializable
data class UserProfile(
    val name: String,
    val age: Int,
)
