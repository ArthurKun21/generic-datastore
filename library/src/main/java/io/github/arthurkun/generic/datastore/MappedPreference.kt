package io.github.arthurkun.generic.datastore

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlin.reflect.KProperty

/**
 * Maps a [Prefs] of type [T] to a [Prefs] of type [R] using converter functions.
 *
 * This allows you to transform the stored preference value to a different type
 * for application use, while still storing it in its original format.
 *
 * @param T The original type of the preference.
 * @param R The target type after conversion.
 * @param defaultValue The default value for the mapped preference of type [R].
 * @param convert A function to convert from type [T] to type [R].
 * @param reverse A function to convert from type [R] back to type [T] for storage.
 * @return A new [Prefs] instance that handles the type conversion.
 */
@Suppress("unused")
fun <T, R> Prefs<T>.map(
    defaultValue: R,
    convert: (T) -> R,
    reverse: (R) -> T
): Prefs<R> =
    MappedPrefs(
        this,
        defaultValue,
        convert,
        reverse
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
    override suspend fun getValue(thisRef: Any, property: KProperty<*>) = get()
    override fun key(): String = prefs.key()

    override suspend fun get(): R = convert(prefs.get())

    override suspend fun set(value: R) = prefs.set(reverse(value))

    override suspend fun setValue(thisRef: Any, property: KProperty<*>, value: R) = set(value)

    override suspend fun delete() = prefs.delete()


    override fun asFlow(): Flow<R> = prefs.asFlow().map { convert(it) }

    override suspend fun stateIn(scope: CoroutineScope): StateFlow<R> =
        asFlow().stateIn(scope, SharingStarted.Eagerly, get())

    override suspend fun resetToDefault() = prefs.resetToDefault()

}