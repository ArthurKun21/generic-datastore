package io.github.arthurkun.generic.datastore.preferences.optional

import io.github.arthurkun.generic.datastore.DesktopTestHelper
import io.github.arthurkun.generic.datastore.preferences.GenericPreferencesDatastore
import io.github.arthurkun.generic.datastore.preferences.core.AbstractNullableCustomSetBlockingTest
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.test.AfterTest
import kotlin.test.BeforeTest

private const val TEST_DATASTORE_NAME = "test_nullable_custom_set_blocking_datastore"

class DesktopNullableCustomSetBlockingTest : AbstractNullableCustomSetBlockingTest() {

    @TempDir
    lateinit var tempFolder: File

    private val helper = DesktopTestHelper.blocking(TEST_DATASTORE_NAME)

    override val preferenceDatastore: GenericPreferencesDatastore get() = helper.preferenceDatastore

    @BeforeTest
    fun setup() = helper.setup(tempFolder.absolutePath)

    @AfterTest
    fun tearDown() = helper.tearDown()
}
