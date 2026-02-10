package io.github.arthurkun.generic.datastore

import io.github.arthurkun.generic.datastore.proto.GenericProtoDatastore
import io.github.arthurkun.generic.datastore.proto.createProtoDatastore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import platform.Foundation.NSFileManager
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSUUID
import kotlin.test.AfterTest
import kotlin.test.BeforeTest

class IosProtoDatastoreBlockingTest : AbstractProtoDatastoreBlockingTest() {

    private lateinit var tempDir: String
    private lateinit var _protoDatastore: GenericProtoDatastore<TestProtoData>
    private lateinit var testScope: CoroutineScope

    override val protoDatastore: GenericProtoDatastore<TestProtoData> get() = _protoDatastore

    @BeforeTest
    fun setup() {
        tempDir = NSTemporaryDirectory() + NSUUID().UUIDString
        val dispatcher = UnconfinedTestDispatcher()
        Dispatchers.setMain(dispatcher)
        testScope = CoroutineScope(Job() + dispatcher)
        _protoDatastore = createProtoDatastore(
            serializer = TestProtoDataSerializer,
            defaultValue = TestProtoData(),
            scope = testScope,
            producePath = {
                "$tempDir/test_proto_blocking.pb"
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
            try {
                if (::tempDir.isInitialized) {
                    NSFileManager.defaultManager.removeItemAtPath(tempDir, null)
                }
            } finally {
                Dispatchers.resetMain()
            }
        }
    }
}
