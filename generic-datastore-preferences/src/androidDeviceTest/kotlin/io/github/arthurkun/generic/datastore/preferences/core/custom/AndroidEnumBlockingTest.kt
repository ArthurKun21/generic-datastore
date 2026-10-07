package io.github.arthurkun.generic.datastore.preferences.core.custom

import androidx.test.ext.junit.runners.AndroidJUnit4
import io.github.arthurkun.generic.datastore.AndroidTestHelper
import io.github.arthurkun.generic.datastore.preferences.GenericPreferencesDatastore
import io.github.arthurkun.generic.datastore.preferences.core.custom.AbstractEnumBlockingTest
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.runner.RunWith

private const val TEST_DATASTORE_NAME = "test_enum_datastore_blocking"

@RunWith(AndroidJUnit4::class)
class AndroidEnumBlockingTest : AbstractEnumBlockingTest() {

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
