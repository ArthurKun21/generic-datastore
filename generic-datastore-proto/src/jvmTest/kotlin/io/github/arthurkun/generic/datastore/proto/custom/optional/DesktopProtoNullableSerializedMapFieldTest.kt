package io.github.arthurkun.generic.datastore.proto.custom.optional

import io.github.arthurkun.generic.datastore.proto.custom.core.DesktopCustomFieldProtoTestHelper
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.test.AfterTest
import kotlin.test.BeforeTest

class DesktopProtoNullableSerializedMapFieldTest : AbstractProtoNullableSerializedMapFieldTest() {

    @TempDir
    lateinit var tempFolder: File

    private val helper = DesktopCustomFieldProtoTestHelper.standard("test_proto_nullable_serialized_map_field")

    override val protoDatastore get() = helper.protoDatastore
    override val testDispatcher get() = helper.testDispatcher

    @BeforeTest
    fun setup() = helper.setup(tempFolder.absolutePath)

    @AfterTest
    fun tearDown() = helper.tearDown()
}
