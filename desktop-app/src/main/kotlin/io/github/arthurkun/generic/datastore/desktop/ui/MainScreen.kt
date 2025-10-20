package io.github.arthurkun.generic.datastore.desktop.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.github.arthurkun.generic.datastore.desktop.domain.Animal
import io.github.arthurkun.generic.datastore.desktop.domain.Theme
import io.github.arthurkun.generic.datastore.remember
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.awt.FileDialog
import java.awt.Frame
import java.io.File
import kotlin.time.Clock

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    vm: MainViewModel,
) {
    var text by vm.preferenceStore.text.remember()
    var theme by vm.preferenceStore.theme.remember()
    var num by vm.preferenceStore.num.remember()
    var bool by vm.preferenceStore.bool.remember()
    var animal by vm.preferenceStore.customObject.remember()
    var duration by vm.preferenceStore.duration.remember()

    val scope = rememberCoroutineScope()

    // Show error dialog if there's an error
    vm.errorMessage?.let { error ->
        AlertDialog(
            onDismissRequest = { vm.clearError() },
            title = { Text("Error") },
            text = { Text(error) },
            confirmButton = {
                Button(onClick = { vm.clearError() }) {
                    Text("OK")
                }
            },
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("GenericDataStore Desktop Sample") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ),
            )
        },
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            // String Preference
            item {
                Text(
                    "String",
                    style = MaterialTheme.typography.headlineSmall,
                )
            }
            item {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text("Text") },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            item {
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                )
            }

            // Integer Preference
            item {
                Text(
                    "Integer",
                    style = MaterialTheme.typography.headlineSmall,
                )
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Button(
                        onClick = { num -= 1 },
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = "Decrement")
                    }
                    Text(
                        "$num",
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.headlineMedium,
                    )
                    Button(
                        onClick = { num += 1 },
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Increment")
                    }
                }
            }
            item {
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                )
            }

            // Boolean Preference
            item {
                Text(
                    "Boolean",
                    style = MaterialTheme.typography.headlineSmall,
                )
            }
            item {
                Text("Current: $bool")
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Button(
                        onClick = { bool = true },
                        enabled = !bool,
                        modifier = Modifier.weight(1f),
                    ) {
                        Text("true")
                    }
                    Button(
                        onClick = { bool = false },
                        enabled = bool,
                        modifier = Modifier.weight(1f),
                    ) {
                        Text("false")
                    }
                }
            }
            item {
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                )
            }

            // Enum Preference (Theme)
            item {
                Text(
                    "Enum (Theme)",
                    style = MaterialTheme.typography.headlineSmall,
                )
            }
            items(Theme.entries) { entry ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = entry == theme,
                            onClick = { theme = entry },
                        )
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    RadioButton(
                        selected = entry == theme,
                        onClick = null,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(entry.name)
                }
            }
            item {
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                )
            }

            // Serialized Object Preference (Animal)
            item {
                Text(
                    "Serializer (Custom Object)",
                    style = MaterialTheme.typography.headlineSmall,
                )
            }
            items(Animal.entries) { entry ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = entry == animal,
                            onClick = { animal = entry },
                        )
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    RadioButton(
                        selected = entry == animal,
                        onClick = null,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(entry.toString())
                }
            }
            item {
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                )
            }

            // Duration Preference
            item {
                Text(
                    "Duration (Timestamp)",
                    style = MaterialTheme.typography.headlineSmall,
                )
            }
            item {
                Text(
                    "$duration",
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
            item {
                Button(
                    onClick = { duration = Clock.System.now() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                ) {
                    Text("Update Duration")
                }
            }
            item {
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                )
            }

            // Export/Import Buttons
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Button(
                        onClick = {
                            scope.launch {
                                exportPreferences(vm)
                            }
                        },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text("Export to JSON")
                    }
                    Button(
                        onClick = {
                            scope.launch {
                                importPreferences(vm)
                            }
                        },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text("Import from JSON")
                    }
                }
            }
        }
    }
}

private suspend fun exportPreferences(vm: MainViewModel) {
    withContext(Dispatchers.IO) {
        try {
            val json = vm.exportPreferences()

            val fileDialog = FileDialog(null as Frame?, "Save Preferences", FileDialog.SAVE)
            fileDialog.file = "preferences.json"
            fileDialog.isVisible = true

            val directory = fileDialog.directory
            val filename = fileDialog.file

            if (directory != null && filename != null) {
                val file = File(directory, filename)
                file.writeText(json)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

private suspend fun importPreferences(vm: MainViewModel) {
    withContext(Dispatchers.IO) {
        try {
            val fileDialog = FileDialog(null as Frame?, "Load Preferences", FileDialog.LOAD)
            fileDialog.file = "*.json"
            fileDialog.isVisible = true

            val directory = fileDialog.directory
            val filename = fileDialog.file

            if (directory != null && filename != null) {
                val file = File(directory, filename)
                if (file.exists()) {
                    val jsonString = file.readText()
                    vm.importPreferences(jsonString)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
