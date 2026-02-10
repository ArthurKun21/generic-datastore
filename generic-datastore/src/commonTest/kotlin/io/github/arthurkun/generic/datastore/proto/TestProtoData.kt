package io.github.arthurkun.generic.datastore.proto

import androidx.datastore.core.okio.OkioSerializer
import okio.BufferedSink
import okio.BufferedSource

data class TestProtoData(val id: Int = 0, val name: String = "")

object TestProtoDataSerializer : OkioSerializer<TestProtoData> {
    override val defaultValue: TestProtoData = TestProtoData()

    override suspend fun readFrom(source: BufferedSource): TestProtoData {
        val line = source.readUtf8()
        if (line.isBlank()) return defaultValue
        val parts = line.split(",", limit = 2)
        return TestProtoData(parts[0].toInt(), parts[1])
    }

    override suspend fun writeTo(t: TestProtoData, sink: BufferedSink) {
        sink.writeUtf8("${t.id},${t.name}")
    }
}
