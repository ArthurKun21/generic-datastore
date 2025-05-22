package io.github.arthurkun.generic.datastore.app.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.arthurkun.generic.datastore.app.domain.Theme

@Composable
fun MainScreen(
    vm: MainViewModel = viewModel(),
) {
    val theme by vm.theme.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
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
                        }
                    )
            )
        }
    }
}