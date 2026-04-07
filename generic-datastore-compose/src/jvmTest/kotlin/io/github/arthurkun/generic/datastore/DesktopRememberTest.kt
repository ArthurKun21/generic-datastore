package io.github.arthurkun.generic.datastore

import androidx.compose.runtime.Composable
import kotlinx.coroutines.test.TestDispatcher
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.test.AfterTest
import kotlin.test.BeforeTest

private const val TEST_DATASTORE_NAME = "test_remember"

class DesktopRememberTest : AbstractRememberTest() {

    @TempDir
    lateinit var tempFolder: File

    private val helper = DesktopTestHelper.standard(TEST_DATASTORE_NAME)

    override val testDispatcher: TestDispatcher get() = helper.testDispatcher

    @Composable
    override fun PlatformProviders(content: @Composable () -> Unit) = content()

    @BeforeTest
    fun setup() = helper.setup(tempFolder.absolutePath)

    @AfterTest
    fun tearDown() = helper.tearDown()
}