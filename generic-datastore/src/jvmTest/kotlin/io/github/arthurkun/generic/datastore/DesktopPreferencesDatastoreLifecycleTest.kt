package io.github.arthurkun.generic.datastore

import io.github.arthurkun.generic.datastore.preferences.createPreferencesDatastore
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals

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
}
