package io.github.arthurkun.generic.datastore.proto.batch

import androidx.datastore.core.DataStore

/**
 * Internal bridge letting batch scopes read and write a single field of a proto snapshot.
 *
 * Implemented by every preference created through [io.github.arthurkun.generic.datastore.proto.ProtoDatastore]
 * (`field`, `data`, and all custom field families). [boundDatastore] guards against mixing
 * preferences that belong to a different datastore instance.
 *
 * @param P The proto message type.
 * @param T The field value type.
 */
internal interface ProtoAccessor<P, T> {
    val boundDatastore: DataStore<P>

    fun readFrom(proto: P): T

    fun writeInto(proto: P, value: T): P
}
