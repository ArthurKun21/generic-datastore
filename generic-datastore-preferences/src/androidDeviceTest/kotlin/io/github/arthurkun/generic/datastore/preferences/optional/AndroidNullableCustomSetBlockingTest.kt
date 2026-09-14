package io.github.arthurkun.generic.datastore.preferences.optional

import androidx.test.ext.junit.runners.AndroidJUnit4
import io.github.arthurkun.generic.datastore.AndroidTestHelper
import io.github.arthurkun.generic.datastore.preferences.GenericPreferencesDatastore
import io.github.arthurkun.generic.datastore.preferences.core.AbstractNullableCustomSetBlockingTest
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.runner.RunWith

private const val TEST_DATASTORE_NAME = "test_nullable_custom_set_blocking_datastore"

@RunWith(AndroidJUnit4::class)
class AndroidNullableCustomSetBlockingTest : AbstractNullableCustomSetBlockingTest() {

    companion object {
        private val helper = AndroidTestHelper.blocking(TEST_DATASTORE_NAME)

        @JvmStatic
        @BeforeClass
        fun setupClass() = helper.setup()

        @JvmStatic
        @AfterClass
        fun tearDownClass() = helper.tearDown()
    }

    override val preferenceDatastore: GenericPreferencesDatastore get() = helper.preferenceDatastore
}
