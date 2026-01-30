package io.github.arthurkun.generic.datastore

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlin.reflect.KProperty

/**
 * Maps a [Prefs] of type [T] to a [Prefs] of type [R], inferring the default value by converting
 * the default value of the original preference.
 *
 * This is a convenience function for cases where the conversion of the default value is guaranteed
 * to be safe.
 *
 * **Warning:** This function will throw an exception during initialization if the `convert`
 * function fails on the original preference's default value. For a safer version where you can
 * provide an explicit default value of type [R], see [map].
 *
 * @param T The original type of the preference value in storage.
 * @param R The target type for the preference value in the application.
 * @param convert A lambda function `(T) -> R` for converting the stored value from type [T] to [R].
 * @param reverse A lambda function `(R) -> T` for converting an application value from type [R]
 *   back to type [T] for storage.
 * @return A new [Prefs] instance of type [R].
 * @throws Exception if `convert(this.defaultValue)` fails.
 */
@Suppress("unused")
fun <T, R> Prefs<T>.mapIO(
    convert: (T) -> R,
    reverse: (R) -> T,
): Prefs<R> =
    MappedPrefs(
        prefs = this,
        defaultValue = convert(this.defaultValue),
        convert = convert,
        reverse = reverse,
    )

/**
 * Maps a [Prefs] of type [T] to a [Prefs] of type [R] using provided converter functions.
 *
 * This function allows transforming a stored preference value to a different type for
 * application use, while the underlying storage remains in its original type [T].
 * It incorporates error handling for the conversion processes:
 * - When converting from [T] to [R] (e.g., on reads): if [convert] fails, [defaultValue] (of type [R]) is used.
 * - When converting from [R] to [T] (e.g., on writes): if [reverse] fails, the `defaultValue`
 *   of the original [Prefs] instance (of type [T]) is used.
 *
 * @param T The original type of the preference value in storage.
 * @param R The target type for the preference value in the application.
 * @param defaultValue The default value of type [R] for the mapped preference. This is also
 *   used as a fallback if the [convert] (T -> R) operation fails.
 * @param convert A lambda function `(T) -> R` for converting the stored value from type [T] to [R].
 *   If this function throws an exception, [defaultValue] (type [R]) is returned.
 * @param reverse A lambda function `(R) -> T` for converting an application value from type [R]
 *   back to type [T] for storage. If this function throws an exception, the `defaultValue`
 *   of the original `Prefs<T>` instance is stored.
 * @return A new [Prefs] instance of type [R] that applies the specified conversions
 *   and error handling logic.
 */
@Suppress("unused")
fun <T, R> Prefs<T>.map(
    defaultValue: R,
    convert: (T) -> R,
    reverse: (R) -> T,
): Prefs<R> =
    MappedPrefs(
        this,
        defaultValue,
        convert,
        reverse,
    )

/**
 * Internal implementation of a mapped [Prefs].
 *
 * @param T The original type of the preference.
 * @param R The target type after conversion.
 * @property prefs The original [Prefs] instance.
 * @property defaultValue The default value for the mapped preference of type [R].
 * @property convert A function to convert from type [T] to type [R].
 * @property reverse A function to convert from type [R] back to type [T] for storage.
 */
internal class MappedPrefs<T, R>(
    private val prefs: Prefs<T>,
    override val defaultValue: R,
    private val convert: (T) -> R,
    private val reverse: (R) -> T,
) : Prefs<R> {
    override fun key(): String = prefs.key()

    /**
     * Safely converts a value from type [T] to [R] using the provided [convert] function.
     * If [convert] throws an exception, logs the error and returns [defaultValue] of type [R].
     *
     * @param value The value of type [T] to convert.
     * @return The converted value of type [R], or [defaultValue] if conversion fails.
     */
    private fun convertFallback(value: T): R {
        return try {
            convert(value)
        } catch (_: Exception) {
            defaultValue
        }
    }

    /**
     * Safely converts a value from type [R] to [T] using the provided [reverse] function.
     * If [reverse] throws an exception, logs the error and returns the `defaultValue`
     * of the original [Prefs] instance (type [T]).
     *
     * @param value The value of type [R] to convert.
     * @return The converted value of type [T], or the original preference's default value if conversion fails.
     */
    private fun reverseFallback(value: R): T {
        return try {
            reverse(value)
        } catch (_: Exception) {
            prefs.defaultValue
        }
    }

    override suspend fun get(): R = convertFallback(prefs.get())

    override suspend fun set(value: R) = prefs.set(reverseFallback(value))

    override suspend fun delete() = prefs.delete()

    override fun asFlow(): Flow<R> = prefs.asFlow().map { convertFallback(it) }

    override fun stateIn(scope: CoroutineScope): StateFlow<R> =
        asFlow().stateIn(scope, SharingStarted.Eagerly, defaultValue)

    override fun getBlocking(): R = convertFallback(prefs.getBlocking())

    override fun setBlocking(value: R) = prefs.setBlocking(reverseFallback(value))

    override fun resetToDefault() = prefs.resetToDefault()

    override fun getValue(thisRef: Any?, property: KProperty<*>): R {
        return getBlocking()
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: R) {
        setBlocking(value)
    }
}
