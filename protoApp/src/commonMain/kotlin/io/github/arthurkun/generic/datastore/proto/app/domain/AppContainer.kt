package io.github.arthurkun.generic.datastore.proto.app.domain

import io.github.arthurkun.generic.datastore.proto.GenericProtoDatastore
import io.github.arthurkun.generic.datastore.proto.app.wire.AppConfig
import io.github.arthurkun.generic.datastore.proto.app.wire.UserSettings
import io.github.arthurkun.generic.datastore.proto.createProtoDatastore
import java.io.File

/**
 * Application-level container that creates and holds the Proto DataStore instances.
 *
 * One datastore per proto message type, each stored in a separate file under
 * `~/.generic-datastore-proto-sample/`.
 */
class AppContainer {

    private val appDir: String by lazy {
        val dir = File(System.getProperty("user.home"), ".generic-datastore-proto-sample")
        dir.mkdirs()
        dir.absolutePath
    }

    /**
     * Proto2 [UserSettings] DataStore.
     */
    val userSettingsDatastore: GenericProtoDatastore<UserSettings> = createProtoDatastore(
        serializer = WireOkioSerializer(
            adapter = UserSettings.ADAPTER,
            defaultValue = UserSettings(),
        ),
        defaultValue = UserSettings(),
        key = "user_settings",
        producePath = {
            File(appDir, "user_settings.pb").absolutePath
        },
    )

    /**
     * Proto3 [AppConfig] DataStore.
     */
    val appConfigDatastore: GenericProtoDatastore<AppConfig> = createProtoDatastore(
        serializer = WireOkioSerializer(
            adapter = AppConfig.ADAPTER,
            defaultValue = AppConfig(),
        ),
        defaultValue = AppConfig(),
        key = "app_config",
        producePath = {
            File(appDir, "app_config.pb").absolutePath
        },
    )
}
