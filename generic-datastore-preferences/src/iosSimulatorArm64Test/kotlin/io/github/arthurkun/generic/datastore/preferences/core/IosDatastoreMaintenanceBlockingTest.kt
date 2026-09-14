package io.github.arthurkun.generic.datastore.preferences.core

import io.github.arthurkun.generic.datastore.IosTestHelper
import io.github.arthurkun.generic.datastore.preferences.GenericPreferencesDatastore
import kotlin.test.AfterTest
import kotlin.test.BeforeTest

class IosDatastoreMaintenanceBlockingTest : AbstractDatastoreMaintenanceBlockingTest() {

    private val helper = IosTestHelper.blocking("test_datastore_maintenance_blocking")

    override val preferenceDatastore: GenericPreferencesDatastore get() = helper.preferenceDatastore

    @BeforeTest
    fun setup() = helper.setup()

    @AfterTest
    fun tearDown() = helper.tearDown()
}
