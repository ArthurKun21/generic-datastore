package io.github.arthurkun.generic.datastore

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import io.github.arthurkun.generic.datastore.core.Prefs
import kotlin.coroutines.CoroutineContext

/**
 * Desktop implementation of [Prefs.remember] that uses [collectAsState]
 * to observe the preference flow.
 *
 * @param context The [CoroutineContext] to use for collecting the flow.
 * @return A [MutableState] representing the preference value.
 */
@Composable
actual fun <T> Prefs<T>.remember(
    context: CoroutineContext,
): MutableState<T> {
    val state = this.asFlow().collectAsState(defaultValue, context = context)
    val scope = rememberCoroutineScope()
    return remember(this) {
        PrefsComposeState(
            prefs = this,
            state = state,
            scope = scope,
        )
    }
}
