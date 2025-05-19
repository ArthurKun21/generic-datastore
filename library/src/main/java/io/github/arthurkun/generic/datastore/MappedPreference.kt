package io.github.arthurkun.generic.datastore

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlin.reflect.KProperty

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

internal class MappedPrefs<T, R>(
    private val prefs: Prefs<T>,
    private val defaultValue: R,
    private val convert: (T) -> R,
    private val reverse: (R) -> T
) : Prefs<R> {
    override suspend fun getValue(thisRef: Any, property: KProperty<*>) = get()
    override fun key(): String = prefs.key()

    override suspend fun get(): R = convert(prefs.get())

    override suspend fun set(value: R) = prefs.set(reverse(value))

    override suspend fun setValue(thisRef: Any, property: KProperty<*>, value: R) = set(value)

    override suspend fun delete() = prefs.delete()
    override fun defaultValue(): R = defaultValue

    override fun asFlow(): Flow<R> = prefs.asFlow().map { convert(it) }

    override suspend fun stateIn(scope: CoroutineScope): StateFlow<R> =
        asFlow().stateIn(scope, SharingStarted.Eagerly, get())

    override suspend fun resetToDefault() = prefs.resetToDefault()

}