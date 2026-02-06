package io.github.arthurkun.generic.datastore.compose.app.ui

import androidx.compose.runtime.Composable

/**
 * Platform-specific export/import handler for preferences.
 *
 * Each platform implements file picker and file write/read differently,
 * so this is declared as an expect composable that provides callbacks.
 */
@Composable
expect fun ExportImportButtons(
    onExport: suspend () -> String,
    onImport: suspend (String) -> Unit,
)
