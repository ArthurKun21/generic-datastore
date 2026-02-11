package io.github.arthurkun.generic.datastore.proto

import kotlin.test.AfterTest
import kotlin.test.BeforeTest

class IosNullableProtoDatastoreBlockingTest : AbstractNullableProtoDatastoreBlockingTest() {

    private val helper = IosNullableProtoTestHelper.blocking("test_nullable_proto_blocking")

    override val protoDatastore get() = helper.protoDatastore

    @BeforeTest
    fun setup() = helper.setup()

    @AfterTest
    fun tearDown() = helper.tearDown()
}
