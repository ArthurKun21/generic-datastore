package io.github.arthurkun.generic.datastore.proto.backup

import io.github.arthurkun.generic.datastore.core.systemFileSystem
import okio.Path

internal class ProtoBackupCreator(private val path: Path) {

    suspend fun exportAsByteArray(): ByteArray {
        if (!systemFileSystem.exists(path)) {
            return ByteArray(0)
        }

        return systemFileSystem.read(path) {
            readByteArray()
        }
    }
}
