package io.github.arthurkun.generic.datastore.proto

import androidx.datastore.core.DataStore
import androidx.datastore.core.IOException
import io.github.arthurkun.generic.datastore.core.Preference
import io.github.arthurkun.generic.datastore.core.Prefs
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlin.reflect.KProperty

/**
 * A [Preference] implementation backed by a Proto DataStore.
 *
 * @param T The proto message type.
 * @param datastore The [DataStore<T>] instance.
 * @param defaultValue The default value for the proto message.
 */
internal class ProtoPreference<T>(
    private val datastore: DataStore<T>,
    override val defaultValue: T,
    private val key: String = "proto_data",
) : Prefs<T> {

    init {
        require(key.isNotBlank()) {
            "Proto key cannot be blank."
        }
    }

    private val ioDispatcher = Dispatchers.IO

    override fun key(): String = key

    override suspend fun get(): T = withContext(ioDispatcher) {
        asFlow().first()
    }

    override suspend fun set(value: T) {
        withContext(ioDispatcher) {
            datastore.updateData { value }
        }
    }

    override suspend fun update(transform: (T) -> T) {
        withContext(ioDispatcher) {
            datastore.updateData { current -> transform(current) }
        }
    }

    override suspend fun resetToDefault() = set(defaultValue)

    override suspend fun delete() {
        withContext(ioDispatcher) {
            datastore.updateData { defaultValue }
        }
    }

    override fun asFlow(): Flow<T> = datastore
        .data
        .catch { e ->
            if (e is IOException) {
                emit(defaultValue)
            } else {
                throw e
            }
        }

    override fun stateIn(scope: CoroutineScope, started: SharingStarted): StateFlow<T> =
        asFlow().stateIn(scope, started, defaultValue)

    override fun getBlocking(): T = runBlocking { get() }

    override fun setBlocking(value: T) = runBlocking { set(value) }

    override fun resetToDefaultBlocking() = setBlocking(defaultValue)

    override fun getValue(thisRef: Any?, property: KProperty<*>): T = getBlocking()

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) = setBlocking(value)
}
