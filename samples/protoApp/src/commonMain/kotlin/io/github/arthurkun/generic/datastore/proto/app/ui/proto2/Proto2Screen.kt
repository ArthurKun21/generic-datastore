package io.github.arthurkun.generic.datastore.proto.app.ui.proto2

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
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import io.github.arthurkun.generic.datastore.proto.app.wire.UserSettings
import io.github.arthurkun.generic.datastore.utils.collectAsStatePlatform

@Composable
fun Proto2Screen(
    viewModel: Proto2ViewModel,
) {
    val state by viewModel.uiState.collectAsStatePlatform()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        item {
            Text(
                text = "Proto2 — UserSettings",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 8.dp),
            )
        }

        // --- Username field ---
        item {
            UsernameSection(
                username = state.username,
                onUsernameChange = viewModel::setUsername,
            )
        }

        item { HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp)) }

        // --- Age field with increment (demonstrates update()) ---
        item {
            AgeSection(
                age = state.age,
                onAgeChange = viewModel::setAge,
                onIncrement = viewModel::incrementAge,
            )
        }

        item { HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp)) }

        // --- Dark mode toggle ---
        item {
            DarkModeSection(
                darkMode = state.darkMode,
                onToggle = viewModel::toggleDarkMode,
            )
        }

        item { HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp)) }

        // --- Theme enum selection ---
        item {
            ThemeSection(
                theme = state.theme,
                onThemeChange = viewModel::setTheme,
            )
        }

        item { HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp)) }

        // --- Nested Address fields ---
        item {
            AddressSection(
                street = state.street,
                city = state.city,
                zipCode = state.zipCode,
                onStreetChange = viewModel::setStreet,
                onCityChange = viewModel::setCity,
                onZipCodeChange = viewModel::setZipCode,
                onResetAddress = viewModel::resetAddress,
            )
        }

        item { HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp)) }

        // --- Whole-object operations ---
        item {
            WholeObjectSection(
                onResetAll = viewModel::resetAll,
                onDeleteAll = viewModel::deleteAll,
            )
        }
    }
}

@Composable
private fun UsernameSection(
    username: String,
    onUsernameChange: (String) -> Unit,
) {
    Column {
        Text("Username (field)", style = MaterialTheme.typography.titleSmall)
        OutlinedTextField(
            value = username,
            onValueChange = onUsernameChange,
            label = { Text("Username") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )
    }
}

@Composable
private fun AgeSection(
    age: Int,
    onAgeChange: (Int) -> Unit,
    onIncrement: () -> Unit,
) {
    Column {
        Text("Age (field + update)", style = MaterialTheme.typography.titleSmall)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OutlinedTextField(
                value = age.toString(),
                onValueChange = { it.toIntOrNull()?.let(onAgeChange) },
                label = { Text("Age") },
                modifier = Modifier.weight(1f),
                singleLine = true,
            )
            IconButton(onClick = onIncrement) {
                Icon(Icons.Filled.Add, contentDescription = "Increment age")
            }
        }
    }
}

@Composable
private fun DarkModeSection(
    darkMode: Boolean,
    onToggle: () -> Unit,
) {
    Column {
        Text("Dark Mode (data().update toggle)", style = MaterialTheme.typography.titleSmall)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .toggleable(
                    value = darkMode,
                    role = Role.Checkbox,
                    onValueChange = { onToggle() },
                ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Checkbox(checked = darkMode, onCheckedChange = null)
            Text(
                text = if (darkMode) "Dark Mode ON" else "Dark Mode OFF",
                modifier = Modifier.padding(start = 8.dp),
            )
        }
    }
}

@Composable
private fun ThemeSection(
    theme: UserSettings.Theme,
    onThemeChange: (UserSettings.Theme) -> Unit,
) {
    Column {
        Text("Theme (field enum)", style = MaterialTheme.typography.titleSmall)
        UserSettings.Theme.entries.forEach { entry ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = theme == entry,
                        role = Role.RadioButton,
                        onClick = { onThemeChange(entry) },
                    ),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RadioButton(selected = theme == entry, onClick = null)
                Text(
                    text = entry.name,
                    modifier = Modifier.padding(start = 8.dp),
                )
            }
        }
    }
}

@Composable
private fun AddressSection(
    street: String,
    city: String,
    zipCode: String,
    onStreetChange: (String) -> Unit,
    onCityChange: (String) -> Unit,
    onZipCodeChange: (String) -> Unit,
    onResetAddress: () -> Unit,
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Address (nested field())", style = MaterialTheme.typography.titleSmall)
            IconButton(onClick = onResetAddress) {
                Icon(Icons.Filled.Delete, contentDescription = "Reset address")
            }
        }
        OutlinedTextField(
            value = street,
            onValueChange = onStreetChange,
            label = { Text("Street") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )
        OutlinedTextField(
            value = city,
            onValueChange = onCityChange,
            label = { Text("City") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )
        OutlinedTextField(
            value = zipCode,
            onValueChange = onZipCodeChange,
            label = { Text("Zip Code") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )
    }
}

@Composable
private fun WholeObjectSection(
    onResetAll: () -> Unit,
    onDeleteAll: () -> Unit,
) {
    Column {
        Text("Whole-Object Operations", style = MaterialTheme.typography.titleSmall)
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
    }
}
