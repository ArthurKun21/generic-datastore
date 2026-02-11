package io.github.arthurkun.generic.datastore.proto

import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.test.TestDispatcher
import org.junit.After
import org.junit.Before
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AndroidNullableProtoFieldPreferenceTest : AbstractNullableProtoFieldPreferenceTest() {

    private val helper = AndroidNullableProtoTestHelper.standard("test_nullable_proto_field")

    override val nullableProtoDatastore get() = helper.protoDatastore
    override val testDispatcher: TestDispatcher get() = helper.testDispatcher

    @Before
    fun setup() = helper.setup()

    @After
    fun tearDown() = helper.tearDown()
}
