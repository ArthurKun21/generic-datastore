package io.github.arthurkun.generic.datastore.proto

import kotlinx.coroutines.test.TestDispatcher
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.test.AfterTest
import kotlin.test.BeforeTest

class DesktopNullableProtoDatastoreTest : AbstractNullableProtoDatastoreTest() {

    @TempDir
    lateinit var tempFolder: File

    private val helper = DesktopNullableProtoTestHelper.standard("test_nullable_proto")

    override val protoDatastore get() = helper.protoDatastore
    override val testDispatcher: TestDispatcher get() = helper.testDispatcher

    @BeforeTest
    fun setup() = helper.setup(tempFolder.absolutePath)

    @AfterTest
    fun tearDown() = helper.tearDown()
}
