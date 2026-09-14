package io.github.arthurkun.generic.datastore.preferences.core

import io.github.arthurkun.generic.datastore.DesktopTestHelper
import io.github.arthurkun.generic.datastore.preferences.GenericPreferencesDatastore
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.test.AfterTest
import kotlin.test.BeforeTest

class DesktopDatastoreMaintenanceBlockingTest : AbstractDatastoreMaintenanceBlockingTest() {

    @TempDir
    lateinit var tempFolder: File

    private val helper = DesktopTestHelper.blocking("test_datastore_maintenance_blocking")

    override val preferenceDatastore: GenericPreferencesDatastore get() = helper.preferenceDatastore

    @BeforeTest
    fun setup() = helper.setup(tempFolder.absolutePath)

    @AfterTest
    fun tearDown() = helper.tearDown()
}
