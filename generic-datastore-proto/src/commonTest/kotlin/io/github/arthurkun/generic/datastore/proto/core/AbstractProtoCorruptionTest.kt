@file:OptIn(io.github.arthurkun.generic.datastore.core.InternalGenericDatastoreApi::class)

package io.github.arthurkun.generic.datastore.proto.core

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.DataStore
import androidx.datastore.core.IOException
import io.github.arthurkun.generic.datastore.proto.GenericProtoDatastore
import io.github.arthurkun.generic.datastore.proto.ProtoPreference
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertSame
import kotlin.test.assertTrue

/**
 * Verifies the error-handling contract of the proto read pipeline: a [CorruptionException]
 * propagates unchanged, a plain [IOException] is rethrown wrapped as a [CorruptionException],
 * and non-I/O exceptions are never intercepted. There is no silent fallback to defaults.
 */
abstract class AbstractProtoCorruptionTest {

    abstract val testDispatcher: TestDispatcher

    private fun <P> corruptDatastore(error: Throwable): DataStore<P> = object : DataStore<P> {
        override val data = flow<P> { throw error }

        override suspend fun updateData(transform: suspend (t: P) -> P): P {
            throw UnsupportedOperationException("updateData is not exercised by corruption tests")
        }
    }

    private fun corruptIntField(error: Throwable): ProtoPreference<Int> = GenericProtoDatastore(
        datastore = corruptDatastore<TestProtoData>(error),
        defaultValue = TestProtoData(),
    ).field(
        defaultValue = 0,
        getter = { it.id },
        updater = { proto, id -> proto.copy(id = id) },
    )

    @Test
    fun field_asFlow_rethrowsCorruptionExceptionUnchanged() = runTest(testDispatcher) {
        val corruption = CorruptionException("corrupted proto file")
        val pref = corruptIntField(corruption)

        val thrown = assertFailsWith<CorruptionException> {
            pref.asFlow().first()
        }
        assertSame(corruption, thrown)
    }

    @Test
    fun field_asFlow_wrapsIOExceptionAsCorruptionException() = runTest(testDispatcher) {
        val ioError = IOException("disk read failed")
        val pref = corruptIntField(ioError)

        val thrown = assertFailsWith<CorruptionException> {
            pref.asFlow().first()
        }
        assertSame(ioError, thrown.cause)
        assertEquals("disk read failed", thrown.message)
    }

    @Test
    fun field_get_propagatesCorruptionException() = runTest(testDispatcher) {
        val corruption = CorruptionException("corrupted proto file")
        val pref = corruptIntField(corruption)

        val thrown = assertFailsWith<CorruptionException> {
            pref.get()
        }
        // get() hops to an IO dispatcher; coroutine stack-trace recovery may re-wrap the
        // exception, so assert type, message, and that the original is in the cause chain.
        assertEquals("corrupted proto file", thrown.message)
        assertTrue(thrown.containsInChain(corruption))
    }

    @Test
    fun field_get_wrapsIOExceptionAsCorruptionException() = runTest(testDispatcher) {
        val ioError = IOException("disk read failed")
        val pref = corruptIntField(ioError)

        val thrown = assertFailsWith<CorruptionException> {
            pref.get()
        }
        assertEquals("disk read failed", thrown.message)
        assertTrue(thrown.containsInChain(ioError))
    }

    private fun Throwable.containsInChain(throwable: Throwable): Boolean =
        generateSequence(this) { it.cause }.any { it === throwable }

    @Test
    fun field_asFlow_propagatesNonIOExceptionsUnchanged() = runTest(testDispatcher) {
        val unexpected = IllegalStateException("unexpected failure")
        val pref = corruptIntField(unexpected)

        val thrown = assertFailsWith<IllegalStateException> {
            pref.asFlow().first()
        }
        assertSame(unexpected, thrown)
    }

    @Test
    fun data_asFlow_rethrowsCorruptionExceptionUnchanged() = runTest(testDispatcher) {
        val corruption = CorruptionException("corrupted proto file")
        val datastore = GenericProtoDatastore(
            datastore = corruptDatastore<TestProtoData>(corruption),
            defaultValue = TestProtoData(),
        )

        val thrown = assertFailsWith<CorruptionException> {
            datastore.data().asFlow().first()
        }
        assertSame(corruption, thrown)
    }
}
