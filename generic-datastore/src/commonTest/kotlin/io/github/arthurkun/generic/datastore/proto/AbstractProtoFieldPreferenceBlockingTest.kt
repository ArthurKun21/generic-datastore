package io.github.arthurkun.generic.datastore.proto

import kotlin.test.Test
import kotlin.test.assertEquals

abstract class AbstractProtoFieldPreferenceBlockingTest {

    abstract val protoDatastore: GenericProtoDatastore<TestProtoData>

    // ---- Top-level (level 1) ----

    @Test
    fun field_getBlockingReturnsDefault() {
        val namePref = protoDatastore.field(
            key = "name",
            defaultValue = "",
            getter = { it.name },
            updater = { proto, value -> proto.copy(name = value) },
        )
        assertEquals("", namePref.getBlocking())
    }

    @Test
    fun field_setBlockingAndGetBlocking() {
        val namePref = protoDatastore.field(
            key = "name",
            defaultValue = "",
            getter = { it.name },
            updater = { proto, value -> proto.copy(name = value) },
        )
        namePref.setBlocking("BlockingName")
        assertEquals("BlockingName", namePref.getBlocking())
    }

    @Test
    fun field_resetToDefaultBlocking() {
        val namePref = protoDatastore.field(
            key = "name",
            defaultValue = "",
            getter = { it.name },
            updater = { proto, value -> proto.copy(name = value) },
        )
        namePref.setBlocking("ToReset")
        namePref.resetToDefaultBlocking()
        assertEquals("", namePref.getBlocking())
    }

    @Test
    fun field_propertyDelegation() {
        val namePref = protoDatastore.field(
            key = "name",
            defaultValue = "",
            getter = { it.name },
            updater = { proto, value -> proto.copy(name = value) },
        )
        var name: String by namePref
        name = "Delegated"
        assertEquals("Delegated", name)
    }

    // ---- Nested (level 2 — profile.*) ----

    @Test
    fun field_getBlockingReturnsDefaultForNickname() {
        val nicknamePref = protoDatastore.field(
            key = "profile_nickname",
            defaultValue = "",
            getter = { it.profile.nickname },
            updater = { proto, value ->
                proto.copy(profile = proto.profile.copy(nickname = value))
            },
        )
        assertEquals("", nicknamePref.getBlocking())
    }

    @Test
    fun field_setBlockingAgeRoundTrip() {
        val agePref = protoDatastore.field(
            key = "profile_age",
            defaultValue = 0,
            getter = { it.profile.age },
            updater = { proto, value ->
                proto.copy(profile = proto.profile.copy(age = value))
            },
        )
        agePref.setBlocking(25)
        assertEquals(25, agePref.getBlocking())
    }

    @Test
    fun field_propertyDelegationForNickname() {
        val nicknamePref = protoDatastore.field(
            key = "profile_nickname",
            defaultValue = "",
            getter = { it.profile.nickname },
            updater = { proto, value ->
                proto.copy(profile = proto.profile.copy(nickname = value))
            },
        )
        var nickname: String by nicknamePref
        nickname = "DelegatedNick"
        assertEquals("DelegatedNick", nickname)
    }

    // ---- Deeply nested (level 3 — profile.address.*) ----

    @Test
    fun field_getBlockingReturnsDefaultForCity() {
        val cityPref = protoDatastore.field(
            key = "profile_address_city",
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
        assertEquals("", cityPref.getBlocking())
    }

    @Test
    fun field_setBlockingCityRoundTrip() {
        val cityPref = protoDatastore.field(
            key = "profile_address_city",
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
        cityPref.setBlocking("Berlin")
        assertEquals("Berlin", cityPref.getBlocking())
    }

    @Test
    fun field_resetToDefaultBlockingOnStreetDoesNotAffectSiblings() {
        val streetPref = protoDatastore.field(
            key = "profile_address_street",
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
        val cityPref = protoDatastore.field(
            key = "profile_address_city",
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
        streetPref.setBlocking("Reset St")
        cityPref.setBlocking("KeepCity")
        streetPref.resetToDefaultBlocking()
        assertEquals("", streetPref.getBlocking())
        assertEquals("KeepCity", cityPref.getBlocking())
    }

    @Test
    fun field_propertyDelegationForZipCode() {
        val zipPref = protoDatastore.field(
            key = "profile_address_zip",
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
        var zip: String by zipPref
        zip = "99999"
        assertEquals("99999", zip)
    }
}
