package io.github.arthurkun.generic.datastore

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.emptyPreferences
import io.github.arthurkun.generic.datastore.preferences.createPreferencesDatastore
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class DesktopPreferencesDatastoreCorruptionTest {

    @TempDir
    lateinit var tempFolder: File

    @Test
    fun getThrowsCorruptionExceptionWhenFileIsCorruptAndNoHandlerIsRegistered() = runTest {
        val path = "${tempFolder.absolutePath}/corrupt.preferences_pb"
        File(path).writeBytes("not-a-preferences-file".toByteArray(Charsets.ISO_8859_1))

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
        val path = "${tempFolder.absolutePath}/corrupt_handler.preferences_pb"
        File(path).writeBytes("not-a-preferences-file".toByteArray(Charsets.ISO_8859_1))

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
