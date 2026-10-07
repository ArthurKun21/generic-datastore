package io.github.arthurkun.generic.datastore.preferences

import io.github.arthurkun.generic.datastore.IosTestHelper
import kotlinx.coroutines.test.TestDispatcher
import kotlin.test.AfterTest
import kotlin.test.BeforeTest

private const val TEST_IN_MEMORY_DATASTORE_NAME = "test_in_memory"

class IosInMemoryGenericPreferenceDatastoreTest : AbstractInMemoryGenericPreferenceDatastoreTest() {

    private val helper = IosTestHelper.standard(TEST_IN_MEMORY_DATASTORE_NAME)

    override val preferenceDatastore: GenericPreferencesDatastore get() = helper.preferenceDatastore
    override val testDispatcher: TestDispatcher get() = helper.testDispatcher

    @BeforeTest
    fun setup() = helper.setup()

    @AfterTest
    fun tearDown() = helper.tearDown()
}
