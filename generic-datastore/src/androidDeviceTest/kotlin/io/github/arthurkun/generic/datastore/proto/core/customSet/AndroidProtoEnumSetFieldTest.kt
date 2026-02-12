package io.github.arthurkun.generic.datastore.proto.core.customSet

import io.github.arthurkun.generic.datastore.proto.core.custom.AndroidCustomFieldProtoTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.test.TestDispatcher
import org.junit.After
import org.junit.Before
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AndroidProtoEnumSetFieldTest : AbstractProtoEnumSetFieldTest() {

    private val helper = AndroidCustomFieldProtoTestHelper.standard("test_proto_enum_set_field")

    override val protoDatastore get() = helper.protoDatastore
    override val testDispatcher: TestDispatcher get() = helper.testDispatcher

    @Before
    fun setup() = helper.setup()

    @After
    fun tearDown() = helper.tearDown()
}
