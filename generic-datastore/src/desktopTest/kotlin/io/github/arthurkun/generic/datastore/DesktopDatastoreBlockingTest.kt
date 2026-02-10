package io.github.arthurkun.generic.datastore

import io.github.arthurkun.generic.datastore.preferences.GenericPreferencesDatastore
import io.github.arthurkun.generic.datastore.preferences.core.AbstractDatastoreBlockingTest
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.test.AfterTest
import kotlin.test.BeforeTest

private const val TEST_DATASTORE_BLOCKING_NAME = "test_datastore_blocking"

class DesktopDatastoreBlockingTest : AbstractDatastoreBlockingTest() {

    @TempDir
    lateinit var tempFolder: File

    private val helper = DesktopTestHelper.blocking(TEST_DATASTORE_BLOCKING_NAME)

    override val preferenceDatastore: GenericPreferencesDatastore get() = helper.preferenceDatastore

    @BeforeTest
    fun setup() = helper.setup(tempFolder.absolutePath)

    @AfterTest
    fun tearDown() = helper.tearDown()
}
