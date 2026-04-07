package io.github.arthurkun.generic.datastore

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import io.github.arthurkun.generic.datastore.batch.ComposeRuntimeTestHarness
import io.github.arthurkun.generic.datastore.core.DelegatedPreference
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.reflect.KProperty
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

abstract class AbstractRememberTest {

    abstract val testDispatcher: TestDispatcher

    @Composable
    protected abstract fun PlatformProviders(content: @Composable () -> Unit)

    @Test
    fun remember_clearsOptimisticOverrideWhenWriteFails() = runTest(testDispatcher) {
        val preference = FailingDelegatedPreference(defaultValue = "fallback")
        var observedState: MutableState<String>? = null

        val harness = ComposeRuntimeTestHarness(testDispatcher)
        harness.setContent {
            PlatformProviders {
                observedState = preference.remember(context = testDispatcher)
            }
        }

        val state = assertNotNull(observedState)

        harness.awaitIdle()
        assertEquals("fallback", state.value)

        state.value = "local"
        assertEquals("local", state.value)

        runCurrent()
        harness.awaitIdle()

        assertEquals("fallback", state.value)
        assertEquals("fallback", preference.get())
        assertEquals(1, preference.setCalls)

        harness.dispose()
    }
}

private class FailingDelegatedPreference<T>(
    override val defaultValue: T,
) : DelegatedPreference<T> {

    private val flow = MutableStateFlow(defaultValue)

    var setCalls: Int = 0
        private set

    override fun key(): String = "failing_remember_preference"

    override suspend fun get(): T = flow.value

    override suspend fun set(value: T) {
        setCalls += 1
        throw IllegalStateException("forced preference write failure")
    }

    override suspend fun update(transform: (T) -> T) {
        set(transform(flow.value))
    }

    override suspend fun delete() = Unit

    override suspend fun resetToDefault() = Unit

    override fun asFlow(): Flow<T> = flow.asStateFlow()

    override fun stateIn(
        scope: kotlinx.coroutines.CoroutineScope,
        started: kotlinx.coroutines.flow.SharingStarted,
    ): StateFlow<T> = flow.asStateFlow()

    override fun getBlocking(): T = flow.value

    override fun setBlocking(value: T) {
        setCalls += 1
        throw IllegalStateException("forced preference write failure")
    }

    override fun resetToDefaultBlocking() {
        flow.value = defaultValue
    }

    override fun getValue(thisRef: Any?, property: KProperty<*>): T = getBlocking()

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) = setBlocking(value)
}