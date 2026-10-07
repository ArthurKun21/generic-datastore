package io.github.arthurkun.generic.datastore.preferences.core.customSet

import io.github.arthurkun.generic.datastore.IosTestHelper
import io.github.arthurkun.generic.datastore.preferences.GenericPreferencesDatastore
import io.github.arthurkun.generic.datastore.preferences.core.customSet.AbstractSerializedSetBlockingTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest

private const val TEST_DATASTORE_NAME = "test_serialized_set_datastore_blocking"

class IosSerializedSetBlockingTest : AbstractSerializedSetBlockingTest() {

    private val helper = IosTestHelper.blocking(TEST_DATASTORE_NAME)

    override val preferenceDatastore: GenericPreferencesDatastore get() = helper.preferenceDatastore

    @BeforeTest
    fun setup() = helper.setup()

    @AfterTest
    fun tearDown() = helper.tearDown()
}
