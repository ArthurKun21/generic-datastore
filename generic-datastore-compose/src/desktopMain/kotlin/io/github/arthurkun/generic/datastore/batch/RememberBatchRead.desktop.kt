package io.github.arthurkun.generic.datastore.batch

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import io.github.arthurkun.generic.datastore.preferences.PreferencesDatastore
import io.github.arthurkun.generic.datastore.preferences.batch.BatchReadScope
import kotlin.coroutines.CoroutineContext

/**
 * Desktop implementation of [PreferencesDatastore.rememberBatchRead] that uses
 * [collectAsState] to observe the batch read flow.
 *
 * @param context The [CoroutineContext] to use for collecting the flow.
 * @return A [State] containing the latest [BatchReadScope], or `null` until the first
 *   snapshot is available.
 */
@Composable
public actual fun <R> PreferencesDatastore.rememberBatchRead(
    context: CoroutineContext,
    block: BatchReadScope.() -> R,
): State<R?> = batchReadFlow(block = block).collectAsState(initial = null, context = context)
