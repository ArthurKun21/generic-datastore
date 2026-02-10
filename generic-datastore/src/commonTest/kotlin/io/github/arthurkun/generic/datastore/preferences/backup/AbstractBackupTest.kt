package io.github.arthurkun.generic.datastore.preferences.backup

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import io.github.arthurkun.generic.datastore.core.BasePreference
import io.github.arthurkun.generic.datastore.preferences.GenericPreferencesDatastore
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

abstract class AbstractBackupTest {

    abstract val preferenceDatastore: GenericPreferencesDatastore
    abstract val dataStore: DataStore<Preferences>
    abstract val testDispatcher: TestDispatcher

    // region Export tests

    @Test
    fun exportAsData_emptyDatastore_returnsEmptyBackup() = runTest(testDispatcher) {
        val backup = preferenceDatastore.exportAsData()
        assertTrue(backup.preferences.isEmpty())
    }

    @Test
    fun exportAsData_singleStringPreference() = runTest(testDispatcher) {
        val pref = preferenceDatastore.string("backupStr", "default")
        pref.set("hello")

        val backup = preferenceDatastore.exportAsData()
        val entry = backup.preferences.single()
        assertEquals("backupStr", entry.key)
        assertEquals(StringPreferenceValue("hello"), entry.value)
    }

    @Test
    fun exportAsData_allPrimitiveTypes() = runTest(testDispatcher) {
        preferenceDatastore.int("bkInt", 0).set(42)
        preferenceDatastore.long("bkLong", 0L).set(100L)
        preferenceDatastore.float("bkFloat", 0f).set(1.5f)
        preferenceDatastore.double("bkDouble", 0.0).set(2.5)
        preferenceDatastore.string("bkString", "").set("test")
        preferenceDatastore.bool("bkBool", false).set(true)
        preferenceDatastore.stringSet("bkStringSet", emptySet()).set(setOf("a", "b"))

        val backup = preferenceDatastore.exportAsData()
        val map = backup.preferences.associateBy { it.key }

        assertEquals(7, map.size)
        assertEquals(IntPreferenceValue(42), map["bkInt"]?.value)
        assertEquals(LongPreferenceValue(100L), map["bkLong"]?.value)
        assertEquals(FloatPreferenceValue(1.5f), map["bkFloat"]?.value)
        assertEquals(DoublePreferenceValue(2.5), map["bkDouble"]?.value)
        assertEquals(StringPreferenceValue("test"), map["bkString"]?.value)
        assertEquals(BooleanPreferenceValue(true), map["bkBool"]?.value)
        assertEquals(StringSetPreferenceValue(setOf("a", "b")), map["bkStringSet"]?.value)
    }

    @Test
    fun exportAsData_excludesPrivateByDefault() = runTest(testDispatcher) {
        preferenceDatastore.string("publicKey", "").set("public")
        val privateKey = BasePreference.privateKey("secretKey")
        dataStore.edit { it[stringPreferencesKey(privateKey)] = "secret" }

        val backup = preferenceDatastore.exportAsData()
        val keys = backup.preferences.map { it.key }
        assertTrue(keys.contains("publicKey"))
        assertTrue(!keys.contains(privateKey))
    }

    @Test
    fun exportAsData_includesPrivateWhenRequested() = runTest(testDispatcher) {
        preferenceDatastore.string("publicKey2", "").set("public")
        val privateKey = BasePreference.privateKey("secretKey2")
        dataStore.edit { it[stringPreferencesKey(privateKey)] = "secret" }

        val backup = preferenceDatastore.exportAsData(exportPrivate = true)
        val keys = backup.preferences.map { it.key }
        assertTrue(keys.contains("publicKey2"))
        assertTrue(keys.contains(privateKey))
    }

    @Test
    fun exportAsData_excludesAppStateByDefault() = runTest(testDispatcher) {
        preferenceDatastore.string("normalKey", "").set("normal")
        val appStateKey = BasePreference.appStateKey("stateKey")
        dataStore.edit { it[stringPreferencesKey(appStateKey)] = "state" }

        val backup = preferenceDatastore.exportAsData()
        val keys = backup.preferences.map { it.key }
        assertTrue(keys.contains("normalKey"))
        assertTrue(!keys.contains(appStateKey))
    }

    @Test
    fun exportAsData_includesAppStateWhenRequested() = runTest(testDispatcher) {
        preferenceDatastore.string("normalKey2", "").set("normal")
        val appStateKey = BasePreference.appStateKey("stateKey2")
        dataStore.edit { it[stringPreferencesKey(appStateKey)] = "state" }

        val backup = preferenceDatastore.exportAsData(exportAppState = true)
        val keys = backup.preferences.map { it.key }
        assertTrue(keys.contains("normalKey2"))
        assertTrue(keys.contains(appStateKey))
    }

    @Test
    fun exportAsString_producesValidJson() = runTest(testDispatcher) {
        preferenceDatastore.int("jsonInt", 0).set(7)
        preferenceDatastore.string("jsonStr", "").set("value")

        val jsonString = preferenceDatastore.exportAsString()
        val decoded = Json.decodeFromString<PreferencesBackup>(jsonString)
        val dataBackup = preferenceDatastore.exportAsData()

        assertEquals(dataBackup.preferences.toSet(), decoded.preferences.toSet())
    }

    // endregion

    // region Import tests

    @Test
    fun importData_restoresStringPreference() = runTest(testDispatcher) {
        val backup = PreferencesBackup(
            preferences = listOf(
                BackupPreference("importStr", StringPreferenceValue("imported")),
            ),
        )
        preferenceDatastore.importData(backup)

        val pref = preferenceDatastore.string("importStr", "default")
        assertEquals("imported", pref.get())
    }

    @Test
    fun importData_restoresAllPrimitiveTypes() = runTest(testDispatcher) {
        val backup = PreferencesBackup(
            preferences = listOf(
                BackupPreference("impInt", IntPreferenceValue(99)),
                BackupPreference("impLong", LongPreferenceValue(999L)),
                BackupPreference("impFloat", FloatPreferenceValue(3.14f)),
                BackupPreference("impDouble", DoublePreferenceValue(2.718)),
                BackupPreference("impString", StringPreferenceValue("hello")),
                BackupPreference("impBool", BooleanPreferenceValue(true)),
                BackupPreference("impStringSet", StringSetPreferenceValue(setOf("x", "y"))),
            ),
        )
        preferenceDatastore.importData(backup)

        assertEquals(99, preferenceDatastore.int("impInt", 0).get())
        assertEquals(999L, preferenceDatastore.long("impLong", 0L).get())
        assertEquals(3.14f, preferenceDatastore.float("impFloat", 0f).get())
        assertEquals(2.718, preferenceDatastore.double("impDouble", 0.0).get())
        assertEquals("hello", preferenceDatastore.string("impString", "").get())
        assertEquals(true, preferenceDatastore.bool("impBool", false).get())
        assertEquals(
            setOf("x", "y"),
            preferenceDatastore.stringSet("impStringSet", emptySet()).get(),
        )
    }

    @Test
    fun importData_skipsPrivateByDefault() = runTest(testDispatcher) {
        val privateKey = BasePreference.privateKey("privImport")
        val backup = PreferencesBackup(
            preferences = listOf(
                BackupPreference(privateKey, StringPreferenceValue("secret")),
            ),
        )
        preferenceDatastore.importData(backup)

        val pref = preferenceDatastore.string(privateKey, "default")
        assertEquals("default", pref.get())
    }

    @Test
    fun importData_includesPrivateWhenRequested() = runTest(testDispatcher) {
        val privateKey = BasePreference.privateKey("privImport2")
        val backup = PreferencesBackup(
            preferences = listOf(
                BackupPreference(privateKey, StringPreferenceValue("secret")),
            ),
        )
        preferenceDatastore.importData(backup, importPrivate = true)

        val pref = preferenceDatastore.string(privateKey, "default")
        assertEquals("secret", pref.get())
    }

    @Test
    fun importData_skipsAppStateByDefault() = runTest(testDispatcher) {
        val appStateKey = BasePreference.appStateKey("stateImport")
        val backup = PreferencesBackup(
            preferences = listOf(
                BackupPreference(appStateKey, StringPreferenceValue("state")),
            ),
        )
        preferenceDatastore.importData(backup)

        val pref = preferenceDatastore.string(appStateKey, "default")
        assertEquals("default", pref.get())
    }

    @Test
    fun importData_includesAppStateWhenRequested() = runTest(testDispatcher) {
        val appStateKey = BasePreference.appStateKey("stateImport2")
        val backup = PreferencesBackup(
            preferences = listOf(
                BackupPreference(appStateKey, StringPreferenceValue("state")),
            ),
        )
        preferenceDatastore.importData(backup, importAppState = true)

        val pref = preferenceDatastore.string(appStateKey, "default")
        assertEquals("state", pref.get())
    }

    @Test
    fun importData_preservesExistingKeys() = runTest(testDispatcher) {
        preferenceDatastore.string("existingKey", "default").set("existing")

        val backup = PreferencesBackup(
            preferences = listOf(
                BackupPreference("otherKey", StringPreferenceValue("other")),
            ),
        )
        preferenceDatastore.importData(backup)

        assertEquals("existing", preferenceDatastore.string("existingKey", "default").get())
        assertEquals("other", preferenceDatastore.string("otherKey", "default").get())
    }

    @Test
    fun importData_overwritesExistingKeys() = runTest(testDispatcher) {
        preferenceDatastore.string("overwriteKey", "default").set("old")

        val backup = PreferencesBackup(
            preferences = listOf(
                BackupPreference("overwriteKey", StringPreferenceValue("new")),
            ),
        )
        preferenceDatastore.importData(backup)

        assertEquals("new", preferenceDatastore.string("overwriteKey", "default").get())
    }

    // endregion

    // region Round-trip tests

    @Test
    fun roundTrip_exportThenImport_preservesData() = runTest(testDispatcher) {
        preferenceDatastore.int("rtInt", 0).set(10)
        preferenceDatastore.long("rtLong", 0L).set(20L)
        preferenceDatastore.float("rtFloat", 0f).set(3.0f)
        preferenceDatastore.double("rtDouble", 0.0).set(4.0)
        preferenceDatastore.string("rtString", "").set("round")
        preferenceDatastore.bool("rtBool", false).set(true)
        preferenceDatastore.stringSet("rtStringSet", emptySet()).set(setOf("r", "t"))

        val backup = preferenceDatastore.exportAsData()

        preferenceDatastore.clearAll()

        preferenceDatastore.importData(backup)

        assertEquals(10, preferenceDatastore.int("rtInt", 0).get())
        assertEquals(20L, preferenceDatastore.long("rtLong", 0L).get())
        assertEquals(3.0f, preferenceDatastore.float("rtFloat", 0f).get())
        assertEquals(4.0, preferenceDatastore.double("rtDouble", 0.0).get())
        assertEquals("round", preferenceDatastore.string("rtString", "").get())
        assertEquals(true, preferenceDatastore.bool("rtBool", false).get())
        assertEquals(
            setOf("r", "t"),
            preferenceDatastore.stringSet("rtStringSet", emptySet()).get(),
        )
    }

    @Test
    fun roundTrip_exportAsStringThenImportAsString() = runTest(testDispatcher) {
        preferenceDatastore.int("rtStrInt", 0).set(5)
        preferenceDatastore.string("rtStrStr", "").set("trip")
        preferenceDatastore.bool("rtStrBool", false).set(true)

        val jsonString = preferenceDatastore.exportAsString()

        preferenceDatastore.clearAll()

        preferenceDatastore.importDataAsString(jsonString)

        assertEquals(5, preferenceDatastore.int("rtStrInt", 0).get())
        assertEquals("trip", preferenceDatastore.string("rtStrStr", "").get())
        assertEquals(true, preferenceDatastore.bool("rtStrBool", false).get())
    }

    // endregion

    // region Error handling

    @Test
    fun importDataAsString_malformedJson_throwsBackupParsingException() = runTest(testDispatcher) {
        assertFailsWith<BackupParsingException> {
            preferenceDatastore.importDataAsString("{invalid json!!}")
        }
    }

    // endregion

    // region PreferenceValue.fromAny tests

    @Test
    fun preferenceValue_fromAny_int() {
        assertEquals(IntPreferenceValue(42), PreferenceValue.fromAny(42))
    }

    @Test
    fun preferenceValue_fromAny_long() {
        assertEquals(LongPreferenceValue(42L), PreferenceValue.fromAny(42L))
    }

    @Test
    fun preferenceValue_fromAny_float() {
        assertEquals(FloatPreferenceValue(1.5f), PreferenceValue.fromAny(1.5f))
    }

    @Test
    fun preferenceValue_fromAny_double() {
        assertEquals(DoublePreferenceValue(1.5), PreferenceValue.fromAny(1.5))
    }

    @Test
    fun preferenceValue_fromAny_string() {
        assertEquals(StringPreferenceValue("hello"), PreferenceValue.fromAny("hello"))
    }

    @Test
    fun preferenceValue_fromAny_boolean() {
        assertEquals(BooleanPreferenceValue(true), PreferenceValue.fromAny(true))
    }

    @Test
    fun preferenceValue_fromAny_stringSet() {
        assertEquals(
            StringSetPreferenceValue(setOf("a", "b")),
            PreferenceValue.fromAny(setOf("a", "b")),
        )
    }

    @Test
    fun preferenceValue_fromAny_null_returnsNull() {
        assertNull(PreferenceValue.fromAny(null))
    }

    @Test
    fun preferenceValue_fromAny_unsupportedType_returnsNull() {
        assertNull(PreferenceValue.fromAny(listOf(1)))
    }

    // endregion
}
