package io.github.arthurkun.generic.datastore.proto.custom.set

import io.github.arthurkun.generic.datastore.proto.custom.core.IosCustomFieldProtoTestHelper
import kotlin.test.AfterTest
import kotlin.test.BeforeTest

class IosProtoSerializedSetFieldBlockingTest : AbstractProtoSerializedSetFieldBlockingTest() {

    private val helper = IosCustomFieldProtoTestHelper.blocking("test_proto_serialized_set_field_blocking")

    override val protoDatastore get() = helper.protoDatastore

    @BeforeTest
    fun setup() = helper.setup()

    @AfterTest
    fun tearDown() = helper.tearDown()
}
