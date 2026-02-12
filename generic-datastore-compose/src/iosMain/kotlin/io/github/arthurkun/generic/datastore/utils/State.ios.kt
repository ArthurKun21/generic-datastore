package io.github.arthurkun.generic.datastore.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import kotlinx.coroutines.flow.StateFlow

@Composable
public actual fun <T> StateFlow<T>.collectAsStatePlatform(): State<T> =
    this.collectAsState()
