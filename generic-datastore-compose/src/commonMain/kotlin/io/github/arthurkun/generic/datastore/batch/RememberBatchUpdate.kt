package io.github.arthurkun.generic.datastore.batch

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import io.github.arthurkun.generic.datastore.preferences.PreferencesDatastore
import io.github.arthurkun.generic.datastore.preferences.batch.BatchUpdateScope
import kotlinx.coroutines.launch

/**
 * Remembers and returns a lambda that performs a batch update on the [PreferencesDatastore].
 *
 * All reads and writes within the [BatchUpdateScope] share a single DataStore `edit`
 * transaction, guaranteeing consistency when values depend on each other.
 *
 * The update is launched asynchronously in the current [rememberCoroutineScope].
 *
 * @return A lambda that, when invoked, executes a batch update with the given [BatchUpdateScope] block.
 */
@Composable
public fun PreferencesDatastore.rememberBatchUpdate(): (block: BatchUpdateScope.() -> Unit) -> Unit {
    val datastore = this
    val scope = rememberCoroutineScope()
    return remember(datastore) {
        { block: BatchUpdateScope.() -> Unit ->
            scope.launch { datastore.batchUpdate(block) }
            Unit
        }
    }
}
