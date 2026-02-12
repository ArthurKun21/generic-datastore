package io.github.arthurkun.generic.datastore.proto.core.custom

import androidx.datastore.core.okio.OkioSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okio.BufferedSink
import okio.BufferedSource

enum class TestColor { RED, GREEN, BLUE }

@Serializable
data class TestItem(val name: String = "", val quantity: Int = 0)

@Serializable
data class TestCustomFieldProtoData(
    val enumRaw: String = "",
    val nullableEnumRaw: String? = null,
    val enumSetRaw: Set<String> = emptySet(),
    val jsonRaw: String = "",
    val nullableJsonRaw: String? = null,
    val jsonListRaw: String = "",
    val nullableJsonListRaw: String? = null,
    val jsonSetRaw: Set<String> = emptySet(),
)

object TestCustomFieldProtoDataSerializer : OkioSerializer<TestCustomFieldProtoData> {
    override val defaultValue: TestCustomFieldProtoData = TestCustomFieldProtoData()

    override suspend fun readFrom(source: BufferedSource): TestCustomFieldProtoData {
        val json = source.readUtf8()
        if (json.isBlank()) return defaultValue
        return Json.decodeFromString(json)
    }

    override suspend fun writeTo(t: TestCustomFieldProtoData, sink: BufferedSink) {
        sink.writeUtf8(Json.encodeToString(t))
    }
}
