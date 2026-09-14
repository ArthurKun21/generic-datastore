package io.github.arthurkun.generic.datastore.preferences.core

import io.github.arthurkun.generic.datastore.DesktopTestHelper
import io.github.arthurkun.generic.datastore.preferences.GenericPreferencesDatastore
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.test.AfterTest
import kotlin.test.BeforeTest

private const val TEST_BYTES_BLOCKING_DATASTORE_NAME = "test_bytes_blocking_datastore"

class DesktopBytesBlockingTest : AbstractBytesBlockingTest() {

    @TempDir
    lateinit var tempFolder: File

    private val helper = DesktopTestHelper.blocking(TEST_BYTES_BLOCKING_DATASTORE_NAME)

    override val preferenceDatastore: GenericPreferencesDatastore get() = helper.preferenceDatastore

    @BeforeTest
    fun setup() = helper.setup(tempFolder.absolutePath)

    @AfterTest
    fun tearDown() = helper.tearDown()
}
