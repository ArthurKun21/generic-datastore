package io.github.arthurkun.generic.datastore.proto.app.domain

import io.github.arthurkun.generic.datastore.proto.ProtoDatastore
import io.github.arthurkun.generic.datastore.proto.app.wire.AppConfig
import io.github.arthurkun.generic.datastore.proto.app.wire.UserSettings
import io.github.arthurkun.generic.datastore.proto.createProtoDatastore
import java.io.File

/**
 * Desktop (JVM) implementation of [AppContainer].
 *
 * Stores proto files under `~/.generic-datastore-proto-sample/`.
 */
actual class AppContainer {

    private val appDir: String by lazy {
        val dir = File(System.getProperty("user.home"), ".generic-datastore-proto-sample")
        dir.mkdirs()
        dir.absolutePath
    }

    actual val userSettingsDatastore: ProtoDatastore<UserSettings> = createProtoDatastore(
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

    actual val appConfigDatastore: ProtoDatastore<AppConfig> = createProtoDatastore(
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
