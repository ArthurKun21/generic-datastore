package io.github.arthurkun.generic.datastore.proto

import io.github.arthurkun.generic.datastore.proto.custom.core.IosCustomFieldProtoTestHelper
import io.github.arthurkun.generic.datastore.proto.custom.core.TestCustomFieldProtoData
import kotlinx.coroutines.test.TestDispatcher
import kotlin.test.AfterTest
import kotlin.test.BeforeTest

class IosDecodeFailureCallbackTest : AbstractDecodeFailureCallbackTest() {

    private val helper = IosCustomFieldProtoTestHelper.standard("test_proto_decode_failure_callback")

    override val protoDatastore get() = helper.protoDatastore
    override val testDispatcher: TestDispatcher get() = helper.testDispatcher

    @BeforeTest
    fun setup() = helper.setup()

    @AfterTest
    fun tearDown() = helper.tearDown()
}
