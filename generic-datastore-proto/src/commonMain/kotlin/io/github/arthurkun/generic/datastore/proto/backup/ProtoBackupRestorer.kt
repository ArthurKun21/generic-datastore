package io.github.arthurkun.generic.datastore.proto.backup

import androidx.datastore.core.DataStore
import androidx.datastore.core.okio.OkioSerializer
import okio.Buffer

internal class ProtoBackupRestorer<T>(
    private val datastore: DataStore<T>,
    private val serializer: OkioSerializer<T>,
) {

    suspend fun importFromByteArray(data: ByteArray) {
        val imported = serializer.readFrom(Buffer().write(data))
        datastore.updateData { imported }
    }
}
