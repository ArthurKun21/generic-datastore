package io.github.arthurkun.generic.datastore.proto.app.ui.proto2

import io.github.arthurkun.generic.datastore.proto.app.wire.UserSettings

/**
 * UI state for the Proto2 (UserSettings) screen.
 *
 * Each field mirrors a proto field, including nested [UserSettings.Address] fields.
 */
data class Proto2ScreenState(
    val username: String = "",
    val age: Int = 0,
    val darkMode: Boolean = false,
    val theme: UserSettings.Theme = UserSettings.Theme.SYSTEM,
    val street: String = "",
    val city: String = "",
    val zipCode: String = "",
)
