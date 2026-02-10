package io.github.arthurkun.generic.datastore.batch

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import io.github.arthurkun.generic.datastore.preferences.PreferencesDatastore
import io.github.arthurkun.generic.datastore.preferences.batch.BatchReadScope
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * Collects the [PreferencesDatastore.batchReadFlow] as a Compose [State], emitting a
 * [BatchReadScope] that shares a single DataStore snapshot for reading multiple preferences.
 *
 * On Android, this uses `collectAsStateWithLifecycle` for lifecycle-aware collection.
 * On Desktop and iOS, this uses `collectAsState`.
 *
 * @param context The [CoroutineContext] to use for collecting the flow.
 * @return A [State] containing the latest [BatchReadScope], or `null` until the first
 *   snapshot is available.
 */
@Composable
public expect fun PreferencesDatastore.rememberBatchRead(
    context: CoroutineContext = EmptyCoroutineContext,
): State<BatchReadScope?>
