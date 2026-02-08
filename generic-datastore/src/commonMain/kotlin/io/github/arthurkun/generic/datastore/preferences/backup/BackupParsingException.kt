package io.github.arthurkun.generic.datastore.preferences.backup

/**
 * Thrown when a backup string cannot be parsed into a [PreferencesBackup].
 */
public class BackupParsingException(
    message: String,
    cause: Throwable? = null,
) : Exception(message, cause)
