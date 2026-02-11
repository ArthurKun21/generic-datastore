package io.github.arthurkun.generic.datastore.batch

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import io.github.arthurkun.generic.datastore.preferences.GenericPreferencesDatastore
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

abstract class AbstractRememberPreferencesAndBatchReadTest {

    abstract val preferenceDatastore: GenericPreferencesDatastore
    abstract val testDispatcher: TestDispatcher

    @Composable
    protected abstract fun PlatformProviders(content: @Composable () -> Unit)

    @Test
    fun rememberBatchRead_initialNullThenEmitsAndUpdates() = runTest(testDispatcher) {
        val stringPref = preferenceDatastore.string("remember_batch_read_string", "default")
        val intPref = preferenceDatastore.int("remember_batch_read_int", 1)
        var observedState: State<Pair<String, Int>?>? = null

        val harness = ComposeRuntimeTestHarness(testDispatcher)
        harness.setContent {
            PlatformProviders {
                observedState = preferenceDatastore.rememberBatchRead(context = testDispatcher) {
                    get(stringPref) to get(intPref)
                }
            }
        }

        val state = assertNotNull(observedState)
        assertNull(state.value)

        waitForNonNullValue(state, harness)
        assertEquals("default" to 1, state.value)

        stringPref.set("updated")
        intPref.set(99)

        runCurrent()
        harness.awaitIdle()
        assertEquals("updated" to 99, state.value)

        harness.dispose()
    }

    @Test
    fun rememberPreferences_twoPreferences_readsAndWrites() = runTest(testDispatcher) {
        val stringPref = preferenceDatastore.string("remember_preferences_2_string", "hello")
        val intPref = preferenceDatastore.int("remember_preferences_2_int", 10)
        var stateA: MutableState<String>? = null
        var stateB: MutableState<Int>? = null

        val harness = ComposeRuntimeTestHarness(testDispatcher)
        harness.setContent {
            PlatformProviders {
                val states = preferenceDatastore.rememberPreferences(
                    pref1 = stringPref,
                    pref2 = intPref,
                    context = testDispatcher,
                )
                stateA = states.component1()
                stateB = states.component2()
            }
        }

        val first = assertNotNull(stateA)
        val second = assertNotNull(stateB)

        harness.awaitIdle()
        assertEquals("hello", first.value)
        assertEquals(10, second.value)

        first.value = "world"
        second.value = 20

        waitUntil(harness) {
            stringPref.getBlocking() == "world" && intPref.getBlocking() == 20
        }

        assertEquals("world", stringPref.get())
        assertEquals(20, intPref.get())
        assertEquals("world", first.value)
        assertEquals(20, second.value)

        harness.dispose()
    }

    @Test
    fun rememberPreferences_threePreferences_updatesFromDatastore() = runTest(testDispatcher) {
        val pref1 = preferenceDatastore.string("remember_preferences_3_string", "a")
        val pref2 = preferenceDatastore.int("remember_preferences_3_int", 1)
        val pref3 = preferenceDatastore.bool("remember_preferences_3_bool", false)
        var state1: MutableState<String>? = null
        var state2: MutableState<Int>? = null
        var state3: MutableState<Boolean>? = null

        val harness = ComposeRuntimeTestHarness(testDispatcher)
        harness.setContent {
            PlatformProviders {
                val states = preferenceDatastore.rememberPreferences(
                    pref1 = pref1,
                    pref2 = pref2,
                    pref3 = pref3,
                    context = testDispatcher,
                )
                state1 = states.component1()
                state2 = states.component2()
                state3 = states.component3()
            }
        }

        val first = assertNotNull(state1)
        val second = assertNotNull(state2)
        val third = assertNotNull(state3)

        harness.awaitIdle()
        assertEquals("a", first.value)
        assertEquals(1, second.value)
        assertEquals(false, third.value)

        pref1.set("b")
        pref2.set(2)
        pref3.set(true)

        waitUntil(harness) {
            first.value == "b" && second.value == 2 && third.value
        }

        assertEquals("b", first.value)
        assertEquals(2, second.value)
        assertEquals(true, third.value)

        harness.dispose()
    }

    @Test
    fun rememberPreferences_fourPreferences_componentOrder() = runTest(testDispatcher) {
        val pref1 = preferenceDatastore.string("remember_preferences_4_string", "v1")
        val pref2 = preferenceDatastore.int("remember_preferences_4_int", 2)
        val pref3 = preferenceDatastore.bool("remember_preferences_4_bool", true)
        val pref4 = preferenceDatastore.long("remember_preferences_4_long", 4L)
        var state1: MutableState<String>? = null
        var state2: MutableState<Int>? = null
        var state3: MutableState<Boolean>? = null
        var state4: MutableState<Long>? = null

        val harness = ComposeRuntimeTestHarness(testDispatcher)
        harness.setContent {
            PlatformProviders {
                val states = preferenceDatastore.rememberPreferences(
                    pref1 = pref1,
                    pref2 = pref2,
                    pref3 = pref3,
                    pref4 = pref4,
                    context = testDispatcher,
                )
                state1 = states.component1()
                state2 = states.component2()
                state3 = states.component3()
                state4 = states.component4()
            }
        }

        val first = assertNotNull(state1)
        val second = assertNotNull(state2)
        val third = assertNotNull(state3)
        val fourth = assertNotNull(state4)

        harness.awaitIdle()
        assertEquals("v1", first.value)
        assertEquals(2, second.value)
        assertEquals(true, third.value)
        assertEquals(4L, fourth.value)

        harness.dispose()
    }

    @Test
    fun rememberPreferences_fivePreferences_componentOrder() = runTest(testDispatcher) {
        val pref1 = preferenceDatastore.string("remember_preferences_5_string", "v1")
        val pref2 = preferenceDatastore.int("remember_preferences_5_int", 2)
        val pref3 = preferenceDatastore.bool("remember_preferences_5_bool", true)
        val pref4 = preferenceDatastore.long("remember_preferences_5_long", 4L)
        val pref5 = preferenceDatastore.float("remember_preferences_5_float", 5f)
        var state1: MutableState<String>? = null
        var state2: MutableState<Int>? = null
        var state3: MutableState<Boolean>? = null
        var state4: MutableState<Long>? = null
        var state5: MutableState<Float>? = null

        val harness = ComposeRuntimeTestHarness(testDispatcher)
        harness.setContent {
            PlatformProviders {
                val states = preferenceDatastore.rememberPreferences(
                    pref1 = pref1,
                    pref2 = pref2,
                    pref3 = pref3,
                    pref4 = pref4,
                    pref5 = pref5,
                    context = testDispatcher,
                )
                state1 = states.component1()
                state2 = states.component2()
                state3 = states.component3()
                state4 = states.component4()
                state5 = states.component5()
            }
        }

        val first = assertNotNull(state1)
        val second = assertNotNull(state2)
        val third = assertNotNull(state3)
        val fourth = assertNotNull(state4)
        val fifth = assertNotNull(state5)

        harness.awaitIdle()
        assertEquals("v1", first.value)
        assertEquals(2, second.value)
        assertEquals(true, third.value)
        assertEquals(4L, fourth.value)
        assertEquals(5f, fifth.value)

        harness.dispose()
    }

    private suspend fun <T> TestScope.waitForNonNullValue(
        state: State<T?>,
        harness: ComposeRuntimeTestHarness,
    ) {
        repeat(10) {
            runCurrent()
            harness.awaitIdle()
            if (state.value != null) {
                return
            }
        }
    }

    private suspend fun TestScope.waitUntil(
        harness: ComposeRuntimeTestHarness,
        condition: () -> Boolean,
    ) {
        repeat(30) {
            if (condition()) {
                return
            }
            runCurrent()
            harness.awaitIdle()
        }
    }
}
