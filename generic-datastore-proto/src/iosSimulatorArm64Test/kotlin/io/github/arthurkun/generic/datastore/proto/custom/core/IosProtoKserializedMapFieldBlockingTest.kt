package io.github.arthurkun.generic.datastore.proto.custom.core

import kotlin.test.AfterTest
import kotlin.test.BeforeTest

class IosProtoKserializedMapFieldBlockingTest : AbstractProtoKserializedMapFieldBlockingTest() {

    private val helper = IosCustomFieldProtoTestHelper.blocking("test_proto_kserialized_map_field_blocking")

    override val protoDatastore get() = helper.protoDatastore

    @BeforeTest
    fun setup() = helper.setup()

    @AfterTest
    fun tearDown() = helper.tearDown()
}
