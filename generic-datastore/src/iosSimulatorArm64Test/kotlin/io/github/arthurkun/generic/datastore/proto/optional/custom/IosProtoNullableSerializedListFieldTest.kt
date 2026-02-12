package io.github.arthurkun.generic.datastore.proto.optional.custom

import kotlinx.coroutines.test.TestDispatcher
import kotlin.test.AfterTest
import kotlin.test.BeforeTest

class IosProtoNullableSerializedListFieldTest : AbstractProtoNullableSerializedListFieldTest() {

    private val helper = IosCustomFieldProtoTestHelper.standard("test_proto_nullable_serialized_list_field")

    override val protoDatastore get() = helper.protoDatastore
    override val testDispatcher: TestDispatcher get() = helper.testDispatcher

    @BeforeTest
    fun setup() = helper.setup()

    @AfterTest
    fun tearDown() = helper.tearDown()
}
