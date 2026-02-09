package io.github.arthurkun.generic.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import io.github.arthurkun.generic.datastore.preferences.GenericPreferencesDatastore
import io.github.arthurkun.generic.datastore.preferences.createPreferencesDatastore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import platform.Foundation.NSFileManager
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSUUID
import kotlin.test.AfterTest
import kotlin.test.BeforeTest

private const val TEST_DATASTORE_NAME = "test_kserialized_set_datastore"

class IosKSerializedSetTest : AbstractKSerializedSetTest() {

    private lateinit var tempDir: String
    private lateinit var _preferenceDatastore: GenericPreferencesDatastore
    private val _testDispatcher = StandardTestDispatcher()
    private lateinit var testScope: CoroutineScope

    override val preferenceDatastore: GenericPreferencesDatastore get() = _preferenceDatastore
    override val dataStore: DataStore<Preferences> get() = _preferenceDatastore.datastore
    override val testDispatcher: TestDispatcher get() = _testDispatcher

    @BeforeTest
    fun setup() {
        tempDir = NSTemporaryDirectory() + NSUUID().UUIDString
        Dispatchers.setMain(_testDispatcher)
        testScope = CoroutineScope(Job() + _testDispatcher)
        _preferenceDatastore = createPreferencesDatastore(
            fileName = "${TEST_DATASTORE_NAME}.preferences_pb",
            scope = testScope,
        ) {
            tempDir
        }
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
        testScope.cancel()
        NSFileManager.defaultManager.removeItemAtPath(tempDir, null)
    }
}
