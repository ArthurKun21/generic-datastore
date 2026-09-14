package io.github.arthurkun.generic.datastore.proto.core

import kotlin.test.AfterTest
import kotlin.test.BeforeTest

class IosProtoBatchBlockingTest : AbstractProtoBatchBlockingTest() {

    private val helper = IosProtoTestHelper.blocking("test_proto_batch_blocking")

    override val protoDatastore get() = helper.protoDatastore

    @BeforeTest
    fun setup() = helper.setup()

    @AfterTest
    fun tearDown() = helper.tearDown()
}
