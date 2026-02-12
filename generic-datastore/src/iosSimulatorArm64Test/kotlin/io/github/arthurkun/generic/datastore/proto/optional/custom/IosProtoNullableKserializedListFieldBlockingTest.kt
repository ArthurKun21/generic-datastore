package io.github.arthurkun.generic.datastore.proto.optional.custom

import kotlin.test.AfterTest
import kotlin.test.BeforeTest

class IosProtoNullableKserializedListFieldBlockingTest : AbstractProtoNullableKserializedListFieldBlockingTest() {

    private val helper = IosCustomFieldProtoTestHelper.blocking("test_proto_nullable_kserialized_list_field_blocking")

    override val protoDatastore get() = helper.protoDatastore

    @BeforeTest
    fun setup() = helper.setup()

    @AfterTest
    fun tearDown() = helper.tearDown()
}
