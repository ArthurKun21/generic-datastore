package io.github.arthurkun.generic.datastore.batch

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.SnapshotMutationPolicy
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.runtime.structuralEqualityPolicy
import io.github.arthurkun.generic.datastore.preferences.PreferencesDatastore
import io.github.arthurkun.generic.datastore.preferences.batch.BatchPref
import io.github.arthurkun.generic.datastore.preferences.batch.BatchValues
import io.github.arthurkun.generic.datastore.preferences.batch.PreferenceBatch
import io.github.arthurkun.generic.datastore.preferences.batch.prefBatch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

private object Unset

/**
 * A [MutableState] that reads from a shared [BatchValues] snapshot and writes via
 * [PreferencesDatastore.batchWrite].
 *
 * Reads are derived from the [batchState] snapshot so that all preferences collected in the same
 * `rememberPreferences` call share a single DataStore observation. Writes are launched
 * asynchronously using [PreferencesDatastore.batchWrite] against a single-preference batch. An
 * optimistic local override is applied immediately so that synchronous UI inputs (e.g.,
 * `TextField`) reflect the new value without waiting for the DataStore round-trip.
 *
 * @param T The type of the preference value.
 * @param handle The batch preference handle to read/write.
 * @param batchState A [State] containing the latest [BatchValues], or `null` before the first
 *   snapshot is available.
 * @param datastore The [PreferencesDatastore] used for batch writes.
 * @param scope The [CoroutineScope] used to launch write operations.
 * @param policy The [SnapshotMutationPolicy] used to determine value equivalence.
 */
internal class BatchPrefsComposeState<T>(
    private val handle: BatchPref<T>,
    private val batchState: State<BatchValues?>,
    private val datastore: PreferencesDatastore,
    private val scope: CoroutineScope,
    private val policy: SnapshotMutationPolicy<Any?> = structuralEqualityPolicy(),
) : MutableState<T> {

    private val writeBatch: PreferenceBatch = prefBatch { add(handle) }

    private var localOverride: Any? by mutableStateOf(Unset)

    private val upstreamState = derivedStateOf {
        batchState.value?.get(handle) ?: handle.defaultValue
    }

    override var value: T
        get() {
            val upstream = upstreamState.value
            val current = localOverride
            if (current !== Unset) {
                @Suppress("UNCHECKED_CAST")
                val override = current as T
                return if (!policy.equivalent(override, upstream)) {
                    override
                } else {
                    localOverride = Unset
                    upstream
                }
            }
            return upstream
        }
        set(value) {
            val oldValue = Snapshot.withoutReadObservation { this.value }
            if (!policy.equivalent(oldValue, value)) {
                localOverride = value
                scope.launch {
                    try {
                        datastore.batchWrite(writeBatch) {
                            this@batchWrite[handle] = value
                        }
                    } catch (e: CancellationException) {
                        throw e
                    } catch (_: Exception) {
                        val currentOverride = localOverride
                        if (currentOverride !== Unset && policy.equivalent(currentOverride, value)) {
                            localOverride = Unset
                        }
                    }
                }
            }
        }

    override fun component1(): T = value

    override fun component2(): (T) -> Unit = { value = it }
}
