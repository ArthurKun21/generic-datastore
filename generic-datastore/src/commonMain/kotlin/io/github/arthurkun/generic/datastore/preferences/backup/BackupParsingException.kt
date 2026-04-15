package io.github.arthurkun.generic.datastore.preferences.backup

/**
 * Thrown when a backup payload cannot be decoded into a valid [PreferencesBackup].
 */
public class BackupParsingException(
    message: String,
    cause: Throwable? = null,
) : Exception(message, cause)
