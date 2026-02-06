package io.github.arthurkun.generic.datastore

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.SnapshotMutationPolicy
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.arthurkun.generic.datastore.core.Prefs
import kotlin.coroutines.CoroutineContext

/**
 * Android implementation of [Prefs.remember] that uses [collectAsStateWithLifecycle]
 * to automatically pause collection when the lifecycle is stopped.
 *
 * @param context The [CoroutineContext] to use for collecting the flow.
 * @param policy The [SnapshotMutationPolicy] used to determine value equivalence.
 * @return A [MutableState] representing the preference value.
 */
@Composable
actual fun <T> Prefs<T>.remember(
    context: CoroutineContext,
    policy: SnapshotMutationPolicy<T>,
): MutableState<T> {
    val state = this.asFlow().collectAsStateWithLifecycle(defaultValue, context = context)
    val scope = rememberCoroutineScope()
    return remember(this, policy) {
        PrefsComposeState(
            prefs = this,
            state = state,
            scope = scope,
            policy = policy,
        )
    }
}
