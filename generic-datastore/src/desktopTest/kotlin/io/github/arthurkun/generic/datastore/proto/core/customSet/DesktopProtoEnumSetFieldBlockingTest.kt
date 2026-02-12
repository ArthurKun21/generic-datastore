package io.github.arthurkun.generic.datastore.proto.core.customSet

import io.github.arthurkun.generic.datastore.proto.core.custom.DesktopCustomFieldProtoTestHelper
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.test.AfterTest
import kotlin.test.BeforeTest

class DesktopProtoEnumSetFieldBlockingTest : AbstractProtoEnumSetFieldBlockingTest() {

    @TempDir
    lateinit var tempFolder: File

    private val helper = DesktopCustomFieldProtoTestHelper.blocking("test_proto_enum_set_field_blocking")

    override val protoDatastore get() = helper.protoDatastore

    @BeforeTest
    fun setup() = helper.setup(tempFolder.absolutePath)

    @AfterTest
    fun tearDown() = helper.tearDown()
}
