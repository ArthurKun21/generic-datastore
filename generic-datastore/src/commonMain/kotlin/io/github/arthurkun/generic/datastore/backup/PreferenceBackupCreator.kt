package io.github.arthurkun.generic.datastore.backup

import io.github.arthurkun.generic.datastore.core.Preference
import io.github.arthurkun.generic.datastore.preferences.PreferencesDatastore
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.floatOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.longOrNull

class PreferenceBackupCreator(
    private val preferenceDatastore: PreferencesDatastore,
) {

    suspend fun createBackup(
        includePrivatePreferences: Boolean = false,
        includeAppStatePreferences: Boolean = false,
    ): PreferencesBackup {
        val exportedData = preferenceDatastore.export(
            exportPrivate = includePrivatePreferences,
            exportAppState = includeAppStatePreferences,
        )

        val preferences = exportedData.toBackupPreferences()
            .withPrivatePreferences(includePrivatePreferences)
            .withAppStatePreferences(includeAppStatePreferences)

        return PreferencesBackup(preferences = preferences)
    }

    suspend fun createBackupJson(
        includePrivatePreferences: Boolean = false,
        includeAppStatePreferences: Boolean = false,
    ): String {
        return createBackup(
            includePrivatePreferences = includePrivatePreferences,
            includeAppStatePreferences = includeAppStatePreferences,
        ).toJson()
    }

    private fun Map<String, JsonElement>.toBackupPreferences(): List<BackupPreference> {
        return mapNotNull { (key, element) ->
            val value = element.toPreferenceValue() ?: return@mapNotNull null
            BackupPreference(key = key, value = value)
        }
    }

    private fun JsonElement.toPreferenceValue(): PreferenceValue? {
        return when (this) {
            is JsonPrimitive -> {
                when {
                    isString -> StringPreferenceValue(content)
                    booleanOrNull != null -> BooleanPreferenceValue(booleanOrNull!!)
                    intOrNull != null -> IntPreferenceValue(intOrNull!!)
                    longOrNull != null -> LongPreferenceValue(longOrNull!!)
                    floatOrNull != null -> FloatPreferenceValue(floatOrNull!!)
                    doubleOrNull != null -> DoublePreferenceValue(doubleOrNull!!)
                    else -> null
                }
            }
            is kotlinx.serialization.json.JsonArray -> {
                val stringSet = mapNotNull { elem ->
                    (elem as? JsonPrimitive)?.takeIf { it.isString }?.content
                }.toSet()
                StringSetPreferenceValue(stringSet)
            }
            else -> null
        }
    }

    private fun List<BackupPreference>.withPrivatePreferences(include: Boolean): List<BackupPreference> {
        return if (include) {
            this
        } else {
            filter { !Preference.isPrivate(it.key) }
        }
    }

    private fun List<BackupPreference>.withAppStatePreferences(include: Boolean): List<BackupPreference> {
        return if (include) {
            this
        } else {
            filter { !Preference.isAppState(it.key) }
        }
    }
}
