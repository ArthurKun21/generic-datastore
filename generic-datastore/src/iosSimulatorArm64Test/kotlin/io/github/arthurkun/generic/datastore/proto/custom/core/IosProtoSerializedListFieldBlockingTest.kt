package io.github.arthurkun.generic.datastore.proto.custom.core

import kotlin.test.AfterTest
import kotlin.test.BeforeTest

class IosProtoSerializedListFieldBlockingTest : AbstractProtoSerializedListFieldBlockingTest() {

    private val helper = IosCustomFieldProtoTestHelper.blocking("test_proto_serialized_list_field_blocking")

    override val protoDatastore get() = helper.protoDatastore

    @BeforeTest
    fun setup() = helper.setup()

    @AfterTest
    fun tearDown() = helper.tearDown()
}
