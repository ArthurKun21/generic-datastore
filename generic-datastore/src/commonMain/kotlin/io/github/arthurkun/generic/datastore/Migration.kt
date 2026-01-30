@file:Suppress("unused")

package io.github.arthurkun.generic.datastore

import kotlinx.serialization.json.JsonElement
import io.github.arthurkun.generic.datastore.core.toJsonElement as coreToJsonElement
import io.github.arthurkun.generic.datastore.core.toJsonMap as coreToJsonMap

/**
 * Extension for backwards compatibility.
 * @see io.github.arthurkun.generic.datastore.backup.PreferenceBackupCreator
 */
@Deprecated(
    message = "Use PreferenceBackupCreator for type-safe backup/restore",
    replaceWith = ReplaceWith(
        "PreferenceBackupCreator(datastore).createBackup()",
        "io.github.arthurkun.generic.datastore.backup.PreferenceBackupCreator",
    ),
    level = DeprecationLevel.ERROR,
)
@Suppress("DEPRECATION")
fun Any?.toJsonElement(): JsonElement = coreToJsonElement()

/**
 * Extension for backwards compatibility.
 * @see io.github.arthurkun.generic.datastore.backup.PreferenceBackupRestorer
 */
@Deprecated(
    message = "Use PreferenceBackupRestorer for type-safe backup/restore",
    replaceWith = ReplaceWith(
        "PreferenceBackupRestorer(datastore).restoreFromJson(jsonString)",
        "io.github.arthurkun.generic.datastore.backup.PreferenceBackupRestorer",
    ),
    level = DeprecationLevel.ERROR,
)
@Suppress("DEPRECATION")
fun String.toJsonMap(): Map<String, Any?> = coreToJsonMap()
