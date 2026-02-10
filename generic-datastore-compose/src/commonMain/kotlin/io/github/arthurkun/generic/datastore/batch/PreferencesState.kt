package io.github.arthurkun.generic.datastore.batch

import androidx.compose.runtime.MutableState

/**
 * Holds two [MutableState] values obtained from [rememberPreferences].
 *
 * Supports destructuring: `val (a, b) = rememberPreferences(pref1, pref2)`.
 */
public class PreferencesState2<T1, T2> internal constructor(
    private val state1: MutableState<T1>,
    private val state2: MutableState<T2>,
) {
    public operator fun component1(): MutableState<T1> = state1
    public operator fun component2(): MutableState<T2> = state2
}

/**
 * Holds three [MutableState] values obtained from [rememberPreferences].
 *
 * Supports destructuring: `val (a, b, c) = rememberPreferences(pref1, pref2, pref3)`.
 */
public class PreferencesState3<T1, T2, T3> internal constructor(
    private val state1: MutableState<T1>,
    private val state2: MutableState<T2>,
    private val state3: MutableState<T3>,
) {
    public operator fun component1(): MutableState<T1> = state1
    public operator fun component2(): MutableState<T2> = state2
    public operator fun component3(): MutableState<T3> = state3
}

/**
 * Holds four [MutableState] values obtained from [rememberPreferences].
 *
 * Supports destructuring: `val (a, b, c, d) = rememberPreferences(pref1, pref2, pref3, pref4)`.
 */
public class PreferencesState4<T1, T2, T3, T4> internal constructor(
    private val state1: MutableState<T1>,
    private val state2: MutableState<T2>,
    private val state3: MutableState<T3>,
    private val state4: MutableState<T4>,
) {
    public operator fun component1(): MutableState<T1> = state1
    public operator fun component2(): MutableState<T2> = state2
    public operator fun component3(): MutableState<T3> = state3
    public operator fun component4(): MutableState<T4> = state4
}

/**
 * Holds five [MutableState] values obtained from [rememberPreferences].
 *
 * Supports destructuring:
 * `val (a, b, c, d, e) = rememberPreferences(pref1, pref2, pref3, pref4, pref5)`.
 */
public class PreferencesState5<T1, T2, T3, T4, T5> internal constructor(
    private val state1: MutableState<T1>,
    private val state2: MutableState<T2>,
    private val state3: MutableState<T3>,
    private val state4: MutableState<T4>,
    private val state5: MutableState<T5>,
) {
    public operator fun component1(): MutableState<T1> = state1
    public operator fun component2(): MutableState<T2> = state2
    public operator fun component3(): MutableState<T3> = state3
    public operator fun component4(): MutableState<T4> = state4
    public operator fun component5(): MutableState<T5> = state5
}
