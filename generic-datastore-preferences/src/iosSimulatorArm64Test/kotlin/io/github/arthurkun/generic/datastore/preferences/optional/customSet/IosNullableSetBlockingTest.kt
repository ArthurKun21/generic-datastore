package io.github.arthurkun.generic.datastore.preferences.optional.customSet

import io.github.arthurkun.generic.datastore.IosTestHelper
import io.github.arthurkun.generic.datastore.preferences.GenericPreferencesDatastore
import io.github.arthurkun.generic.datastore.preferences.optional.customSet.AbstractNullableSetBlockingTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest

private const val TEST_DATASTORE_NAME = "test_nullable_set_datastore_blocking"

class IosNullableSetBlockingTest : AbstractNullableSetBlockingTest() {

    private val helper = IosTestHelper.blocking(TEST_DATASTORE_NAME)

    override val preferenceDatastore: GenericPreferencesDatastore get() = helper.preferenceDatastore

    @BeforeTest
    fun setup() = helper.setup()

    @AfterTest
    fun tearDown() = helper.tearDown()
}
