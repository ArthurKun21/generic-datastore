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
import kotlinx.coroutines.withContext
import java.awt.EventQueue
import java.io.File
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

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
                        val file = showFileChooserOnEDT { chooser ->
                            chooser.dialogTitle = "Export Preferences"
                            chooser.selectedFile = File("preferences.json")
                            chooser.fileFilter = FileNameExtensionFilter("JSON files", "json")
                            chooser.showSaveDialog(null)
                        }
                        file?.writeText(json)
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
                        val file = showFileChooserOnEDT { chooser ->
                            chooser.dialogTitle = "Import Preferences"
                            chooser.fileFilter = FileNameExtensionFilter("JSON files", "json")
                            chooser.showOpenDialog(null)
                        }
                        if (file != null) {
                            val jsonString = withContext(Dispatchers.IO) { file.readText() }
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

private suspend fun showFileChooserOnEDT(
    configure: (JFileChooser) -> Int,
): File? = suspendCoroutine { cont ->
    EventQueue.invokeLater {
        val chooser = JFileChooser()
        val result = configure(chooser)
        val file = if (result == JFileChooser.APPROVE_OPTION) chooser.selectedFile else null
        cont.resume(file)
    }
}
