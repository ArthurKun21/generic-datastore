package io.github.arthurkun.generic.datastore.proto.optional.custom

import io.github.arthurkun.generic.datastore.proto.core.custom.DesktopCustomFieldProtoTestHelper
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.test.AfterTest
import kotlin.test.BeforeTest

class DesktopProtoNullableKserializedFieldBlockingTest : AbstractProtoNullableKserializedFieldBlockingTest() {

    @TempDir
    lateinit var tempFolder: File

    private val helper = DesktopCustomFieldProtoTestHelper.blocking("test_proto_nullable_kserialized_field_blocking")

    override val protoDatastore get() = helper.protoDatastore

    @BeforeTest
    fun setup() = helper.setup(tempFolder.absolutePath)

    @AfterTest
    fun tearDown() = helper.tearDown()
}
