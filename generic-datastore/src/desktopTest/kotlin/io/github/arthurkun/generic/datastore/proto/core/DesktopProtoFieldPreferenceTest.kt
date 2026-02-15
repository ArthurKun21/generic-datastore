package io.github.arthurkun.generic.datastore.proto.core

import kotlinx.coroutines.test.TestDispatcher
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.test.AfterTest
import kotlin.test.BeforeTest

class DesktopProtoFieldPreferenceTest : AbstractProtoFieldPreferenceTest() {

    @TempDir
    lateinit var tempFolder: File

    private val helper = DesktopProtoTestHelper.standard("test_proto_field")

    override val protoDatastore get() = helper.protoDatastore
    override val testDispatcher: TestDispatcher get() = helper.testDispatcher

    @BeforeTest
    fun setup() = helper.setup(tempFolder.absolutePath)

    @AfterTest
    fun tearDown() = helper.tearDown()
}
