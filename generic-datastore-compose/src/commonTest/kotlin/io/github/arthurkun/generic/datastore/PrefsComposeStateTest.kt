package io.github.arthurkun.generic.datastore

import androidx.compose.runtime.mutableStateOf
import io.github.arthurkun.generic.datastore.core.DelegatedPreference
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.yield
import kotlin.reflect.KProperty
import kotlin.test.Test
import kotlin.test.assertEquals

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
