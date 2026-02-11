package io.github.arthurkun.generic.datastore.batch

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.SnapshotMutationPolicy
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.runtime.structuralEqualityPolicy
import io.github.arthurkun.generic.datastore.preferences.Preference
import io.github.arthurkun.generic.datastore.preferences.PreferencesDatastore
import io.github.arthurkun.generic.datastore.preferences.batch.BatchReadScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

private object Unset

/**
 * A [MutableState] that reads from a shared [BatchReadScope] snapshot and writes
 * via [PreferencesDatastore.batchWrite].
 *
 * Reads are derived from the [batchState] snapshot so that all preferences collected
 * in the same `rememberPreferences` call share a single DataStore observation.
 * Writes are launched asynchronously using [PreferencesDatastore.batchWrite].
 * An optimistic local override is applied immediately so that synchronous UI
 * inputs (e.g., `TextField`) reflect the new value without waiting for the
 * DataStore round-trip.
 *
 * @param T The type of the preference value.
 * @param preference The preference to read/write.
 * @param batchState A [State] containing the latest [BatchReadScope], or `null` before the
 *   first snapshot is available.
 * @param datastore The [PreferencesDatastore] used for batch writes.
 * @param scope The [CoroutineScope] used to launch write operations.
 * @param policy The [SnapshotMutationPolicy] used to determine value equivalence.
 */
internal class BatchPrefsComposeState<T>(
    private val preference: Preference<T>,
    private val batchState: State<BatchReadScope?>,
    private val datastore: PreferencesDatastore,
    private val scope: CoroutineScope,
    private val policy: SnapshotMutationPolicy<Any?> = structuralEqualityPolicy(),
) : MutableState<T> {

    private var localOverride: Any? by mutableStateOf(Unset)

    private val upstreamState = derivedStateOf {
        batchState.value?.get(preference) ?: preference.defaultValue
    }

    init {
        scope.launch {
            snapshotFlow { upstreamState.value }
                .collect { upstream ->
                    val current = localOverride
                    if (current !== Unset) {
                        @Suppress("UNCHECKED_CAST")
                        if (policy.equivalent(current as T, upstream)) {
                            localOverride = Unset
                        }
                    }
                }
        }
    }

    override var value: T
        get() {
            val upstream = upstreamState.value
            val current = localOverride
            if (current !== Unset) {
                @Suppress("UNCHECKED_CAST")
                return current as T
            }
            return upstream
        }
        set(value) {
            val oldValue = Snapshot.withoutReadObservation { this.value }
            if (!policy.equivalent(oldValue, value)) {
                localOverride = value
                scope.launch {
                    datastore.batchWrite { this[preference] = value }
                }
            }
        }

    override fun component1(): T = value

    override fun component2(): (T) -> Unit = { value = it }
}
