package io.github.arthurkun.generic.datastore.app.domain

import kotlinx.serialization.Serializable

/**
 * Example data class demonstrating the use of kotlinx.serialization
 * with the kserialized() preference function.
 *
 * This class is annotated with @Serializable, which allows it to be
 * automatically serialized to/from JSON when stored in DataStore.
 */
@Serializable
data class UserProfile(
    val username: String = "",
    val email: String = "",
    val age: Int = 0,
    val isPremium: Boolean = false,
    val favoriteColors: List<String> = emptyList(),
)
