package io.github.arthurkun.generic.datastore.proto

import kotlin.test.AfterTest
import kotlin.test.BeforeTest

class IosProtoKserializedListFieldBlockingTest : AbstractProtoKserializedListFieldBlockingTest() {

    private val helper = IosCustomFieldProtoTestHelper.blocking("test_proto_kserialized_list_field_blocking")

    override val protoDatastore get() = helper.protoDatastore

    @BeforeTest
    fun setup() = helper.setup()

    @AfterTest
    fun tearDown() = helper.tearDown()
}
