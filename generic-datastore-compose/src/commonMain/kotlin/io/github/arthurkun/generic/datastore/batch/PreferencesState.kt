package io.github.arthurkun.generic.datastore.batch

import androidx.compose.runtime.MutableState
import kotlin.reflect.KProperty

/**
 * Holds two [MutableState] values obtained from [rememberPreferences].
 *
 * Supports destructuring: `val (a, b) = rememberPreferences(pref1, pref2)`.
 *
 * Supports property delegation: `val prefs by rememberPreferences(pref1, pref2)`,
 * then use `prefs.first` and `prefs.second` to read and write values directly.
 */
public class PreferencesState2<T1, T2> internal constructor(
    private val state1: MutableState<T1>,
    private val state2: MutableState<T2>,
) {
    public var first: T1
        get() = state1.value
        set(value) {
            state1.value = value
        }

    public var second: T2
        get() = state2.value
        set(value) {
            state2.value = value
        }

    public operator fun component1(): MutableState<T1> = state1
    public operator fun component2(): MutableState<T2> = state2

    public operator fun getValue(thisRef: Any?, property: KProperty<*>): PreferencesState2<T1, T2> = this
}

/**
 * Holds three [MutableState] values obtained from [rememberPreferences].
 *
 * Supports destructuring: `val (a, b, c) = rememberPreferences(pref1, pref2, pref3)`.
 *
 * Supports property delegation: `val prefs by rememberPreferences(pref1, pref2, pref3)`,
 * then use `prefs.first`, `prefs.second`, and `prefs.third` to read and write values directly.
 */
public class PreferencesState3<T1, T2, T3> internal constructor(
    private val state1: MutableState<T1>,
    private val state2: MutableState<T2>,
    private val state3: MutableState<T3>,
) {
    public var first: T1
        get() = state1.value
        set(value) {
            state1.value = value
        }

    public var second: T2
        get() = state2.value
        set(value) {
            state2.value = value
        }

    public var third: T3
        get() = state3.value
        set(value) {
            state3.value = value
        }

    public operator fun component1(): MutableState<T1> = state1
    public operator fun component2(): MutableState<T2> = state2
    public operator fun component3(): MutableState<T3> = state3

    public operator fun getValue(thisRef: Any?, property: KProperty<*>): PreferencesState3<T1, T2, T3> = this
}

/**
 * Holds four [MutableState] values obtained from [rememberPreferences].
 *
 * Supports destructuring: `val (a, b, c, d) = rememberPreferences(pref1, pref2, pref3, pref4)`.
 *
 * Supports property delegation: `val prefs by rememberPreferences(pref1, pref2, pref3, pref4)`,
 * then use `prefs.first` through `prefs.fourth` to read and write values directly.
 */
public class PreferencesState4<T1, T2, T3, T4> internal constructor(
    private val state1: MutableState<T1>,
    private val state2: MutableState<T2>,
    private val state3: MutableState<T3>,
    private val state4: MutableState<T4>,
) {
    public var first: T1
        get() = state1.value
        set(value) {
            state1.value = value
        }

    public var second: T2
        get() = state2.value
        set(value) {
            state2.value = value
        }

    public var third: T3
        get() = state3.value
        set(value) {
            state3.value = value
        }

    public var fourth: T4
        get() = state4.value
        set(value) {
            state4.value = value
        }

    public operator fun component1(): MutableState<T1> = state1
    public operator fun component2(): MutableState<T2> = state2
    public operator fun component3(): MutableState<T3> = state3
    public operator fun component4(): MutableState<T4> = state4

    public operator fun getValue(thisRef: Any?, property: KProperty<*>): PreferencesState4<T1, T2, T3, T4> = this
}

/**
 * Holds five [MutableState] values obtained from [rememberPreferences].
 *
 * Supports destructuring:
 * `val (a, b, c, d, e) = rememberPreferences(pref1, pref2, pref3, pref4, pref5)`.
 *
 * Supports property delegation: `val prefs by rememberPreferences(pref1, pref2, pref3, pref4, pref5)`,
 * then use `prefs.first` through `prefs.fifth` to read and write values directly.
 */
public class PreferencesState5<T1, T2, T3, T4, T5> internal constructor(
    private val state1: MutableState<T1>,
    private val state2: MutableState<T2>,
    private val state3: MutableState<T3>,
    private val state4: MutableState<T4>,
    private val state5: MutableState<T5>,
) {
    public var first: T1
        get() = state1.value
        set(value) {
            state1.value = value
        }

    public var second: T2
        get() = state2.value
        set(value) {
            state2.value = value
        }

    public var third: T3
        get() = state3.value
        set(value) {
            state3.value = value
        }

    public var fourth: T4
        get() = state4.value
        set(value) {
            state4.value = value
        }

    public var fifth: T5
        get() = state5.value
        set(value) {
            state5.value = value
        }

    public operator fun component1(): MutableState<T1> = state1
    public operator fun component2(): MutableState<T2> = state2
    public operator fun component3(): MutableState<T3> = state3
    public operator fun component4(): MutableState<T4> = state4
    public operator fun component5(): MutableState<T5> = state5

    public operator fun getValue(thisRef: Any?, property: KProperty<*>): PreferencesState5<T1, T2, T3, T4, T5> = this
}
