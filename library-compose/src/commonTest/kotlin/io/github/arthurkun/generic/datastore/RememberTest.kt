package io.github.arthurkun.generic.datastore

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class RememberTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun createTestDataStore(): DataStore<Preferences> {
        val tmpFile = File.createTempFile("test_datastore", ".preferences_pb")
        tmpFile.deleteOnExit()
        
        return PreferenceDataStoreFactory.create(
            produceFile = { tmpFile }
        )
    }

    @Test
    fun testSingleRemember_readsAndWritesCorrectly() = runTest {
        val datastore = GenericPreferenceDatastore(createTestDataStore())
        val pref = datastore.string("test_key", "default")
        
        composeTestRule.setContent {
            var value by pref.remember()
            
            // Verify initial value
            assertEquals("default", value)
            
            // Update value
            value = "new_value"
        }
        
        composeTestRule.waitForIdle()
        
        // Verify value was persisted
        assertEquals("new_value", pref.get())
    }

    @Test
    fun testMultipleRemembers_shareResourcesEfficiently() = runTest {
        val datastore = GenericPreferenceDatastore(createTestDataStore())
        val pref1 = datastore.string("key1", "default1")
        val pref2 = datastore.int("key2", 0)
        val pref3 = datastore.bool("key3", false)
        
        composeTestRule.setContent {
            var value1 by pref1.remember()
            var value2 by pref2.remember()
            var value3 by pref3.remember()
            
            // Verify all initial values
            assertEquals("default1", value1)
            assertEquals(0, value2)
            assertEquals(false, value3)
            
            // Update all values
            value1 = "updated1"
            value2 = 42
            value3 = true
        }
        
        composeTestRule.waitForIdle()
        
        // Verify all values were persisted
        assertEquals("updated1", pref1.get())
        assertEquals(42, pref2.get())
        assertEquals(true, pref3.get())
    }

    @Test
    fun testRapidUpdates_areCoalesced() = runTest {
        val datastore = GenericPreferenceDatastore(createTestDataStore())
        val pref = datastore.int("counter", 0)
        
        composeTestRule.setContent {
            var counter by pref.remember()
            
            // Rapid updates - only last value should be persisted
            counter = 1
            counter = 2
            counter = 3
            counter = 4
            counter = 5
        }
        
        composeTestRule.waitForIdle()
        
        // Should have the latest value
        assertEquals(5, pref.get())
    }

    @Test
    fun testObserveAsState_readOnlyAccess() = runTest {
        val datastore = GenericPreferenceDatastore(createTestDataStore())
        val pref = datastore.string("readonly", "initial")
        
        // Set value before observing
        pref.set("observed_value")
        
        composeTestRule.setContent {
            val value by pref.observeAsState()
            
            // Verify read access works
            assertEquals("observed_value", value)
        }
        
        composeTestRule.waitForIdle()
    }

    @Test
    fun testRememberPreferences_batchObservation() = runTest {
        val datastore = GenericPreferenceDatastore(createTestDataStore())
        val pref1 = datastore.string("batch1", "value1")
        val pref2 = datastore.int("batch2", 10)
        val pref3 = datastore.bool("batch3", true)
        
        composeTestRule.setContent {
            val prefs by rememberPreferences(
                "key1" to pref1,
                "key2" to pref2,
                "key3" to pref3
            )
            
            // Verify all values are accessible
            assertEquals("value1", prefs["key1"])
            assertEquals(10, prefs["key2"])
            assertEquals(true, prefs["key3"])
        }
        
        composeTestRule.waitForIdle()
    }

    @Test
    fun testRemember_withRecomposition() = runTest {
        val datastore = GenericPreferenceDatastore(createTestDataStore())
        val pref = datastore.string("recomp", "initial")
        
        var recomposeCount = 0
        
        composeTestRule.setContent {
            recomposeCount++
            var value by pref.remember()
            
            if (recomposeCount == 1) {
                assertEquals("initial", value)
                value = "updated"
            } else if (recomposeCount >= 2) {
                // After recomposition, should have updated value
                assertEquals("updated", value)
            }
        }
        
        composeTestRule.waitForIdle()
    }

    @Test
    fun testRemember_cleanupOnForgotten() = runTest {
        val datastore = GenericPreferenceDatastore(createTestDataStore())
        val pref = datastore.string("cleanup", "value")
        
        var isShowing = true
        
        composeTestRule.setContent {
            if (isShowing) {
                val value by pref.remember()
                assertNotNull(value)
            }
        }
        
        composeTestRule.waitForIdle()
        
        // Hide composable to trigger cleanup
        isShowing = false
        composeTestRule.waitForIdle()
        
        // Resources should be cleaned up (no way to directly verify,
        // but test shouldn't leak memory or crash)
    }

    @Test
    fun testMultipleRemembers_differentKeys() = runTest {
        val datastore = GenericPreferenceDatastore(createTestDataStore())
        
        // Create many preferences
        val prefs = (1..10).map { i ->
            datastore.string("key_$i", "default_$i")
        }
        
        composeTestRule.setContent {
            // Remember all preferences
            val states = prefs.map { pref ->
                var value by pref.remember()
                value
            }
            
            // Verify all have correct values
            states.forEachIndexed { index, value ->
                assertEquals("default_${index + 1}", value)
            }
        }
        
        composeTestRule.waitForIdle()
    }

    @Test
    fun testRemember_errorHandling() = runTest {
        val datastore = GenericPreferenceDatastore(createTestDataStore())
        val pref = datastore.string("error_test", "initial")
        
        composeTestRule.setContent {
            var value by pref.remember()
            
            // Should not crash on errors (graceful degradation)
            value = "updated_value"
        }
        
        composeTestRule.waitForIdle()
        
        // Value should still be updated despite any internal errors
        assertEquals("updated_value", pref.get())
    }
}
