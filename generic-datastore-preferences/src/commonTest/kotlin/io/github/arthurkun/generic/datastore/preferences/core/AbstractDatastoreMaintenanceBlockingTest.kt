package io.github.arthurkun.generic.datastore.preferences.core

import io.github.arthurkun.generic.datastore.core.BasePreference
import io.github.arthurkun.generic.datastore.preferences.GenericPreferencesDatastore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

abstract class AbstractDatastoreMaintenanceBlockingTest {

    abstract val preferenceDatastore: GenericPreferencesDatastore

    @Test
    fun blockingMirrors_matchSuspendingBehavior() {
        preferenceDatastore.string("blocking_key", "").setBlocking("stored")
        preferenceDatastore.string(BasePreference.privateKey("blocking_private"), "").setBlocking("secret")

        assertTrue(preferenceDatastore.containsBlocking("blocking_key"))
        assertEquals(
            setOf("blocking_key", BasePreference.privateKey("blocking_private")),
            preferenceDatastore.keysBlocking(),
        )

        preferenceDatastore.clearPrivateBlocking()
        assertFalse(preferenceDatastore.containsBlocking(BasePreference.privateKey("blocking_private")))

        preferenceDatastore.clearBlocking("blocking_")
        assertFalse(preferenceDatastore.containsBlocking("blocking_key"))

        preferenceDatastore.string(BasePreference.appStateKey("blocking_state"), "").setBlocking("x")
        preferenceDatastore.clearAppStateBlocking()
        assertFalse(preferenceDatastore.containsBlocking(BasePreference.appStateKey("blocking_state")))
    }
}
