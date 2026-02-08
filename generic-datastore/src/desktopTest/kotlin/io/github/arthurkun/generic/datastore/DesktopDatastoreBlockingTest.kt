package io.github.arthurkun.generic.datastore

import io.github.arthurkun.generic.datastore.preferences.GenericPreferencesDatastore
import io.github.arthurkun.generic.datastore.preferences.createPreferencesDatastore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.test.AfterTest
import kotlin.test.BeforeTest

private const val TEST_DATASTORE_BLOCKING_NAME = "test_datastore_blocking"

class DesktopDatastoreBlockingTest : AbstractDatastoreBlockingTest() {

    @TempDir
    lateinit var tempFolder: File

    private lateinit var _preferenceDatastore: GenericPreferencesDatastore
    private val testDispatcher = UnconfinedTestDispatcher()

    override val preferenceDatastore: GenericPreferencesDatastore get() = _preferenceDatastore

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        _preferenceDatastore = createPreferencesDatastore(
            fileName = "${TEST_DATASTORE_BLOCKING_NAME}.preferences_pb",
        ) {
            tempFolder.absolutePath
        }
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }
}
