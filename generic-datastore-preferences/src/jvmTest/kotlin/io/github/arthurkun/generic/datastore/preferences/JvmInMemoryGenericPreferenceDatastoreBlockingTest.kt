package io.github.arthurkun.generic.datastore.preferences

import io.github.arthurkun.generic.datastore.JvmTestHelper
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.test.AfterTest
import kotlin.test.BeforeTest

private const val TEST_IN_MEMORY_BLOCKING_DATASTORE_NAME = "test_in_memory_blocking"

class JvmInMemoryGenericPreferenceDatastoreBlockingTest : AbstractInMemoryGenericPreferenceDatastoreBlockingTest() {

    @TempDir
    lateinit var tempFolder: File

    private val helper = JvmTestHelper.blocking(TEST_IN_MEMORY_BLOCKING_DATASTORE_NAME)

    override val preferenceDatastore: GenericPreferencesDatastore get() = helper.preferenceDatastore

    @BeforeTest
    fun setup() = helper.setup(tempFolder.absolutePath)

    @AfterTest
    fun tearDown() = helper.tearDown()
}
