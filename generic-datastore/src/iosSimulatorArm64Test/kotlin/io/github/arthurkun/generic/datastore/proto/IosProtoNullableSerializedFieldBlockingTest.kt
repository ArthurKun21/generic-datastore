package io.github.arthurkun.generic.datastore.proto

import kotlin.test.AfterTest
import kotlin.test.BeforeTest

class IosProtoNullableSerializedFieldBlockingTest : AbstractProtoNullableSerializedFieldBlockingTest() {

    private val helper = IosCustomFieldProtoTestHelper.blocking("test_proto_nullable_serialized_field_blocking")

    override val protoDatastore get() = helper.protoDatastore

    @BeforeTest
    fun setup() = helper.setup()

    @AfterTest
    fun tearDown() = helper.tearDown()
}
