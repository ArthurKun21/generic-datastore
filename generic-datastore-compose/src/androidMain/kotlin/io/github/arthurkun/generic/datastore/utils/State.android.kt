package io.github.arthurkun.generic.datastore.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.StateFlow

@Composable
public actual fun <T> StateFlow<T>.collectAsStatePlatform(): State<T> =
    this.collectAsStateWithLifecycle()
