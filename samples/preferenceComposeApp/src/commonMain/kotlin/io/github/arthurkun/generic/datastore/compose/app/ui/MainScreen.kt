package io.github.arthurkun.generic.datastore.compose.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.github.arthurkun.generic.datastore.batch.rememberPreferences
import io.github.arthurkun.generic.datastore.compose.app.domain.Animal
import io.github.arthurkun.generic.datastore.compose.app.domain.FontScale
import io.github.arthurkun.generic.datastore.compose.app.domain.Latitude
import io.github.arthurkun.generic.datastore.compose.app.domain.SessionTimeout
import io.github.arthurkun.generic.datastore.compose.app.domain.Theme
import io.github.arthurkun.generic.datastore.compose.app.domain.UserProfile
import io.github.arthurkun.generic.datastore.compose.app.domain.Volume
import io.github.arthurkun.generic.datastore.remember
import io.github.arthurkun.generic.datastore.utils.collectAsStatePlatform

@Composable
fun MainScreen(
    viewModel: MainViewModel,
) {
    var text by viewModel.text.remember()
    var theme by viewModel.theme.remember()
    var num by viewModel.num.remember()
    var bool by viewModel.bool.remember()
    var animal by viewModel.customObject.remember()
    var fontScale by viewModel.fontScale.remember()
    var sessionTimeout by viewModel.sessionTimeout.remember()
    var volume by viewModel.volume.remember()
    var latitude by viewModel.latitude.remember()
    var nullableSessionTimeout by viewModel.nullableSessionTimeout.remember()
    var userProfile by viewModel.userProfile.remember()
    var userProfileSet by viewModel.userProfileSet.remember()
    var animalSet by viewModel.animalSet.remember()
    var themeSet by viewModel.themeSet.remember()
    val apiCoverageStatus by viewModel.apiCoverageStatus.collectAsStatePlatform()

    val (batchText, batchNum, batchBool) = rememberPreferences(
        viewModel.text,
        viewModel.num,
        viewModel.bool,
    )
    var bText by batchText
    var bNum by batchNum
    var bBool by batchBool

    Scaffold { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp),
        ) {
            item {
                BatchRememberSection(
                    text = bText,
                    onTextChange = { bText = it },
                    num = bNum,
                    onDecrement = { bNum -= 1 },
                    onIncrement = { bNum += 1 },
                    bool = bBool,
                    onBoolChange = { bBool = it },
                    onClickRandomize = viewModel::randomize,
                )
            }
            item {
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                )
            }
            item {
                StringSection(
                    text = text,
                    onTextChange = { text = it },
                    onReset = { viewModel.resetText() },
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
                    onReset = { viewModel.resetNum() },
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
                    onReset = { viewModel.resetBool() },
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
                    onReset = { viewModel.resetTheme() },
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
                    onReset = { viewModel.resetCustomObject() },
                )
            }
            item {
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                )
            }
            item {
                SerializedAsPrimitivesSection(
                    fontScale = fontScale,
                    onFontScaleChange = { fontScale = it },
                    sessionTimeout = sessionTimeout,
                    onSessionTimeoutChange = { sessionTimeout = it },
                    nullableSessionTimeout = nullableSessionTimeout,
                    onNullableSessionTimeoutChange = { nullableSessionTimeout = it },
                    volume = volume,
                    onVolumeChange = { volume = it },
                    latitude = latitude,
                    onLatitudeChange = { latitude = it },
                    onReset = { viewModel.resetNumericSettings() },
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
                    onReset = { viewModel.resetUserProfile() },
                )
            }
            item {
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                )
            }
            item {
                KSerializedSetSection(
                    userProfileSet = userProfileSet,
                    onAdd = { profile -> viewModel.addUserProfile(profile) },
                    onRemove = { profile -> viewModel.removeUserProfile(profile) },
                    onReset = { viewModel.resetUserProfileSet() },
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
                    onToggle = { entry -> viewModel.toggleAnimal(entry) },
                    onReset = { viewModel.resetAnimalSet() },
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
                    onToggle = { entry -> viewModel.toggleTheme(entry) },
                    onReset = { viewModel.resetThemeSet() },
                )
            }
            item {
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                )
            }
            item {
                ExportImportButtons(
                    onExport = { viewModel.exportPreferences() },
                    onImport = { jsonString -> viewModel.importPreferences(jsonString) },
                )
            }
            item {
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                )
            }
            item {
                ApiCoverageSection(
                    status = apiCoverageStatus,
                    onRun = viewModel::runApiCoverageShowcase,
                )
            }
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
private fun SerializedAsPrimitivesSection(
    fontScale: FontScale,
    onFontScaleChange: (FontScale) -> Unit,
    sessionTimeout: SessionTimeout,
    onSessionTimeoutChange: (SessionTimeout) -> Unit,
    nullableSessionTimeout: SessionTimeout?,
    onNullableSessionTimeoutChange: (SessionTimeout?) -> Unit,
    volume: Volume,
    onVolumeChange: (Volume) -> Unit,
    latitude: Latitude,
    onLatitudeChange: (Latitude) -> Unit,
    onReset: () -> Unit,
) {
    Column {
        Text(
            "Serialized as Primitives",
            style = MaterialTheme.typography.headlineSmall,
        )
        Text(
            "Custom values stored under int/long/float/double keys instead of a string entry.",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(bottom = 8.dp),
        )
        StepperRow(
            label = "serializedAsInt · FontScale (%)",
            value = "${fontScale.percent}",
            onDecrement = { onFontScaleChange(FontScale(fontScale.percent - 10)) },
            onIncrement = { onFontScaleChange(FontScale(fontScale.percent + 10)) },
        )
        StepperRow(
            label = "serializedAsLong · SessionTimeout (ms)",
            value = "${sessionTimeout.millis}",
            onDecrement = { onSessionTimeoutChange(SessionTimeout(sessionTimeout.millis - 5_000L)) },
            onIncrement = { onSessionTimeoutChange(SessionTimeout(sessionTimeout.millis + 5_000L)) },
        )
        Text(
            "nullableSerializedAsLong · ${nullableSessionTimeout?.millis ?: "null"}",
            modifier = Modifier.padding(top = 8.dp),
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Button(
                onClick = { onNullableSessionTimeoutChange(SessionTimeout(15_000L)) },
                modifier = Modifier.weight(1f),
            ) {
                Text("Set 15000")
            }
            Button(
                onClick = { onNullableSessionTimeoutChange(null) },
                modifier = Modifier.weight(1f),
            ) {
                Text("Clear")
            }
        }
        StepperRow(
            label = "serializedAsFloat · Volume",
            value = "${volume.level}",
            onDecrement = { onVolumeChange(Volume(volume.level - 0.1f)) },
            onIncrement = { onVolumeChange(Volume(volume.level + 0.1f)) },
        )
        StepperRow(
            label = "serializedAsDouble · Latitude (°)",
            value = "${latitude.degrees}",
            onDecrement = { onLatitudeChange(Latitude(latitude.degrees - 0.5)) },
            onIncrement = { onLatitudeChange(Latitude(latitude.degrees + 0.5)) },
        )
        TextButton(onClick = onReset) {
            Text("Reset to Default")
        }
    }
}

@Composable
private fun StepperRow(
    label: String,
    value: String,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit,
) {
    Column {
        Text(label)
        ListItem(
            headlineContent = {
                Text(
                    value,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                )
            },
            leadingContent = {
                Button(onClick = onDecrement) {
                    Icon(Icons.Default.Remove, contentDescription = "Decrement $label")
                }
            },
            trailingContent = {
                Button(onClick = onIncrement) {
                    Icon(Icons.Default.Add, contentDescription = "Increment $label")
                }
            },
            modifier = Modifier.fillMaxWidth(),
        )
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
private fun KSerializedSetSection(
    userProfileSet: Set<UserProfile>,
    onAdd: (UserProfile) -> Unit,
    onRemove: (UserProfile) -> Unit,
    onReset: () -> Unit,
) {
    Column {
        Text(
            "KSerialized Set",
            style = MaterialTheme.typography.headlineSmall,
        )
        Text("Count: ${userProfileSet.size}")
        userProfileSet.forEach { profile ->
            ListItem(
                headlineContent = { Text(profile.name) },
                supportingContent = { Text("Age: ${profile.age}") },
                trailingContent = {
                    Button(onClick = { onRemove(profile) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Remove")
                    }
                },
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Button(
                onClick = {
                    onAdd(
                        UserProfile(
                            name = "User ${userProfileSet.size + 1}",
                            age = (18..65).random(),
                        ),
                    )
                },
                modifier = Modifier.weight(1f),
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add")
                Text(" Add Profile")
            }
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

@Composable
private fun BatchRememberSection(
    text: String,
    onTextChange: (String) -> Unit,
    num: Int,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit,
    bool: Boolean,
    onBoolChange: (Boolean) -> Unit,
    onClickRandomize: () -> Unit,
) {
    Column {
        Text(
            "Batch Remember (rememberPreferences)",
            style = MaterialTheme.typography.headlineSmall,
        )
        Text(
            "All three preferences below share a single DataStore observation via rememberPreferences.",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(bottom = 8.dp),
        )
        OutlinedTextField(
            value = TextFieldValue(text, TextRange(text.length)),
            onValueChange = { onTextChange(it.text) },
            label = { Text("Batch Text") },
            modifier = Modifier.fillMaxWidth(),
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
        ListItem(
            headlineContent = {
                Text(
                    "Batch Boolean: $bool",
                    modifier = Modifier.padding(start = 8.dp),
                )
            },
            leadingContent = {
                Checkbox(
                    checked = bool,
                    onCheckedChange = null,
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .toggleable(
                    value = bool,
                    onValueChange = onBoolChange,
                ),
        )

        Button(
            onClick = onClickRandomize,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Randomize All")
        }
    }
}

@Composable
private fun ApiCoverageSection(
    status: String,
    onRun: () -> Unit,
) {
    Column {
        Text(
            "API Coverage",
            style = MaterialTheme.typography.headlineSmall,
        )
        Text(status)
        Button(
            onClick = onRun,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Run Preferences API Showcase")
        }
    }
}
