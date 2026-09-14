package io.github.arthurkun.generic.datastore.proto.core

import kotlinx.coroutines.test.TestDispatcher
import kotlin.test.AfterTest
import kotlin.test.BeforeTest

class IosProtoBatchTest : AbstractProtoBatchTest() {

    private val helper = IosProtoTestHelper.standard("test_proto_batch")

    override val protoDatastore get() = helper.protoDatastore
    override val testDispatcher: TestDispatcher get() = helper.testDispatcher

    @BeforeTest
    fun setup() = helper.setup()

    @AfterTest
    fun tearDown() = helper.tearDown()
}
