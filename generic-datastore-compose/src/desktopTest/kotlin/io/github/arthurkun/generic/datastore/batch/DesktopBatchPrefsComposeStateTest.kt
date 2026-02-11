package io.github.arthurkun.generic.datastore.batch

import io.github.arthurkun.generic.datastore.DesktopTestHelper
import io.github.arthurkun.generic.datastore.preferences.GenericPreferencesDatastore
import kotlinx.coroutines.test.TestDispatcher
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.test.AfterTest
import kotlin.test.BeforeTest

private const val TEST_DATASTORE_NAME = "test_compose_batch_prefs_state"

class DesktopBatchPrefsComposeStateTest : AbstractBatchPrefsComposeStateTest() {

    @TempDir
    lateinit var tempFolder: File

    private val helper = DesktopTestHelper.standard(TEST_DATASTORE_NAME)

    override val preferenceDatastore: GenericPreferencesDatastore get() = helper.preferenceDatastore
    override val testDispatcher: TestDispatcher get() = helper.testDispatcher

    @BeforeTest
    fun setup() = helper.setup(tempFolder.absolutePath)

    @AfterTest
    fun tearDown() = helper.tearDown()
}
