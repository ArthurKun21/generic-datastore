package io.github.arthurkun.generic.datastore.proto.custom.core

import kotlinx.coroutines.test.TestDispatcher
import kotlin.test.AfterTest
import kotlin.test.BeforeTest

class IosProtoKserializedFieldTest : AbstractProtoKserializedFieldTest() {

    private val helper = IosCustomFieldProtoTestHelper.standard("test_proto_kserialized_field")

    override val protoDatastore get() = helper.protoDatastore
    override val testDispatcher: TestDispatcher get() = helper.testDispatcher

    @BeforeTest
    fun setup() = helper.setup()

    @AfterTest
    fun tearDown() = helper.tearDown()
}
