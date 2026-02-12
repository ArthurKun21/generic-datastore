package io.github.arthurkun.generic.datastore.proto.core

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Before
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AndroidProtoDatastoreBlockingTest : AbstractProtoDatastoreBlockingTest() {

    private val helper = AndroidProtoTestHelper.blocking("test_proto_blocking")

    override val protoDatastore get() = helper.protoDatastore

    @Before
    fun setup() = helper.setup()

    @After
    fun tearDown() = helper.tearDown()
}
