package io.github.arthurkun.generic.datastore.batch

import androidx.compose.runtime.Composable
import androidx.compose.runtime.SnapshotMutationPolicy
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.structuralEqualityPolicy
import io.github.arthurkun.generic.datastore.preferences.Preference
import io.github.arthurkun.generic.datastore.preferences.PreferencesDatastore
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * Remembers two preferences, reading from a shared [batchReadFlow][PreferencesDatastore.batchReadFlow]
 * snapshot and writing via [batchWrite][PreferencesDatastore.batchWrite].
 *
 * All reads share a single DataStore observation. Writes are launched asynchronously.
 * An optimistic local override is applied immediately for responsive UI.
 *
 * Usage:
 * ```
 * val (name, age) = datastore.rememberPreferences(namePref, agePref)
 * ```
 *
 * @param context The [CoroutineContext] to use for collecting the batch read flow.
 * @param policy The [SnapshotMutationPolicy] used to determine value equivalence.
 */
@Composable
public fun <T1, T2> PreferencesDatastore.rememberPreferences(
    pref1: Preference<T1>,
    pref2: Preference<T2>,
    context: CoroutineContext = EmptyCoroutineContext,
    policy: SnapshotMutationPolicy<Any?> = structuralEqualityPolicy(),
): PreferencesState2<T1, T2> {
    val batchState = rememberBatchRead(context)
    val scope = rememberCoroutineScope()
    val datastore = this
    return remember(datastore, pref1, pref2, policy) {
        PreferencesState2(
            state1 = BatchPrefsComposeState(pref1, batchState, datastore, scope, policy),
            state2 = BatchPrefsComposeState(pref2, batchState, datastore, scope, policy),
        )
    }
}

/**
 * Remembers three preferences, reading from a shared [batchReadFlow][PreferencesDatastore.batchReadFlow]
 * snapshot and writing via [batchWrite][PreferencesDatastore.batchWrite].
 *
 * Usage:
 * ```
 * val (name, age, enabled) = datastore.rememberPreferences(namePref, agePref, enabledPref)
 * ```
 *
 * @param context The [CoroutineContext] to use for collecting the batch read flow.
 * @param policy The [SnapshotMutationPolicy] used to determine value equivalence.
 */
@Composable
public fun <T1, T2, T3> PreferencesDatastore.rememberPreferences(
    pref1: Preference<T1>,
    pref2: Preference<T2>,
    pref3: Preference<T3>,
    context: CoroutineContext = EmptyCoroutineContext,
    policy: SnapshotMutationPolicy<Any?> = structuralEqualityPolicy(),
): PreferencesState3<T1, T2, T3> {
    val batchState = rememberBatchRead(context)
    val scope = rememberCoroutineScope()
    val datastore = this
    return remember(datastore, pref1, pref2, pref3, policy) {
        PreferencesState3(
            state1 = BatchPrefsComposeState(pref1, batchState, datastore, scope, policy),
            state2 = BatchPrefsComposeState(pref2, batchState, datastore, scope, policy),
            state3 = BatchPrefsComposeState(pref3, batchState, datastore, scope, policy),
        )
    }
}

/**
 * Remembers four preferences, reading from a shared [batchReadFlow][PreferencesDatastore.batchReadFlow]
 * snapshot and writing via [batchWrite][PreferencesDatastore.batchWrite].
 *
 * Usage:
 * ```
 * val (a, b, c, d) = datastore.rememberPreferences(pref1, pref2, pref3, pref4)
 * ```
 *
 * @param context The [CoroutineContext] to use for collecting the batch read flow.
 * @param policy The [SnapshotMutationPolicy] used to determine value equivalence.
 */
@Composable
public fun <T1, T2, T3, T4> PreferencesDatastore.rememberPreferences(
    pref1: Preference<T1>,
    pref2: Preference<T2>,
    pref3: Preference<T3>,
    pref4: Preference<T4>,
    context: CoroutineContext = EmptyCoroutineContext,
    policy: SnapshotMutationPolicy<Any?> = structuralEqualityPolicy(),
): PreferencesState4<T1, T2, T3, T4> {
    val batchState = rememberBatchRead(context)
    val scope = rememberCoroutineScope()
    val datastore = this
    return remember(datastore, pref1, pref2, pref3, pref4, policy) {
        PreferencesState4(
            state1 = BatchPrefsComposeState(pref1, batchState, datastore, scope, policy),
            state2 = BatchPrefsComposeState(pref2, batchState, datastore, scope, policy),
            state3 = BatchPrefsComposeState(pref3, batchState, datastore, scope, policy),
            state4 = BatchPrefsComposeState(pref4, batchState, datastore, scope, policy),
        )
    }
}

/**
 * Remembers five preferences, reading from a shared [batchReadFlow][PreferencesDatastore.batchReadFlow]
 * snapshot and writing via [batchWrite][PreferencesDatastore.batchWrite].
 *
 * Usage:
 * ```
 * val (a, b, c, d, e) = datastore.rememberPreferences(pref1, pref2, pref3, pref4, pref5)
 * ```
 *
 * @param context The [CoroutineContext] to use for collecting the batch read flow.
 * @param policy The [SnapshotMutationPolicy] used to determine value equivalence.
 */
@Composable
public fun <T1, T2, T3, T4, T5> PreferencesDatastore.rememberPreferences(
    pref1: Preference<T1>,
    pref2: Preference<T2>,
    pref3: Preference<T3>,
    pref4: Preference<T4>,
    pref5: Preference<T5>,
    context: CoroutineContext = EmptyCoroutineContext,
    policy: SnapshotMutationPolicy<Any?> = structuralEqualityPolicy(),
): PreferencesState5<T1, T2, T3, T4, T5> {
    val batchState = rememberBatchRead(context)
    val scope = rememberCoroutineScope()
    val datastore = this
    return remember(datastore, pref1, pref2, pref3, pref4, pref5, policy) {
        PreferencesState5(
            state1 = BatchPrefsComposeState(pref1, batchState, datastore, scope, policy),
            state2 = BatchPrefsComposeState(pref2, batchState, datastore, scope, policy),
            state3 = BatchPrefsComposeState(pref3, batchState, datastore, scope, policy),
            state4 = BatchPrefsComposeState(pref4, batchState, datastore, scope, policy),
            state5 = BatchPrefsComposeState(pref5, batchState, datastore, scope, policy),
        )
    }
}
