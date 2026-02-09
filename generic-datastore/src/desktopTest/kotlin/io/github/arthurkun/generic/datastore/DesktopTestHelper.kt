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
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain

/**
 * Helper class for Desktop tests that provides common setup and teardown logic
 * for datastore tests.
 */
class DesktopTestHelper private constructor(
    private val datastoreName: String,
    private val useStandardDispatcher: Boolean,
) {
    private lateinit var _preferenceDatastore: GenericPreferencesDatastore
    private lateinit var _testDispatcher: TestDispatcher
    private var testScope: CoroutineScope? = null

    val preferenceDatastore: GenericPreferencesDatastore get() = _preferenceDatastore
    val dataStore: DataStore<Preferences> get() = _preferenceDatastore.datastore
    val testDispatcher: TestDispatcher get() = _testDispatcher

    /**
     * Sets up the test environment.
     * @param tempFolderPath The path to the temporary folder for the datastore file.
     *                       This should be provided from JUnit's @TempDir annotation.
     */
    fun setup(tempFolderPath: String) {
        _testDispatcher = if (useStandardDispatcher) {
            StandardTestDispatcher()
        } else {
            UnconfinedTestDispatcher()
        }
        Dispatchers.setMain(_testDispatcher)

        if (useStandardDispatcher) {
            testScope = CoroutineScope(Job() + _testDispatcher)
            _preferenceDatastore = createPreferencesDatastore(
                fileName = "$datastoreName.preferences_pb",
                scope = testScope!!,
            ) {
                tempFolderPath
            }
        } else {
            _preferenceDatastore = createPreferencesDatastore(
                fileName = "$datastoreName.preferences_pb",
            ) {
                tempFolderPath
            }
        }
    }

    fun tearDown() {
        Dispatchers.resetMain()
        testScope?.cancel()
    }

    companion object {
        /**
         * Creates a helper for standard tests that use StandardTestDispatcher
         * and a custom CoroutineScope.
         */
        fun standard(datastoreName: String): DesktopTestHelper {
            return DesktopTestHelper(datastoreName, useStandardDispatcher = true)
        }

        /**
         * Creates a helper for blocking tests that use UnconfinedTestDispatcher
         * without a custom CoroutineScope.
         */
        fun blocking(datastoreName: String): DesktopTestHelper {
            return DesktopTestHelper(datastoreName, useStandardDispatcher = false)
        }
    }
}
