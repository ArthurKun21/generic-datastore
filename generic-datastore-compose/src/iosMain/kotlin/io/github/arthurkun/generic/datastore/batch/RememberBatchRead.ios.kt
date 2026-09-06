package io.github.arthurkun.generic.datastore.batch

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import io.github.arthurkun.generic.datastore.preferences.PreferencesDatastore
import io.github.arthurkun.generic.datastore.preferences.batch.BatchValues
import io.github.arthurkun.generic.datastore.preferences.batch.PrefBuilder
import kotlin.coroutines.CoroutineContext

/**
 * iOS implementation of [PreferencesDatastore.rememberBatchRead] that uses
 * [collectAsState] to observe the batch read flow.
 *
 * @param context The [CoroutineContext] to use for collecting the flow.
 * @param declare A lambda with [PrefBuilder] receiver that declares the batch to observe.
 * @return A [State] containing the latest [BatchValues] snapshot, or `null` until the first
 *   snapshot is available.
 */
@Composable
public actual fun PreferencesDatastore.rememberBatchRead(
    context: CoroutineContext,
    declare: PrefBuilder.() -> Unit,
): State<BatchValues?> {
    val flow = remember(this) { batchReadFlowValues(declare = declare) }
    return flow.collectAsState(initial = null, context = context)
}

/**
 * iOS implementation of [PreferencesDatastore.rememberBatchRead] that uses
 * [collectAsState] to observe the batch read flow.
 *
 * @param R The type of the derived state value.
 * @param context The [CoroutineContext] to use for collecting the flow.
 * @param declare A lambda with [PrefBuilder] receiver that declares the batch to observe.
 * @param block A lambda with receiver on [BatchValues] to derive the desired state from the batch
 *   read snapshot.
 * @return A [State] containing the latest value returned by [block], or `null` until the first
 *   snapshot is available.
 */
@Composable
public actual fun <R> PreferencesDatastore.rememberBatchRead(
    context: CoroutineContext,
    declare: PrefBuilder.() -> Unit,
    block: BatchValues.() -> R,
): State<R?> {
    val flow = remember(this) { batchReadFlow(declare = declare, block = block) }
    return flow.collectAsState(initial = null, context = context)
}
