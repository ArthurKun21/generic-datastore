@file:OptIn(io.github.arthurkun.generic.datastore.core.InternalGenericDatastoreApi::class)

package io.github.arthurkun.generic.datastore.proto.backup

import androidx.datastore.core.DataMigration
import io.github.arthurkun.generic.datastore.proto.GenericProtoDatastore
import io.github.arthurkun.generic.datastore.proto.ProtoDatastore
import io.github.arthurkun.generic.datastore.proto.core.TestAddress
import io.github.arthurkun.generic.datastore.proto.core.TestProfile
import io.github.arthurkun.generic.datastore.proto.core.TestProtoData
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

abstract class AbstractProtoBackupTest {

    abstract val protoDatastore: GenericProtoDatastore<TestProtoData>
    abstract val testDispatcher: TestDispatcher

    abstract fun createProtoDatastore(
        datastoreName: String,
        migrations: List<DataMigration<TestProtoData>> = emptyList(),
    ): GenericProtoDatastore<TestProtoData>

    open fun deleteProtoDatastore(datastoreName: String) = Unit

    @Test
    fun exportAsByteArray_fileDoesNotExist_returnsEmptyBytes() = runTest(testDispatcher) {
        assertTrue(protoDatastore.exportAsByteArray().isEmpty())
    }

    @Test
    fun exportAsByteArray_firstOperation_runsMigrationsBeforeReadingBytes() = runTest(testDispatcher) {
        val datastoreName = "test_proto_backup_migration"
        val migrated = TestProtoData(id = 10, name = "Migrated")
        var cleanUpCalled = false
        val migration = object : DataMigration<TestProtoData> {
            override suspend fun shouldMigrate(currentData: TestProtoData): Boolean = true

            override suspend fun migrate(currentData: TestProtoData): TestProtoData = migrated

            override suspend fun cleanUp() {
                cleanUpCalled = true
            }
        }
        val datastore = createProtoDatastore(
            datastoreName = datastoreName,
            migrations = listOf(migration),
        )

        try {
            val backup = datastore.exportAsByteArray()

            assertTrue(backup.isNotEmpty())
            assertTrue(cleanUpCalled)

            datastore.data().set(TestProtoData(id = 11, name = "Changed"))
            datastore.importFromByteArray(backup)

            assertEquals(migrated, datastore.data().get())
        } finally {
            datastore.close()
            deleteProtoDatastore(datastoreName)
        }
    }

    @Test
    fun exportAsByteArray_afterSet_returnsNonEmptyBytes() = runTest(testDispatcher) {
        protoDatastore.data().set(TestProtoData(id = 1, name = "Alice"))

        val backup = protoDatastore.exportAsByteArray()

        assertTrue(backup.isNotEmpty())
    }

    @Test
    fun importFromByteArray_restoresWholeProtoAfterMutation() = runTest(testDispatcher) {
        val original = TestProtoData(
            id = 2,
            name = "Original",
            profile = TestProfile(
                nickname = "first",
                age = 30,
                address = TestAddress(street = "One", city = "A", zipCode = "1000"),
            ),
        )
        protoDatastore.data().set(original)
        val backup = protoDatastore.exportAsByteArray()

        protoDatastore.data().set(TestProtoData(id = 3, name = "Changed"))
        protoDatastore.importFromByteArray(backup)

        assertEquals(original, protoDatastore.data().get())
    }

    @Test
    fun importFromByteArray_replacesWholeProto() = runTest(testDispatcher) {
        val imported = TestProtoData(id = 4, name = "Imported")
        protoDatastore.data().set(imported)
        val backup = protoDatastore.exportAsByteArray()

        val local = TestProtoData(
            id = 5,
            name = "Local",
            profile = TestProfile(
                nickname = "local",
                age = 40,
                address = TestAddress(street = "Two", city = "B", zipCode = "2000"),
            ),
        )
        protoDatastore.data().set(local)

        protoDatastore.importFromByteArray(backup)

        assertEquals(imported, protoDatastore.data().get())
    }

    @Test
    fun factoryCreatedDatastore_supportsByteBackupApisThroughContract() = runTest(testDispatcher) {
        val datastore: ProtoDatastore<TestProtoData> = protoDatastore
        val original = TestProtoData(id = 6, name = "Contract")
        datastore.data().set(original)
        val backup = datastore.exportAsByteArray()

        datastore.data().set(TestProtoData(id = 7, name = "Mutated"))
        datastore.importFromByteArray(backup)

        assertEquals(original, datastore.data().get())
    }

    @Test
    fun directGenericProtoDatastore_withoutBackupWiring_throwsUnsupported() = runTest(testDispatcher) {
        val directDatastore = GenericProtoDatastore(
            datastore = protoDatastore.datastore,
            defaultValue = TestProtoData(),
        )

        assertFailsWith<UnsupportedOperationException> {
            directDatastore.exportAsByteArray()
        }
        assertFailsWith<UnsupportedOperationException> {
            directDatastore.importFromByteArray(ByteArray(0))
        }
    }
}
