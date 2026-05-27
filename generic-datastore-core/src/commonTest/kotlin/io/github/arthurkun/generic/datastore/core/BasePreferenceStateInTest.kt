package io.github.arthurkun.generic.datastore.core

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class BasePreferenceStateInTest {

    @Test
    fun stateInCurrentInitializesWithCurrentValue() = runTest {
        val preference = FakePreference(
            key = "state_key",
            defaultValue = 0,
            initialValue = 42,
        )

        val state = preference.stateInCurrent(backgroundScope)

        assertEquals(42, state.value)
    }

    private class FakePreference(
        private val key: String,
        override val defaultValue: Int,
        initialValue: Int,
    ) : BasePreference<Int> {
        private val values = MutableStateFlow(initialValue)

        override fun key(): String = key

        override suspend fun get(): Int = values.value

        override suspend fun set(value: Int) {
            values.value = value
        }

        override suspend fun update(transform: (Int) -> Int) {
            values.value = transform(values.value)
        }

        override suspend fun delete() {
            resetToDefault()
        }

        override suspend fun resetToDefault() {
            values.value = defaultValue
        }

        override fun asFlow(): Flow<Int> = values

        override fun stateIn(scope: CoroutineScope, started: SharingStarted): StateFlow<Int> =
            asFlow().stateIn(scope, started, defaultValue)

        override fun getBlocking(): Int = values.value

        override fun setBlocking(value: Int) {
            values.value = value
        }
    }
}
