package io.github.arthurkun.generic.datastore.preferences.core.mem

import io.github.arthurkun.generic.datastore.JvmTestHelper
import io.github.arthurkun.generic.datastore.preferences.GenericPreferencesDatastore
import kotlinx.coroutines.test.TestDispatcher
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.test.AfterTest
import kotlin.test.BeforeTest

private const val TEST_IN_MEMORY_DATASTORE_NAME = "test_in_memory"

class JvmInMemoryGenericPreferenceDatastoreTest : AbstractInMemoryGenericPreferenceDatastoreTest() {

    @TempDir
    lateinit var tempFolder: File

    private val helper = JvmTestHelper.standard(TEST_IN_MEMORY_DATASTORE_NAME)

    override val preferenceDatastore: GenericPreferencesDatastore get() = helper.preferenceDatastore
    override val testDispatcher: TestDispatcher get() = helper.testDispatcher

    @BeforeTest
    fun setup() = helper.setup(tempFolder.absolutePath)

    @AfterTest
    fun tearDown() = helper.tearDown()
}
