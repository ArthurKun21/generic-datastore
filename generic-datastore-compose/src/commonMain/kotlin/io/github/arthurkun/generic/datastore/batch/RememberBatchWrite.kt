package io.github.arthurkun.generic.datastore.batch

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import io.github.arthurkun.generic.datastore.preferences.PreferencesDatastore
import io.github.arthurkun.generic.datastore.preferences.batch.BatchWriteScope
import kotlinx.coroutines.launch

/**
 * Remembers and returns a lambda that performs a batch write on the [PreferencesDatastore].
 *
 * All [BatchWriteScope.set], [BatchWriteScope.delete], and [BatchWriteScope.resetToDefault]
 * calls within [block] share a single DataStore `edit` transaction.
 *
 * The write is launched asynchronously in the current [rememberCoroutineScope].
 *
 * @return A lambda that, when invoked, executes a batch write with the given [BatchWriteScope] block.
 */
@Composable
public fun PreferencesDatastore.rememberBatchWrite(): (block: BatchWriteScope.() -> Unit) -> Unit {
    val datastore = this
    val scope = rememberCoroutineScope()
    return remember(datastore) {
        { block: BatchWriteScope.() -> Unit ->
            scope.launch { datastore.batchWrite(block) }
            Unit
        }
    }
}
