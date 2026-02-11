package io.github.arthurkun.generic.datastore.batch

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import io.github.arthurkun.generic.datastore.preferences.PreferencesDatastore
import io.github.arthurkun.generic.datastore.preferences.batch.BatchReadScope
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * Collects the [PreferencesDatastore.batchReadFlow] as a Compose [State], applying [block]
 * to each [BatchReadScope] snapshot to derive a value of type [R].
 *
 * All preferences read inside [block] share a single DataStore snapshot, eliminating
 * redundant transactions.
 *
 * On Android, this uses `collectAsStateWithLifecycle` for lifecycle-aware collection.
 * On Desktop and iOS, this uses `collectAsState`.
 *
 * @param R The type of the derived state value.
 * @param context The [CoroutineContext] to use for collecting the flow.
 * @param block A lambda with receiver on [BatchReadScope] that reads one or more preferences
 *   and returns the derived state value.
 * @return A [State] containing the latest value returned by [block], or `null` until the first
 *   snapshot is available.
 */
@Composable
public expect fun<R> PreferencesDatastore.rememberBatchRead(
    context: CoroutineContext = EmptyCoroutineContext,
    block: BatchReadScope.() -> R
): State<R?>
