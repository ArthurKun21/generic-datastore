package io.github.arthurkun.generic.datastore.proto.backup

import androidx.datastore.core.DataMigration
import io.github.arthurkun.generic.datastore.proto.GenericProtoDatastore
import io.github.arthurkun.generic.datastore.proto.core.IosProtoTestHelper
import io.github.arthurkun.generic.datastore.proto.core.TestProtoData
import io.github.arthurkun.generic.datastore.proto.core.TestProtoDataSerializer
import kotlinx.coroutines.test.TestDispatcher
import platform.Foundation.NSFileManager
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSUUID
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import io.github.arthurkun.generic.datastore.proto.createProtoDatastore as createProtoDatastoreFactory

class IosProtoBackupTest : AbstractProtoBackupTest() {

    private val helper = IosProtoTestHelper.standard("test_proto_backup")
    private val extraTempDirs = mutableListOf<String>()

    override val protoDatastore get() = helper.protoDatastore
    override val testDispatcher: TestDispatcher get() = helper.testDispatcher

    override fun createProtoDatastore(
        datastoreName: String,
        migrations: List<DataMigration<TestProtoData>>,
    ): GenericProtoDatastore<TestProtoData> {
        val tempDir = NSTemporaryDirectory() + NSUUID().UUIDString
        extraTempDirs += tempDir
        return createProtoDatastoreFactory(
            serializer = TestProtoDataSerializer,
            defaultValue = TestProtoData(),
            migrations = migrations,
            producePath = {
                "$tempDir/$datastoreName.pb"
            },
        )
    }

    override fun deleteProtoDatastore(datastoreName: String) {
        extraTempDirs.forEach { tempDir ->
            NSFileManager.defaultManager.removeItemAtPath(tempDir, null)
        }
        extraTempDirs.clear()
    }

    @BeforeTest
    fun setup() = helper.setup()

    @AfterTest
    fun tearDown() {
        try {
            helper.tearDown()
        } finally {
            deleteProtoDatastore("test_proto_backup_migration")
        }
    }
}
