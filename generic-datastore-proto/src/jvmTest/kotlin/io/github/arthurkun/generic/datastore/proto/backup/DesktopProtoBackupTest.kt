package io.github.arthurkun.generic.datastore.proto.backup

import androidx.datastore.core.DataMigration
import io.github.arthurkun.generic.datastore.proto.GenericProtoDatastore
import io.github.arthurkun.generic.datastore.proto.core.DesktopProtoTestHelper
import io.github.arthurkun.generic.datastore.proto.core.TestProtoData
import io.github.arthurkun.generic.datastore.proto.core.TestProtoDataSerializer
import kotlinx.coroutines.test.TestDispatcher
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import io.github.arthurkun.generic.datastore.proto.createProtoDatastore as createProtoDatastoreFactory

class DesktopProtoBackupTest : AbstractProtoBackupTest() {

    @TempDir
    lateinit var tempFolder: File

    private val helper = DesktopProtoTestHelper.standard("test_proto_backup")

    override val protoDatastore get() = helper.protoDatastore
    override val testDispatcher: TestDispatcher get() = helper.testDispatcher

    override fun createProtoDatastore(
        datastoreName: String,
        migrations: List<DataMigration<TestProtoData>>,
    ): GenericProtoDatastore<TestProtoData> = createProtoDatastoreFactory(
        serializer = TestProtoDataSerializer,
        defaultValue = TestProtoData(),
        migrations = migrations,
        producePath = {
            "${tempFolder.absolutePath}/$datastoreName.pb"
        },
    )

    @BeforeTest
    fun setup() = helper.setup(tempFolder.absolutePath)

    @AfterTest
    fun tearDown() = helper.tearDown()
}
