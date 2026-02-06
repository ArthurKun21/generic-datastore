package io.github.arthurkun.generic.datastore.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import io.github.arthurkun.generic.datastore.backup.BackupPreference
import io.github.arthurkun.generic.datastore.backup.BooleanPreferenceValue
import io.github.arthurkun.generic.datastore.backup.DoublePreferenceValue
import io.github.arthurkun.generic.datastore.backup.FloatPreferenceValue
import io.github.arthurkun.generic.datastore.backup.IntPreferenceValue
import io.github.arthurkun.generic.datastore.backup.LongPreferenceValue
import io.github.arthurkun.generic.datastore.backup.PreferenceValue
import io.github.arthurkun.generic.datastore.backup.StringPreferenceValue
import io.github.arthurkun.generic.datastore.backup.StringSetPreferenceValue
import io.github.arthurkun.generic.datastore.core.Preference
import io.github.arthurkun.generic.datastore.core.Prefs
import io.github.arthurkun.generic.datastore.core.PrefsImpl
import kotlinx.coroutines.flow.first
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json

/**
 * A DataStore implementation that provides methods for creating and managing various types of preferences.
 *
 * This class wraps a [DataStore<Preferences>] instance and offers convenient functions
 * to define preferences for common data types like String, Long, Int, Float, Boolean,
 * and Set<String>, as well as custom serialized objects.
 *
 * @property datastore The underlying [DataStore<Preferences>] instance.
 */
@Suppress("unused")
class GenericPreferencesDatastore(
    private val datastore: DataStore<Preferences>,
) : PreferencesDatastore {

    /**
     * Creates a String preference.
     *
     * @param key The preference key.
     * @param defaultValue The default String value.
     * @return A [Prefs] instance for the String preference.
     */
    override fun string(key: String, defaultValue: String): Prefs<String> =
        PrefsImpl(
            StringPrimitive(
                datastore = datastore,
                key = key,
                defaultValue = defaultValue,
            ),
        )

    /**
     * Creates a Long preference.
     *
     * @param key The preference key.
     * @param defaultValue The default Long value.
     * @return A [Prefs] instance for the Long preference.
     */
    override fun long(key: String, defaultValue: Long): Prefs<Long> =
        PrefsImpl(
            LongPrimitive(
                datastore = datastore,
                key = key,
                defaultValue = defaultValue,
            ),
        )

    /**
     * Creates an Int preference.
     *
     * @param key The preference key.
     * @param defaultValue The default Int value.
     * @return A [Prefs] instance for the Int preference.
     */
    override fun int(key: String, defaultValue: Int): Prefs<Int> =
        PrefsImpl(
            IntPrimitive(
                datastore = datastore,
                key = key,
                defaultValue = defaultValue,
            ),
        )

    /**
     * Creates a Float preference.
     *
     * @param key The preference key.
     * @param defaultValue The default Float value.
     * @return A [Prefs] instance for the Float preference.
     */
    override fun float(key: String, defaultValue: Float): Prefs<Float> =
        PrefsImpl(
            FloatPrimitive(
                datastore = datastore,
                key = key,
                defaultValue = defaultValue,
            ),
        )

    /**
     * Creates a Boolean preference.
     *
     * @param key The preference key.
     * @param defaultValue The default Boolean value.
     * @return A [Prefs] instance for the Boolean preference.
     */
    override fun bool(key: String, defaultValue: Boolean): Prefs<Boolean> =
        PrefsImpl(
            BooleanPrimitive(
                datastore = datastore,
                key = key,
                defaultValue = defaultValue,
            ),
        )

    /**
     * Creates a Set<String> preference.
     *
     * @param key The preference key.
     * @param defaultValue The default Set<String> value.
     * @return A [Prefs] instance for the Set<String> preference.
     */
    override fun stringSet(
        key: String,
        defaultValue: Set<String>,
    ): Prefs<Set<String>> =
        PrefsImpl(
            StringSetPrimitive(
                datastore = datastore,
                key = key,
                defaultValue = defaultValue,
            ),
        )

    /**
     * Creates a preference for a custom object that can be serialized to and deserialized from a String.
     *
     * @param T The type of the custom object.
     * @param key The preference key.
     * @param defaultValue The default value for the custom object.
     * @param serializer A function to serialize the object to a String.
     * @param deserializer A function to deserialize the String back to the object.
     * @return A [Prefs] instance for the custom object preference.
     */
    override fun <T> serialized(
        key: String,
        defaultValue: T,
        serializer: (T) -> String,
        deserializer: (String) -> T,
    ): Prefs<T> = PrefsImpl(
        ObjectPrimitive(
            datastore = datastore,
            key = key,
            defaultValue = defaultValue,
            serializer = serializer,
            deserializer = deserializer,
        ),
    )

    /**
     * Creates a preference for a custom object using Kotlin Serialization.
     *
     * @param T The type of the custom object. Must be annotated with @Serializable.
     * @param key The preference key.
     * @param defaultValue The default value for the custom object.
     * @param serializer The [KSerializer] for the type [T].
     * @param json Optional custom [Json] instance for serialization.
     * @return A [Prefs] instance for the custom object preference.
     */
    override fun <T> kserialized(
        key: String,
        defaultValue: T,
        serializer: KSerializer<T>,
        json: Json,
    ): Prefs<T> = PrefsImpl(
        KSerializedPrimitive(
            datastore = datastore,
            key = key,
            defaultValue = defaultValue,
            serializer = serializer,
            json = json,
        ),
    )

    override suspend fun export(
        exportPrivate: Boolean,
        exportAppState: Boolean,
    ): List<BackupPreference> {
        return datastore
            .data
            .first()
            .toPreferences()
            .asMap()
            .mapNotNull { (key, value) ->
                if (!exportPrivate && Preference.isPrivate(key.name)) {
                    return@mapNotNull null
                }
                if (!exportAppState && Preference.isAppState(key.name)) {
                    return@mapNotNull null
                }
                val preferenceValue = value.toBackupPreferenceValue() ?: return@mapNotNull null
                BackupPreference(key = key.name, value = preferenceValue)
            }
    }

    override suspend fun import(backupPreferences: List<BackupPreference>) {
        datastore.updateData { currentPreferences ->
            val mutablePreferences = currentPreferences.toMutablePreferences()
            backupPreferences.forEach { backupPref ->
                val key = backupPref.key
                when (val value = backupPref.value) {
                    is IntPreferenceValue -> mutablePreferences[intPreferencesKey(key)] = value.value
                    is LongPreferenceValue -> mutablePreferences[longPreferencesKey(key)] = value.value
                    is FloatPreferenceValue -> mutablePreferences[floatPreferencesKey(key)] = value.value
                    is DoublePreferenceValue -> mutablePreferences[stringPreferencesKey(key)] = value.value.toString()
                    is StringPreferenceValue -> mutablePreferences[stringPreferencesKey(key)] = value.value
                    is BooleanPreferenceValue -> mutablePreferences[booleanPreferencesKey(key)] = value.value
                    is StringSetPreferenceValue -> mutablePreferences[stringSetPreferencesKey(key)] = value.value
                }
            }
            mutablePreferences.toPreferences()
        }
    }

    override suspend fun migrate(migration: suspend (MutablePreferences) -> Unit) {
        datastore.edit { mutablePreferences ->
            migration(mutablePreferences)
        }
    }

    private fun Any?.toBackupPreferenceValue(): PreferenceValue? {
        return PreferenceValue.fromAny(this)
    }
}
