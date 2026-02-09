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
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.test.AfterTest
import kotlin.test.BeforeTest

private const val TEST_DATASTORE_NAME = "test_kserialized_datastore"

class DesktopKSerializedTest : AbstractKSerializedTest() {

    @TempDir
    lateinit var tempFolder: File

    private lateinit var _preferenceDatastore: GenericPreferencesDatastore
    private val _testDispatcher = StandardTestDispatcher()
    private lateinit var testScope: CoroutineScope

    override val preferenceDatastore: GenericPreferencesDatastore get() = _preferenceDatastore
    override val dataStore: DataStore<Preferences> get() = _preferenceDatastore.datastore
    override val testDispatcher: TestDispatcher get() = _testDispatcher

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(_testDispatcher)
        testScope = CoroutineScope(Job() + _testDispatcher)
        _preferenceDatastore = createPreferencesDatastore(
            fileName = "${TEST_DATASTORE_NAME}.preferences_pb",
            scope = testScope,
        ) {
            tempFolder.absolutePath
        }
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
        testScope.cancel()
    }
}
