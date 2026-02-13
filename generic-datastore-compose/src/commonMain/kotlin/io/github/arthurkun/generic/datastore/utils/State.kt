package io.github.arthurkun.generic.datastore.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

@Composable
public expect fun <T> StateFlow<T>.collectAsStatePlatform(): State<T>

@Composable
public expect fun <T> Flow<T>.collectAsStatePlatform(
    initialValue: T,
    context: CoroutineContext = EmptyCoroutineContext,
): State<T>
