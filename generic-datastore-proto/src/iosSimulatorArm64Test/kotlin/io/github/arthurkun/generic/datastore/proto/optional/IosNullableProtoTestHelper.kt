package io.github.arthurkun.generic.datastore.proto.optional

import io.github.arthurkun.generic.datastore.proto.GenericProtoDatastore
import io.github.arthurkun.generic.datastore.proto.createProtoDatastore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import platform.Foundation.NSFileManager
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSUUID

class IosNullableProtoTestHelper private constructor(
    private val datastoreName: String,
    private val useStandardDispatcher: Boolean,
) {
    private lateinit var tempDir: String
    private lateinit var _protoDatastore: GenericProtoDatastore<TestNullableProtoData>
    private lateinit var _testDispatcher: TestDispatcher
    private lateinit var testScope: CoroutineScope

    val protoDatastore: GenericProtoDatastore<TestNullableProtoData> get() = _protoDatastore
    val testDispatcher: TestDispatcher get() = _testDispatcher

    fun setup() {
        tempDir = NSTemporaryDirectory() + NSUUID().UUIDString
        _testDispatcher = if (useStandardDispatcher) {
            StandardTestDispatcher()
        } else {
            UnconfinedTestDispatcher()
        }
        Dispatchers.setMain(_testDispatcher)
        testScope = CoroutineScope(Job() + _testDispatcher)
        _protoDatastore = createProtoDatastore(
            serializer = TestNullableProtoDataSerializer,
            defaultValue = TestNullableProtoData(),
            scope = testScope,
            producePath = {
                "$tempDir/$datastoreName.pb"
            },
        )
    }

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

    companion object {
        fun standard(datastoreName: String): IosNullableProtoTestHelper {
            return IosNullableProtoTestHelper(datastoreName, useStandardDispatcher = true)
        }

        fun blocking(datastoreName: String): IosNullableProtoTestHelper {
            return IosNullableProtoTestHelper(datastoreName, useStandardDispatcher = false)
        }
    }
}
