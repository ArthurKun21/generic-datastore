package io.github.arthurkun.generic.datastore.proto

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.test.AfterTest
import kotlin.test.BeforeTest

class DesktopProtoDatastoreBlockingTest : AbstractProtoDatastoreBlockingTest() {

    @TempDir
    lateinit var tempFolder: File

    private lateinit var _protoDatastore: GenericProtoDatastore<TestProtoData>
    private lateinit var testScope: CoroutineScope

    override val protoDatastore: GenericProtoDatastore<TestProtoData> get() = _protoDatastore

    @BeforeTest
    fun setup() {
        val dispatcher = UnconfinedTestDispatcher()
        Dispatchers.setMain(dispatcher)
        testScope = CoroutineScope(Job() + dispatcher)
        _protoDatastore = createProtoDatastore(
            serializer = TestProtoDataSerializer,
            defaultValue = TestProtoData(),
            scope = testScope,
            producePath = {
                "${tempFolder.absolutePath}/test_proto_blocking.pb"
            },
        )
    }

    @AfterTest
    fun tearDown() {
        try {
            if (::testScope.isInitialized) {
                testScope.cancel()
            }
        } finally {
            Dispatchers.resetMain()
        }
    }
}
