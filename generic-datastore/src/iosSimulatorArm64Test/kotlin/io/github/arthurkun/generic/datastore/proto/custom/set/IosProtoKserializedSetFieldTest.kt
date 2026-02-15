package io.github.arthurkun.generic.datastore.proto.custom.set

import io.github.arthurkun.generic.datastore.proto.custom.core.IosCustomFieldProtoTestHelper
import kotlinx.coroutines.test.TestDispatcher
import kotlin.test.AfterTest
import kotlin.test.BeforeTest

class IosProtoKserializedSetFieldTest : AbstractProtoKserializedSetFieldTest() {

    private val helper = IosCustomFieldProtoTestHelper.standard("test_proto_kserialized_set_field")

    override val protoDatastore get() = helper.protoDatastore
    override val testDispatcher: TestDispatcher get() = helper.testDispatcher

    @BeforeTest
    fun setup() = helper.setup()

    @AfterTest
    fun tearDown() = helper.tearDown()
}
