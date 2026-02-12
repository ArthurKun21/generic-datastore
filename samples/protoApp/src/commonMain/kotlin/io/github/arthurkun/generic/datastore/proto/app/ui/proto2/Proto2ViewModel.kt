package io.github.arthurkun.generic.datastore.proto.app.ui.proto2

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.arthurkun.generic.datastore.proto.GenericProtoDatastore
import io.github.arthurkun.generic.datastore.proto.ProtoPreference
import io.github.arthurkun.generic.datastore.proto.app.wire.UserSettings
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel for the Proto2 (UserSettings) screen.
 *
 * Demonstrates all [ProtoDatastore] features:
 * - `data()` for whole-object operations
 * - `field()` for per-field access including nested fields
 * - `asFlow()`, `get()`, `set()`, `update()`, `delete()`, `resetToDefault()`
 * - `getBlocking()`, `setBlocking()`, `resetToDefaultBlocking()`
 * - `stateIn()` for StateFlow conversion
 * - Property delegation via `by`
 */
class Proto2ViewModel(
    private val datastore: GenericProtoDatastore<UserSettings>,
) : ViewModel() {

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

    val themePref: ProtoPreference<UserSettings.Theme?> = datastore.field(
        defaultValue = null,
        getter = { it.theme },
        updater = { proto, value -> proto.copy(theme = value) },
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
}
