package io.github.arthurkun.generic.datastore.preferences.batch

import io.github.arthurkun.generic.datastore.preferences.GenericPreferencesDatastore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

abstract class AbstractBatchOperationsBlockingTest {

    abstract val preferenceDatastore: GenericPreferencesDatastore

    /**
     * Bridges a [PreferenceBatch] built by [prefBatch] to an inline `declare` block, so existing
     * declaration fixtures keep working with the inline batch operations.
     */
    protected fun declare(batch: PreferenceBatch): PrefBuilder.() -> Unit = {
        batch.forEach { pref ->
            @Suppress("UNCHECKED_CAST")
            add(pref as BatchPref<Any?>)
        }
    }

    @Test
    fun batchReadBlocking_readWholeBatch() {
        var stringPref: BatchPref<String>? = null
        var intPref: BatchPref<Int>? = null
        val batch = prefBatch {
            stringPref = string("batch_b_str", "hello")
            intPref = int("batch_b_int", 42)
        }

        val values = preferenceDatastore.batchReadBlockingValues(declare(batch))

        assertEquals("hello", values[requireNotNull(stringPref)])
        assertEquals(42, values[requireNotNull(intPref)])
    }

    @Test
    fun batchReadBlocking_nullablePreference() {
        var nullablePref: BatchPref<String?>? = null
        val batch = prefBatch {
            nullablePref = nullableString("batch_b_nullable")
        }

        val values = preferenceDatastore.batchReadBlockingValues(declare(batch))

        assertNull(values[requireNotNull(nullablePref)])
    }

    @Test
    fun batchReadBlocking_derivedProjection() {
        var stringPref: BatchPref<String>? = null
        val batch = prefBatch {
            stringPref = string("batch_b_projection", "value")
        }

        val length = preferenceDatastore.batchReadBlocking(declare(batch)) {
            this[requireNotNull(stringPref)].length
        }

        assertEquals(5, length)
    }

    @Test
    fun batchWriteBlocking_writeMultiplePreferences() {
        var stringPref: BatchPref<String>? = null
        var intPref: BatchPref<Int>? = null
        val batch = prefBatch {
            stringPref = string("batch_bw_str", "default")
            intPref = int("batch_bw_int", 0)
        }

        preferenceDatastore.batchWriteBlocking {
            set(requireNotNull(stringPref), "written")
            set(requireNotNull(intPref), 123)
        }

        assertEquals("written", preferenceDatastore.string("batch_bw_str", "default").getBlocking())
        assertEquals(123, preferenceDatastore.int("batch_bw_int", 0).getBlocking())
    }

    @Test
    fun batchWriteBlocking_deletePreference() {
        var stringPref: BatchPref<String>? = null
        val batch = prefBatch {
            stringPref = string("batch_bw_del", "default")
        }
        val single = preferenceDatastore.string("batch_bw_del", "default")
        single.setBlocking("set_value")

        preferenceDatastore.batchWriteBlocking {
            delete(requireNotNull(stringPref))
        }

        assertEquals("default", single.getBlocking())
    }

    @Test
    fun batchWriteBlocking_resetToDefault() {
        var intPref: BatchPref<Int>? = null
        val batch = prefBatch {
            intPref = int("batch_bw_reset", 42)
        }
        val single = preferenceDatastore.int("batch_bw_reset", 42)
        single.setBlocking(99)

        preferenceDatastore.batchWriteBlocking {
            resetToDefault(requireNotNull(intPref))
        }

        assertEquals(42, single.getBlocking())
    }

    @Test
    fun batchUpdateBlocking_readAndWrite() {
        var counter: BatchPref<Int>? = null
        var label: BatchPref<String>? = null
        val batch = prefBatch {
            counter = int("batch_bu_counter", 10)
            label = string("batch_bu_label", "count:")
        }

        preferenceDatastore.batchUpdateBlocking {
            val currentCount = get(requireNotNull(counter))
            set(requireNotNull(counter), currentCount + 5)
            set(requireNotNull(label), "updated")
        }

        assertEquals(15, preferenceDatastore.int("batch_bu_counter", 10).getBlocking())
        assertEquals("updated", preferenceDatastore.string("batch_bu_label", "count:").getBlocking())
    }

    @Test
    fun batchUpdateBlocking_transformFunction() {
        var intPref: BatchPref<Int>? = null
        val batch = prefBatch {
            intPref = int("batch_bu_transform", 10)
        }

        preferenceDatastore.batchUpdateBlocking {
            update(requireNotNull(intPref)) { it * 2 }
        }

        assertEquals(20, preferenceDatastore.int("batch_bu_transform", 10).getBlocking())
    }

    @Test
    fun batchUpdateBlocking_readsWritesMadeEarlierInSameTransaction() {
        var intPref: BatchPref<Int>? = null
        val batch = prefBatch {
            intPref = int("batch_bu_intra_tx", 1)
        }
        val handle = requireNotNull(intPref)

        preferenceDatastore.batchUpdateBlocking {
            update(handle) { it + 1 }
            update(handle) { it + 1 }
            set(handle, get(handle) + 1)
        }

        assertEquals(4, preferenceDatastore.int("batch_bu_intra_tx", 1).getBlocking())
    }

    @Test
    fun batchDeleteBlocking_removesEveryDeclaredKey() {
        var intPref: BatchPref<Int>? = null
        var stringPref: BatchPref<String>? = null
        val batch = prefBatch {
            intPref = int("batch_bd_int", 3)
            stringPref = string("batch_bd_string", "d")
        }

        preferenceDatastore.int("batch_bd_int", 3).setBlocking(50)
        preferenceDatastore.string("batch_bd_string", "d").setBlocking("stored")

        preferenceDatastore.batchDeleteBlocking(declare(batch))

        val values = preferenceDatastore.batchReadBlockingValues(declare(batch))
        assertEquals(3, values[requireNotNull(intPref)])
        assertEquals("d", values[requireNotNull(stringPref)])
    }

    @Test
    fun batchReadBlocking_inlineDeclarations() {
        val single = preferenceDatastore.string("batch_b_inline_str", "hello")
        single.setBlocking("stored")

        var handle: BatchPref<String>? = null
        val values = preferenceDatastore.batchReadBlockingValues {
            handle = add(single)
        }

        assertEquals("stored", values[requireNotNull(handle)])
        val (text: String) = values
        assertEquals("stored", text)
    }

    @Test
    fun batchWriteBlocking_inlineDeclarations() {
        preferenceDatastore.batchWriteBlocking {
            val handle = string("batch_bw_inline", "default")
            set(handle, "inline_written")
        }

        assertEquals(
            "inline_written",
            preferenceDatastore.string("batch_bw_inline", "default").getBlocking(),
        )
    }
}
