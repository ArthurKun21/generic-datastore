package io.github.arthurkun.generic.datastore.app.ui

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.arthurkun.generic.datastore.app.domain.Theme
import io.github.arthurkun.generic.datastore.app.domain.setAppCompatDelegateThemeMode

@Composable
fun MainScreen(
    vm: MainViewModel = viewModel(),
) {
    val text by vm.text.collectAsStateWithLifecycle()
    val theme by vm.theme.collectAsStateWithLifecycle()
    val num by vm.num.collectAsStateWithLifecycle()
    val bool by vm.bool.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        item {
            Text(
                "String",
                style = MaterialTheme.typography.headlineSmall
            )
        }
        item {
            OutlinedTextField(
                value = text,
                onValueChange = {
                    vm.updateText(it)
                },
                label = {
                    Text("text")
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
        item {
            Text(
                "Integer",
                style = MaterialTheme.typography.headlineSmall
            )
        }
        item {
            ListItem(
                headlineContent = {
                    Text(
                        "$num",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                },
                leadingContent = {
                    Button(onClick = {
                        vm.updateNum(num - 1)
                    }
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = "Decrement")
                    }
                },
                trailingContent = {
                    Button(onClick = {
                        vm.updateNum(num + 1)
                    }
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Increment")
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
        item {
            Text(
                "Boolean",
                style = MaterialTheme.typography.headlineSmall
            )
        }
        item {
            Text("Current: $bool")
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        vm.updateBool(true)
                    },
                    enabled = !bool,
                    modifier = Modifier
                        .weight(1f)
                ) {
                    Text("true")
                }
                Button(
                    onClick = {
                        vm.updateBool(false)
                    },
                    enabled = bool,
                    modifier = Modifier
                        .weight(1f)
                ) {
                    Text("false")
                }

            }
        }
        item {
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
        item {
            Text(
                "Enum",
                style = MaterialTheme.typography.headlineSmall
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
                        onClick = null
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = entry == theme,
                        onClick = {
                            vm.updateTheme(entry)
                            setAppCompatDelegateThemeMode(entry)
                        }
                    )
            )
        }
    }
}