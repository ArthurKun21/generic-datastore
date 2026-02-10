package io.github.arthurkun.generic.datastore.proto

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class AndroidProtoDatastoreBlockingTest : AbstractProtoDatastoreBlockingTest() {

    private lateinit var _protoDatastore: GenericProtoDatastore<TestProtoData>
    private lateinit var testScope: CoroutineScope
    private lateinit var testContext: Context

    override val protoDatastore: GenericProtoDatastore<TestProtoData> get() = _protoDatastore

    @Before
    fun setup() {
        val dispatcher = UnconfinedTestDispatcher()
        Dispatchers.setMain(dispatcher)
        testContext = ApplicationProvider.getApplicationContext()
        testScope = CoroutineScope(Job() + dispatcher)
        _protoDatastore = createProtoDatastore(
            serializer = TestProtoDataSerializer,
            defaultValue = TestProtoData(),
            scope = testScope,
            producePath = {
                testContext.filesDir.resolve("datastore/test_proto_blocking.pb").absolutePath
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
                    val dataStoreFile =
                        File(testContext.filesDir, "datastore/test_proto_blocking.pb")
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
