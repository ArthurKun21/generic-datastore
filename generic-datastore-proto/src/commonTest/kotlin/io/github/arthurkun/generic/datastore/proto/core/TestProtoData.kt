package io.github.arthurkun.generic.datastore.proto.core

import androidx.datastore.core.okio.OkioSerializer
import okio.BufferedSink
import okio.BufferedSource

data class TestAddress(
    val street: String = "",
    val city: String = "",
    val zipCode: String = "",
)

data class TestProfile(
    val nickname: String = "",
    val age: Int = 0,
    val address: TestAddress = TestAddress(),
)

data class TestProtoData(
    val id: Int = 0,
    val name: String = "",
    val profile: TestProfile = TestProfile(),
)

object TestProtoDataSerializer : OkioSerializer<TestProtoData> {
    override val defaultValue: TestProtoData = TestProtoData()

    override suspend fun readFrom(source: BufferedSource): TestProtoData {
        val line = source.readUtf8()
        if (line.isBlank()) return defaultValue
        val parts = line.split("|", limit = 7)
        return TestProtoData(
            id = parts[0].toInt(),
            name = parts[1],
            profile = TestProfile(
                nickname = parts[2],
                age = parts[3].toInt(),
                address = TestAddress(
                    street = parts[4],
                    city = parts[5],
                    zipCode = parts[6],
                ),
            ),
        )
    }

    override suspend fun writeTo(t: TestProtoData, sink: BufferedSink) {
        sink.writeUtf8(
            "${t.id}|${t.name}|${t.profile.nickname}|${t.profile.age}" +
                "|${t.profile.address.street}|${t.profile.address.city}" +
                "|${t.profile.address.zipCode}",
        )
    }
}
