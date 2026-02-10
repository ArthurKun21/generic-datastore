package io.github.arthurkun.generic.datastore

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.SnapshotMutationPolicy
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import io.github.arthurkun.generic.datastore.core.DelegatedPreference
import kotlin.coroutines.CoroutineContext

/**
 * Desktop implementation of [DelegatedPreference.remember] that uses [collectAsState]
 * to observe the preference flow.
 *
 * @param context The [CoroutineContext] to use for collecting the flow.
 * @param policy The [SnapshotMutationPolicy] used to determine value equivalence.
 * @return A [MutableState] representing the preference value.
 */
@Composable
public actual fun <T> DelegatedPreference<T>.remember(
    context: CoroutineContext,
    policy: SnapshotMutationPolicy<T>,
): MutableState<T> {
    val state = this.asFlow().collectAsState(defaultValue, context = context)
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
