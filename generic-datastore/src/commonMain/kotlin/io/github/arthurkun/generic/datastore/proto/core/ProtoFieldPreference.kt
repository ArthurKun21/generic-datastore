package io.github.arthurkun.generic.datastore.proto.core

import androidx.datastore.core.DataStore
import androidx.datastore.core.IOException
import io.github.arthurkun.generic.datastore.core.BasePreference
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

/**
 * A [BasePreference] implementation for an individual field within a Proto DataStore.
 *
 * @param P The proto/data class type.
 * @param T The field type.
 * @param datastore The [DataStore] instance holding the proto.
 * @param key A unique key identifying this field preference.
 * @param defaultValue The default value for the field.
 * @param getter A function that extracts the field from the proto snapshot.
 * @param updater A function that returns a new proto with the field updated.
 * @param defaultProtoValue The default proto value, used as fallback on [IOException].
 * @param ioDispatcher The dispatcher for IO operations.
 */
internal class ProtoFieldPreference<P, T>(
    internal val datastore: DataStore<P>,
    private val key: String,
    override val defaultValue: T,
    internal val getter: (P) -> T,
    internal val updater: (P, T) -> P,
    private val defaultProtoValue: P,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : BasePreference<T> {

    init {
        require(key.isNotBlank()) {
            "Proto key cannot be blank."
        }
    }

    override fun key(): String = key

    override suspend fun get(): T = withContext(ioDispatcher) {
        asFlow().first()
    }

    override suspend fun set(value: T) {
        withContext(ioDispatcher) {
            datastore.updateData { current -> updater(current, value) }
        }
    }

    override suspend fun update(transform: (T) -> T) {
        withContext(ioDispatcher) {
            datastore.updateData { current ->
                val currentField = getter(current)
                updater(current, transform(currentField))
            }
        }
    }

    override suspend fun delete() = resetToDefault()

    override suspend fun resetToDefault() = set(defaultValue)

    override fun asFlow(): Flow<T> = datastore.data
        .catch { e ->
            if (e is IOException) emit(defaultProtoValue) else throw e
        }
        .map { getter(it) }

    override fun stateIn(scope: CoroutineScope, started: SharingStarted): StateFlow<T> =
        asFlow().stateIn(scope, started, defaultValue)

    override fun getBlocking(): T = runBlocking { get() }

    override fun setBlocking(value: T) = runBlocking { set(value) }
}
