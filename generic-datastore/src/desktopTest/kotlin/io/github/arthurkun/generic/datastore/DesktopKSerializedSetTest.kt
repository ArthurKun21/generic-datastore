package io.github.arthurkun.generic.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import io.github.arthurkun.generic.datastore.preferences.GenericPreferencesDatastore
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

private const val TEST_DATASTORE_NAME = "test_kserialized_set_datastore"

class DesktopKSerializedSetTest : AbstractKSerializedSetTest() {

    @TempDir
    lateinit var tempFolder: File

    private lateinit var _dataStore: DataStore<Preferences>
    private lateinit var _preferenceDatastore: GenericPreferencesDatastore
    private val _testDispatcher = StandardTestDispatcher()
    private lateinit var testScope: CoroutineScope

    override val preferenceDatastore: GenericPreferencesDatastore get() = _preferenceDatastore
    override val dataStore: DataStore<Preferences> get() = _dataStore
    override val testDispatcher: TestDispatcher get() = _testDispatcher

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(_testDispatcher)
        testScope = CoroutineScope(Job() + _testDispatcher)
        _dataStore = PreferenceDataStoreFactory.create(
            scope = testScope,
            produceFile = {
                File(tempFolder, "${TEST_DATASTORE_NAME}.preferences_pb")
            },
        )
        _preferenceDatastore = GenericPreferencesDatastore(_dataStore)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
        testScope.cancel()
    }
}
