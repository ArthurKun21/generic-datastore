package io.github.arthurkun.generic.datastore.preferences.optional

import io.github.arthurkun.generic.datastore.IosTestHelper
import io.github.arthurkun.generic.datastore.preferences.GenericPreferencesDatastore
import io.github.arthurkun.generic.datastore.preferences.core.AbstractKSerializedSetBlockingTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest

private const val TEST_DATASTORE_BLOCKING_NAME = "test_kserialized_set_datastore_blocking"

class IosKSerializedSetBlockingTest : AbstractKSerializedSetBlockingTest() {

    private val helper = IosTestHelper.blocking(TEST_DATASTORE_BLOCKING_NAME)

    override val preferenceDatastore: GenericPreferencesDatastore get() = helper.preferenceDatastore

    @BeforeTest
    fun setup() = helper.setup()

    @AfterTest
    fun tearDown() = helper.tearDown()
}
