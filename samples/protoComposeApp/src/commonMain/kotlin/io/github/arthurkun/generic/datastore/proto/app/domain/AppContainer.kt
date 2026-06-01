package io.github.arthurkun.generic.datastore.proto.app.domain

import io.github.arthurkun.generic.datastore.proto.ProtoDatastore
import io.github.arthurkun.generic.datastore.proto.app.wire.AppConfig
import io.github.arthurkun.generic.datastore.proto.app.wire.UserSettings

/**
 * Application-level container that creates and holds the Proto DataStore instances.
 *
 * One datastore per proto message type, each stored in a separate file under
 * a platform-specific application directory.
 */
expect class AppContainer {

    /**
     * Proto2 [UserSettings] DataStore.
     */
    val userSettingsDatastore: ProtoDatastore<UserSettings>

    /**
     * Proto3 [AppConfig] DataStore.
     */
    val appConfigDatastore: ProtoDatastore<AppConfig>
}
