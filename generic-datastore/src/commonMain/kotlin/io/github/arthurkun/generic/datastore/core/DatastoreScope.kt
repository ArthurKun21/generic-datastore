package io.github.arthurkun.generic.datastore.core

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlin.coroutines.EmptyCoroutineContext

/**
 * Creates a [CoroutineScope] specifically for DataStore operations.
 *
 * If a [parentScope] is provided, the new scope inherits its context and links a
 * new [SupervisorJob] to the parent's [Job] to maintain structured concurrency.
 * If no parent is provided, a standalone scope is created.
 *
 * If the parent does not provide a dispatcher, the scope uses [Dispatchers.IO]
 * to ensure disk I/O operations are performed on the appropriate thread pool.
 * It also uses a [SupervisorJob] to prevent failures in individual tasks from
 * canceling the entire scope.
 *
 * @param parentScope An optional [CoroutineScope] to link the new scope to.
 * @return A [CoroutineScope] configured with fallback [Dispatchers.IO] and a [SupervisorJob].
 */
internal fun createDatastoreScope(parentScope: CoroutineScope?): CoroutineScope {
    val parentContext = parentScope?.coroutineContext ?: EmptyCoroutineContext
    val parentJob = parentContext[Job]
    return CoroutineScope(
        Dispatchers.IO + parentContext +
            SupervisorJob(parentJob),
    )
}
