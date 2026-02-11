package io.github.arthurkun.generic.datastore.proto

import androidx.datastore.core.okio.OkioSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okio.BufferedSink
import okio.BufferedSource

@Serializable
data class TestCoordinates(
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
)

@Serializable
data class TestNullableAddress(
    val street: String = "",
    val city: String = "",
    val coordinates: TestCoordinates? = null,
)

@Serializable
data class TestNullableProfile(
    val nickname: String = "",
    val age: Int? = null,
    val address: TestNullableAddress? = null,
)

@Serializable
data class TestNullableProtoData(
    val id: Int = 0,
    val name: String = "",
    val label: String? = null,
    val profile: TestNullableProfile? = null,
)

object TestNullableProtoDataSerializer : OkioSerializer<TestNullableProtoData> {
    override val defaultValue: TestNullableProtoData = TestNullableProtoData()

    override suspend fun readFrom(source: BufferedSource): TestNullableProtoData {
        val json = source.readUtf8()
        if (json.isBlank()) return defaultValue
        return Json.decodeFromString(json)
    }

    override suspend fun writeTo(t: TestNullableProtoData, sink: BufferedSink) {
        sink.writeUtf8(Json.encodeToString(t))
    }
}
