package io.github.arthurkun.generic.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import io.github.arthurkun.generic.datastore.preferences.GenericPreferencesDatastore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.TestMethodOrder
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.test.AfterTest
import kotlin.test.BeforeTest

private const val TEST_DATASTORE_BLOCKING_NAME = "test_datastore_blocking"

@TestMethodOrder(MethodOrderer.MethodName::class)
class DesktopDatastoreBlockingTest : AbstractDatastoreBlockingTest() {

    @TempDir
    lateinit var tempFolder: File

    private lateinit var dataStore: DataStore<Preferences>
    private lateinit var _preferenceDatastore: GenericPreferencesDatastore
    private val testDispatcher = UnconfinedTestDispatcher()

    override val preferenceDatastore: GenericPreferencesDatastore get() = _preferenceDatastore

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        dataStore = PreferenceDataStoreFactory.create(
            produceFile = {
                File(tempFolder, "${TEST_DATASTORE_BLOCKING_NAME}.preferences_pb")
            },
        )
        _preferenceDatastore = GenericPreferencesDatastore(dataStore)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }
}
