package io.github.arthurkun.generic.datastore.proto.core

import io.github.arthurkun.generic.datastore.proto.createProtoDatastore
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DesktopProtoDatastoreLifecycleTest {

    @TempDir
    lateinit var tempFolder: File

    @Test
    fun closeAllowsDatastorePathToBeReopened() = runTest {
        val path = "${tempFolder.absolutePath}/lifecycle.pb"
        val firstDatastore = createProtoDatastore(
            serializer = TestProtoDataSerializer,
            defaultValue = TestProtoData(),
            producePath = { path },
        )

        firstDatastore.data().set(TestProtoData(id = 1, name = "first"))
        firstDatastore.close()

        val secondDatastore = createProtoDatastore(
            serializer = TestProtoDataSerializer,
            defaultValue = TestProtoData(),
            producePath = { path },
        )

        try {
            assertEquals("first", secondDatastore.data().get().name)
        } finally {
            secondDatastore.close()
        }
    }

    @Test
    fun closeDoesNotCancelParentScope() = runTest {
        val parentScope = TestScope(StandardTestDispatcher(testScheduler))
        val parentJob = checkNotNull(parentScope.coroutineContext[Job])
        val datastore = createProtoDatastore(
            serializer = TestProtoDataSerializer,
            defaultValue = TestProtoData(),
            scope = parentScope,
            producePath = { "${tempFolder.absolutePath}/parent_scope.pb" },
        )

        try {
            datastore.close()

            assertTrue(parentJob.isActive)
        } finally {
            parentScope.cancel()
        }
    }
}
