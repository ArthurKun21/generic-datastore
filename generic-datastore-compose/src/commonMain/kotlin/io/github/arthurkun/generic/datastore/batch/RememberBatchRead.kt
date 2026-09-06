package io.github.arthurkun.generic.datastore.batch

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import io.github.arthurkun.generic.datastore.preferences.PreferencesDatastore
import io.github.arthurkun.generic.datastore.preferences.batch.BatchValues
import io.github.arthurkun.generic.datastore.preferences.batch.PrefBuilder
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * Collects the [PreferencesDatastore.batchReadFlowValues] for the preferences declared in
 * [declare] as a Compose [State].
 *
 * Every emission maps the whole declaration to its stored values (or defaults) from one DataStore
 * snapshot, so all preferences in the batch share a single observation. Declarations may reuse
 * existing preferences via `add(pref)` or declare from scratch via `string(…)`, `int(…)`, …:
 *
 * ```kotlin
 * val values by datastore.rememberBatchRead({ add(namePref); add(agePref) })
 * ```
 *
 * The flow is remembered on this datastore, so [declare] must be stable (same keys and defaults
 * on every recomposition); changing the declaration requires a different datastore instance or a
 * `key(…)` scope around the call.
 *
 * On Android, this uses `collectAsStateWithLifecycle` for lifecycle-aware collection.
 * On Desktop and iOS, this uses `collectAsState`.
 *
 * @param context The [CoroutineContext] to use for collecting the flow.
 * @param declare A lambda with [PrefBuilder] receiver that declares the batch.
 * @return A [State] containing the latest [BatchValues] snapshot, or `null` until the first
 *   snapshot is available.
 */
@Composable
public expect fun PreferencesDatastore.rememberBatchRead(
    context: CoroutineContext = EmptyCoroutineContext,
    declare: PrefBuilder.() -> Unit,
): State<BatchValues?>

/**
 * Collects the [PreferencesDatastore.batchReadFlow] for the preferences declared in [declare] as
 * a Compose [State], applying [block] to each [BatchValues] snapshot to derive a value of type
 * [R].
 *
 * All preferences in the declaration share a single DataStore snapshot, eliminating redundant
 * transactions.
 *
 * ```kotlin
 * val name by datastore.rememberBatchRead(declare = { add(namePref) }) {
 *     this[nameHandle]
 * }
 * ```
 *
 * The flow is remembered on this datastore; see [rememberBatchRead] for the stability requirement.
 *
 * On Android, this uses `collectAsStateWithLifecycle` for lifecycle-aware collection.
 * On Desktop and iOS, this uses `collectAsState`.
 *
 * @param R The type of the derived state value.
 * @param context The [CoroutineContext] to use for collecting the flow.
 * @param declare A lambda with [PrefBuilder] receiver that declares the batch.
 * @param block A lambda with receiver on [BatchValues] that derives the state value.
 * @return A [State] containing the latest value returned by [block], or `null` until the first
 *   snapshot is available.
 */
@Composable
public expect fun <R> PreferencesDatastore.rememberBatchRead(
    context: CoroutineContext = EmptyCoroutineContext,
    declare: PrefBuilder.() -> Unit,
    block: BatchValues.() -> R,
): State<R?>
