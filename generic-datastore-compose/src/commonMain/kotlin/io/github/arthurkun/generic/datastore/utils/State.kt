package io.github.arthurkun.generic.datastore.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import kotlinx.coroutines.flow.StateFlow

@Composable
public expect fun <T> StateFlow<T>.collectAsStatePlatform(): State<T>
