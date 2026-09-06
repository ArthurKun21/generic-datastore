package io.github.arthurkun.generic.datastore.batch

import androidx.compose.runtime.Composable
import androidx.compose.runtime.SnapshotMutationPolicy
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.structuralEqualityPolicy
import io.github.arthurkun.generic.datastore.preferences.Preference
import io.github.arthurkun.generic.datastore.preferences.PreferencesDatastore
import io.github.arthurkun.generic.datastore.preferences.batch.BatchPref
import io.github.arthurkun.generic.datastore.preferences.batch.PreferenceBatch
import io.github.arthurkun.generic.datastore.preferences.batch.prefBatch
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * Remembers a [PreferenceBatch] that contains every given preference, in the given order.
 *
 * The batch is rebuilt only when one of the preferences changes identity.
 *
 * @throws IllegalStateException If any preference was not created by this library. The failure
 *   surfaces during composition (when the batch is remembered), not on first read.
 */
@Composable
internal fun rememberBatch(vararg preferences: Preference<*>): PreferenceBatch =
    remember(*preferences) {
        prefBatch {
            preferences.forEach { preference -> add(preference) }
        }
    }

/**
 * Remembers two preferences as [MutableState][androidx.compose.runtime.MutableState] values,
 * reading from a shared [batchReadFlow][PreferencesDatastore.batchReadFlow] snapshot and
 * writing via [batchWrite][PreferencesDatastore.batchWrite].
 *
 * All reads share a single DataStore snapshot, eliminating redundant transactions.
 * Writes are launched asynchronously with an optimistic local override applied
 * immediately for responsive UI.
 *
 * Usage:
 * ```
 * val (name, age) = datastore.rememberPreferences(namePref, agePref)
 * ```
 *
 * @param pref1 The first [Preference] to observe and write.
 * @param pref2 The second [Preference] to observe and write.
 * @param context The [CoroutineContext] to use for collecting the batch read flow.
 * @param policy The [SnapshotMutationPolicy] used to determine value equivalence.
 * @throws IllegalStateException If any preference was not created by this library. Only
 *   library-created [Preference] instances can join a batch.
 * @return A [PreferencesState2] containing two [MutableState][androidx.compose.runtime.MutableState] values.
 */
@Composable
public fun <T1, T2> PreferencesDatastore.rememberPreferences(
    pref1: Preference<T1>,
    pref2: Preference<T2>,
    context: CoroutineContext = EmptyCoroutineContext,
    policy: SnapshotMutationPolicy<Any?> = structuralEqualityPolicy(),
): PreferencesState2<T1, T2> {
    val batch = rememberBatch(pref1, pref2)

    @Suppress("UNCHECKED_CAST")
    val handle1 = batch[0] as BatchPref<T1>

    @Suppress("UNCHECKED_CAST")
    val handle2 = batch[1] as BatchPref<T2>
    val batchState = rememberBatchRead(batch, context)
    val scope = rememberCoroutineScope()
    val datastore = this
    return remember(datastore, pref1, pref2, policy) {
        PreferencesState2(
            state1 = BatchPrefsComposeState(handle1, batchState, datastore, scope, policy),
            state2 = BatchPrefsComposeState(handle2, batchState, datastore, scope, policy),
        )
    }
}

/**
 * Remembers three preferences as [MutableState][androidx.compose.runtime.MutableState] values,
 * reading from a shared [batchReadFlow][PreferencesDatastore.batchReadFlow] snapshot and
 * writing via [batchWrite][PreferencesDatastore.batchWrite].
 *
 * All reads share a single DataStore snapshot, eliminating redundant transactions.
 * Writes are launched asynchronously with an optimistic local override applied
 * immediately for responsive UI.
 *
 * Usage:
 * ```
 * val (name, age, enabled) = datastore.rememberPreferences(namePref, agePref, enabledPref)
 * ```
 *
 * @param pref1 The first [Preference] to observe and write.
 * @param pref2 The second [Preference] to observe and write.
 * @param pref3 The third [Preference] to observe and write.
 * @param context The [CoroutineContext] to use for collecting the batch read flow.
 * @param policy The [SnapshotMutationPolicy] used to determine value equivalence.
 * @throws IllegalStateException If any preference was not created by this library. Only
 *   library-created [Preference] instances can join a batch.
 * @return A [PreferencesState3] containing three [MutableState][androidx.compose.runtime.MutableState] values.
 */
@Composable
public fun <T1, T2, T3> PreferencesDatastore.rememberPreferences(
    pref1: Preference<T1>,
    pref2: Preference<T2>,
    pref3: Preference<T3>,
    context: CoroutineContext = EmptyCoroutineContext,
    policy: SnapshotMutationPolicy<Any?> = structuralEqualityPolicy(),
): PreferencesState3<T1, T2, T3> {
    val batch = rememberBatch(pref1, pref2, pref3)

    @Suppress("UNCHECKED_CAST")
    val handle1 = batch[0] as BatchPref<T1>

    @Suppress("UNCHECKED_CAST")
    val handle2 = batch[1] as BatchPref<T2>

    @Suppress("UNCHECKED_CAST")
    val handle3 = batch[2] as BatchPref<T3>
    val batchState = rememberBatchRead(batch, context)
    val scope = rememberCoroutineScope()
    val datastore = this
    return remember(datastore, pref1, pref2, pref3, policy) {
        PreferencesState3(
            state1 = BatchPrefsComposeState(handle1, batchState, datastore, scope, policy),
            state2 = BatchPrefsComposeState(handle2, batchState, datastore, scope, policy),
            state3 = BatchPrefsComposeState(handle3, batchState, datastore, scope, policy),
        )
    }
}

/**
 * Remembers four preferences as [MutableState][androidx.compose.runtime.MutableState] values,
 * reading from a shared [batchReadFlow][PreferencesDatastore.batchReadFlow] snapshot and
 * writing via [batchWrite][PreferencesDatastore.batchWrite].
 *
 * All reads share a single DataStore snapshot, eliminating redundant transactions.
 * Writes are launched asynchronously with an optimistic local override applied
 * immediately for responsive UI.
 *
 * Usage:
 * ```
 * val (a, b, c, d) = datastore.rememberPreferences(pref1, pref2, pref3, pref4)
 * ```
 *
 * @param pref1 The first [Preference] to observe and write.
 * @param pref2 The second [Preference] to observe and write.
 * @param pref3 The third [Preference] to observe and write.
 * @param pref4 The fourth [Preference] to observe and write.
 * @param context The [CoroutineContext] to use for collecting the batch read flow.
 * @param policy The [SnapshotMutationPolicy] used to determine value equivalence.
 * @throws IllegalStateException If any preference was not created by this library. Only
 *   library-created [Preference] instances can join a batch.
 * @return A [PreferencesState4] containing four [MutableState][androidx.compose.runtime.MutableState] values.
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
    val batch = rememberBatch(pref1, pref2, pref3, pref4)

    @Suppress("UNCHECKED_CAST")
    val handle1 = batch[0] as BatchPref<T1>

    @Suppress("UNCHECKED_CAST")
    val handle2 = batch[1] as BatchPref<T2>

    @Suppress("UNCHECKED_CAST")
    val handle3 = batch[2] as BatchPref<T3>

    @Suppress("UNCHECKED_CAST")
    val handle4 = batch[3] as BatchPref<T4>
    val batchState = rememberBatchRead(batch, context)
    val scope = rememberCoroutineScope()
    val datastore = this
    return remember(datastore, pref1, pref2, pref3, pref4, policy) {
        PreferencesState4(
            state1 = BatchPrefsComposeState(handle1, batchState, datastore, scope, policy),
            state2 = BatchPrefsComposeState(handle2, batchState, datastore, scope, policy),
            state3 = BatchPrefsComposeState(handle3, batchState, datastore, scope, policy),
            state4 = BatchPrefsComposeState(handle4, batchState, datastore, scope, policy),
        )
    }
}

/**
 * Remembers five preferences as [MutableState][androidx.compose.runtime.MutableState] values,
 * reading from a shared [batchReadFlow][PreferencesDatastore.batchReadFlow] snapshot and
 * writing via [batchWrite][PreferencesDatastore.batchWrite].
 *
 * All reads share a single DataStore snapshot, eliminating redundant transactions.
 * Writes are launched asynchronously with an optimistic local override applied
 * immediately for responsive UI.
 *
 * Usage:
 * ```
 * val (a, b, c, d, e) = datastore.rememberPreferences(pref1, pref2, pref3, pref4, pref5)
 * ```
 *
 * @param pref1 The first [Preference] to observe and write.
 * @param pref2 The second [Preference] to observe and write.
 * @param pref3 The third [Preference] to observe and write.
 * @param pref4 The fourth [Preference] to observe and write.
 * @param pref5 The fifth [Preference] to observe and write.
 * @param context The [CoroutineContext] to use for collecting the batch read flow.
 * @param policy The [SnapshotMutationPolicy] used to determine value equivalence.
 * @throws IllegalStateException If any preference was not created by this library. Only
 *   library-created [Preference] instances can join a batch.
 * @return A [PreferencesState5] containing five [MutableState][androidx.compose.runtime.MutableState] values.
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
    val batch = rememberBatch(pref1, pref2, pref3, pref4, pref5)

    @Suppress("UNCHECKED_CAST")
    val handle1 = batch[0] as BatchPref<T1>

    @Suppress("UNCHECKED_CAST")
    val handle2 = batch[1] as BatchPref<T2>

    @Suppress("UNCHECKED_CAST")
    val handle3 = batch[2] as BatchPref<T3>

    @Suppress("UNCHECKED_CAST")
    val handle4 = batch[3] as BatchPref<T4>

    @Suppress("UNCHECKED_CAST")
    val handle5 = batch[4] as BatchPref<T5>
    val batchState = rememberBatchRead(batch, context)
    val scope = rememberCoroutineScope()
    val datastore = this
    return remember(datastore, pref1, pref2, pref3, pref4, pref5, policy) {
        PreferencesState5(
            state1 = BatchPrefsComposeState(handle1, batchState, datastore, scope, policy),
            state2 = BatchPrefsComposeState(handle2, batchState, datastore, scope, policy),
            state3 = BatchPrefsComposeState(handle3, batchState, datastore, scope, policy),
            state4 = BatchPrefsComposeState(handle4, batchState, datastore, scope, policy),
            state5 = BatchPrefsComposeState(handle5, batchState, datastore, scope, policy),
        )
    }
}
