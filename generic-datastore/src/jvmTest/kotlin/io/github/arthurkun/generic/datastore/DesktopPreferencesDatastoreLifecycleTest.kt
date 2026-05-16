package io.github.arthurkun.generic.datastore

import io.github.arthurkun.generic.datastore.preferences.createPreferencesDatastore
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

class DesktopPreferencesDatastoreLifecycleTest {

    @TempDir
    lateinit var tempFolder: File

    @Test
    fun closeAllowsDatastorePathToBeReopened() = runTest {
        val path = "${tempFolder.absolutePath}/lifecycle.preferences_pb"
        val firstDatastore = createPreferencesDatastore(
            producePath = { path },
        )

        firstDatastore.string("name").set("first")
        firstDatastore.close()

        val secondDatastore = createPreferencesDatastore(
            producePath = { path },
        )

        try {
            assertEquals("first", secondDatastore.string("name").get())
        } finally {
            secondDatastore.close()
        }
    }

    @Test
    fun closeDoesNotCancelParentScope() = runTest {
        val parentScope = TestScope(StandardTestDispatcher(testScheduler))
        val parentJob = checkNotNull(parentScope.coroutineContext[Job])
        val datastore = createPreferencesDatastore(
            scope = parentScope,
            producePath = { "${tempFolder.absolutePath}/parent_scope.preferences_pb" },
        )

        try {
            datastore.close()

            assertTrue(parentJob.isActive)
        } finally {
            parentScope.cancel()
        }
    }
}
