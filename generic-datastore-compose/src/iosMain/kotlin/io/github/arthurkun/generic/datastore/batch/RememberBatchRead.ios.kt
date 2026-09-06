package io.github.arthurkun.generic.datastore.batch

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import io.github.arthurkun.generic.datastore.preferences.PreferencesDatastore
import io.github.arthurkun.generic.datastore.preferences.batch.BatchValues
import io.github.arthurkun.generic.datastore.preferences.batch.PreferenceBatch
import kotlin.coroutines.CoroutineContext

/**
 * iOS implementation of [PreferencesDatastore.rememberBatchRead] that uses
 * [collectAsState] to observe the batch read flow.
 *
 * @param batch The batch declaration to observe.
 * @param context The [CoroutineContext] to use for collecting the flow.
 * @return A [State] containing the latest [BatchValues] snapshot, or `null` until the first
 *   snapshot is available.
 */
@Composable
public actual fun PreferencesDatastore.rememberBatchRead(
    batch: PreferenceBatch,
    context: CoroutineContext,
): State<BatchValues?> = batchReadFlow(batch).collectAsState(initial = null, context = context)

/**
 * iOS implementation of [PreferencesDatastore.rememberBatchRead] that uses
 * [collectAsState] to observe the batch read flow.
 *
 * @param R The type of the derived state value.
 * @param batch The batch declaration to observe.
 * @param context The [CoroutineContext] to use for collecting the flow.
 * @param block A lambda with receiver on [BatchValues] to derive the desired state from the batch
 *   read snapshot.
 * @return A [State] containing the latest value returned by [block], or `null` until the first
 *   snapshot is available.
 */
@Composable
public actual fun <R> PreferencesDatastore.rememberBatchRead(
    batch: PreferenceBatch,
    context: CoroutineContext,
    block: BatchValues.() -> R,
): State<R?> = batchReadFlow(batch, block = block).collectAsState(initial = null, context = context)
