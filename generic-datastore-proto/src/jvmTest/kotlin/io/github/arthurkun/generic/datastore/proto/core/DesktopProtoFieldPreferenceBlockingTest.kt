package io.github.arthurkun.generic.datastore.proto.core

import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.test.AfterTest
import kotlin.test.BeforeTest

class DesktopProtoFieldPreferenceBlockingTest : AbstractProtoFieldPreferenceBlockingTest() {

    @TempDir
    lateinit var tempFolder: File

    private val helper = DesktopProtoTestHelper.blocking("test_proto_field_blocking")

    override val protoDatastore get() = helper.protoDatastore

    @BeforeTest
    fun setup() = helper.setup(tempFolder.absolutePath)

    @AfterTest
    fun tearDown() = helper.tearDown()
}
