package io.github.arthurkun.generic.datastore.proto.core.custom

import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.test.AfterTest
import kotlin.test.BeforeTest

class DesktopProtoKserializedListFieldBlockingTest : AbstractProtoKserializedListFieldBlockingTest() {

    @TempDir
    lateinit var tempFolder: File

    private val helper = DesktopCustomFieldProtoTestHelper.blocking("test_proto_kserialized_list_field_blocking")

    override val protoDatastore get() = helper.protoDatastore

    @BeforeTest
    fun setup() = helper.setup(tempFolder.absolutePath)

    @AfterTest
    fun tearDown() = helper.tearDown()
}
