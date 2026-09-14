package io.github.arthurkun.generic.datastore.preferences.core

import io.github.arthurkun.generic.datastore.IosTestHelper
import io.github.arthurkun.generic.datastore.preferences.GenericPreferencesDatastore
import kotlin.test.AfterTest
import kotlin.test.BeforeTest

private const val TEST_BYTES_BLOCKING_DATASTORE_NAME = "test_bytes_blocking_datastore"

class IosBytesBlockingTest : AbstractBytesBlockingTest() {

    private val helper = IosTestHelper.blocking(TEST_BYTES_BLOCKING_DATASTORE_NAME)

    override val preferenceDatastore: GenericPreferencesDatastore get() = helper.preferenceDatastore

    @BeforeTest
    fun setup() = helper.setup()

    @AfterTest
    fun tearDown() = helper.tearDown()
}
