package io.github.arthurkun.generic.datastore

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import io.github.arthurkun.generic.datastore.core.Prefs
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * A [MutableState] backed by a [Prefs] instance.
 *
 * Reads are delegated to a collected [State] snapshot of the preference flow,
 * and writes are launched asynchronously via the provided [CoroutineScope].
 *
 * @param T The type of the preference value.
 * @param prefs The underlying [Prefs] instance to read from and write to.
 * @param state The collected [State] snapshot of the preference flow.
 * @param scope The [CoroutineScope] used to launch write operations.
 */
class PrefsComposeState<T>(
    private val prefs: Prefs<T>,
    private val state: State<T>,
    private val scope: CoroutineScope,
) : MutableState<T> {

    override var value: T
        get() = state.value
        set(value) {
            scope.launch {
                prefs.set(value)
            }
        }

    override fun component1(): T = value

    override fun component2(): (T) -> Unit = { value = it }
}
