package io.github.arthurkun.generic.datastore.proto.core.customSet

import io.github.arthurkun.generic.datastore.proto.core.custom.IosCustomFieldProtoTestHelper
import kotlin.test.AfterTest
import kotlin.test.BeforeTest

class IosProtoEnumSetFieldBlockingTest : AbstractProtoEnumSetFieldBlockingTest() {

    private val helper = IosCustomFieldProtoTestHelper.blocking("test_proto_enum_set_field_blocking")

    override val protoDatastore get() = helper.protoDatastore

    @BeforeTest
    fun setup() = helper.setup()

    @AfterTest
    fun tearDown() = helper.tearDown()
}
