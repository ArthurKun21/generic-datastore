package io.github.arthurkun.generic.datastore

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.SnapshotMutationPolicy
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.structuralEqualityPolicy
import io.github.arthurkun.generic.datastore.core.DelegatedPreference
import io.github.arthurkun.generic.datastore.utils.collectAsStatePlatform
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * Remembers the value of this preference and returns a [MutableState] that can be used to
 * observe and update the preference value.
 *
 * On Android, this uses `collectAsStateWithLifecycle` for lifecycle-aware collection.
 * On Desktop and IOS, this uses `collectAsState`.
 *
 * @param context The [CoroutineContext] to use for collecting the flow.
 * @param policy The [SnapshotMutationPolicy] used to determine value equivalence.
 *   Only values that are not equivalent according to the policy will trigger a write.
 *   Defaults to [structuralEqualityPolicy].
 * @return A [MutableState] representing the preference value.
 */
@Composable
public fun <T> DelegatedPreference<T>.remember(
    context: CoroutineContext = EmptyCoroutineContext,
    policy: SnapshotMutationPolicy<T> = structuralEqualityPolicy(),
): MutableState<T> {
    val state = this.asFlow().collectAsStatePlatform(defaultValue, context = context)
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
