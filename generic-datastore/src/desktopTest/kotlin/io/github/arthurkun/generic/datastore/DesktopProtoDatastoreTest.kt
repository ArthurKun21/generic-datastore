package io.github.arthurkun.generic.datastore

import io.github.arthurkun.generic.datastore.proto.GenericProtoDatastore
import io.github.arthurkun.generic.datastore.proto.createProtoDatastore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.test.AfterTest
import kotlin.test.BeforeTest

class DesktopProtoDatastoreTest : AbstractProtoDatastoreTest() {

    @TempDir
    lateinit var tempFolder: File

    private lateinit var _protoDatastore: GenericProtoDatastore<TestProtoData>
    private lateinit var _testDispatcher: TestDispatcher
    private lateinit var testScope: CoroutineScope

    override val protoDatastore: GenericProtoDatastore<TestProtoData> get() = _protoDatastore
    override val testDispatcher: TestDispatcher get() = _testDispatcher

    @BeforeTest
    fun setup() {
        _testDispatcher = StandardTestDispatcher()
        Dispatchers.setMain(_testDispatcher)
        testScope = CoroutineScope(Job() + _testDispatcher)
        _protoDatastore = createProtoDatastore(
            serializer = TestProtoDataSerializer,
            defaultValue = TestProtoData(),
            scope = testScope,
            producePath = {
                "${tempFolder.absolutePath}/test_proto.pb"
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
