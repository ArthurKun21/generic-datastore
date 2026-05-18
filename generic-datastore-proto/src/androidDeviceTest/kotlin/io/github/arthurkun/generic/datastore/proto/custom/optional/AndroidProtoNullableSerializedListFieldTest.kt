package io.github.arthurkun.generic.datastore.proto.custom.optional

import androidx.test.ext.junit.runners.AndroidJUnit4
import io.github.arthurkun.generic.datastore.proto.custom.core.AndroidCustomFieldProtoTestHelper
import kotlinx.coroutines.test.TestDispatcher
import org.junit.After
import org.junit.Before
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AndroidProtoNullableSerializedListFieldTest : AbstractProtoNullableSerializedListFieldTest() {

    private val helper = AndroidCustomFieldProtoTestHelper.standard("test_proto_nullable_serialized_list_field")

    override val protoDatastore get() = helper.protoDatastore
    override val testDispatcher: TestDispatcher get() = helper.testDispatcher

    @Before
    fun setup() = helper.setup()

    @After
    fun tearDown() = helper.tearDown()
}
