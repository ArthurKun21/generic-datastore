package io.github.arthurkun.generic.datastore.proto.optional.custom

import androidx.test.ext.junit.runners.AndroidJUnit4
import io.github.arthurkun.generic.datastore.proto.core.custom.AndroidCustomFieldProtoTestHelper
import kotlinx.coroutines.test.TestDispatcher
import org.junit.After
import org.junit.Before
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AndroidProtoNullableKserializedFieldTest : AbstractProtoNullableKserializedFieldTest() {

    private val helper = AndroidCustomFieldProtoTestHelper.standard("test_proto_nullable_kserialized_field")

    override val protoDatastore get() = helper.protoDatastore
    override val testDispatcher: TestDispatcher get() = helper.testDispatcher

    @Before
    fun setup() = helper.setup()

    @After
    fun tearDown() = helper.tearDown()
}
