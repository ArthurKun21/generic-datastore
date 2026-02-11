package io.github.arthurkun.generic.datastore.batch

import androidx.compose.runtime.SnapshotMutationPolicy
import androidx.compose.runtime.mutableStateOf
import io.github.arthurkun.generic.datastore.preferences.GenericPreferencesDatastore
import io.github.arthurkun.generic.datastore.preferences.PreferencesDatastore
import io.github.arthurkun.generic.datastore.preferences.batch.BatchReadScope
import io.github.arthurkun.generic.datastore.preferences.batch.BatchWriteScope
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

abstract class AbstractBatchPrefsComposeStateTest {

    abstract val preferenceDatastore: GenericPreferencesDatastore
    abstract val testDispatcher: TestDispatcher

    @Test
    fun value_returnsDefaultWhenBatchSnapshotIsUnavailable() = runTest(testDispatcher) {
        val preference = preferenceDatastore.string("compose_batch_state_default", "fallback")
        val batchState = mutableStateOf<BatchReadScope?>(null)

        val state = BatchPrefsComposeState(
            preference = preference,
            batchState = batchState,
            datastore = preferenceDatastore,
            scope = this,
        )

        assertEquals("fallback", state.value)
    }

    @Test
    fun value_readsLatestSnapshotWhenBatchSnapshotIsAvailable() = runTest(testDispatcher) {
        val preference = preferenceDatastore.string("compose_batch_state_snapshot", "fallback")
        preference.set("stored")

        val batchState = mutableStateOf<BatchReadScope?>(currentSnapshot())
        val state = BatchPrefsComposeState(
            preference = preference,
            batchState = batchState,
            datastore = preferenceDatastore,
            scope = this,
        )

        assertEquals("stored", state.value)
    }

    @Test
    fun set_appliesOptimisticOverrideUntilUpstreamMatches() = runTest(testDispatcher) {
        val preference = preferenceDatastore.string("compose_batch_state_override", "fallback")
        preference.set("upstream")

        val batchState = mutableStateOf<BatchReadScope?>(currentSnapshot())
        val state = BatchPrefsComposeState(
            preference = preference,
            batchState = batchState,
            datastore = preferenceDatastore,
            scope = this,
        )

        assertEquals("upstream", state.value)

        state.value = "local"
        assertEquals("local", state.value)

        runCurrent()
        assertEquals("local", preference.get())

        batchState.value = currentSnapshot()
        assertEquals("local", state.value)

        preference.set("remote")
        batchState.value = currentSnapshot()
        assertEquals("remote", state.value)
    }

    @Test
    fun set_doesNotWriteWhenValueIsEquivalent() = runTest(testDispatcher) {
        val countingDatastore = CountingPreferencesDatastore(preferenceDatastore)
        val preference = countingDatastore.string("compose_batch_state_equivalent", "same")
        val batchState = mutableStateOf<BatchReadScope?>(null)
        val state = BatchPrefsComposeState(
            preference = preference,
            batchState = batchState,
            datastore = countingDatastore,
            scope = this,
        )

        state.value = "same"
        runCurrent()

        assertEquals(0, countingDatastore.batchWriteCalls)
    }

    @Test
    fun set_respectsCustomMutationPolicy() = runTest(testDispatcher) {
        val countingDatastore = CountingPreferencesDatastore(preferenceDatastore)
        val preference = countingDatastore.string("compose_batch_state_custom_policy", "same")
        val batchState = mutableStateOf<BatchReadScope?>(null)
        val ignoreCasePolicy = object : SnapshotMutationPolicy<Any?> {
            override fun equivalent(a: Any?, b: Any?): Boolean {
                if (a is String && b is String) {
                    return a.equals(b, ignoreCase = true)
                }
                return a == b
            }
        }
        val state = BatchPrefsComposeState(
            preference = preference,
            batchState = batchState,
            datastore = countingDatastore,
            scope = this,
            policy = ignoreCasePolicy,
        )

        state.value = "SAME"
        runCurrent()

        assertEquals(0, countingDatastore.batchWriteCalls)
    }

    @Test
    fun set_clearsOptimisticOverrideWhenBatchWriteFails() = runTest(testDispatcher) {
        val failingDatastore = FailingBatchWritePreferencesDatastore(preferenceDatastore)
        val preference = failingDatastore.string("compose_batch_state_write_failure", "fallback")
        val batchState = mutableStateOf<BatchReadScope?>(null)
        val state = BatchPrefsComposeState(
            preference = preference,
            batchState = batchState,
            datastore = failingDatastore,
            scope = this,
        )

        state.value = "local"
        assertEquals("local", state.value)

        runCurrent()

        assertEquals("fallback", state.value)
        assertEquals("fallback", preference.get())
    }

    private suspend fun currentSnapshot(): BatchReadScope = preferenceDatastore.batchGet { this }
}

private class CountingPreferencesDatastore(
    private val delegate: PreferencesDatastore,
) : PreferencesDatastore by delegate {
    var batchWriteCalls: Int = 0
        private set

    override suspend fun batchWrite(block: BatchWriteScope.() -> Unit) {
        batchWriteCalls += 1
        delegate.batchWrite(block)
    }
}

private class FailingBatchWritePreferencesDatastore(
    private val delegate: PreferencesDatastore,
) : PreferencesDatastore by delegate {
    override suspend fun batchWrite(block: BatchWriteScope.() -> Unit) {
        throw IllegalStateException("forced batch write failure")
    }
}
