package io.github.arthurkun.generic.datastore.core

/**
 * Marks low-level wiring APIs that support Generic Datastore internals and are not intended for
 * regular library consumers.
 */
@MustBeDocumented
@Retention(value = AnnotationRetention.BINARY)
@RequiresOptIn(
    message = "This API is intended for Generic Datastore internals and may change without notice.",
    level = RequiresOptIn.Level.ERROR,
)
public annotation class InternalGenericDatastoreApi
