package io.github.arthurkun.generic.datastore.proto.app.domain

import kotlinx.serialization.Serializable

@Serializable
data class ProtoApiItem(
    val name: String = "",
    val quantity: Int = 0,
)
