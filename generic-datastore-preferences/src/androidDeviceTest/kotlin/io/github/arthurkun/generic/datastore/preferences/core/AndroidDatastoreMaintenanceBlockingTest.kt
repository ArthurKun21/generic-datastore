package io.github.arthurkun.generic.datastore.preferences.core

import androidx.test.ext.junit.runners.AndroidJUnit4
import io.github.arthurkun.generic.datastore.AndroidTestHelper
import io.github.arthurkun.generic.datastore.preferences.GenericPreferencesDatastore
import org.junit.After
import org.junit.Before
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AndroidDatastoreMaintenanceBlockingTest : AbstractDatastoreMaintenanceBlockingTest() {

    private val helper = AndroidTestHelper.blocking("test_datastore_maintenance_blocking")

    override val preferenceDatastore: GenericPreferencesDatastore get() = helper.preferenceDatastore

    @Before
    fun setup() = helper.setup()

    @After
    fun tearDown() = helper.tearDown()
}
