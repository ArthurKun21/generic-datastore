package io.github.arthurkun.generic.datastore.preferences.core.data.custom

import io.github.arthurkun.generic.datastore.JvmTestHelper
import io.github.arthurkun.generic.datastore.preferences.GenericPreferencesDatastore
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.test.AfterTest
import kotlin.test.BeforeTest

private const val TEST_DATASTORE_BLOCKING_NAME = "test_kserialized_datastore_blocking"

class JvmKSerializedBlockingTest : AbstractKSerializedBlockingTest() {

    @TempDir
    lateinit var tempFolder: File

    private val helper = JvmTestHelper.blocking(TEST_DATASTORE_BLOCKING_NAME)

    override val preferenceDatastore: GenericPreferencesDatastore get() = helper.preferenceDatastore

    @BeforeTest
    fun setup() = helper.setup(tempFolder.absolutePath)

    @AfterTest
    fun tearDown() = helper.tearDown()
}
