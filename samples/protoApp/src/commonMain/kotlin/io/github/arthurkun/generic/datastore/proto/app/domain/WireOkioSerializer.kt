package io.github.arthurkun.generic.datastore.proto.app.domain

import androidx.datastore.core.okio.OkioSerializer
import com.squareup.wire.ProtoAdapter
import okio.BufferedSink
import okio.BufferedSource

/**
 * A generic [OkioSerializer] that bridges Square Wire's [ProtoAdapter] to
 * AndroidX DataStore's serialization contract.
 *
 * This allows any Wire-generated message to be stored in a Proto DataStore
 * via [createProtoDatastore].
 *
 * @param T The Wire-generated message type.
 * @param adapter The [ProtoAdapter] for encoding/decoding the message.
 * @param defaultValue The default (empty) instance of the message.
 */
class WireOkioSerializer<T>(
    private val adapter: ProtoAdapter<T>,
    override val defaultValue: T,
) : OkioSerializer<T> {

    override suspend fun readFrom(source: BufferedSource): T {
        return adapter.decode(source)
    }

    override suspend fun writeTo(t: T, sink: BufferedSink) {
        adapter.encode(sink, t)
    }
}
