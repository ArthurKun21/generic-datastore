package io.github.arthurkun.generic.datastore.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlin.coroutines.CoroutineContext

@Composable
public actual fun <T> StateFlow<T>.collectAsStatePlatform(): State<T> =
    this.collectAsState()

@Composable
public actual fun <T> Flow<T>.collectAsStatePlatform(
    initialValue: T,
    context: CoroutineContext,
): State<T> = this.collectAsState(initialValue, context = context)
