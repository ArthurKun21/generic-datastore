package io.github.arthurkun.generic.datastore.proto.backup

import io.github.arthurkun.generic.datastore.proto.core.IosProtoTestHelper
import kotlinx.coroutines.test.TestDispatcher
import kotlin.test.AfterTest
import kotlin.test.BeforeTest

class IosProtoBackupTest : AbstractProtoBackupTest() {

    private val helper = IosProtoTestHelper.standard("test_proto_backup")

    override val protoDatastore get() = helper.protoDatastore
    override val testDispatcher: TestDispatcher get() = helper.testDispatcher

    @BeforeTest
    fun setup() = helper.setup()

    @AfterTest
    fun tearDown() = helper.tearDown()
}
