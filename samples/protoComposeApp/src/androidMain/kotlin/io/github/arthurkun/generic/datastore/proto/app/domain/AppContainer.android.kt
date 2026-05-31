package io.github.arthurkun.generic.datastore.proto.app.domain

import android.content.Context
import io.github.arthurkun.generic.datastore.proto.ProtoDatastore
import io.github.arthurkun.generic.datastore.proto.app.wire.AppConfig
import io.github.arthurkun.generic.datastore.proto.app.wire.UserSettings
import io.github.arthurkun.generic.datastore.proto.createProtoDatastore

actual class AppContainer(
    private val context: Context,
) {

    actual val userSettingsDatastore: ProtoDatastore<UserSettings> = createProtoDatastore(
        serializer = WireOkioSerializer(
            adapter = UserSettings.ADAPTER,
            defaultValue = UserSettings(),
        ),
        defaultValue = UserSettings(),
        key = "user_settings",
        producePath = {
            context.filesDir.resolve("user_settings.pb").absolutePath
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
            context.filesDir.resolve("app_config.pb").absolutePath
        },
    )
}
