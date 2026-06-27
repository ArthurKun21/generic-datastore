package io.github.arthurkun.generic.datastore.proto.backup

import android.content.Context
import androidx.datastore.core.DataMigration
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.github.arthurkun.generic.datastore.proto.GenericProtoDatastore
import io.github.arthurkun.generic.datastore.proto.core.AndroidProtoTestHelper
import io.github.arthurkun.generic.datastore.proto.core.TestProtoData
import io.github.arthurkun.generic.datastore.proto.core.TestProtoDataSerializer
import kotlinx.coroutines.test.TestDispatcher
import org.junit.After
import org.junit.Before
import org.junit.runner.RunWith
import java.io.File
import io.github.arthurkun.generic.datastore.proto.createProtoDatastore as createProtoDatastoreFactory

@RunWith(AndroidJUnit4::class)
class AndroidProtoBackupTest : AbstractProtoBackupTest() {

    private val helper = AndroidProtoTestHelper.standard("test_proto_backup")

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
            val context = ApplicationProvider.getApplicationContext<Context>()
            context.filesDir.resolve("datastore/$datastoreName.pb").absolutePath
        },
    )

    override fun deleteProtoDatastore(datastoreName: String) {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val dataStoreFile = File(context.filesDir, "datastore/$datastoreName.pb")
        if (dataStoreFile.exists()) {
            dataStoreFile.delete()
        }
    }

    @Before
    fun setup() = helper.setup()

    @After
    fun tearDown() = helper.tearDown()
}
