package io.github.arthurkun.generic.datastore.preferences.optional.customSet

import io.github.arthurkun.generic.datastore.JvmTestHelper
import io.github.arthurkun.generic.datastore.preferences.GenericPreferencesDatastore
import io.github.arthurkun.generic.datastore.preferences.optional.customSet.AbstractNullableSetBlockingTest
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.test.AfterTest
import kotlin.test.BeforeTest

private const val TEST_DATASTORE_NAME = "test_nullable_set_datastore_blocking"

class JvmNullableSetBlockingTest : AbstractNullableSetBlockingTest() {

    @TempDir
    lateinit var tempFolder: File

    private val helper = JvmTestHelper.blocking(TEST_DATASTORE_NAME)

    override val preferenceDatastore: GenericPreferencesDatastore get() = helper.preferenceDatastore

    @BeforeTest
    fun setup() = helper.setup(tempFolder.absolutePath)

    @AfterTest
    fun tearDown() = helper.tearDown()
}
