package io.github.arthurkun.generic.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.test.core.app.ApplicationProvider
import io.github.arthurkun.generic.datastore.preferences.GenericPreferencesDatastore
import io.github.arthurkun.generic.datastore.preferences.createPreferencesDatastore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import java.io.File

/**
 * Helper class for Android instrumented tests that provides common setup and teardown logic
 * for datastore tests.
 */
class AndroidTestHelper private constructor(
    private val datastoreName: String,
    private val useStandardDispatcher: Boolean,
) {
    private lateinit var _preferenceDatastore: GenericPreferencesDatastore
    private lateinit var _testDispatcher: TestDispatcher
    private lateinit var testContext: Context
    private var testScope: CoroutineScope? = null

    val preferenceDatastore: GenericPreferencesDatastore get() = _preferenceDatastore
    val dataStore: DataStore<Preferences> get() = _preferenceDatastore.datastore
    val testDispatcher: TestDispatcher get() = _testDispatcher

    fun setup() {
        _testDispatcher = if (useStandardDispatcher) {
            StandardTestDispatcher()
        } else {
            UnconfinedTestDispatcher()
        }
        Dispatchers.setMain(_testDispatcher)
        testContext = ApplicationProvider.getApplicationContext()

        if (useStandardDispatcher) {
            testScope = CoroutineScope(Job() + _testDispatcher)
            _preferenceDatastore = createPreferencesDatastore(
                scope = testScope!!,
                producePath = {
                    testContext.preferencesDataStoreFile(datastoreName).absolutePath
                },
            )
        } else {
            _preferenceDatastore = createPreferencesDatastore(
                producePath = {
                    testContext.preferencesDataStoreFile(datastoreName).absolutePath
                },
            )
        }
    }

    fun tearDown() {
        Dispatchers.resetMain()
        val dataStoreFile =
            File(testContext.filesDir, "datastore/$datastoreName.preferences_pb")
        if (dataStoreFile.exists()) {
            dataStoreFile.delete()
        }
        testScope?.cancel()
    }

    companion object {
        /**
         * Creates a helper for standard tests that use StandardTestDispatcher
         * and a custom CoroutineScope.
         */
        fun standard(datastoreName: String): AndroidTestHelper {
            return AndroidTestHelper(datastoreName, useStandardDispatcher = true)
        }

        /**
         * Creates a helper for blocking tests that use UnconfinedTestDispatcher
         * without a custom CoroutineScope.
         */
        fun blocking(datastoreName: String): AndroidTestHelper {
            return AndroidTestHelper(datastoreName, useStandardDispatcher = false)
        }
    }
}
