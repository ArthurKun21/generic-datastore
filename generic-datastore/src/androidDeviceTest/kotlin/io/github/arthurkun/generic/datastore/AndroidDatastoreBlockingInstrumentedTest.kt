package io.github.arthurkun.generic.datastore

import android.content.Context
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.github.arthurkun.generic.datastore.preferences.GenericPreferencesDatastore
import io.github.arthurkun.generic.datastore.preferences.createPreferencesDatastore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.runner.RunWith
import java.io.File

private const val TEST_DATASTORE_BLOCKING_NAME = "test_datastore_blocking"

@RunWith(AndroidJUnit4::class)
class AndroidDatastoreBlockingInstrumentedTest : AbstractDatastoreBlockingTest() {

    companion object {
        private lateinit var _preferenceDatastore: GenericPreferencesDatastore
        private lateinit var testContext: Context
        private val testDispatcher = UnconfinedTestDispatcher()

        @JvmStatic
        @BeforeClass
        fun setupClass() {
            Dispatchers.setMain(testDispatcher)
            testContext = ApplicationProvider.getApplicationContext()
            _preferenceDatastore = createPreferencesDatastore(
                producePath = {
                    testContext.preferencesDataStoreFile(TEST_DATASTORE_BLOCKING_NAME).absolutePath
                },
            )
        }

        @JvmStatic
        @AfterClass
        fun tearDownClass() {
            Dispatchers.resetMain()
            val dataStoreFile =
                File(
                    testContext.filesDir,
                    "datastore/${TEST_DATASTORE_BLOCKING_NAME}.preferences_pb",
                )
            if (dataStoreFile.exists()) {
                dataStoreFile.delete()
            }
        }
    }

    override val preferenceDatastore: GenericPreferencesDatastore get() = _preferenceDatastore
}
