package io.github.arthurkun.generic.datastore.preferences.utils

import androidx.datastore.preferences.core.MutablePreferences
import io.github.arthurkun.generic.datastore.core.DelegatedPreference
import io.github.arthurkun.generic.datastore.preferences.Preference
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

/**
 * Maps a [Preference] of type [T] to a [Preference] of type [R], deriving the mapped default by
 * converting the source preference's default value.
 *
 * Use this only when [convert] is guaranteed to succeed for the source default value. If that is
 * not guaranteed, prefer [map] and supply an explicit mapped default.
 *
 * @param T The stored value type.
 * @param R The mapped value type.
 * @param convert Converts the stored value to the exposed mapped value.
 * @param reverse Converts the mapped value back to the stored representation.
 * @return A new [Preference] of type [R].
 * @throws Exception if `convert(this.defaultValue)` fails.
 */
internal fun <T, R> Preference<T>.mapIO(
    convert: (T) -> R,
    reverse: (R) -> T,
): Preference<R> =
    MappedPrefs(
        prefs = this,
        defaultValue = convert(this.defaultValue),
        convert = convert,
        reverse = reverse,
    )

/**
 * Maps a [Preference] of type [T] to a [Preference] of type [R] using explicit conversion
 * functions and a mapped default value.
 *
 * Read-side conversion failures return [defaultValue]. Write-side conversion failures fall back
 * to the original preference's default value.
 *
 * @param T The stored value type.
 * @param R The mapped value type.
 * @param defaultValue The default value for the mapped preference and the read-side fallback.
 * @param convert Converts the stored value to the exposed mapped value.
 * @param reverse Converts the mapped value back to the stored representation.
 * @return A new [Preference] of type [R] with fallback behavior around both conversions.
 */
internal fun <T, R> Preference<T>.map(
    defaultValue: R,
    convert: (T) -> R,
    reverse: (R) -> T,
): Preference<R> =
    MappedPrefs(
        this,
        defaultValue,
        convert,
        reverse,
    )

/**
 * Internal [Preference] implementation used by [map] and [mapIO].
 *
 * The wrapper delegates persistence to [prefs] and applies conversion functions on top. It also
 * implements [PreferencesAccessor] so mapped preferences continue to work with batch APIs.
 *
 * @param T The stored value type.
 * @param R The mapped value type.
 * @property prefs The original preference instance.
 * @property defaultValue The default value exposed by the mapped preference.
 * @property convert Converts stored values to mapped values.
 * @property reverse Converts mapped values back to stored values.
 */
internal class MappedPrefs<T, R>(
    private val prefs: DelegatedPreference<T>,
    override val defaultValue: R,
    private val convert: (T) -> R,
    private val reverse: (R) -> T,
) : Preference<R>, PreferencesAccessor<R> {
    override fun key(): String = prefs.key()

    /**
     * Safely converts a value from type [T] to [R] using the provided [convert] function.
     * If [convert] throws an exception, returns [defaultValue] of type [R].
     *
     * @param value The value of type [T] to convert.
     * @return The converted value of type [R], or [defaultValue] if conversion fails.
     */
    private fun convertFallback(value: T): R {
        return try {
            convert(value)
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            defaultValue
        }
    }

    /**
     * Safely converts a value from type [R] to [T] using the provided [reverse] function.
     * If [reverse] throws an exception, returns the `defaultValue`
     * of the original [DelegatedPreference] instance (type [T]).
     *
     * @param value The value of type [R] to convert.
     * @return The converted value of type [T], or the original preference's default value if conversion fails.
     */
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

    override fun getValue(thisRef: Any?, property: KProperty<*>): R {
        return getBlocking()
    }

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
