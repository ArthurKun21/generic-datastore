package io.github.arthurkun.generic.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import io.github.arthurkun.generic.datastore.preferences.GenericPreferencesDatastore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.assertEquals

private const val TEST_DATASTORE_NAME = "kserialized_test_datastore"

@Serializable
data class UserSettings(
    val theme: String = "light",
    val notifications: Boolean = true,
    val fontSize: Int = 14,
)

@Serializable
data class ComplexObject(
    val id: Int,
    val name: String,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, String> = emptyMap(),
)

class KSerializedPreferenceTest {

    @TempDir
    lateinit var tempFolder: File

    private lateinit var dataStore: DataStore<Preferences>
    private lateinit var preferenceDatastore: GenericPreferencesDatastore
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var testScope: CoroutineScope

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        testScope = CoroutineScope(Job() + testDispatcher)
        dataStore = PreferenceDataStoreFactory.create(
            scope = testScope,
            produceFile = {
                File(tempFolder, "${TEST_DATASTORE_NAME}.preferences_pb")
            },
        )
        preferenceDatastore = GenericPreferencesDatastore(dataStore)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
        testScope.cancel()
    }

    @Test
    fun kserializedPreference_defaultValueWhenNotSet() = runTest(testDispatcher) {
        val defaultSettings = UserSettings()
        val pref = preferenceDatastore.kserialized(
            key = "user_settings_default",
            defaultValue = defaultSettings,
            serializer = UserSettings.serializer(),
        )
        assertEquals(defaultSettings, pref.get())
    }

    @Test
    fun kserializedPreference_setAndGetValue() = runTest(testDispatcher) {
        val defaultSettings = UserSettings()
        val newSettings = UserSettings(theme = "dark", notifications = false, fontSize = 18)
        val pref = preferenceDatastore.kserialized(
            key = "user_settings_set_get",
            defaultValue = defaultSettings,
            serializer = UserSettings.serializer(),
        )
        pref.set(newSettings)
        assertEquals(newSettings, pref.get())
    }

    @Test
    fun kserializedPreference_observeSetValue() = runTest(testDispatcher) {
        val defaultSettings = UserSettings()
        val newSettings = UserSettings(theme = "system", notifications = true, fontSize = 16)
        val pref = preferenceDatastore.kserialized(
            key = "user_settings_flow",
            defaultValue = defaultSettings,
            serializer = UserSettings.serializer(),
        )
        pref.set(newSettings)
        val value = pref.asFlow().first()
        assertEquals(newSettings, value)
    }

    @Test
    fun kserializedPreference_deleteValue() = runTest(testDispatcher) {
        val defaultSettings = UserSettings()
        val newSettings = UserSettings(theme = "dark", notifications = false, fontSize = 20)
        val pref = preferenceDatastore.kserialized(
            key = "user_settings_delete",
            defaultValue = defaultSettings,
            serializer = UserSettings.serializer(),
        )
        pref.set(newSettings)
        assertEquals(newSettings, pref.get())

        pref.delete()
        assertEquals(defaultSettings, pref.get())
    }

    @Test
    fun kserializedPreference_complexObjectWithCollections() = runTest(testDispatcher) {
        val defaultObj = ComplexObject(id = 0, name = "default")
        val complexObj = ComplexObject(
            id = 42,
            name = "Test Object",
            tags = listOf("tag1", "tag2", "tag3"),
            metadata = mapOf("key1" to "value1", "key2" to "value2"),
        )
        val pref = preferenceDatastore.kserialized(
            key = "complex_object",
            defaultValue = defaultObj,
            serializer = ComplexObject.serializer(),
        )
        pref.set(complexObj)
        assertEquals(complexObj, pref.get())
    }

    @Test
    fun kserializedPreference_handleCorruptedDataGracefully() = runTest(testDispatcher) {
        val defaultSettings = UserSettings()
        val pref = preferenceDatastore.kserialized(
            key = "corrupted_data_test",
            defaultValue = defaultSettings,
            serializer = UserSettings.serializer(),
        )

        val stringKey = stringPreferencesKey("corrupted_data_test")
        dataStore.edit { settings ->
            settings[stringKey] = "invalid json { not valid"
        }

        assertEquals(defaultSettings, pref.get())
    }

    @Test
    fun kserializedPreference_handleUnknownKeysGracefully() = runTest(testDispatcher) {
        val defaultSettings = UserSettings()
        val pref = preferenceDatastore.kserialized(
            key = "unknown_keys_test",
            defaultValue = defaultSettings,
            serializer = UserSettings.serializer(),
        )

        val stringKey = stringPreferencesKey("unknown_keys_test")
        dataStore.edit { settings ->
            settings[stringKey] = """{"theme":"dark","notifications":true,"fontSize":16,"unknownField":"ignored"}"""
        }

        val expected = UserSettings(theme = "dark", notifications = true, fontSize = 16)
        assertEquals(expected, pref.get())
    }

    @Test
    fun kserializedPreference_customJsonInstance() = runTest(testDispatcher) {
        val customJson = Json {
            ignoreUnknownKeys = false
            encodeDefaults = false
        }
        val defaultSettings = UserSettings()
        val newSettings = UserSettings(theme = "custom", notifications = false, fontSize = 12)

        val pref = preferenceDatastore.kserialized(
            key = "custom_json_test",
            defaultValue = defaultSettings,
            serializer = UserSettings.serializer(),
            json = customJson,
        )

        pref.set(newSettings)
        assertEquals(newSettings, pref.get())
    }

    @Test
    fun kserializedPreference_stateInScope() = runTest(testDispatcher) {
        val defaultSettings = UserSettings()
        val newSettings = UserSettings(theme = "dark", notifications = true, fontSize = 20)

        val pref = preferenceDatastore.kserialized(
            key = "state_in_test",
            defaultValue = defaultSettings,
            serializer = UserSettings.serializer(),
        )

        val stateFlow = pref.stateIn(testScope)
        assertEquals(defaultSettings, stateFlow.value)

        pref.set(newSettings)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(newSettings, stateFlow.value)
    }
}
