package io.github.arthurkun.generic.datastore.preferences.core.custom

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import io.github.arthurkun.generic.datastore.JvmTestHelper
import io.github.arthurkun.generic.datastore.preferences.GenericPreferencesDatastore
import kotlinx.coroutines.test.TestDispatcher
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.test.AfterTest
import kotlin.test.BeforeTest

private const val TEST_DATASTORE_NAME = "test_serialized_as_primitive_datastore"

class JvmSerializedAsPrimitiveTest : AbstractSerializedAsPrimitiveTest() {

    @TempDir
    lateinit var tempFolder: File

    private val helper = JvmTestHelper.standard(TEST_DATASTORE_NAME)

    override val preferenceDatastore: GenericPreferencesDatastore get() = helper.preferenceDatastore
    override val dataStore: DataStore<Preferences> get() = helper.dataStore
    override val testDispatcher: TestDispatcher get() = helper.testDispatcher

    @BeforeTest
    fun setup() = helper.setup(tempFolder.absolutePath)

    @AfterTest
    fun tearDown() = helper.tearDown()
}
