package io.github.arthurkun.generic.datastore.proto.core

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.github.arthurkun.generic.datastore.proto.createProtoDatastore
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import kotlin.test.assertEquals

@RunWith(AndroidJUnit4::class)
class AndroidProtoDatastoreLifecycleTest {

    private val context: Context = ApplicationProvider.getApplicationContext()
    private val dataStoreFile: File = File(context.filesDir, "datastore/$DATASTORE_NAME.pb")

    @After
    fun tearDown() {
        if (dataStoreFile.exists()) {
            dataStoreFile.delete()
        }
    }

    @Test
    fun closeAllowsDatastorePathToBeReopenedAndClosedAgain() = runTest {
        val firstDatastore = createProtoDatastore(
            serializer = TestProtoDataSerializer,
            defaultValue = TestProtoData(),
            producePath = { dataStoreFile.absolutePath },
        )

        firstDatastore.data().set(TestProtoData(id = 1, name = "first"))
        firstDatastore.close()

        val secondDatastore = createProtoDatastore(
            serializer = TestProtoDataSerializer,
            defaultValue = TestProtoData(),
            producePath = { dataStoreFile.absolutePath },
        )

        try {
            assertEquals("first", secondDatastore.data().get().name)
            secondDatastore.data().set(TestProtoData(id = 2, name = "second"))
            assertEquals("second", secondDatastore.data().get().name)
        } finally {
            secondDatastore.close()
        }
    }

    private companion object {
        const val DATASTORE_NAME = "android_proto_lifecycle"
    }
}
