package io.github.arthurkun.generic.datastore.compose.app.domain

import io.github.arthurkun.generic.datastore.preferences.PreferencesDatastore
import io.github.arthurkun.generic.datastore.preferences.backup.PreferencesBackup
import io.github.arthurkun.generic.datastore.preferences.batch.BatchWriteScope
import io.github.arthurkun.generic.datastore.preferences.enum
import io.github.arthurkun.generic.datastore.preferences.enumSet
import io.github.arthurkun.generic.datastore.preferences.kserialized
import io.github.arthurkun.generic.datastore.preferences.kserializedList
import io.github.arthurkun.generic.datastore.preferences.kserializedSet
import io.github.arthurkun.generic.datastore.preferences.map
import io.github.arthurkun.generic.datastore.preferences.mapIO
import io.github.arthurkun.generic.datastore.preferences.nullableEnum
import io.github.arthurkun.generic.datastore.preferences.nullableKserialized
import io.github.arthurkun.generic.datastore.preferences.nullableKserializedList
import io.github.arthurkun.generic.datastore.preferences.toJsonElement
import io.github.arthurkun.generic.datastore.preferences.toJsonMap
import io.github.arthurkun.generic.datastore.preferences.toggle
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json
import kotlin.time.Instant

class PreferenceStore(
    val datastore: PreferencesDatastore,
) {

    val theme = datastore.enum(
        "theme",
        Theme.SYSTEM,
    )

    val text = datastore.string(
        "text",
        defaultValue = "Hello World!",
    )

    val num = datastore.int(
        "num",
        defaultValue = 0,
    )

    val bool = datastore.bool(
        "bool",
        defaultValue = false,
    )

    val customObject = datastore.serialized(
        key = "animal",
        defaultValue = Animal.Dog,
        serializer = { Animal.to(it) },
        deserializer = { Animal.from(it) },
    )

    val duration = datastore.long(
        key = "duration",
        defaultValue = 0L,
    ).mapIO(
        convert = {
            Instant.fromEpochMilliseconds(it)
        },
        reverse = {
            it.toEpochMilliseconds()
        },
    )

    val userProfile = datastore.kserialized(
        key = "user_profile",
        defaultValue = UserProfile(name = "John", age = 25),
    )

    val animalSet = datastore.serializedSet(
        key = "animal_set",
        defaultValue = emptySet(),
        serializer = { Animal.to(it) },
        deserializer = { Animal.from(it) },
    )

    val userProfileSet = datastore.kserializedSet<UserProfile>(
        key = "user_profile_set",
        defaultValue = emptySet(),
    )

    val themeSet = datastore.enumSet<Theme>(
        key = "theme_set",
        defaultValue = emptySet(),
    )

    suspend fun exportPreferences(json: Json? = null): String =
        datastore.exportAsString(json = json)

    suspend fun importPreferences(backupString: String, json: Json? = null) =
        datastore.importDataAsString(backupString = backupString, json = json)

    suspend fun batchWriteBlock(block: BatchWriteScope.() -> Unit) = datastore.batchWrite(block)

    @Suppress("DEPRECATION")
    suspend fun runApiCoverageShowcase(json: Json? = null): String {
        val backup = datastore.exportAsData()
        return try {
            val floatPref = datastore.float("api_float", 1.5f)
            val doublePref = datastore.double("api_double", 2.5)
            val stringSetPref = datastore.stringSet("api_string_set", setOf("alpha"))
            val nullableStringPref = datastore.nullableString("api_nullable_string")
            val nullableStringSetPref = datastore.nullableStringSet("api_nullable_string_set")
            val nullableIntPref = datastore.nullableInt("api_nullable_int")
            val nullableLongPref = datastore.nullableLong("api_nullable_long")
            val nullableFloatPref = datastore.nullableFloat("api_nullable_float")
            val nullableDoublePref = datastore.nullableDouble("api_nullable_double")
            val nullableBoolPref = datastore.nullableBool("api_nullable_bool")
            val nullableEnumPref = datastore.nullableEnum<Theme>("api_nullable_theme")
            val serializedListPref = datastore.serializedList(
                key = "api_serialized_list",
                defaultValue = emptyList(),
                serializer = { Animal.to(it) },
                deserializer = { Animal.from(it) },
            )
            val kserializedListPref = datastore.kserializedList<UserProfile>(
                key = "api_kserialized_list",
                defaultValue = emptyList(),
            )
            val nullableSerializedPref = datastore.nullableSerialized(
                key = "api_nullable_animal",
                serializer = { Animal.to(it) },
                deserializer = { Animal.from(it) },
            )
            val nullableKserializedPref = datastore.nullableKserialized<UserProfile>("api_nullable_profile")
            val nullableSerializedListPref = datastore.nullableSerializedList(
                key = "api_nullable_serialized_list",
                serializer = { Animal.to(it) },
                deserializer = { Animal.from(it) },
            )
            val nullableKserializedListPref = datastore.nullableKserializedList<UserProfile>(
                key = "api_nullable_kserialized_list",
            )
            val mappedNum = num.map(
                defaultValue = "num=0",
                convert = { "num=$it" },
                reverse = { it.removePrefix("num=").toIntOrNull() ?: 0 },
            )

            floatPref.set(3.5f)
            doublePref.set(4.5)
            stringSetPref.set(setOf("alpha", "beta"))
            nullableStringPref.set("nullable")
            nullableStringSetPref.set(setOf("one", "two"))
            nullableIntPref.set(7)
            nullableLongPref.set(8L)
            nullableFloatPref.set(9.5f)
            nullableDoublePref.set(10.5)
            nullableBoolPref.set(true)
            nullableEnumPref.set(Theme.DARK)
            serializedListPref.set(listOf(Animal.Cat, Animal.Dog))
            kserializedListPref.set(listOf(UserProfile("Ada", 36)))
            nullableSerializedPref.set(Animal.Cat)
            nullableKserializedPref.set(UserProfile("Lin", 31))
            nullableSerializedListPref.set(listOf(Animal.Dog))
            nullableKserializedListPref.set(listOf(UserProfile("Grace", 40)))
            mappedNum.set("num=12")
            bool.toggle()
            bool.toggle()

            val batchFlow = datastore.batchReadFlow {
                Triple(get(text), get(num), get(bool))
            }
            val batchFlowValue = batchFlow.getFirstForShowcase()
            val batchReadValue = datastore.batchRead {
                Triple(get(text), get(num), get(bool))
            }
            datastore.batchUpdate {
                set(text, "API Coverage")
                update(num) { it + 1 }
                resetToDefault(bool)
            }
            val blockingValue = datastore.batchReadBlocking {
                Triple(get(text), get(num), get(bool))
            }
            datastore.batchWriteBlocking {
                set(text, batchReadValue.first)
                set(num, batchReadValue.second)
                set(bool, batchReadValue.third)
            }
            datastore.batchUpdateBlocking {
                delete(nullableStringPref)
                resetToDefault(floatPref)
            }

            val backupData: PreferencesBackup = datastore.exportAsData()
            val backupString = datastore.exportAsString(json = json)
            datastore.importData(backupData)
            datastore.importDataAsString(backupString, json = json)

            val deprecatedExport = datastore.export()
            datastore.import(
                mapOf(
                    "api_legacy_string" to "legacy",
                    "api_legacy_int" to 1,
                    "api_legacy_set" to listOf("a", "b"),
                ),
            )
            mapOf("sample" to listOf("value")).toJsonElement()
            backupString.toJsonMap()

            datastore.clearAll()
            datastore.importData(backup)

            listOf(
                batchFlowValue,
                blockingValue,
                "backup=${backupData.preferences.size}",
                "legacy=${deprecatedExport.size}",
            ).joinToString(prefix = "Preferences APIs covered: ")
        } catch (failure: Throwable) {
            datastore.clearAll()
            datastore.importData(backup)
            throw failure
        }
    }
}

private suspend fun <T> kotlinx.coroutines.flow.Flow<T>.getFirstForShowcase(): T = first()
