package io.github.arthurkun.generic.datastore.proto.core.customSet

import io.github.arthurkun.generic.datastore.proto.core.custom.AndroidCustomFieldProtoTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Before
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AndroidProtoKserializedSetFieldBlockingTest : AbstractProtoKserializedSetFieldBlockingTest() {

    private val helper = AndroidCustomFieldProtoTestHelper.blocking("test_proto_kserialized_set_field_blocking")

    override val protoDatastore get() = helper.protoDatastore

    @Before
    fun setup() = helper.setup()

    @After
    fun tearDown() = helper.tearDown()
}
