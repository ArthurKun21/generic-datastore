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
import platform.Foundation.NSFileManager
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSUUID

class IosTestHelper private constructor(
    private val datastoreName: String,
    private val useStandardDispatcher: Boolean,
) {
    private lateinit var tempDir: String
    private lateinit var _preferenceDatastore: GenericPreferencesDatastore
    private lateinit var _testDispatcher: TestDispatcher
    private lateinit var testScope: CoroutineScope

    val preferenceDatastore: GenericPreferencesDatastore get() = _preferenceDatastore
    val dataStore: DataStore<Preferences> get() = _preferenceDatastore.datastore
    val testDispatcher: TestDispatcher get() = _testDispatcher

    fun setup() {
        tempDir = NSTemporaryDirectory() + NSUUID().UUIDString
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
            tempDir
        }
    }

    fun tearDown() {
        try {
            if (::testScope.isInitialized) {
                testScope.cancel()
            }
        } finally {
            try {
                if (::tempDir.isInitialized) {
                    NSFileManager.defaultManager.removeItemAtPath(tempDir, null)
                }
            } finally {
                Dispatchers.resetMain()
            }
        }
    }

    companion object {
        fun standard(datastoreName: String): IosTestHelper {
            return IosTestHelper(datastoreName, useStandardDispatcher = true)
        }

        fun blocking(datastoreName: String): IosTestHelper {
            return IosTestHelper(datastoreName, useStandardDispatcher = false)
        }
    }
}
