package io.github.arthurkun.generic.datastore

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.emptyPreferences
import io.github.arthurkun.generic.datastore.core.systemFileSystem
import io.github.arthurkun.generic.datastore.preferences.createPreferencesDatastore
import kotlinx.coroutines.test.runTest
import okio.Path.Companion.toPath
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

/**
 * End-to-end tests against a real androidx DataStore backed by a file containing garbage bytes.
 * Each platform subclass supplies a writable [tempDirectory]; the file itself is written through
 * [systemFileSystem] so the same assertions run on Android, JVM, and iOS.
 */
abstract class AbstractPreferencesDatastoreCorruptionTest {

    abstract val tempDirectory: String

    private fun writeCorruptFile(name: String): String {
        systemFileSystem.createDirectories(tempDirectory.toPath())
        val path = "$tempDirectory/$name"
        systemFileSystem.write(path.toPath()) {
            writeUtf8("not-a-preferences-file")
        }
        return path
    }

    @Test
    fun getThrowsCorruptionExceptionWhenFileIsCorruptAndNoHandlerIsRegistered() = runTest {
        val path = writeCorruptFile("corrupt.preferences_pb")

        val datastore = createPreferencesDatastore(producePath = { path })
        try {
            assertFailsWith<CorruptionException> {
                datastore.string("key").get()
            }
        } finally {
            datastore.close()
        }
    }

    @Test
    fun corruptionHandlerReplacesFileAndReturnsDefaults() = runTest {
        val path = writeCorruptFile("corrupt_handler.preferences_pb")

        val datastore = createPreferencesDatastore(
            corruptionHandler = ReplaceFileCorruptionHandler { emptyPreferences() },
            producePath = { path },
        )
        try {
            assertEquals("default", datastore.string("key", "default").get())
        } finally {
            datastore.close()
        }
    }
}
