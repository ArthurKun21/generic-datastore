package io.github.arthurkun.generic.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.github.arthurkun.generic.datastore.preferences.AbstractKSerializedSetTest
import io.github.arthurkun.generic.datastore.preferences.GenericPreferencesDatastore
import kotlinx.coroutines.test.TestDispatcher
import org.junit.After
import org.junit.Before
import org.junit.runner.RunWith

private const val TEST_DATASTORE_NAME = "test_kserialized_set_datastore"

@RunWith(AndroidJUnit4::class)
class AndroidKSerializedSetTest : AbstractKSerializedSetTest() {

    private val helper = AndroidTestHelper.standard(TEST_DATASTORE_NAME)

    override val preferenceDatastore: GenericPreferencesDatastore get() = helper.preferenceDatastore
    override val dataStore: DataStore<Preferences> get() = helper.dataStore
    override val testDispatcher: TestDispatcher get() = helper.testDispatcher

    @Before
    fun setup() = helper.setup()

    @After
    fun tearDown() = helper.tearDown()
}
