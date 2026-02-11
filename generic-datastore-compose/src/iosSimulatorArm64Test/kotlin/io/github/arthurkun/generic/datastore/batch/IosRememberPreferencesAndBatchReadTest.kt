package io.github.arthurkun.generic.datastore.batch

import androidx.compose.runtime.Composable
import io.github.arthurkun.generic.datastore.IosTestHelper
import io.github.arthurkun.generic.datastore.preferences.GenericPreferencesDatastore
import kotlinx.coroutines.test.TestDispatcher
import kotlin.test.AfterTest
import kotlin.test.BeforeTest

private const val TEST_DATASTORE_NAME = "test_remember_preferences_and_batch_read"

class IosRememberPreferencesAndBatchReadTest : AbstractRememberPreferencesAndBatchReadTest() {

    private val helper = IosTestHelper.standard(TEST_DATASTORE_NAME)

    override val preferenceDatastore: GenericPreferencesDatastore get() = helper.preferenceDatastore
    override val testDispatcher: TestDispatcher get() = helper.testDispatcher

    @Composable
    override fun PlatformProviders(content: @Composable () -> Unit) = content()

    @BeforeTest
    fun setup() = helper.setup()

    @AfterTest
    fun tearDown() = helper.tearDown()
}
