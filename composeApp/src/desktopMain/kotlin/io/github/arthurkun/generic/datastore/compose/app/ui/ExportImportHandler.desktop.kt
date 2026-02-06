package io.github.arthurkun.generic.datastore.compose.app.ui

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

@Composable
actual fun ExportImportButtons(
    onExport: suspend () -> String,
    onImport: suspend (String) -> Unit,
) {
    val scope = rememberCoroutineScope()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Button(
            onClick = {
                scope.launch(Dispatchers.IO) {
                    try {
                        val json = onExport()
                        val chooser = JFileChooser().apply {
                            dialogTitle = "Export Preferences"
                            selectedFile = File("preferences.json")
                            fileFilter = FileNameExtensionFilter("JSON files", "json")
                        }
                        val result = chooser.showSaveDialog(null)
                        if (result == JFileChooser.APPROVE_OPTION) {
                            chooser.selectedFile.writeText(json)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
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
                scope.launch(Dispatchers.IO) {
                    try {
                        val chooser = JFileChooser().apply {
                            dialogTitle = "Import Preferences"
                            fileFilter = FileNameExtensionFilter("JSON files", "json")
                        }
                        val result = chooser.showOpenDialog(null)
                        if (result == JFileChooser.APPROVE_OPTION) {
                            val jsonString = chooser.selectedFile.readText()
                            onImport(jsonString)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
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
