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

class DesktopTestHelper private constructor(
    private val datastoreName: String,
    private val useStandardDispatcher: Boolean,
) {
    private lateinit var _preferenceDatastore: GenericPreferencesDatastore
    private lateinit var _testDispatcher: TestDispatcher
    private lateinit var testScope: CoroutineScope

    val preferenceDatastore: GenericPreferencesDatastore get() = _preferenceDatastore
    val dataStore: DataStore<Preferences> get() = _preferenceDatastore.datastore
    val testDispatcher: TestDispatcher get() = _testDispatcher

    fun setup(tempFolderPath: String) {
        _testDispatcher = if (useStandardDispatcher) {
            StandardTestDispatcher()
        } else {
            UnconfinedTestDispatcher()
        }
        Dispatchers.setMain(_testDispatcher)

        testScope = CoroutineScope(Job() + _testDispatcher)
        _preferenceDatastore = createPreferencesDatastore(
            fileName = "$datastoreName.preferences_pb",
            scope = testScope,
        ) {
            tempFolderPath
        }
    }

    fun tearDown() {
        try {
            if (::testScope.isInitialized) {
                testScope.cancel()
            }
        } finally {
            Dispatchers.resetMain()
        }
    }

    companion object {
        fun standard(datastoreName: String): DesktopTestHelper {
            return DesktopTestHelper(datastoreName, useStandardDispatcher = true)
        }

        fun blocking(datastoreName: String): DesktopTestHelper {
            return DesktopTestHelper(datastoreName, useStandardDispatcher = false)
        }
    }
}
