package io.github.arthurkun.generic.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.github.arthurkun.generic.datastore.preferences.GenericPreferencesDatastore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.runner.RunWith
import java.io.File

private const val TEST_DATASTORE_NAME = "test_kserialized_list_datastore"

@RunWith(AndroidJUnit4::class)
class AndroidKSerializedListTest : AbstractKSerializedListTest() {

    private lateinit var _dataStore: DataStore<Preferences>
    private lateinit var _preferenceDatastore: GenericPreferencesDatastore
    private lateinit var testContext: Context
    private val _testDispatcher = StandardTestDispatcher()
    private lateinit var testScope: CoroutineScope

    override val preferenceDatastore: GenericPreferencesDatastore get() = _preferenceDatastore
    override val dataStore: DataStore<Preferences> get() = _dataStore
    override val testDispatcher: TestDispatcher get() = _testDispatcher

    @Before
    fun setup() {
        Dispatchers.setMain(_testDispatcher)
        testContext = ApplicationProvider.getApplicationContext()
        testScope = CoroutineScope(Job() + _testDispatcher)
        _dataStore = PreferenceDataStoreFactory.create(
            scope = testScope,
            produceFile = { testContext.preferencesDataStoreFile(TEST_DATASTORE_NAME) },
        )
        _preferenceDatastore = GenericPreferencesDatastore(_dataStore)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        val dataStoreFile =
            File(testContext.filesDir, "datastore/${TEST_DATASTORE_NAME}.preferences_pb")
        if (dataStoreFile.exists()) {
            dataStoreFile.delete()
        }
        testScope.cancel()
    }
}
