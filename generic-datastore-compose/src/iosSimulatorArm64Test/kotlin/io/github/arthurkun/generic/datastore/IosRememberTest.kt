package io.github.arthurkun.generic.datastore

import androidx.compose.runtime.Composable
import kotlinx.coroutines.test.TestDispatcher
import kotlin.test.AfterTest
import kotlin.test.BeforeTest

private const val TEST_DATASTORE_NAME = "test_remember"

class IosRememberTest : AbstractRememberTest() {

    private val helper = IosTestHelper.standard(TEST_DATASTORE_NAME)

    override val testDispatcher: TestDispatcher get() = helper.testDispatcher

    @Composable
    override fun PlatformProviders(content: @Composable () -> Unit) = content()

    @BeforeTest
    fun setup() = helper.setup()

    @AfterTest
    fun tearDown() = helper.tearDown()
}