package io.github.arthurkun.generic.datastore.proto

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Before
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AndroidNullableProtoFieldPreferenceBlockingTest :
    AbstractNullableProtoFieldPreferenceBlockingTest() {

    private val helper =
        AndroidNullableProtoTestHelper.blocking("test_nullable_proto_field_blocking")

    override val nullableProtoDatastore get() = helper.protoDatastore

    @Before
    fun setup() = helper.setup()

    @After
    fun tearDown() = helper.tearDown()
}
