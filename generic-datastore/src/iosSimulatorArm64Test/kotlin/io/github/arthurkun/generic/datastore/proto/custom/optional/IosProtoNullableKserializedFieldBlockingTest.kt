package io.github.arthurkun.generic.datastore.proto.custom.optional

import io.github.arthurkun.generic.datastore.proto.custom.core.IosCustomFieldProtoTestHelper
import kotlin.test.AfterTest
import kotlin.test.BeforeTest

class IosProtoNullableKserializedFieldBlockingTest : AbstractProtoNullableKserializedFieldBlockingTest() {

    private val helper = IosCustomFieldProtoTestHelper.blocking("test_proto_nullable_kserialized_field_blocking")

    override val protoDatastore get() = helper.protoDatastore

    @BeforeTest
    fun setup() = helper.setup()

    @AfterTest
    fun tearDown() = helper.tearDown()
}
