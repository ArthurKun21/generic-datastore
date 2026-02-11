package io.github.arthurkun.generic.datastore.batch

import androidx.compose.runtime.Composable
import androidx.compose.runtime.SnapshotMutationPolicy
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.structuralEqualityPolicy
import io.github.arthurkun.generic.datastore.preferences.Preference
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * Remembers two preferences using [LocalPreferencesDatastore] as the backing datastore.
 *
 * All reads share a single DataStore observation. Writes are launched asynchronously.
 * An optimistic local override is applied immediately for responsive UI.
 *
 * Requires [ProvidePreferencesDatastore] to be present in the composition tree.
 *
 * Usage:
 * ```
 * ProvidePreferencesDatastore(datastore) {
 *     val (name, age) = rememberPreferences(namePref, agePref)
 * }
 * ```
 *
 * @param context The [CoroutineContext] to use for collecting the batch read flow.
 * @param policy The [SnapshotMutationPolicy] used to determine value equivalence.
 */
@Composable
public fun <T1, T2> rememberPreferences(
    pref1: Preference<T1>,
    pref2: Preference<T2>,
    context: CoroutineContext = EmptyCoroutineContext,
    policy: SnapshotMutationPolicy<Any?> = structuralEqualityPolicy(),
): PreferencesState2<T1, T2> {
    val datastore = LocalPreferencesDatastore.current
    val batchState = datastore.rememberBatchRead(context) { this }
    val scope = rememberCoroutineScope()
    return remember(datastore, pref1, pref2, policy) {
        PreferencesState2(
            state1 = BatchPrefsComposeState(pref1, batchState, datastore, scope, policy),
            state2 = BatchPrefsComposeState(pref2, batchState, datastore, scope, policy),
        )
    }
}

/**
 * Remembers three preferences using [LocalPreferencesDatastore] as the backing datastore.
 *
 * Requires [ProvidePreferencesDatastore] to be present in the composition tree.
 *
 * Usage:
 * ```
 * ProvidePreferencesDatastore(datastore) {
 *     val (name, age, enabled) = rememberPreferences(namePref, agePref, enabledPref)
 * }
 * ```
 *
 * @param context The [CoroutineContext] to use for collecting the batch read flow.
 * @param policy The [SnapshotMutationPolicy] used to determine value equivalence.
 */
@Composable
public fun <T1, T2, T3> rememberPreferences(
    pref1: Preference<T1>,
    pref2: Preference<T2>,
    pref3: Preference<T3>,
    context: CoroutineContext = EmptyCoroutineContext,
    policy: SnapshotMutationPolicy<Any?> = structuralEqualityPolicy(),
): PreferencesState3<T1, T2, T3> {
    val datastore = LocalPreferencesDatastore.current
    val batchState = datastore.rememberBatchRead(context) { this }
    val scope = rememberCoroutineScope()
    return remember(datastore, pref1, pref2, pref3, policy) {
        PreferencesState3(
            state1 = BatchPrefsComposeState(pref1, batchState, datastore, scope, policy),
            state2 = BatchPrefsComposeState(pref2, batchState, datastore, scope, policy),
            state3 = BatchPrefsComposeState(pref3, batchState, datastore, scope, policy),
        )
    }
}

/**
 * Remembers four preferences using [LocalPreferencesDatastore] as the backing datastore.
 *
 * Requires [ProvidePreferencesDatastore] to be present in the composition tree.
 *
 * Usage:
 * ```
 * ProvidePreferencesDatastore(datastore) {
 *     val (a, b, c, d) = rememberPreferences(pref1, pref2, pref3, pref4)
 * }
 * ```
 *
 * @param context The [CoroutineContext] to use for collecting the batch read flow.
 * @param policy The [SnapshotMutationPolicy] used to determine value equivalence.
 */
@Composable
public fun <T1, T2, T3, T4> rememberPreferences(
    pref1: Preference<T1>,
    pref2: Preference<T2>,
    pref3: Preference<T3>,
    pref4: Preference<T4>,
    context: CoroutineContext = EmptyCoroutineContext,
    policy: SnapshotMutationPolicy<Any?> = structuralEqualityPolicy(),
): PreferencesState4<T1, T2, T3, T4> {
    val datastore = LocalPreferencesDatastore.current
    val batchState = datastore.rememberBatchRead(context) { this }
    val scope = rememberCoroutineScope()
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
 * Remembers five preferences using [LocalPreferencesDatastore] as the backing datastore.
 *
 * Requires [ProvidePreferencesDatastore] to be present in the composition tree.
 *
 * Usage:
 * ```
 * ProvidePreferencesDatastore(datastore) {
 *     val (a, b, c, d, e) = rememberPreferences(pref1, pref2, pref3, pref4, pref5)
 * }
 * ```
 *
 * @param context The [CoroutineContext] to use for collecting the batch read flow.
 * @param policy The [SnapshotMutationPolicy] used to determine value equivalence.
 */
@Composable
public fun <T1, T2, T3, T4, T5> rememberPreferences(
    pref1: Preference<T1>,
    pref2: Preference<T2>,
    pref3: Preference<T3>,
    pref4: Preference<T4>,
    pref5: Preference<T5>,
    context: CoroutineContext = EmptyCoroutineContext,
    policy: SnapshotMutationPolicy<Any?> = structuralEqualityPolicy(),
): PreferencesState5<T1, T2, T3, T4, T5> {
    val datastore = LocalPreferencesDatastore.current
    val batchState = datastore.rememberBatchRead(context) { this }
    val scope = rememberCoroutineScope()
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
