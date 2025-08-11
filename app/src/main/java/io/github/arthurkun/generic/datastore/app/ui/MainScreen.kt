package io.github.arthurkun.generic.datastore.app.ui

import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.arthurkun.generic.datastore.app.domain.Animal
import io.github.arthurkun.generic.datastore.app.domain.Theme
import io.github.arthurkun.generic.datastore.app.domain.setAppCompatDelegateThemeMode
import io.github.arthurkun.generic.datastore.remember
import io.github.arthurkun.generic.datastore.toJsonElement
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlin.time.Clock

@Composable
fun MainScreen(
    vm: MainViewModel = viewModel(),
) {
    var text by vm.preferenceStore.text.remember()
    var theme by vm.preferenceStore.theme.remember()
    var num by vm.preferenceStore.num.remember()
    var bool by vm.preferenceStore.bool.remember()
    var animal by vm.preferenceStore.customObject.remember()
    var duration by vm.preferenceStore.duration.remember()

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val importPreference = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument(),
    ) { uri ->
        uri?.let { uriString ->
            scope.launch(Dispatchers.IO) {
                val jsonString = context
                    .contentResolver
                    .openInputStream(uriString)
                    ?.use { inputSteam ->
                        inputSteam.use {
                            it.reader().readText()
                        }
                    }
                jsonString?.let {
                    vm.importPreferences(it)
                }
            }
        }
    }

    val exportPreference = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json"),
    ) { uri ->
        uri?.let { uriString ->
            scope.launch(Dispatchers.IO) {
                try {
                    val values = vm.exportPreferences()
                    val jsonConfig = Json {
                        prettyPrint = true
                    }

                    val json = jsonConfig.encodeToString(
                        values.toJsonElement(),
                    )
                    context.contentResolver
                        .openOutputStream(uriString)
                        ?.use {
                            it.write(json.toByteArray())
                        }
                } catch (e: Exception) {
                    Log.e("MainScreen", "Error exporting preferences", e)
                    Toast.makeText(
                        context,
                        "Error exporting preferences",
                        Toast.LENGTH_SHORT,
                    ).show()
                }
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
        item {
            Text(
                "String",
                style = MaterialTheme.typography.headlineSmall,
            )
        }
        item {
            OutlinedTextField(
                value = TextFieldValue(text, TextRange(text.length)),
                onValueChange = {
                    text = it.text
                },
                label = {
                    Text("text")
                },
                modifier = Modifier.fillMaxWidth(),
            )
        }
        item {
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
            )
        }
        item {
            Text(
                "Integer",
                style = MaterialTheme.typography.headlineSmall,
            )
        }
        item {
            ListItem(
                headlineContent = {
                    Text(
                        "$num",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                    )
                },
                leadingContent = {
                    Button(
                        onClick = {
                            num -= 1
                        },
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = "Decrement")
                    }
                },
                trailingContent = {
                    Button(
                        onClick = {
                            num += 1
                        },
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Increment")
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            )
        }
        item {
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
            )
        }
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
                    onClick = {
                        bool = true
                    },
                    enabled = !bool,
                    modifier = Modifier
                        .weight(1f),
                ) {
                    Text("true")
                }
                Button(
                    onClick = {
                        bool = false
                    },
                    enabled = bool,
                    modifier = Modifier
                        .weight(1f),
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
        item {
            Text(
                "Enum",
                style = MaterialTheme.typography.headlineSmall,
            )
        }
        items(Theme.entries) { entry ->
            ListItem(
                headlineContent = {
                    Text(entry.name)
                },
                leadingContent = {
                    RadioButton(
                        selected = entry == theme,
                        onClick = null,
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = entry == theme,
                        onClick = {
                            theme = entry
                            setAppCompatDelegateThemeMode(entry)
                        },
                    ),
            )
        }

        item {
            Text(
                "Serializer",
                style = MaterialTheme.typography.headlineSmall,
            )
        }

        items(Animal.entries) { entry ->
            ListItem(
                headlineContent = {
                    Text(entry.toString())
                },
                leadingContent = {
                    RadioButton(
                        selected = entry == animal,
                        onClick = null,
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = entry == animal,
                        onClick = {
                            animal = entry
                        },
                    ),
            )
        }

        item {
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
            )
        }
        item {
            Text(
                "Duration",
                style = MaterialTheme.typography.headlineSmall,
            )
        }

        item {
            Text(
                "$duration",
                style = MaterialTheme.typography.headlineSmall,
            )
        }
        item {
            Button(
                onClick = {
                    duration = Clock.System.now()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            ) {
                Text(
                    "Update Duration",
                    style = MaterialTheme.typography.headlineSmall,
                )
            }
        }
        item {
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
            )
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            ) {
                Button(
                    onClick = {
                        exportPreference.launch("preferences.json")
                    },
                    modifier = Modifier.weight(1f),
                ) {
                    Text(
                        "Export",
                        style = MaterialTheme.typography.headlineSmall,
                    )
                }
                Button(
                    onClick = {
                        importPreference.launch(
                            // octet-stream as backup in case Android doesn't detect json
                            arrayOf("application/json", "application/octet-stream"),
                        )
                    },
                    modifier = Modifier.weight(1f),
                ) {
                    Text(
                        "Import",
                        style = MaterialTheme.typography.headlineSmall,
                    )
                }
            }
        }
    }
}
