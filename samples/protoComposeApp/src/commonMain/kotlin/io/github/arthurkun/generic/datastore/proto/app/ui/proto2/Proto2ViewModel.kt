package io.github.arthurkun.generic.datastore.proto.app.ui.proto2

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.arthurkun.generic.datastore.proto.ProtoDatastore
import io.github.arthurkun.generic.datastore.proto.ProtoPreference
import io.github.arthurkun.generic.datastore.proto.app.domain.ProtoApiItem
import io.github.arthurkun.generic.datastore.proto.app.wire.UserSettings
import io.github.arthurkun.generic.datastore.proto.enumField
import io.github.arthurkun.generic.datastore.proto.enumSetField
import io.github.arthurkun.generic.datastore.proto.kserializedField
import io.github.arthurkun.generic.datastore.proto.kserializedListField
import io.github.arthurkun.generic.datastore.proto.kserializedSetField
import io.github.arthurkun.generic.datastore.proto.nullableKserializedField
import io.github.arthurkun.generic.datastore.proto.nullableKserializedListField
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.readBytes
import io.github.vinceglb.filekit.write
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.coroutines.cancellation.CancellationException

/**
 * ViewModel for the Proto2 (UserSettings) screen.
 *
 * Demonstrates all [ProtoDatastore] features:
 * - `data()` for whole-object operations
 * - `field()` / `nullableEnumField()` for per-field access including nested fields
 * - `asFlow()`, `get()`, `set()`, `update()`, `delete()`, `resetToDefault()`
 * - `getBlocking()`, `setBlocking()`, `resetToDefaultBlocking()`
 * - `stateIn()` for StateFlow conversion
 * - Property delegation via `by`
 */
class Proto2ViewModel(
    private val datastore: ProtoDatastore<UserSettings>,
) : ViewModel() {

    private val jsonConfig = Json { ignoreUnknownKeys = true }
    private val _apiCoverageStatus = MutableStateFlow("Not run")
    val apiCoverageStatus: StateFlow<String> = _apiCoverageStatus.asStateFlow()
    private val _backupStatus = MutableStateFlow("Ready")
    val backupStatus: StateFlow<String> = _backupStatus.asStateFlow()

    /** Whole-object preference via `data()` */
    val wholeData: ProtoPreference<UserSettings> = datastore.data()

    /** Individual field preferences via `field()` */
    val usernamePref: ProtoPreference<String?> = datastore.field(
        defaultValue = null,
        getter = { it.username },
        updater = { proto, value -> proto.copy(username = value) },
    )

    val agePref: ProtoPreference<Int?> = datastore.field(
        defaultValue = null,
        getter = { it.age },
        updater = { proto, value -> proto.copy(age = value) },
    )

    val darkModePref: ProtoPreference<Boolean?> = datastore.field(
        defaultValue = null,
        getter = { it.dark_mode },
        updater = { proto, value -> proto.copy(dark_mode = value) },
    )

    val themePref: ProtoPreference<UserSettings.Theme?> = datastore.nullableEnumField(
        enumValues = UserSettings.Theme.entries.toTypedArray(),
        getter = { it.theme?.name },
        updater = { proto, value ->
            proto.copy(
                theme = value?.let { raw ->
                    UserSettings.Theme.entries.firstOrNull { entry -> entry.name == raw }
                },
            )
        },
    )

    /** Nested field: address object */
    val addressPref: ProtoPreference<UserSettings.Address?> = datastore.field(
        defaultValue = null,
        getter = { it.address },
        updater = { proto, value -> proto.copy(address = value) },
    )

    /** Deeply nested field: address.street via copy() chain */
    val streetPref: ProtoPreference<String?> = datastore.field(
        defaultValue = null,
        getter = { it.address?.street },
        updater = { proto, value ->
            proto.copy(
                address = (proto.address ?: UserSettings.Address()).copy(street = value),
            )
        },
    )

    val cityPref: ProtoPreference<String?> = datastore.field(
        defaultValue = null,
        getter = { it.address?.city },
        updater = { proto, value ->
            proto.copy(
                address = (proto.address ?: UserSettings.Address()).copy(city = value),
            )
        },
    )

    val zipCodePref: ProtoPreference<String?> = datastore.field(
        defaultValue = null,
        getter = { it.address?.zip_code },
        updater = { proto, value ->
            proto.copy(
                address = (proto.address ?: UserSettings.Address()).copy(zip_code = value),
            )
        },
    )

    /** stateIn() demo — converts the username flow to a StateFlow */
    val usernameState: StateFlow<String?> = usernamePref.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
    )

    /** Combine all field flows into a single UI state */
    val uiState: StateFlow<Proto2ScreenState> = combine(
        usernamePref.asFlow(),
        agePref.asFlow(),
        darkModePref.asFlow(),
        themePref.asFlow(),
        addressPref.asFlow(),
    ) { username, age, darkMode, theme, address ->
        Proto2ScreenState(
            username = username ?: "",
            age = age ?: 0,
            darkMode = darkMode ?: false,
            theme = theme ?: UserSettings.Theme.SYSTEM,
            street = address?.street ?: "",
            city = address?.city ?: "",
            zipCode = address?.zip_code ?: "",
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = Proto2ScreenState(),
    )

    // --- Whole-object operations ---

    /** Demonstrates `data().set()` — replaces the entire proto */
    fun setWholeObject(settings: UserSettings) {
        viewModelScope.launch { wholeData.set(settings) }
    }

    /** Demonstrates `data().resetToDefault()` — resets entire proto to default */
    fun resetAll() {
        viewModelScope.launch { wholeData.resetToDefault() }
    }

    /** Demonstrates `data().delete()` — same as resetToDefault for proto */
    fun deleteAll() {
        viewModelScope.launch { wholeData.delete() }
    }

    // --- Per-field operations ---

    fun setUsername(value: String) {
        viewModelScope.launch { usernamePref.set(value) }
    }

    fun setAge(value: Int) {
        viewModelScope.launch { agePref.set(value) }
    }

    /** Demonstrates `update()` — atomic read-modify-write on a field */
    fun incrementAge() {
        viewModelScope.launch {
            agePref.update { current -> (current ?: 0) + 1 }
        }
    }

    fun setDarkMode(value: Boolean) {
        viewModelScope.launch { darkModePref.set(value) }
    }

    fun setTheme(value: UserSettings.Theme) {
        viewModelScope.launch { themePref.set(value) }
    }

    fun setStreet(value: String) {
        viewModelScope.launch { streetPref.set(value) }
    }

    fun setCity(value: String) {
        viewModelScope.launch { cityPref.set(value) }
    }

    fun setZipCode(value: String) {
        viewModelScope.launch { zipCodePref.set(value) }
    }

    /** Demonstrates `field().resetToDefault()` on a nested field */
    fun resetAddress() {
        viewModelScope.launch { addressPref.resetToDefault() }
    }

    /** Demonstrates `data().update()` — atomic read-modify-write on whole proto */
    fun toggleDarkMode() {
        viewModelScope.launch {
            wholeData.update { current ->
                current.copy(dark_mode = !(current.dark_mode ?: false))
            }
        }
    }

    // --- Blocking operations (for demonstration) ---

    /** Demonstrates `getBlocking()` */
    fun getUsernameBlocking(): String? = usernamePref.getBlocking()

    /** Demonstrates `setBlocking()` */
    fun setUsernameBlocking(value: String) {
        usernamePref.setBlocking(value)
    }

    /** Demonstrates `resetToDefaultBlocking()` */
    fun resetUsernameBlocking() {
        usernamePref.resetToDefaultBlocking()
    }

    fun exportBackupTo(destination: PlatformFile) {
        viewModelScope.launch {
            _backupStatus.value = "Exporting"
            try {
                val bytes = datastore.exportAsByteArray()
                destination.write(bytes)
                _backupStatus.value = "Exported ${bytes.size} bytes"
            } catch (failure: CancellationException) {
                throw failure
            } catch (failure: Exception) {
                _backupStatus.value = "Export failed: ${failure.message ?: "Unknown error"}"
            }
        }
    }

    fun restoreBackupFrom(source: PlatformFile) {
        viewModelScope.launch {
            _backupStatus.value = "Restoring"
            try {
                val bytes = source.readBytes()
                datastore.importFromByteArray(bytes)
                _backupStatus.value = "Restored ${bytes.size} bytes"
            } catch (failure: CancellationException) {
                throw failure
            } catch (failure: Exception) {
                _backupStatus.value = "Restore failed: ${failure.message ?: "Unknown error"}"
            }
        }
    }

    fun cancelBackupAction(action: String) {
        _backupStatus.value = "$action cancelled"
    }

    fun runApiCoverageShowcase() {
        viewModelScope.launch {
            _apiCoverageStatus.value = "Running"
            _apiCoverageStatus.value = try {
                runApiCoverageShowcaseInternal()
            } catch (failure: CancellationException) {
                throw failure
            } catch (failure: Exception) {
                "Failed: ${failure.message}"
            }
        }
    }

    private suspend fun runApiCoverageShowcaseInternal(): String {
        val snapshot = wholeData.get()
        return try {
            val rawFieldPref = datastore.field(
                defaultValue = "",
                getter = { it.json_raw ?: "" },
                updater = { proto, raw -> proto.copy(json_raw = raw) },
            )
            val enumPref = datastore.enumField(
                defaultValue = UserSettings.Theme.SYSTEM,
                getter = { it.theme_raw ?: "" },
                updater = { proto, raw -> proto.copy(theme_raw = raw) },
            )
            val enumSetPref = datastore.enumSetField(
                defaultValue = emptySet<UserSettings.Theme>(),
                getter = { it.theme_set_raw.toSet() },
                updater = { proto, raw -> proto.copy(theme_set_raw = raw.toList()) },
            )
            val kserializedPref = datastore.kserializedField(
                defaultValue = ProtoApiItem(),
                getter = { it.json_raw ?: "" },
                updater = { proto, raw -> proto.copy(json_raw = raw) },
            )
            val nullableKserializedPref: ProtoPreference<ProtoApiItem?> = datastore.nullableKserializedField(
                getter = { it.nullable_json_raw },
                updater = { proto, raw -> proto.copy(nullable_json_raw = raw) },
            )
            val kserializedListPref = datastore.kserializedListField(
                defaultValue = emptyList<ProtoApiItem>(),
                getter = { it.json_list_raw ?: "" },
                updater = { proto, raw -> proto.copy(json_list_raw = raw) },
            )
            val nullableKserializedListPref: ProtoPreference<List<ProtoApiItem>?> =
                datastore.nullableKserializedListField(
                    getter = { it.nullable_json_list_raw },
                    updater = { proto, raw -> proto.copy(nullable_json_list_raw = raw) },
                )
            val kserializedSetPref = datastore.kserializedSetField(
                defaultValue = emptySet<ProtoApiItem>(),
                getter = { it.json_set_raw.toSet() },
                updater = { proto, raw -> proto.copy(json_set_raw = raw.toList()) },
            )
            val itemSerializer: (ProtoApiItem) -> String = { jsonConfig.encodeToString(it) }
            val itemDeserializer: (String) -> ProtoApiItem = { jsonConfig.decodeFromString(it) }
            val serializedPref = datastore.serializedField(
                defaultValue = ProtoApiItem(),
                serializer = itemSerializer,
                deserializer = itemDeserializer,
                getter = { it.json_raw ?: "" },
                updater = { proto, raw -> proto.copy(json_raw = raw) },
            )
            val nullableSerializedPref = datastore.nullableSerializedField(
                serializer = itemSerializer,
                deserializer = itemDeserializer,
                getter = { it.nullable_json_raw },
                updater = { proto, raw -> proto.copy(nullable_json_raw = raw) },
            )
            val serializedListPref = datastore.serializedListField(
                defaultValue = emptyList<ProtoApiItem>(),
                elementSerializer = itemSerializer,
                elementDeserializer = itemDeserializer,
                getter = { it.json_list_raw ?: "" },
                updater = { proto, raw -> proto.copy(json_list_raw = raw) },
            )
            val nullableSerializedListPref = datastore.nullableSerializedListField(
                elementSerializer = itemSerializer,
                elementDeserializer = itemDeserializer,
                getter = { it.nullable_json_list_raw },
                updater = { proto, raw -> proto.copy(nullable_json_list_raw = raw) },
            )
            val serializedSetPref = datastore.serializedSetField(
                defaultValue = emptySet<ProtoApiItem>(),
                serializer = itemSerializer,
                deserializer = itemDeserializer,
                getter = { it.json_set_raw.toSet() },
                updater = { proto, raw -> proto.copy(json_set_raw = raw.toList()) },
            )

            rawFieldPref.set("raw")
            enumPref.set(UserSettings.Theme.LIGHT)
            themePref.set(UserSettings.Theme.DARK)
            enumSetPref.set(setOf(UserSettings.Theme.SYSTEM, UserSettings.Theme.DARK))
            kserializedPref.set(ProtoApiItem("kserialized", 1))
            nullableKserializedPref.set(ProtoApiItem("nullableKserialized", 2))
            kserializedListPref.set(listOf(ProtoApiItem("kserializedList", 3)))
            nullableKserializedListPref.set(listOf(ProtoApiItem("nullableKserializedList", 4)))
            kserializedSetPref.set(setOf(ProtoApiItem("kserializedSet", 5)))
            serializedPref.set(ProtoApiItem("serialized", 6))
            nullableSerializedPref.set(ProtoApiItem("nullableSerialized", 7))
            serializedListPref.set(listOf(ProtoApiItem("serializedList", 8)))
            nullableSerializedListPref.set(listOf(ProtoApiItem("nullableSerializedList", 9)))
            serializedSetPref.set(setOf(ProtoApiItem("serializedSet", 10)))

            val covered = listOf(
                wholeData.get(),
                rawFieldPref.get(),
                enumPref.get(),
                themePref.get(),
                enumSetPref.get(),
                kserializedPref.get(),
                nullableKserializedPref.get(),
                kserializedListPref.get(),
                nullableKserializedListPref.get(),
                kserializedSetPref.get(),
                serializedPref.get(),
                nullableSerializedPref.get(),
                serializedListPref.get(),
                nullableSerializedListPref.get(),
                serializedSetPref.get(),
            ).size
            wholeData.set(snapshot)
            "Proto APIs covered: $covered"
        } catch (failure: Exception) {
            wholeData.set(snapshot)
            throw failure
        }
    }
}
