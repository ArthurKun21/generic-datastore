package io.github.arthurkun.generic.datastore

import android.content.Context
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.github.arthurkun.generic.datastore.preferences.createPreferencesDatastore
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import kotlin.test.assertEquals

@RunWith(AndroidJUnit4::class)
class AndroidPreferencesDatastoreLifecycleTest {

    private val context: Context = ApplicationProvider.getApplicationContext()
    private val dataStoreFile: File = context.preferencesDataStoreFile(DATASTORE_NAME)

    @After
    fun tearDown() {
        if (dataStoreFile.exists()) {
            dataStoreFile.delete()
        }
    }

    @Test
    fun closeAllowsDatastorePathToBeReopenedAndClosedAgain() = runTest {
        val firstDatastore = createPreferencesDatastore(
            producePath = { dataStoreFile.absolutePath },
        )

        firstDatastore.string("name").set("first")
        firstDatastore.close()

        val secondDatastore = createPreferencesDatastore(
            producePath = { dataStoreFile.absolutePath },
        )

        try {
            assertEquals("first", secondDatastore.string("name").get())
            secondDatastore.string("name").set("second")
            assertEquals("second", secondDatastore.string("name").get())
        } finally {
            secondDatastore.close()
        }
    }

    private companion object {
        const val DATASTORE_NAME = "android_lifecycle"
    }
}
