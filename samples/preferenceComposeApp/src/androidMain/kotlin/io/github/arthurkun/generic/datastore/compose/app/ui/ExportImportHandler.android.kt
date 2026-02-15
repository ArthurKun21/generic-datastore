package io.github.arthurkun.generic.datastore.compose.app.ui

import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
actual fun ExportImportButtons(
    onExport: suspend () -> String,
    onImport: suspend (String) -> Unit,
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument(),
    ) { uri ->
        uri?.let { selectedUri ->
            scope.launch(Dispatchers.IO) {
                try {
                    val jsonString = context
                        .contentResolver
                        .openInputStream(selectedUri)
                        ?.use { inputStream ->
                            inputStream.reader().readText()
                        }
                    jsonString?.let {
                        onImport(it)
                    }
                } catch (e: Exception) {
                    Log.e("ExportImport", "Error importing preferences", e)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            context,
                            "Error importing preferences",
                            Toast.LENGTH_SHORT,
                        ).show()
                    }
                }
            }
        }
    }

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json"),
    ) { uri ->
        uri?.let { selectedUri ->
            scope.launch(Dispatchers.IO) {
                try {
                    val json = onExport()
                    context.contentResolver
                        .openOutputStream(selectedUri)
                        ?.use {
                            it.write(json.toByteArray())
                        }
                } catch (e: Exception) {
                    Log.e("ExportImport", "Error exporting preferences", e)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            context,
                            "Error exporting preferences",
                            Toast.LENGTH_SHORT,
                        ).show()
                    }
                }
            }
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Button(
            onClick = {
                exportLauncher.launch("preferences.json")
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
                importLauncher.launch(
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
