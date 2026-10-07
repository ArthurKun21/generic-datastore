package io.github.arthurkun.generic.datastore.preferences.optional.custom

import io.github.arthurkun.generic.datastore.IosTestHelper
import io.github.arthurkun.generic.datastore.preferences.GenericPreferencesDatastore
import kotlin.test.AfterTest
import kotlin.test.BeforeTest

private const val TEST_DATASTORE_BLOCKING_NAME = "test_nullable_serialized_as_primitive_datastore_blocking"

class IosNullableSerializedAsPrimitiveBlockingTest : AbstractNullableSerializedAsPrimitiveBlockingTest() {

    private val helper = IosTestHelper.blocking(TEST_DATASTORE_BLOCKING_NAME)

    override val preferenceDatastore: GenericPreferencesDatastore get() = helper.preferenceDatastore

    @BeforeTest
    fun setup() = helper.setup()

    @AfterTest
    fun tearDown() = helper.tearDown()
}
