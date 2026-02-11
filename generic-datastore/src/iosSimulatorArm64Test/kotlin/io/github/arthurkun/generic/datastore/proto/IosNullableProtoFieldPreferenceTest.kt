package io.github.arthurkun.generic.datastore.proto

import kotlinx.coroutines.test.TestDispatcher
import kotlin.test.AfterTest
import kotlin.test.BeforeTest

class IosNullableProtoFieldPreferenceTest : AbstractNullableProtoFieldPreferenceTest() {

    private val helper = IosNullableProtoTestHelper.standard("test_nullable_proto_field")

    override val nullableProtoDatastore get() = helper.protoDatastore
    override val testDispatcher: TestDispatcher get() = helper.testDispatcher

    @BeforeTest
    fun setup() = helper.setup()

    @AfterTest
    fun tearDown() = helper.tearDown()
}
