package io.github.arthurkun.generic.datastore.proto.core.customSet

import io.github.arthurkun.generic.datastore.proto.core.custom.IosCustomFieldProtoTestHelper
import kotlinx.coroutines.test.TestDispatcher
import kotlin.test.AfterTest
import kotlin.test.BeforeTest

class IosProtoSerializedSetFieldTest : AbstractProtoSerializedSetFieldTest() {

    private val helper = IosCustomFieldProtoTestHelper.standard("test_proto_serialized_set_field")

    override val protoDatastore get() = helper.protoDatastore
    override val testDispatcher: TestDispatcher get() = helper.testDispatcher

    @BeforeTest
    fun setup() = helper.setup()

    @AfterTest
    fun tearDown() = helper.tearDown()
}
