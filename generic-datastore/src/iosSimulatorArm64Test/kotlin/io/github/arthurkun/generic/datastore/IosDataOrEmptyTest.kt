package io.github.arthurkun.generic.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import io.github.arthurkun.generic.datastore.preferences.AbstractDataOrEmptyTest
import io.github.arthurkun.generic.datastore.preferences.GenericPreferencesDatastore
import kotlinx.coroutines.test.TestDispatcher
import kotlin.test.AfterTest
import kotlin.test.BeforeTest

private const val TEST_DATASTORE_NAME = "test_data_or_empty_datastore"

class IosDataOrEmptyTest : AbstractDataOrEmptyTest() {

    private val helper = IosTestHelper.standard(TEST_DATASTORE_NAME)

    override val preferenceDatastore: GenericPreferencesDatastore get() = helper.preferenceDatastore
    override val dataStore: DataStore<Preferences> get() = helper.dataStore
    override val testDispatcher: TestDispatcher get() = helper.testDispatcher

    @BeforeTest
    fun setup() = helper.setup()

    @AfterTest
    fun tearDown() = helper.tearDown()
}
