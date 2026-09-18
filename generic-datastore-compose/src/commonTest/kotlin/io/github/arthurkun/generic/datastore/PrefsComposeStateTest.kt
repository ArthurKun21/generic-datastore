package io.github.arthurkun.generic.datastore

import androidx.compose.runtime.mutableStateOf
import androidx.datastore.core.CorruptionException
import io.github.arthurkun.generic.datastore.core.DelegatedPreference
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.yield
import kotlin.reflect.KProperty
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class PrefsComposeStateTest {

    @Test
    fun setClearsOptimisticOverrideWhenWriteFails() = runTest {
        val preference = FailingPreference(defaultValue = "fallback")
        val state = PrefsComposeState(
            prefs = preference,
            state = mutableStateOf("fallback"),
            scope = this,
        )

        state.value = "local"
        assertEquals("local", state.value)

        runCurrent()

        assertEquals("fallback", state.value)
        assertEquals("fallback", preference.get())
    }

    @Test
    fun setRethrowsCorruptionExceptionToCallerScope() = runTest {
        val preference = CorruptingPreference(defaultValue = "fallback")
        val caught = mutableListOf<Throwable>()
        val handler = CoroutineExceptionHandler { _, throwable -> caught.add(throwable) }
        val scope = CoroutineScope(SupervisorJob() + StandardTestDispatcher(testScheduler) + handler)

        try {
            val state = PrefsComposeState(
                prefs = preference,
                state = mutableStateOf("fallback"),
                scope = scope,
            )

            state.value = "local"
            assertEquals("local", state.value)

            runCurrent()

            assertEquals(1, caught.size)
            assertIs<CorruptionException>(caught.first())
            assertEquals("fallback", state.value)
            assertEquals("fallback", preference.get())
        } finally {
            scope.cancel()
        }
    }

    @Test
    fun olderFailedWriteDoesNotClearNewerOptimisticOverride() = runTest {
        val preference = FailsFirstWritePreference(defaultValue = "fallback")
        val upstreamState = mutableStateOf("fallback")
        val state = PrefsComposeState(
            prefs = preference,
            state = upstreamState,
            scope = this,
        )

        state.value = "first"
        state.value = "second"

        runCurrent()
        upstreamState.value = preference.get()

        assertEquals("second", state.value)
        assertEquals("second", preference.get())
    }

    private class FailingPreference(
        override val defaultValue: String,
    ) : DelegatedPreference<String> {
        private val values = MutableStateFlow(defaultValue)

        override fun key(): String = "failing"

        override suspend fun get(): String = values.value

        override suspend fun set(value: String) {
            throw IllegalStateException("forced write failure")
        }

        override suspend fun update(transform: (String) -> String) {
            set(transform(values.value))
        }

        override suspend fun delete() {
            resetToDefault()
        }

        override suspend fun resetToDefault() {
            values.value = defaultValue
        }

        override fun asFlow(): Flow<String> = values

        override fun stateIn(scope: CoroutineScope, started: SharingStarted): StateFlow<String> =
            asFlow().stateIn(scope, started, defaultValue)

        override fun getBlocking(): String = values.value

        override fun setBlocking(value: String) {
            values.value = value
        }

        override fun resetToDefaultBlocking() {
            values.value = defaultValue
        }

        override fun getValue(thisRef: Any?, property: KProperty<*>): String = values.value

        override fun setValue(thisRef: Any?, property: KProperty<*>, value: String) {
            values.value = value
        }
    }

    private class CorruptingPreference(
        override val defaultValue: String,
    ) : DelegatedPreference<String> {
        private val values = MutableStateFlow(defaultValue)

        override fun key(): String = "corrupting"

        override suspend fun get(): String = values.value

        override suspend fun set(value: String) {
            throw CorruptionException("corrupted datastore file")
        }

        override suspend fun update(transform: (String) -> String) {
            set(transform(values.value))
        }

        override suspend fun delete() {
            resetToDefault()
        }

        override suspend fun resetToDefault() {
            values.value = defaultValue
        }

        override fun asFlow(): Flow<String> = values

        override fun stateIn(scope: CoroutineScope, started: SharingStarted): StateFlow<String> =
            asFlow().stateIn(scope, started, defaultValue)

        override fun getBlocking(): String = values.value

        override fun setBlocking(value: String) {
            values.value = value
        }

        override fun resetToDefaultBlocking() {
            values.value = defaultValue
        }

        override fun getValue(thisRef: Any?, property: KProperty<*>): String = values.value

        override fun setValue(thisRef: Any?, property: KProperty<*>, value: String) {
            values.value = value
        }
    }

    private class FailsFirstWritePreference(
        override val defaultValue: String,
    ) : DelegatedPreference<String> {
        private val values = MutableStateFlow(defaultValue)
        private var writes = 0

        override fun key(): String = "fails_first"

        override suspend fun get(): String = values.value

        override suspend fun set(value: String) {
            writes += 1
            if (writes == 1) {
                yield()
                throw IllegalStateException("forced first write failure")
            }
            values.value = value
        }

        override suspend fun update(transform: (String) -> String) {
            set(transform(values.value))
        }

        override suspend fun delete() {
            resetToDefault()
        }

        override suspend fun resetToDefault() {
            values.value = defaultValue
        }

        override fun asFlow(): Flow<String> = values

        override fun stateIn(scope: CoroutineScope, started: SharingStarted): StateFlow<String> =
            asFlow().stateIn(scope, started, defaultValue)

        override fun getBlocking(): String = values.value

        override fun setBlocking(value: String) {
            values.value = value
        }

        override fun resetToDefaultBlocking() {
            values.value = defaultValue
        }

        override fun getValue(thisRef: Any?, property: KProperty<*>): String = values.value

        override fun setValue(thisRef: Any?, property: KProperty<*>, value: String) {
            values.value = value
        }
    }
}
