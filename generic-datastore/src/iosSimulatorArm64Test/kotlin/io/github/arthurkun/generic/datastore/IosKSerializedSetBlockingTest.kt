package io.github.arthurkun.generic.datastore

import io.github.arthurkun.generic.datastore.preferences.GenericPreferencesDatastore
import io.github.arthurkun.generic.datastore.preferences.createPreferencesDatastore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import platform.Foundation.NSFileManager
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSUUID
import kotlin.test.AfterTest
import kotlin.test.BeforeTest

private const val TEST_DATASTORE_BLOCKING_NAME = "test_kserialized_set_datastore_blocking"

class IosKSerializedSetBlockingTest : AbstractKSerializedSetBlockingTest() {

    private lateinit var tempDir: String
    private lateinit var _preferenceDatastore: GenericPreferencesDatastore
    private val testDispatcher = UnconfinedTestDispatcher()

    override val preferenceDatastore: GenericPreferencesDatastore get() = _preferenceDatastore

    @BeforeTest
    fun setup() {
        tempDir = NSTemporaryDirectory() + NSUUID().UUIDString
        Dispatchers.setMain(testDispatcher)
        _preferenceDatastore = createPreferencesDatastore(
            fileName = "${TEST_DATASTORE_BLOCKING_NAME}.preferences_pb",
        ) {
            tempDir
        }
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
        NSFileManager.defaultManager.removeItemAtPath(tempDir, null)
    }
}
