package io.github.arthurkun.generic.datastore.batch

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import io.github.arthurkun.generic.datastore.preferences.PreferencesDatastore
import io.github.arthurkun.generic.datastore.preferences.batch.BatchValues
import io.github.arthurkun.generic.datastore.preferences.batch.PreferenceBatch
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * Collects the [PreferencesDatastore.batchReadFlow] for [batch] as a Compose [State].
 *
 * Every emission maps the whole [batch] to its stored values (or defaults) from one DataStore
 * snapshot, so all preferences in the batch share a single observation.
 *
 * On Android, this uses `collectAsStateWithLifecycle` for lifecycle-aware collection.
 * On Desktop and iOS, this uses `collectAsState`.
 *
 * @param batch The batch declaration produced by
 *   [io.github.arthurkun.generic.datastore.preferences.batch.prefBatch].
 * @param context The [CoroutineContext] to use for collecting the flow.
 * @return A [State] containing the latest [BatchValues] snapshot, or `null` until the first
 *   snapshot is available.
 */
@Composable
public expect fun PreferencesDatastore.rememberBatchRead(
    batch: PreferenceBatch,
    context: CoroutineContext = EmptyCoroutineContext,
): State<BatchValues?>

/**
 * Collects the [PreferencesDatastore.batchReadFlow] for [batch] as a Compose [State], applying
 * [block] to each [BatchValues] snapshot to derive a value of type [R].
 *
 * All preferences in [batch] share a single DataStore snapshot, eliminating redundant
 * transactions.
 *
 * On Android, this uses `collectAsStateWithLifecycle` for lifecycle-aware collection.
 * On Desktop and iOS, this uses `collectAsState`.
 *
 * @param R The type of the derived state value.
 * @param batch The batch declaration produced by
 *   [io.github.arthurkun.generic.datastore.preferences.batch.prefBatch].
 * @param context The [CoroutineContext] to use for collecting the flow.
 * @param block A lambda with receiver on [BatchValues] that derives the state value.
 * @return A [State] containing the latest value returned by [block], or `null` until the first
 *   snapshot is available.
 */
@Composable
public expect fun <R> PreferencesDatastore.rememberBatchRead(
    batch: PreferenceBatch,
    context: CoroutineContext = EmptyCoroutineContext,
    block: BatchValues.() -> R,
): State<R?>
