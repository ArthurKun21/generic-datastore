package io.github.arthurkun.generic.datastore.proto.core.custom

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

class DesktopCustomFieldProtoTestHelper private constructor(
    private val datastoreName: String,
    private val useStandardDispatcher: Boolean,
) {
    private lateinit var _protoDatastore: GenericProtoDatastore<TestCustomFieldProtoData>
    private lateinit var _testDispatcher: TestDispatcher
    private lateinit var testScope: CoroutineScope

    val protoDatastore: GenericProtoDatastore<TestCustomFieldProtoData> get() = _protoDatastore
    val testDispatcher: TestDispatcher get() = _testDispatcher

    fun setup(tempFolderPath: String) {
        _testDispatcher = if (useStandardDispatcher) {
            StandardTestDispatcher()
        } else {
            UnconfinedTestDispatcher()
        }
        Dispatchers.setMain(_testDispatcher)
        testScope = CoroutineScope(Job() + _testDispatcher)
        _protoDatastore = createProtoDatastore(
            serializer = TestCustomFieldProtoDataSerializer,
            defaultValue = TestCustomFieldProtoData(),
            scope = testScope,
            producePath = {
                "$tempFolderPath/$datastoreName.pb"
            },
        )
    }

    fun tearDown() {
        try {
            if (::testScope.isInitialized) {
                testScope.cancel()
            }
        } finally {
            Dispatchers.resetMain()
        }
    }

    companion object {
        fun standard(datastoreName: String): DesktopCustomFieldProtoTestHelper {
            return DesktopCustomFieldProtoTestHelper(datastoreName, useStandardDispatcher = true)
        }

        fun blocking(datastoreName: String): DesktopCustomFieldProtoTestHelper {
            return DesktopCustomFieldProtoTestHelper(datastoreName, useStandardDispatcher = false)
        }
    }
}
