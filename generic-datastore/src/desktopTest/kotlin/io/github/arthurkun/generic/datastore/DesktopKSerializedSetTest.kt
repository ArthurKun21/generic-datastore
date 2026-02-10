package io.github.arthurkun.generic.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import io.github.arthurkun.generic.datastore.preferences.AbstractKSerializedSetTest
import io.github.arthurkun.generic.datastore.preferences.GenericPreferencesDatastore
import kotlinx.coroutines.test.TestDispatcher
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.test.AfterTest
import kotlin.test.BeforeTest

private const val TEST_DATASTORE_NAME = "test_kserialized_set_datastore"

class DesktopKSerializedSetTest : AbstractKSerializedSetTest() {

    @TempDir
    lateinit var tempFolder: File

    private val helper = DesktopTestHelper.standard(TEST_DATASTORE_NAME)

    override val preferenceDatastore: GenericPreferencesDatastore get() = helper.preferenceDatastore
    override val dataStore: DataStore<Preferences> get() = helper.dataStore
    override val testDispatcher: TestDispatcher get() = helper.testDispatcher

    @BeforeTest
    fun setup() = helper.setup(tempFolder.absolutePath)

    @AfterTest
    fun tearDown() = helper.tearDown()
}
