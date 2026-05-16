package io.github.arthurkun.generic.datastore.core

import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlin.coroutines.ContinuationInterceptor
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertSame
import kotlin.test.assertTrue

class DatastoreScopeTest {

    @Test
    fun parentDispatcherIsPreserved() {
        val dispatcher = StandardTestDispatcher()
        val parentScope = TestScope(dispatcher)
        val datastoreScope = createDatastoreScope(parentScope)

        try {
            assertSame(dispatcher, datastoreScope.coroutineContext[ContinuationInterceptor])
        } finally {
            datastoreScope.cancel()
            parentScope.cancel()
        }
    }

    @Test
    fun parentCancellationCancelsDatastoreScope() {
        val parentScope = TestScope()
        val datastoreScope = createDatastoreScope(parentScope)
        val datastoreJob = checkNotNull(datastoreScope.coroutineContext[Job])

        parentScope.cancel()

        assertFalse(datastoreJob.isActive)
    }

    @Test
    fun datastoreScopeCancellationDoesNotCancelParent() {
        val parentScope = TestScope()
        val datastoreScope = createDatastoreScope(parentScope)
        val parentJob = checkNotNull(parentScope.coroutineContext[Job])

        try {
            datastoreScope.cancel()

            assertTrue(parentJob.isActive)
        } finally {
            parentScope.cancel()
        }
    }
}
