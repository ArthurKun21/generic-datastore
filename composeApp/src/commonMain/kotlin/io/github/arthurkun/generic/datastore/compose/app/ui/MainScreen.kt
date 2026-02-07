package io.github.arthurkun.generic.datastore.compose.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.github.arthurkun.generic.datastore.compose.app.domain.Animal
import io.github.arthurkun.generic.datastore.compose.app.domain.PreferenceStore
import io.github.arthurkun.generic.datastore.compose.app.domain.Theme
import io.github.arthurkun.generic.datastore.compose.app.domain.UserProfile
import io.github.arthurkun.generic.datastore.core.toJsonElement
import io.github.arthurkun.generic.datastore.core.toJsonMap
import io.github.arthurkun.generic.datastore.preferences.toggle
import io.github.arthurkun.generic.datastore.remember
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlin.time.Clock
import kotlin.time.Instant

private val jsonConfig = Json { prettyPrint = true }

@Composable
fun MainScreen(
    preferenceStore: PreferenceStore,
) {
    val scope = rememberCoroutineScope()
    var text by preferenceStore.text.remember()
    var theme by preferenceStore.theme.remember()
    var num by preferenceStore.num.remember()
    var bool by preferenceStore.bool.remember()
    var animal by preferenceStore.customObject.remember()
    var duration by preferenceStore.duration.remember()
    var userProfile by preferenceStore.userProfile.remember()
    var animalSet by preferenceStore.animalSet.remember()
    var themeSet by preferenceStore.themeSet.remember()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
        item {
            StringSection(
                text = text,
                onTextChange = { text = it },
                onReset = { scope.launch { preferenceStore.text.resetToDefault() } },
            )
        }
        item {
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
            )
        }
        item {
            IntegerSection(
                num = num,
                onDecrement = { num -= 1 },
                onIncrement = { num += 1 },
                onReset = { scope.launch { preferenceStore.num.resetToDefault() } },
            )
        }
        item {
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
            )
        }
        item {
            BooleanSection(
                bool = bool,
                onBoolChange = { bool = it },
                onReset = { scope.launch { preferenceStore.bool.resetToDefault() } },
            )
        }
        item {
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
            )
        }
        item {
            EnumSection(
                theme = theme,
                onThemeChange = { theme = it },
                onReset = { scope.launch { preferenceStore.theme.resetToDefault() } },
            )
        }
        item {
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
            )
        }
        item {
            SerializerSection(
                animal = animal,
                onAnimalChange = { animal = it },
                onReset = { scope.launch { preferenceStore.customObject.resetToDefault() } },
            )
        }
        item {
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
            )
        }
        item {
            DurationSection(
                duration = duration,
                onUpdate = { duration = Clock.System.now() },
                onReset = { scope.launch { preferenceStore.duration.resetToDefault() } },
            )
        }
        item {
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
            )
        }
        item {
            KSerializedSection(
                userProfile = userProfile,
                onUserProfileChange = { userProfile = it },
                onReset = { scope.launch { preferenceStore.userProfile.resetToDefault() } },
            )
        }
        item {
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
            )
        }
        item {
            SerializedSetSection(
                animalSet = animalSet,
                onToggle = { entry -> scope.launch { preferenceStore.animalSet.toggle(entry) } },
                onReset = { scope.launch { preferenceStore.animalSet.resetToDefault() } },
            )
        }
        item {
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
            )
        }
        item {
            EnumSetSection(
                themeSet = themeSet,
                onToggle = { entry -> scope.launch { preferenceStore.themeSet.toggle(entry) } },
                onReset = { scope.launch { preferenceStore.themeSet.resetToDefault() } },
            )
        }
        item {
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
            )
        }
        item {
            ExportImportButtons(
                onExport = {
                    val data = preferenceStore.exportPreferences()
                    jsonConfig.encodeToString(data.toJsonElement())
                },
                onImport = { jsonString ->
                    val newData = jsonString.toJsonMap()
                    preferenceStore.importPreferences(newData)
                },
            )
        }
    }
}

@Composable
private fun StringSection(
    text: String,
    onTextChange: (String) -> Unit,
    onReset: () -> Unit,
) {
    Column {
        Text(
            "String",
            style = MaterialTheme.typography.headlineSmall,
        )
        OutlinedTextField(
            value = TextFieldValue(text, TextRange(text.length)),
            onValueChange = { onTextChange(it.text) },
            label = { Text("text") },
            modifier = Modifier.fillMaxWidth(),
        )
        TextButton(onClick = onReset) {
            Text("Reset to Default")
        }
    }
}

@Composable
private fun IntegerSection(
    num: Int,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit,
    onReset: () -> Unit,
) {
    Column {
        Text(
            "Integer",
            style = MaterialTheme.typography.headlineSmall,
        )
        ListItem(
            headlineContent = {
                Text(
                    "$num",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                )
            },
            leadingContent = {
                Button(onClick = onDecrement) {
                    Icon(Icons.Default.Remove, contentDescription = "Decrement")
                }
            },
            trailingContent = {
                Button(onClick = onIncrement) {
                    Icon(Icons.Default.Add, contentDescription = "Increment")
                }
            },
            modifier = Modifier.fillMaxWidth(),
        )
        TextButton(onClick = onReset) {
            Text("Reset to Default")
        }
    }
}

@Composable
private fun BooleanSection(
    bool: Boolean,
    onBoolChange: (Boolean) -> Unit,
    onReset: () -> Unit,
) {
    Column {
        Text(
            "Boolean",
            style = MaterialTheme.typography.headlineSmall,
        )
        Text("Current: $bool")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Button(
                onClick = { onBoolChange(true) },
                enabled = !bool,
                modifier = Modifier.weight(1f),
            ) {
                Text("true")
            }
            Button(
                onClick = { onBoolChange(false) },
                enabled = bool,
                modifier = Modifier.weight(1f),
            ) {
                Text("false")
            }
        }
        TextButton(onClick = onReset) {
            Text("Reset to Default")
        }
    }
}

@Composable
private fun EnumSection(
    theme: Theme,
    onThemeChange: (Theme) -> Unit,
    onReset: () -> Unit,
) {
    Column {
        Text(
            "Enum",
            style = MaterialTheme.typography.headlineSmall,
        )
        Theme.entries.forEach { entry ->
            ListItem(
                headlineContent = { Text(entry.name) },
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
                        onClick = { onThemeChange(entry) },
                    ),
            )
        }
        TextButton(onClick = onReset) {
            Text("Reset to Default")
        }
    }
}

@Composable
private fun SerializerSection(
    animal: Animal,
    onAnimalChange: (Animal) -> Unit,
    onReset: () -> Unit,
) {
    Column {
        Text(
            "Serializer",
            style = MaterialTheme.typography.headlineSmall,
        )
        Animal.entries.forEach { entry ->
            ListItem(
                headlineContent = { Text(entry.toString()) },
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
                        onClick = { onAnimalChange(entry) },
                    ),
            )
        }
        TextButton(onClick = onReset) {
            Text("Reset to Default")
        }
    }
}

@Composable
private fun DurationSection(
    duration: Instant,
    onUpdate: () -> Unit,
    onReset: () -> Unit,
) {
    Column {
        Text(
            "Duration",
            style = MaterialTheme.typography.headlineSmall,
        )
        Text(
            "$duration",
            style = MaterialTheme.typography.headlineSmall,
        )
        Button(
            onClick = onUpdate,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
        ) {
            Text(
                "Update Duration",
                style = MaterialTheme.typography.headlineSmall,
            )
        }
        TextButton(onClick = onReset) {
            Text("Reset to Default")
        }
    }
}

@Composable
private fun KSerializedSection(
    userProfile: UserProfile,
    onUserProfileChange: (UserProfile) -> Unit,
    onReset: () -> Unit,
) {
    Column {
        Text(
            "KSerialized",
            style = MaterialTheme.typography.headlineSmall,
        )
        OutlinedTextField(
            value = TextFieldValue(userProfile.name, TextRange(userProfile.name.length)),
            onValueChange = { onUserProfileChange(userProfile.copy(name = it.text)) },
            label = { Text("Name") },
            modifier = Modifier.fillMaxWidth(),
        )
        ListItem(
            headlineContent = {
                Text(
                    "${userProfile.age}",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                )
            },
            leadingContent = {
                Button(onClick = { onUserProfileChange(userProfile.copy(age = userProfile.age - 1)) }) {
                    Icon(Icons.Default.Remove, contentDescription = "Decrement Age")
                }
            },
            trailingContent = {
                Button(onClick = { onUserProfileChange(userProfile.copy(age = userProfile.age + 1)) }) {
                    Icon(Icons.Default.Add, contentDescription = "Increment Age")
                }
            },
            modifier = Modifier.fillMaxWidth(),
        )
        TextButton(onClick = onReset) {
            Text("Reset to Default")
        }
    }
}

@Composable
private fun SerializedSetSection(
    animalSet: Set<Animal>,
    onToggle: (Animal) -> Unit,
    onReset: () -> Unit,
) {
    Column {
        Text(
            "Serialized Set",
            style = MaterialTheme.typography.headlineSmall,
        )
        Text("Selected: ${animalSet.joinToString { it.name }}")
        Animal.entries.forEach { entry ->
            val isSelected = entry in animalSet
            ListItem(
                headlineContent = { Text(entry.toString()) },
                leadingContent = {
                    Checkbox(
                        checked = isSelected,
                        onCheckedChange = null,
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .toggleable(
                        value = isSelected,
                        onValueChange = { onToggle(entry) },
                    ),
            )
        }
        TextButton(onClick = onReset) {
            Text("Reset to Default")
        }
    }
}

@Composable
private fun EnumSetSection(
    themeSet: Set<Theme>,
    onToggle: (Theme) -> Unit,
    onReset: () -> Unit,
) {
    Column {
        Text(
            "Enum Set",
            style = MaterialTheme.typography.headlineSmall,
        )
        Text("Selected: ${themeSet.joinToString { it.name }}")
        Theme.entries.forEach { entry ->
            val isSelected = entry in themeSet
            ListItem(
                headlineContent = { Text(entry.name) },
                leadingContent = {
                    Checkbox(
                        checked = isSelected,
                        onCheckedChange = null,
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .toggleable(
                        value = isSelected,
                        onValueChange = { onToggle(entry) },
                    ),
            )
        }
        TextButton(onClick = onReset) {
            Text("Reset to Default")
        }
    }
}
