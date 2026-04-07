package io.github.arthurkun.generic.datastore.preferences

import androidx.datastore.preferences.core.MutablePreferences
import io.github.arthurkun.generic.datastore.core.DelegatedPreference
import io.github.arthurkun.generic.datastore.preferences.batch.PreferencesAccessor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlin.coroutines.cancellation.CancellationException
import kotlin.reflect.KProperty
import androidx.datastore.preferences.core.Preferences as DataStorePreferences

internal class MappedPreferenceImpl<T, R>(
    private val prefs: DelegatedPreference<T>,
    override val defaultValue: R,
    private val convert: (T) -> R,
    private val reverse: (R) -> T,
) : Preference<R>, PreferencesAccessor<R> {
    override fun key(): String = prefs.key()

    private fun convertFallback(value: T): R {
        return try {
            convert(value)
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            defaultValue
        }
    }

    private fun reverseFallback(value: R): T {
        return try {
            reverse(value)
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            prefs.defaultValue
        }
    }

    override suspend fun get(): R = asFlow().first()

    override suspend fun set(value: R) = prefs.set(reverseFallback(value))

    override suspend fun update(transform: (R) -> R) {
        prefs.update { current ->
            reverseFallback(transform(convertFallback(current)))
        }
    }

    override suspend fun resetToDefault() = prefs.set(reverseFallback(defaultValue))

    override suspend fun delete() = prefs.delete()

    override fun asFlow(): Flow<R> = prefs.asFlow().map { convertFallback(it) }

    override fun stateIn(scope: CoroutineScope, started: SharingStarted): StateFlow<R> =
        asFlow().stateIn(scope, started, defaultValue)

    override fun getBlocking(): R = convertFallback(prefs.getBlocking())

    override fun setBlocking(value: R) = prefs.setBlocking(reverseFallback(value))

    override fun resetToDefaultBlocking() = prefs.setBlocking(reverseFallback(defaultValue))

    override fun getValue(thisRef: Any?, property: KProperty<*>): R = getBlocking()

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: R) {
        setBlocking(value)
    }

    @Suppress("UNCHECKED_CAST")
    override fun readFrom(preferences: DataStorePreferences): R {
        val raw = (prefs as PreferencesAccessor<T>).readFrom(preferences)
        return convertFallback(raw)
    }

    @Suppress("UNCHECKED_CAST")
    override fun writeInto(mutablePreferences: MutablePreferences, value: R) {
        (prefs as PreferencesAccessor<T>).writeInto(mutablePreferences, reverseFallback(value))
    }

    @Suppress("UNCHECKED_CAST")
    override fun removeFrom(mutablePreferences: MutablePreferences) {
        (prefs as PreferencesAccessor<T>).removeFrom(mutablePreferences)
    }
}
