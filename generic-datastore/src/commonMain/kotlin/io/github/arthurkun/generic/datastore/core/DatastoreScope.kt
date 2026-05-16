package io.github.arthurkun.generic.datastore.core

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob


/**
 * Creates a [CoroutineScope] specifically for DataStore operations.
 *
 * If a [parentScope] is provided, the new scope inherits its context and links a
 * new [SupervisorJob] to the parent's [Job] to maintain structured concurrency.
 * If no parent is provided, a standalone scope is created.
 *
 * In both cases, the scope uses [Dispatchers.IO] to ensure disk I/O operations
 * are performed on the appropriate thread pool and a [SupervisorJob] to prevent
 * failures in individual tasks from canceling the entire scope.
 *
 * @param parentScope An optional [CoroutineScope] to link the new scope to.
 * @return A [CoroutineScope] configured with [Dispatchers.IO] and a [SupervisorJob].
 */
internal fun createDatastoreScope(parentScope: CoroutineScope?): CoroutineScope {
    if (parentScope == null) {
        return CoroutineScope(Dispatchers.IO + SupervisorJob())
    }

    val parentJob = parentScope.coroutineContext[Job]
    return CoroutineScope(
        parentScope.coroutineContext + Dispatchers.IO +
            SupervisorJob(parentJob),
    )
}
