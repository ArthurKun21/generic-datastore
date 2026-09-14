package io.github.arthurkun.generic.datastore.proto.core

import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.test.AfterTest
import kotlin.test.BeforeTest

class DesktopProtoBatchTest : AbstractProtoBatchTest() {

    @TempDir
    lateinit var tempFolder: File

    private val helper = DesktopProtoTestHelper.standard("test_proto_batch")

    override val protoDatastore get() = helper.protoDatastore
    override val testDispatcher get() = helper.testDispatcher

    @BeforeTest
    fun setup() = helper.setup(tempFolder.absolutePath)

    @AfterTest
    fun tearDown() = helper.tearDown()
}
