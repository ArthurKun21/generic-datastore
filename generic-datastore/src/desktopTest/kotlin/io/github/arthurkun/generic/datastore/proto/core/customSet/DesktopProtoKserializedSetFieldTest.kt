package io.github.arthurkun.generic.datastore.proto.core.customSet

import io.github.arthurkun.generic.datastore.proto.core.custom.DesktopCustomFieldProtoTestHelper
import kotlinx.coroutines.test.TestDispatcher
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.test.AfterTest
import kotlin.test.BeforeTest

class DesktopProtoKserializedSetFieldTest : AbstractProtoKserializedSetFieldTest() {

    @TempDir
    lateinit var tempFolder: File

    private val helper = DesktopCustomFieldProtoTestHelper.standard("test_proto_kserialized_set_field")

    override val protoDatastore get() = helper.protoDatastore
    override val testDispatcher: TestDispatcher get() = helper.testDispatcher

    @BeforeTest
    fun setup() = helper.setup(tempFolder.absolutePath)

    @AfterTest
    fun tearDown() = helper.tearDown()
}
