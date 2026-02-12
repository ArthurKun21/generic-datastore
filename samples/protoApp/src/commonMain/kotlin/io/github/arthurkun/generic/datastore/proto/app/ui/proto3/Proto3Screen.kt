package io.github.arthurkun.generic.datastore.proto.app.ui.proto3

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp

@Composable
fun Proto3Screen(
    viewModel: Proto3ViewModel,
) {
    val state by viewModel.uiState.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        item {
            Text(
                text = "Proto3 — AppConfig",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 8.dp),
            )
        }

        // --- Top-level fields ---
        item {
            AppNameSection(
                appName = state.appName,
                onAppNameChange = viewModel::setAppName,
            )
        }

        item { HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp)) }

        item {
            MaxRetriesSection(
                maxRetries = state.maxRetries,
                onMaxRetriesChange = viewModel::setMaxRetries,
                onIncrement = viewModel::incrementMaxRetries,
            )
        }

        item { HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp)) }

        item {
            DebugModeSection(
                debugMode = state.debugMode,
                onToggle = { viewModel.setDebugMode(!state.debugMode) },
            )
        }

        item { HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp)) }

        item {
            RefreshIntervalSection(
                refreshInterval = state.refreshInterval,
                onRefreshIntervalChange = viewModel::setRefreshInterval,
            )
        }

        item { HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp)) }

        // --- Nested NetworkConfig fields ---
        item {
            Text(
                text = "NetworkConfig (nested field())",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 4.dp),
            )
        }

        item {
            BaseUrlSection(
                baseUrl = state.baseUrl,
                onBaseUrlChange = viewModel::setBaseUrl,
            )
        }

        item { HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp)) }

        item {
            TimeoutSection(
                timeoutMs = state.timeoutMs,
                onTimeoutChange = viewModel::setTimeoutMs,
            )
        }

        item { HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp)) }

        // --- Deeply nested RetryPolicy fields ---
        item {
            Text(
                text = "RetryPolicy (3-level nesting)",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 4.dp),
            )
        }

        item {
            MaxAttemptsSection(
                maxAttempts = state.maxAttempts,
                onMaxAttemptsChange = viewModel::setMaxAttempts,
            )
        }

        item { HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp)) }

        item {
            BackoffSection(
                backoffMs = state.backoffMs,
                onBackoffChange = viewModel::setBackoffMs,
            )
        }

        item { HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp)) }

        item {
            ExponentialSection(
                exponential = state.exponential,
                onToggle = viewModel::toggleExponential,
            )
        }

        item { HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp)) }

        // --- Whole-object / network operations ---
        item {
            OperationsSection(
                onResetAll = viewModel::resetAll,
                onDeleteAll = viewModel::deleteAll,
                onResetNetwork = viewModel::resetNetwork,
            )
        }
    }
}

@Composable
private fun AppNameSection(
    appName: String,
    onAppNameChange: (String) -> Unit,
) {
    Column {
        Text("App Name (field)", style = MaterialTheme.typography.titleSmall)
        OutlinedTextField(
            value = appName,
            onValueChange = onAppNameChange,
            label = { Text("App Name") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )
    }
}

@Composable
private fun MaxRetriesSection(
    maxRetries: Int,
    onMaxRetriesChange: (Int) -> Unit,
    onIncrement: () -> Unit,
) {
    Column {
        Text("Max Retries (field + update)", style = MaterialTheme.typography.titleSmall)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OutlinedTextField(
                value = maxRetries.toString(),
                onValueChange = { it.toIntOrNull()?.let(onMaxRetriesChange) },
                label = { Text("Max Retries") },
                modifier = Modifier.weight(1f),
                singleLine = true,
            )
            IconButton(onClick = onIncrement) {
                Icon(Icons.Filled.Add, contentDescription = "Increment retries")
            }
        }
    }
}

@Composable
private fun DebugModeSection(
    debugMode: Boolean,
    onToggle: () -> Unit,
) {
    Column {
        Text("Debug Mode (field)", style = MaterialTheme.typography.titleSmall)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .toggleable(
                    value = debugMode,
                    role = Role.Checkbox,
                    onValueChange = { onToggle() },
                ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Checkbox(checked = debugMode, onCheckedChange = null)
            Text(
                text = if (debugMode) "Debug ON" else "Debug OFF",
                modifier = Modifier.padding(start = 8.dp),
            )
        }
    }
}

@Composable
private fun RefreshIntervalSection(
    refreshInterval: Double,
    onRefreshIntervalChange: (Double) -> Unit,
) {
    Column {
        Text("Refresh Interval (field double)", style = MaterialTheme.typography.titleSmall)
        OutlinedTextField(
            value = refreshInterval.toString(),
            onValueChange = { it.toDoubleOrNull()?.let(onRefreshIntervalChange) },
            label = { Text("Refresh Interval (seconds)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )
    }
}

@Composable
private fun BaseUrlSection(
    baseUrl: String,
    onBaseUrlChange: (String) -> Unit,
) {
    Column {
        Text("Base URL (nested field)", style = MaterialTheme.typography.titleSmall)
        OutlinedTextField(
            value = baseUrl,
            onValueChange = onBaseUrlChange,
            label = { Text("Base URL") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )
    }
}

@Composable
private fun TimeoutSection(
    timeoutMs: Int,
    onTimeoutChange: (Int) -> Unit,
) {
    Column {
        Text("Timeout (nested field)", style = MaterialTheme.typography.titleSmall)
        OutlinedTextField(
            value = timeoutMs.toString(),
            onValueChange = { it.toIntOrNull()?.let(onTimeoutChange) },
            label = { Text("Timeout (ms)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )
    }
}

@Composable
private fun MaxAttemptsSection(
    maxAttempts: Int,
    onMaxAttemptsChange: (Int) -> Unit,
) {
    Column {
        Text("Max Attempts (deeply nested)", style = MaterialTheme.typography.titleSmall)
        OutlinedTextField(
            value = maxAttempts.toString(),
            onValueChange = { it.toIntOrNull()?.let(onMaxAttemptsChange) },
            label = { Text("Max Attempts") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )
    }
}

@Composable
private fun BackoffSection(
    backoffMs: Long,
    onBackoffChange: (Long) -> Unit,
) {
    Column {
        Text("Backoff (deeply nested)", style = MaterialTheme.typography.titleSmall)
        OutlinedTextField(
            value = backoffMs.toString(),
            onValueChange = { it.toLongOrNull()?.let(onBackoffChange) },
            label = { Text("Backoff (ms)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )
    }
}

@Composable
private fun ExponentialSection(
    exponential: Boolean,
    onToggle: () -> Unit,
) {
    Column {
        Text("Exponential Backoff (deeply nested toggle)", style = MaterialTheme.typography.titleSmall)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .toggleable(
                    value = exponential,
                    role = Role.Checkbox,
                    onValueChange = { onToggle() },
                ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Checkbox(checked = exponential, onCheckedChange = null)
            Text(
                text = if (exponential) "Exponential ON" else "Exponential OFF",
                modifier = Modifier.padding(start = 8.dp),
            )
        }
    }
}

@Composable
private fun OperationsSection(
    onResetAll: () -> Unit,
    onDeleteAll: () -> Unit,
    onResetNetwork: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Operations", style = MaterialTheme.typography.titleSmall)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Button(
                onClick = onResetAll,
                modifier = Modifier.weight(1f),
            ) {
                Text("Reset All")
            }
            Button(
                onClick = onDeleteAll,
                modifier = Modifier.weight(1f),
            ) {
                Text("Delete All")
            }
        }
        Button(
            onClick = onResetNetwork,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Icon(Icons.Filled.Delete, contentDescription = null)
            Text("Reset Network Config", modifier = Modifier.padding(start = 4.dp))
        }
    }
}
