package io.github.arthurkun.generic.datastore.proto

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Before
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AndroidNullableProtoDatastoreBlockingTest : AbstractNullableProtoDatastoreBlockingTest() {

    private val helper = AndroidNullableProtoTestHelper.blocking("test_nullable_proto_blocking")

    override val protoDatastore get() = helper.protoDatastore

    @Before
    fun setup() = helper.setup()

    @After
    fun tearDown() = helper.tearDown()
}
