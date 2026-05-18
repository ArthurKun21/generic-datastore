package io.github.arthurkun.generic.datastore.proto.core

import io.github.arthurkun.generic.datastore.proto.GenericProtoDatastore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

abstract class AbstractProtoFieldPreferenceTest {

    abstract val protoDatastore: GenericProtoDatastore<TestProtoData>
    abstract val testDispatcher: TestDispatcher

    // ---- Top-level field tests (level 1) ----

    @Test
    fun field_readsTopLevelName() = runTest(testDispatcher) {
        val namePref = protoDatastore.field(
            defaultValue = "",
            getter = { it.name },
            updater = { proto, value -> proto.copy(name = value) },
        )
        namePref.set("Alice")
        assertEquals("Alice", namePref.get())
    }

    @Test
    fun field_readsTopLevelId() = runTest(testDispatcher) {
        val idPref = protoDatastore.field(
            defaultValue = 0,
            getter = { it.id },
            updater = { proto, value -> proto.copy(id = value) },
        )
        idPref.set(42)
        assertEquals(42, idPref.get())
    }

    @Test
    fun field_getReturnsDefaultWhenProtoAtDefault() = runTest(testDispatcher) {
        val namePref = protoDatastore.field(
            defaultValue = "",
            getter = { it.name },
            updater = { proto, value -> proto.copy(name = value) },
        )
        assertEquals("", namePref.get())
    }

    @Test
    fun field_setUpdatesOnlyTargetedField() = runTest(testDispatcher) {
        val namePref = protoDatastore.field(
            defaultValue = "",
            getter = { it.name },
            updater = { proto, value -> proto.copy(name = value) },
        )
        val idPref = protoDatastore.field(
            defaultValue = 0,
            getter = { it.id },
            updater = { proto, value -> proto.copy(id = value) },
        )
        idPref.set(10)
        namePref.set("Bob")
        assertEquals("Bob", namePref.get())
        assertEquals(10, idPref.get())
    }

    @Test
    fun field_asFlowEmitsWhenFieldChanges() = runTest(testDispatcher) {
        val namePref = protoDatastore.field(
            defaultValue = "",
            getter = { it.name },
            updater = { proto, value -> proto.copy(name = value) },
        )
        assertEquals("", namePref.asFlow().first())
        namePref.set("Charlie")
        assertEquals("Charlie", namePref.asFlow().first())
    }

    @Test
    fun field_asFlowReEmitsWhenUnrelatedFieldChanges() = runTest(testDispatcher) {
        val namePref = protoDatastore.field(
            defaultValue = "",
            getter = { it.name },
            updater = { proto, value -> proto.copy(name = value) },
        )
        val idPref = protoDatastore.field(
            defaultValue = 0,
            getter = { it.id },
            updater = { proto, value -> proto.copy(id = value) },
        )
        namePref.set("Dave")
        idPref.set(99)
        // name should still be readable after unrelated field change
        assertEquals("Dave", namePref.asFlow().first())
    }

    @Test
    fun field_updateAtomicallyReadsAndWrites() = runTest(testDispatcher) {
        val idPref = protoDatastore.field(
            defaultValue = 0,
            getter = { it.id },
            updater = { proto, value -> proto.copy(id = value) },
        )
        idPref.set(5)
        idPref.update { it + 10 }
        assertEquals(15, idPref.get())
    }

    @Test
    fun field_deleteResetsToDefault() = runTest(testDispatcher) {
        val namePref = protoDatastore.field(
            defaultValue = "",
            getter = { it.name },
            updater = { proto, value -> proto.copy(name = value) },
        )
        namePref.set("ToDelete")
        namePref.delete()
        assertEquals("", namePref.get())
    }

    @Test
    fun field_resetToDefaultResetsToDefault() = runTest(testDispatcher) {
        val namePref = protoDatastore.field(
            defaultValue = "",
            getter = { it.name },
            updater = { proto, value -> proto.copy(name = value) },
        )
        namePref.set("ToReset")
        namePref.resetToDefault()
        assertEquals("", namePref.get())
    }

    @Test
    fun field_keyReturnsConfiguredKey() = runTest(testDispatcher) {
        val namePref = protoDatastore.field(
            defaultValue = "",
            getter = { it.name },
            updater = { proto, value -> proto.copy(name = value) },
        )
        assertEquals("proto_datastore", namePref.key())
    }

    @Test
    fun field_usesDatastoreKey() {
        val namePref = protoDatastore.field(
            defaultValue = "",
            getter = { it.name },
            updater = { proto, value -> proto.copy(name = value) },
        )
        assertEquals("proto_datastore", namePref.key())
    }

    // ---- Nested field tests (level 2 — profile.*) ----

    @Test
    fun field_readsProfileNickname() = runTest(testDispatcher) {
        val nicknamePref = protoDatastore.field(
            defaultValue = "",
            getter = { it.profile.nickname },
            updater = { proto, value ->
                proto.copy(profile = proto.profile.copy(nickname = value))
            },
        )
        nicknamePref.set("Nick")
        assertEquals("Nick", nicknamePref.get())
    }

    @Test
    fun field_getReturnsDefaultForProfileNickname() = runTest(testDispatcher) {
        val nicknamePref = protoDatastore.field(
            defaultValue = "",
            getter = { it.profile.nickname },
            updater = { proto, value ->
                proto.copy(profile = proto.profile.copy(nickname = value))
            },
        )
        assertEquals("", nicknamePref.get())
    }

    @Test
    fun field_setOnNicknameDoesNotAffectAgeOrTopLevel() = runTest(testDispatcher) {
        val nicknamePref = protoDatastore.field(
            defaultValue = "",
            getter = { it.profile.nickname },
            updater = { proto, value ->
                proto.copy(profile = proto.profile.copy(nickname = value))
            },
        )
        val agePref = protoDatastore.field(
            defaultValue = 0,
            getter = { it.profile.age },
            updater = { proto, value ->
                proto.copy(profile = proto.profile.copy(age = value))
            },
        )
        val namePref = protoDatastore.field(
            defaultValue = "",
            getter = { it.name },
            updater = { proto, value -> proto.copy(name = value) },
        )
        agePref.set(25)
        namePref.set("TopLevel")
        nicknamePref.set("Nick")
        assertEquals(25, agePref.get())
        assertEquals("TopLevel", namePref.get())
    }

    @Test
    fun field_updateOnAgeAtomicallyIncrements() = runTest(testDispatcher) {
        val agePref = protoDatastore.field(
            defaultValue = 0,
            getter = { it.profile.age },
            updater = { proto, value ->
                proto.copy(profile = proto.profile.copy(age = value))
            },
        )
        agePref.set(20)
        agePref.update { it + 1 }
        assertEquals(21, agePref.get())
    }

    // ---- Deeply nested field tests (level 3 — profile.address.*) ----

    @Test
    fun field_readsProfileAddressCity() = runTest(testDispatcher) {
        val cityPref = protoDatastore.field(
            defaultValue = "",
            getter = { it.profile.address.city },
            updater = { proto, value ->
                proto.copy(
                    profile = proto.profile.copy(
                        address = proto.profile.address.copy(city = value),
                    ),
                )
            },
        )
        cityPref.set("Tokyo")
        assertEquals("Tokyo", cityPref.get())
    }

    @Test
    fun field_getReturnsDefaultForCity() = runTest(testDispatcher) {
        val cityPref = protoDatastore.field(
            defaultValue = "",
            getter = { it.profile.address.city },
            updater = { proto, value ->
                proto.copy(
                    profile = proto.profile.copy(
                        address = proto.profile.address.copy(city = value),
                    ),
                )
            },
        )
        assertEquals("", cityPref.get())
    }

    @Test
    fun field_setOnCityDoesNotAffectStreetOrZipCode() = runTest(testDispatcher) {
        val cityPref = protoDatastore.field(
            defaultValue = "",
            getter = { it.profile.address.city },
            updater = { proto, value ->
                proto.copy(
                    profile = proto.profile.copy(
                        address = proto.profile.address.copy(city = value),
                    ),
                )
            },
        )
        val streetPref = protoDatastore.field(
            defaultValue = "",
            getter = { it.profile.address.street },
            updater = { proto, value ->
                proto.copy(
                    profile = proto.profile.copy(
                        address = proto.profile.address.copy(street = value),
                    ),
                )
            },
        )
        val zipPref = protoDatastore.field(
            defaultValue = "",
            getter = { it.profile.address.zipCode },
            updater = { proto, value ->
                proto.copy(
                    profile = proto.profile.copy(
                        address = proto.profile.address.copy(zipCode = value),
                    ),
                )
            },
        )
        streetPref.set("123 Main St")
        zipPref.set("90210")
        cityPref.set("LA")
        assertEquals("123 Main St", streetPref.get())
        assertEquals("90210", zipPref.get())
    }

    @Test
    fun field_setOnStreetDoesNotAffectNicknameOrName() = runTest(testDispatcher) {
        val streetPref = protoDatastore.field(
            defaultValue = "",
            getter = { it.profile.address.street },
            updater = { proto, value ->
                proto.copy(
                    profile = proto.profile.copy(
                        address = proto.profile.address.copy(street = value),
                    ),
                )
            },
        )
        val nicknamePref = protoDatastore.field(
            defaultValue = "",
            getter = { it.profile.nickname },
            updater = { proto, value ->
                proto.copy(profile = proto.profile.copy(nickname = value))
            },
        )
        val namePref = protoDatastore.field(
            defaultValue = "",
            getter = { it.name },
            updater = { proto, value -> proto.copy(name = value) },
        )
        nicknamePref.set("Nick")
        namePref.set("Top")
        streetPref.set("456 Oak Ave")
        assertEquals("Nick", nicknamePref.get())
        assertEquals("Top", namePref.get())
    }

    @Test
    fun field_asFlowOnZipCodeEmitsWhenChanged() = runTest(testDispatcher) {
        val zipPref = protoDatastore.field(
            defaultValue = "",
            getter = { it.profile.address.zipCode },
            updater = { proto, value ->
                proto.copy(
                    profile = proto.profile.copy(
                        address = proto.profile.address.copy(zipCode = value),
                    ),
                )
            },
        )
        assertEquals("", zipPref.asFlow().first())
        zipPref.set("12345")
        assertEquals("12345", zipPref.asFlow().first())
    }

    @Test
    fun field_updateOnCityAtomicallyAppendsSuffix() = runTest(testDispatcher) {
        val cityPref = protoDatastore.field(
            defaultValue = "",
            getter = { it.profile.address.city },
            updater = { proto, value ->
                proto.copy(
                    profile = proto.profile.copy(
                        address = proto.profile.address.copy(city = value),
                    ),
                )
            },
        )
        cityPref.set("New")
        cityPref.update { "$it York" }
        assertEquals("New York", cityPref.get())
    }

    @Test
    fun field_deleteOnCityResetsWithoutAffectingSiblings() = runTest(testDispatcher) {
        val cityPref = protoDatastore.field(
            defaultValue = "",
            getter = { it.profile.address.city },
            updater = { proto, value ->
                proto.copy(
                    profile = proto.profile.copy(
                        address = proto.profile.address.copy(city = value),
                    ),
                )
            },
        )
        val streetPref = protoDatastore.field(
            defaultValue = "",
            getter = { it.profile.address.street },
            updater = { proto, value ->
                proto.copy(
                    profile = proto.profile.copy(
                        address = proto.profile.address.copy(street = value),
                    ),
                )
            },
        )
        streetPref.set("Keep St")
        cityPref.set("DeleteMe")
        cityPref.delete()
        assertEquals("", cityPref.get())
        assertEquals("Keep St", streetPref.get())
    }

    @Test
    fun field_multipleFieldsFromDifferentLevelsCoexist() = runTest(testDispatcher) {
        val namePref = protoDatastore.field(
            defaultValue = "",
            getter = { it.name },
            updater = { proto, value -> proto.copy(name = value) },
        )
        val agePref = protoDatastore.field(
            defaultValue = 0,
            getter = { it.profile.age },
            updater = { proto, value ->
                proto.copy(profile = proto.profile.copy(age = value))
            },
        )
        val cityPref = protoDatastore.field(
            defaultValue = "",
            getter = { it.profile.address.city },
            updater = { proto, value ->
                proto.copy(
                    profile = proto.profile.copy(
                        address = proto.profile.address.copy(city = value),
                    ),
                )
            },
        )
        namePref.set("Alice")
        agePref.set(30)
        cityPref.set("Paris")
        assertEquals("Alice", namePref.get())
        assertEquals(30, agePref.get())
        assertEquals("Paris", cityPref.get())
    }
}
