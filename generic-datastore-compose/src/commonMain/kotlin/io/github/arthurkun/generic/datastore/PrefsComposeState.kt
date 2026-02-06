package io.github.arthurkun.generic.datastore

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.SnapshotMutationPolicy
import androidx.compose.runtime.State
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.runtime.structuralEqualityPolicy
import io.github.arthurkun.generic.datastore.core.Prefs
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * A [MutableState] backed by a [Prefs] instance.
 *
 * Reads are delegated to a collected [State] snapshot of the preference flow,
 * and writes are launched asynchronously via the provided [CoroutineScope].
 *
 * The [policy] controls when a new value is considered different from the current one.
 * Only values that are not equivalent according to the policy will trigger a write
 * to the underlying [Prefs], avoiding redundant persistence operations.
 *
 * @param T The type of the preference value.
 * @param prefs The underlying [Prefs] instance to read from and write to.
 * @param state The collected [State] snapshot of the preference flow.
 * @param scope The [CoroutineScope] used to launch write operations.
 * @param policy The [SnapshotMutationPolicy] used to determine value equivalence.
 *   Defaults to [structuralEqualityPolicy].
 */
class PrefsComposeState<T>(
    private val prefs: Prefs<T>,
    private val state: State<T>,
    private val scope: CoroutineScope,
    private val policy: SnapshotMutationPolicy<T> = structuralEqualityPolicy(),
) : MutableState<T> {

    override var value: T
        get() = state.value
        set(value) {
            val oldValue = Snapshot.withoutReadObservation { state.value }
            if (!policy.equivalent(oldValue, value)) {
                scope.launch {
                    prefs.set(value)
                }
            }
        }

    override fun component1(): T = value

    override fun component2(): (T) -> Unit = { value = it }
}
