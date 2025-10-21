package io.github.arthurkun.generic.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@Serializable
data class TestUser(
    val id: Int = 0,
    val name: String = "",
    val email: String = "",
    val isActive: Boolean = false,
)

@Serializable
data class TestSettings(
    val theme: String = "light",
    val fontSize: Int = 14,
    val notifications: Boolean = true,
)

@OptIn(ExperimentalCoroutinesApi::class)
class KSerializerPreferenceTest {

    private lateinit var testFile: File
    private lateinit var datastore: DataStore<Preferences>
    private lateinit var preferenceDatastore: GenericPreferenceDatastore

    @Before
    fun setup() {
        testFile = createTempFile(suffix = ".preferences_pb")
        val testDispatcher = UnconfinedTestDispatcher()
        datastore =
            PreferenceDataStoreFactory.create(
                scope = kotlinx.coroutines.CoroutineScope(testDispatcher),
            ) {
                testFile
            }
        preferenceDatastore = GenericPreferenceDatastore(datastore)
    }

    @After
    fun cleanup() {
        testFile.delete()
    }

    @Test
    fun `kserializer preference stores and retrieves object correctly`() =
        runTest {
            val userPref =
                preferenceDatastore.kserializer(
                    key = "user",
                    defaultValue = TestUser(),
                    serializer = serializer<TestUser>(),
                )

            val testUser = TestUser(id = 1, name = "John Doe", email = "john@example.com", isActive = true)
            userPref.set(testUser)

            val retrieved = userPref.get()
            assertEquals(testUser, retrieved)
        }

    @Test
    fun `kserializer preference returns default when not set`() =
        runTest {
            val defaultUser = TestUser(id = 0, name = "Guest")
            val userPref =
                preferenceDatastore.kserializer(
                    key = "user",
                    defaultValue = defaultUser,
                    serializer = serializer<TestUser>(),
                )

            val retrieved = userPref.get()
            assertEquals(defaultUser, retrieved)
        }

    @Test
    fun `kserializer preference can be deleted`() =
        runTest {
            val userPref =
                preferenceDatastore.kserializer(
                    key = "user",
                    defaultValue = TestUser(),
                    serializer = serializer<TestUser>(),
                )

            val testUser = TestUser(id = 1, name = "John Doe")
            userPref.set(testUser)
            assertEquals(testUser, userPref.get())

            userPref.delete()
            assertEquals(TestUser(), userPref.get())
        }

    @Test
    fun `kserializer preference flow emits updates`() =
        runTest {
            val userPref =
                preferenceDatastore.kserializer(
                    key = "user",
                    defaultValue = TestUser(),
                    serializer = serializer<TestUser>(),
                )

            val initialValue = userPref.asFlow().first()
            assertEquals(TestUser(), initialValue)

            val testUser = TestUser(id = 1, name = "John Doe")
            userPref.set(testUser)

            val updatedValue = userPref.asFlow().first()
            assertEquals(testUser, updatedValue)
        }

    @Test
    fun `kserializer preference with custom Json configuration`() =
        runTest {
            val customJson = Json { prettyPrint = true }
            val settingsPref =
                preferenceDatastore.kserializer(
                    key = "settings",
                    defaultValue = TestSettings(),
                    serializer = serializer<TestSettings>(),
                    json = customJson,
                )

            val testSettings = TestSettings(theme = "dark", fontSize = 16, notifications = false)
            settingsPref.set(testSettings)

            val retrieved = settingsPref.get()
            assertEquals(testSettings, retrieved)
        }

    @Test
    fun `kserializerList preference stores and retrieves list correctly`() =
        runTest {
            val userListPref =
                preferenceDatastore.kserializerList(
                    key = "users",
                    defaultValue = emptyList<TestUser>(),
                    serializer = serializer<TestUser>(),
                )

            val testUsers =
                listOf(
                    TestUser(id = 1, name = "Alice", email = "alice@example.com"),
                    TestUser(id = 2, name = "Bob", email = "bob@example.com"),
                    TestUser(id = 3, name = "Charlie", email = "charlie@example.com"),
                )
            userListPref.set(testUsers)

            val retrieved = userListPref.get()
            assertEquals(3, retrieved.size)
            assertEquals(testUsers, retrieved)
        }

    @Test
    fun `kserializerList preference returns default empty list when not set`() =
        runTest {
            val userListPref =
                preferenceDatastore.kserializerList(
                    key = "users",
                    defaultValue = emptyList<TestUser>(),
                    serializer = serializer<TestUser>(),
                )

            val retrieved = userListPref.get()
            assertTrue(retrieved.isEmpty())
        }

    @Test
    fun `kserializerList preference can be updated with new list`() =
        runTest {
            val userListPref =
                preferenceDatastore.kserializerList(
                    key = "users",
                    defaultValue = emptyList<TestUser>(),
                    serializer = serializer<TestUser>(),
                )

            val initialUsers = listOf(TestUser(id = 1, name = "Alice"))
            userListPref.set(initialUsers)
            assertEquals(1, userListPref.get().size)

            val updatedUsers =
                listOf(
                    TestUser(id = 1, name = "Alice"),
                    TestUser(id = 2, name = "Bob"),
                )
            userListPref.set(updatedUsers)
            assertEquals(2, userListPref.get().size)
        }

    @Test
    fun `kserializerList preference can be deleted`() =
        runTest {
            val userListPref =
                preferenceDatastore.kserializerList(
                    key = "users",
                    defaultValue = emptyList<TestUser>(),
                    serializer = serializer<TestUser>(),
                )

            val testUsers = listOf(TestUser(id = 1, name = "Alice"))
            userListPref.set(testUsers)
            assertEquals(1, userListPref.get().size)

            userListPref.delete()
            assertTrue(userListPref.get().isEmpty())
        }

    @Test
    fun `kserializerList preference flow emits updates`() =
        runTest {
            val userListPref =
                preferenceDatastore.kserializerList(
                    key = "users",
                    defaultValue = emptyList<TestUser>(),
                    serializer = serializer<TestUser>(),
                )

            val initialValue = userListPref.asFlow().first()
            assertTrue(initialValue.isEmpty())

            val testUsers = listOf(TestUser(id = 1, name = "Alice"))
            userListPref.set(testUsers)

            val updatedValue = userListPref.asFlow().first()
            assertEquals(1, updatedValue.size)
            assertEquals(testUsers, updatedValue)
        }

    @Test
    fun `kserializerList preference handles empty list`() =
        runTest {
            val userListPref =
                preferenceDatastore.kserializerList(
                    key = "users",
                    defaultValue = emptyList<TestUser>(),
                    serializer = serializer<TestUser>(),
                )

            userListPref.set(emptyList())
            val retrieved = userListPref.get()
            assertTrue(retrieved.isEmpty())
        }

    @Test
    fun `kserializer and kserializerList work with batch operations`() =
        runTest {
            val userPref =
                preferenceDatastore.kserializer(
                    key = "user",
                    defaultValue = TestUser(),
                    serializer = serializer<TestUser>(),
                )

            val userListPref =
                preferenceDatastore.kserializerList(
                    key = "users",
                    defaultValue = emptyList<TestUser>(),
                    serializer = serializer<TestUser>(),
                )

            val singleUser = TestUser(id = 1, name = "John Doe")
            val multipleUsers =
                listOf(
                    TestUser(id = 2, name = "Alice"),
                    TestUser(id = 3, name = "Bob"),
                )

            // Batch set
            preferenceDatastore.batchSet(
                mapOf(
                    userPref to singleUser,
                    userListPref to multipleUsers,
                ),
            )

            // Verify
            assertEquals(singleUser, userPref.get())
            assertEquals(multipleUsers, userListPref.get())

            // Batch delete
            preferenceDatastore.batchDelete(listOf(userPref, userListPref))

            assertEquals(TestUser(), userPref.get())
            assertTrue(userListPref.get().isEmpty())
        }

    @Test
    fun `kserializer preference validates blank keys`() =
        runTest {
            try {
                preferenceDatastore.kserializer(
                    key = "",
                    defaultValue = TestUser(),
                    serializer = serializer<TestUser>(),
                )
                throw AssertionError("Expected IllegalArgumentException for blank key")
            } catch (e: IllegalArgumentException) {
                assertEquals("Preference key must not be blank", e.message)
            }
        }

    @Test
    fun `kserializerList preference validates blank keys`() =
        runTest {
            try {
                preferenceDatastore.kserializerList(
                    key = "   ",
                    defaultValue = emptyList<TestUser>(),
                    serializer = serializer<TestUser>(),
                )
                throw AssertionError("Expected IllegalArgumentException for blank key")
            } catch (e: IllegalArgumentException) {
                assertEquals("Preference key must not be blank", e.message)
            }
        }

    @Test
    fun `kserializer preference works with caching`() =
        runTest {
            // Enable caching
            GenericPreference.cacheEnabled = true

            val userPref =
                preferenceDatastore.kserializer(
                    key = "user",
                    defaultValue = TestUser(),
                    serializer = serializer<TestUser>(),
                )

            val testUser = TestUser(id = 1, name = "John Doe")
            userPref.set(testUser)

            // First read - should cache
            val first = userPref.get()
            assertEquals(testUser, first)

            // Second read - should use cache
            val second = userPref.get()
            assertEquals(testUser, second)

            // Cache invalidation
            userPref.invalidateCache()
            val third = userPref.get()
            assertEquals(testUser, third)
        }
}
