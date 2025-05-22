package io.github.arthurkun.generic.datastore


import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * Remembers the value of this preference and returns a [MutableState] that can be used to
 * observe and update the preference value.
 *
 * @return A [MutableState] representing the preference value.
 *
 */
@Composable
fun <T> Prefs<T>.remember(
    context: CoroutineContext = EmptyCoroutineContext,
): MutableState<T> {
    val state = this.asFlow().collectAsStateWithLifecycle(defaultValue, context = context)
    return remember(this) {
        object : MutableState<T> {
            override var value: T
                get() = state.value
                set(value) {
                    setValue(value)
                }

            override fun component1(): T = value

            override fun component2(): (T) -> Unit = { value = it }
        }
    }
}