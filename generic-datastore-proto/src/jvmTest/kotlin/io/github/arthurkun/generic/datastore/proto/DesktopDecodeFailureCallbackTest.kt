package io.github.arthurkun.generic.datastore.proto

import io.github.arthurkun.generic.datastore.proto.custom.core.DesktopCustomFieldProtoTestHelper
import io.github.arthurkun.generic.datastore.proto.custom.core.TestCustomFieldProtoData
import kotlinx.coroutines.test.TestDispatcher
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.test.AfterTest
import kotlin.test.BeforeTest

class DesktopDecodeFailureCallbackTest : AbstractDecodeFailureCallbackTest() {

    @TempDir
    lateinit var tempFolder: File

    private val helper = DesktopCustomFieldProtoTestHelper.standard("test_proto_decode_failure_callback")

    override val protoDatastore get() = helper.protoDatastore
    override val testDispatcher: TestDispatcher get() = helper.testDispatcher

    @BeforeTest
    fun setup() = helper.setup(tempFolder.absolutePath)

    @AfterTest
    fun tearDown() = helper.tearDown()
}
