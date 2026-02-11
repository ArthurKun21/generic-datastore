package io.github.arthurkun.generic.datastore.batch

import androidx.test.ext.junit.runners.AndroidJUnit4
import io.github.arthurkun.generic.datastore.AndroidTestHelper
import io.github.arthurkun.generic.datastore.preferences.GenericPreferencesDatastore
import kotlinx.coroutines.test.TestDispatcher
import org.junit.After
import org.junit.Before
import org.junit.runner.RunWith

private const val TEST_DATASTORE_NAME = "test_compose_batch_prefs_state"

@RunWith(AndroidJUnit4::class)
class AndroidBatchPrefsComposeStateTest : AbstractBatchPrefsComposeStateTest() {

    private val helper = AndroidTestHelper.standard(TEST_DATASTORE_NAME)

    override val preferenceDatastore: GenericPreferencesDatastore get() = helper.preferenceDatastore
    override val testDispatcher: TestDispatcher get() = helper.testDispatcher

    @Before
    fun setup() = helper.setup()

    @After
    fun tearDown() = helper.tearDown()
}
