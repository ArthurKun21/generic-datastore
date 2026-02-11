package io.github.arthurkun.generic.datastore.proto

import kotlin.test.AfterTest
import kotlin.test.BeforeTest

class IosNullableProtoFieldPreferenceBlockingTest :
    AbstractNullableProtoFieldPreferenceBlockingTest() {

    private val helper =
        IosNullableProtoTestHelper.blocking("test_nullable_proto_field_blocking")

    override val nullableProtoDatastore get() = helper.protoDatastore

    @BeforeTest
    fun setup() = helper.setup()

    @AfterTest
    fun tearDown() = helper.tearDown()
}
