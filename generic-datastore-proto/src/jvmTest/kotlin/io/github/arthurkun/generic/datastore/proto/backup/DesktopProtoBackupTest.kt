package io.github.arthurkun.generic.datastore.proto.backup

import io.github.arthurkun.generic.datastore.proto.core.DesktopProtoTestHelper
import kotlinx.coroutines.test.TestDispatcher
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.test.AfterTest
import kotlin.test.BeforeTest

class DesktopProtoBackupTest : AbstractProtoBackupTest() {

    @TempDir
    lateinit var tempFolder: File

    private val helper = DesktopProtoTestHelper.standard("test_proto_backup")

    override val protoDatastore get() = helper.protoDatastore
    override val testDispatcher: TestDispatcher get() = helper.testDispatcher

    @BeforeTest
    fun setup() = helper.setup(tempFolder.absolutePath)

    @AfterTest
    fun tearDown() = helper.tearDown()
}
