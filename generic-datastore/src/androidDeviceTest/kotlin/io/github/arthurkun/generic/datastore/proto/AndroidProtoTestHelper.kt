package io.github.arthurkun.generic.datastore.proto

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import java.io.File

class AndroidProtoTestHelper private constructor(
    private val datastoreName: String,
    private val useStandardDispatcher: Boolean,
) {
    private lateinit var _protoDatastore: GenericProtoDatastore<TestProtoData>
    private lateinit var _testDispatcher: TestDispatcher
    private lateinit var testContext: Context
    private lateinit var testScope: CoroutineScope

    val protoDatastore: GenericProtoDatastore<TestProtoData> get() = _protoDatastore
    val testDispatcher: TestDispatcher get() = _testDispatcher

    fun setup() {
        _testDispatcher = if (useStandardDispatcher) {
            StandardTestDispatcher()
        } else {
            UnconfinedTestDispatcher()
        }
        Dispatchers.setMain(_testDispatcher)
        testContext = ApplicationProvider.getApplicationContext()
        testScope = CoroutineScope(Job() + _testDispatcher)
        _protoDatastore = createProtoDatastore(
            serializer = TestProtoDataSerializer,
            defaultValue = TestProtoData(),
            scope = testScope,
            producePath = {
                testContext.filesDir.resolve("datastore/$datastoreName.pb").absolutePath
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
                if (::testContext.isInitialized) {
                    val dataStoreFile =
                        File(testContext.filesDir, "datastore/$datastoreName.pb")
                    if (dataStoreFile.exists()) {
                        dataStoreFile.delete()
                    }
                }
            } finally {
                Dispatchers.resetMain()
            }
        }
    }

    companion object {
        fun standard(datastoreName: String): AndroidProtoTestHelper {
            return AndroidProtoTestHelper(datastoreName, useStandardDispatcher = true)
        }

        fun blocking(datastoreName: String): AndroidProtoTestHelper {
            return AndroidProtoTestHelper(datastoreName, useStandardDispatcher = false)
        }
    }
}
