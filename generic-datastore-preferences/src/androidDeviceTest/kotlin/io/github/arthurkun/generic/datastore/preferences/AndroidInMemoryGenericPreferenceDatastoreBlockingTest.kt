package io.github.arthurkun.generic.datastore.preferences

import androidx.test.ext.junit.runners.AndroidJUnit4
import io.github.arthurkun.generic.datastore.AndroidTestHelper
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.runner.RunWith

private const val TEST_IN_MEMORY_BLOCKING_DATASTORE_NAME = "test_in_memory_blocking"

@RunWith(AndroidJUnit4::class)
class AndroidInMemoryGenericPreferenceDatastoreBlockingTest : AbstractInMemoryGenericPreferenceDatastoreBlockingTest() {

    companion object {
        private val helper = AndroidTestHelper.blocking(TEST_IN_MEMORY_BLOCKING_DATASTORE_NAME)

        @JvmStatic
        @BeforeClass
        fun setupClass() = helper.setup()

        @JvmStatic
        @AfterClass
        fun tearDownClass() = helper.tearDown()
    }

    override val preferenceDatastore: GenericPreferencesDatastore get() = helper.preferenceDatastore
}
