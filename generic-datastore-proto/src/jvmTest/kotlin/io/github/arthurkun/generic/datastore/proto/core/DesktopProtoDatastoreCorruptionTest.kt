package io.github.arthurkun.generic.datastore.proto.core

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.core.okio.OkioSerializer
import io.github.arthurkun.generic.datastore.proto.createProtoDatastore
import kotlinx.coroutines.test.runTest
import okio.BufferedSink
import okio.BufferedSource
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.coroutines.cancellation.CancellationException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

/**
 * End-to-end tests against real androidx DataStore with a corrupt file on disk. The test
 * serializer honors the androidx contract that serializers throw [CorruptionException] when data
 * cannot be de-serialized (unlike [TestProtoDataSerializer], whose parse failures are plain
 * runtime exceptions).
 */
class DesktopProtoDatastoreCorruptionTest {

    @TempDir
    lateinit var tempFolder: File

    @Test
    fun getThrowsCorruptionExceptionWhenFileIsCorruptAndNoHandlerIsRegistered() = runTest {
        val path = "${tempFolder.absolutePath}/corrupt.pb"
        File(path).writeText("this is not a valid TestProtoData payload")

        val datastore = createProtoDatastore(
            serializer = CorruptionAwareTestProtoDataSerializer,
            defaultValue = TestProtoData(),
            producePath = { path },
        )
        try {
            assertFailsWith<CorruptionException> {
                datastore.data().get()
            }
        } finally {
            datastore.close()
        }
    }

    @Test
    fun corruptionHandlerReplacesFileAndReturnsDefaults() = runTest {
        val path = "${tempFolder.absolutePath}/corrupt_handler.pb"
        File(path).writeText("this is not a valid TestProtoData payload")

        val datastore = createProtoDatastore(
            serializer = CorruptionAwareTestProtoDataSerializer,
            defaultValue = TestProtoData(),
            corruptionHandler = ReplaceFileCorruptionHandler { TestProtoData() },
            producePath = { path },
        )
        try {
            assertEquals(TestProtoData(), datastore.data().get())
        } finally {
            datastore.close()
        }
    }
}

private object CorruptionAwareTestProtoDataSerializer : OkioSerializer<TestProtoData> {
    override val defaultValue: TestProtoData = TestProtoData()

    override suspend fun readFrom(source: BufferedSource): TestProtoData = try {
        TestProtoDataSerializer.readFrom(source)
    } catch (e: CancellationException) {
        throw e
    } catch (e: CorruptionException) {
        throw e
    } catch (e: Exception) {
        throw CorruptionException("Unable to parse TestProtoData.", e)
    }

    override suspend fun writeTo(t: TestProtoData, sink: BufferedSink) {
        TestProtoDataSerializer.writeTo(t, sink)
    }
}
