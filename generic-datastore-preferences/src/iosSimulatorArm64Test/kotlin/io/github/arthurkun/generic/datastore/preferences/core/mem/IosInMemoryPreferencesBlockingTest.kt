package io.github.arthurkun.generic.datastore.preferences.core.mem

import io.github.arthurkun.generic.datastore.IosTestHelper
import io.github.arthurkun.generic.datastore.preferences.GenericPreferencesDatastore
import kotlin.test.AfterTest
import kotlin.test.BeforeTest

private const val TEST_IN_MEMORY_BLOCKING_DATASTORE_NAME = "test_in_memory_blocking"

class IosInMemoryPreferencesBlockingTest : AbstractInMemoryPreferencesBlockingTest() {

    private val helper = IosTestHelper.blocking(TEST_IN_MEMORY_BLOCKING_DATASTORE_NAME)

    override val preferenceDatastore: GenericPreferencesDatastore get() = helper.preferenceDatastore

    @BeforeTest
    fun setup() = helper.setup()

    @AfterTest
    fun tearDown() = helper.tearDown()
}
