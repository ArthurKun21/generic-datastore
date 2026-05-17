package io.github.arthurkun.generic.datastore.preferences.batch

import androidx.test.ext.junit.runners.AndroidJUnit4
import io.github.arthurkun.generic.datastore.AndroidTestHelper
import io.github.arthurkun.generic.datastore.preferences.GenericPreferencesDatastore
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.runner.RunWith

private const val TEST_DATASTORE_BLOCKING_NAME = "test_batch_operations_blocking"

@RunWith(AndroidJUnit4::class)
class AndroidBatchOperationsBlockingTest : AbstractBatchOperationsBlockingTest() {

    companion object {
        private val helper = AndroidTestHelper.blocking(TEST_DATASTORE_BLOCKING_NAME)

        @JvmStatic
        @BeforeClass
        fun setupClass() = helper.setup()

        @JvmStatic
        @AfterClass
        fun tearDownClass() = helper.tearDown()
    }

    override val preferenceDatastore: GenericPreferencesDatastore get() = helper.preferenceDatastore
}
