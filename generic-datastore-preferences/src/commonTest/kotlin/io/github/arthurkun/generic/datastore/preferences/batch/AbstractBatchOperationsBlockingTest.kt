package io.github.arthurkun.generic.datastore.preferences.batch

import io.github.arthurkun.generic.datastore.preferences.GenericPreferencesDatastore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

abstract class AbstractBatchOperationsBlockingTest {

    abstract val preferenceDatastore: GenericPreferencesDatastore

    @Test
    fun batchGetBlocking_readMultiplePreferences() {
        val stringPref = preferenceDatastore.string("batch_b_str", "hello")
        val intPref = preferenceDatastore.int("batch_b_int", 42)

        val result = preferenceDatastore.batchGetBlocking {
            Pair(get(stringPref), get(intPref))
        }

        assertEquals("hello", result.first)
        assertEquals(42, result.second)
    }

    @Test
    fun batchGetBlocking_readNullablePreferences() {
        val nullablePref = preferenceDatastore.nullableString("batch_b_nullable")

        val result = preferenceDatastore.batchGetBlocking {
            get(nullablePref)
        }

        assertNull(result)
    }

    @Test
    fun batchWriteBlocking_writeMultiplePreferences() {
        val stringPref = preferenceDatastore.string("batch_bw_str", "default")
        val intPref = preferenceDatastore.int("batch_bw_int", 0)

        preferenceDatastore.batchWriteBlocking {
            set(stringPref, "written")
            set(intPref, 123)
        }

        assertEquals("written", stringPref.getBlocking())
        assertEquals(123, intPref.getBlocking())
    }

    @Test
    fun batchWriteBlocking_deletePreference() {
        val pref = preferenceDatastore.string("batch_bw_del", "default")
        pref.setBlocking("set_value")

        preferenceDatastore.batchWriteBlocking {
            delete(pref)
        }

        assertEquals("default", pref.getBlocking())
    }

    @Test
    fun batchWriteBlocking_resetToDefault() {
        val pref = preferenceDatastore.int("batch_bw_reset", 42)
        pref.setBlocking(99)

        preferenceDatastore.batchWriteBlocking {
            resetToDefault(pref)
        }

        assertEquals(42, pref.getBlocking())
    }

    @Test
    fun batchUpdateBlocking_readAndWrite() {
        val counter = preferenceDatastore.int("batch_bu_counter", 10)
        val label = preferenceDatastore.string("batch_bu_label", "count:")

        preferenceDatastore.batchUpdateBlocking {
            val currentCount = get(counter)
            set(counter, currentCount + 5)
            set(label, "updated")
        }

        assertEquals(15, counter.getBlocking())
        assertEquals("updated", label.getBlocking())
    }

    @Test
    fun batchUpdateBlocking_transformFunction() {
        val pref = preferenceDatastore.int("batch_bu_transform", 10)

        preferenceDatastore.batchUpdateBlocking {
            update(pref) { it * 2 }
        }

        assertEquals(20, pref.getBlocking())
    }

    @Test
    fun batchUpdateBlocking_readsWritesMadeEarlierInSameTransaction() {
        val pref = preferenceDatastore.int("batch_bu_intra_tx", 1)

        preferenceDatastore.batchUpdateBlocking {
            update(pref) { it + 1 }
            update(pref) { it + 1 }
            set(pref, get(pref) + 1)
        }

        assertEquals(4, pref.getBlocking())
    }
}
