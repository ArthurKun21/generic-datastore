package io.github.arthurkun.generic.datastore

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
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
import org.junit.After
import org.junit.Before
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class AndroidProtoDatastoreTest : AbstractProtoDatastoreTest() {

    private lateinit var _protoDatastore: GenericProtoDatastore<TestProtoData>
    private lateinit var _testDispatcher: TestDispatcher
    private lateinit var testScope: CoroutineScope
    private lateinit var testContext: android.content.Context

    override val protoDatastore: GenericProtoDatastore<TestProtoData> get() = _protoDatastore
    override val testDispatcher: TestDispatcher get() = _testDispatcher

    @Before
    fun setup() {
        _testDispatcher = StandardTestDispatcher()
        Dispatchers.setMain(_testDispatcher)
        testContext = ApplicationProvider.getApplicationContext()
        testScope = CoroutineScope(Job() + _testDispatcher)
        _protoDatastore = createProtoDatastore(
            serializer = TestProtoDataSerializer,
            defaultValue = TestProtoData(),
            scope = testScope,
            producePath = {
                testContext.filesDir.resolve("datastore/test_proto.pb").absolutePath
            },
        )
    }

    @After
    fun tearDown() {
        try {
            if (::testScope.isInitialized) {
                testScope.cancel()
            }
        } finally {
            try {
                if (::testContext.isInitialized) {
                    val dataStoreFile = File(testContext.filesDir, "datastore/test_proto.pb")
                    if (dataStoreFile.exists()) {
                        dataStoreFile.delete()
                    }
                }
            } finally {
                Dispatchers.resetMain()
            }
        }
    }
}
