package io.github.arthurkun.generic.datastore.proto.backup

import io.github.arthurkun.generic.datastore.core.systemFileSystem
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import okio.Path

internal class ProtoBackupCreator(
    private val path: Path,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) {

    suspend fun exportAsByteArray(): ByteArray = withContext(ioDispatcher) {
        if (!systemFileSystem.exists(path)) {
            return@withContext ByteArray(0)
        }

        systemFileSystem.read(path) {
            readByteArray()
        }
    }
}
