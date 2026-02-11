package io.github.arthurkun.generic.datastore.proto

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

abstract class AbstractNullableProtoFieldPreferenceBlockingTest {

    abstract val nullableProtoDatastore: GenericProtoDatastore<TestNullableProtoData>

    @Test
    fun field_getBlockingNullForNullableDefault() {
        val labelPref = nullableProtoDatastore.field(
            key = "label",
            defaultValue = null as String?,
            getter = { it.label },
            updater = { proto, value -> proto.copy(label = value) },
        )
        assertNull(labelPref.getBlocking())
    }

    @Test
    fun field_setBlockingAndGetBlockingNullableScalar() {
        val labelPref = nullableProtoDatastore.field(
            key = "label",
            defaultValue = null as String?,
            getter = { it.label },
            updater = { proto, value -> proto.copy(label = value) },
        )
        labelPref.setBlocking("test")
        assertEquals("test", labelPref.getBlocking())
    }

    @Test
    fun field_setBlockingNullClearsNullableField() {
        val labelPref = nullableProtoDatastore.field(
            key = "label",
            defaultValue = null as String?,
            getter = { it.label },
            updater = { proto, value -> proto.copy(label = value) },
        )
        labelPref.setBlocking("test")
        labelPref.setBlocking(null)
        assertNull(labelPref.getBlocking())
    }

    @Test
    fun field_resetToDefaultBlockingNullableField() {
        val labelPref = nullableProtoDatastore.field(
            key = "label",
            defaultValue = null as String?,
            getter = { it.label },
            updater = { proto, value -> proto.copy(label = value) },
        )
        labelPref.setBlocking("test")
        labelPref.resetToDefaultBlocking()
        assertNull(labelPref.getBlocking())
    }

    @Test
    fun field_propertyDelegationNullableField() {
        val labelPref = nullableProtoDatastore.field(
            key = "label",
            defaultValue = null as String?,
            getter = { it.label },
            updater = { proto, value -> proto.copy(label = value) },
        )
        var label: String? by labelPref
        assertNull(label)
        label = "delegated"
        assertEquals("delegated", label)
        label = null
        assertNull(label)
    }

    @Test
    fun field_getBlockingNonNullInsideNullableParent() {
        val nicknamePref = nullableProtoDatastore.field(
            key = "profile_nickname",
            defaultValue = "",
            getter = { it.profile?.nickname ?: "" },
            updater = { proto, value ->
                proto.copy(
                    profile = (proto.profile ?: TestNullableProfile()).copy(nickname = value),
                )
            },
        )
        assertEquals("", nicknamePref.getBlocking())
    }

    @Test
    fun field_setBlockingDeeplyNestedAutoCreatesParents() {
        val cityPref = nullableProtoDatastore.field(
            key = "profile_address_city",
            defaultValue = "",
            getter = { it.profile?.address?.city ?: "" },
            updater = { proto, value ->
                val currentProfile = proto.profile ?: TestNullableProfile()
                val currentAddress = currentProfile.address ?: TestNullableAddress()
                proto.copy(
                    profile = currentProfile.copy(
                        address = currentAddress.copy(city = value),
                    ),
                )
            },
        )
        cityPref.setBlocking("Tokyo")
        assertEquals("Tokyo", cityPref.getBlocking())
    }

    @Test
    fun field_propertyDelegationDeeplyNested() {
        val cityPref = nullableProtoDatastore.field(
            key = "profile_address_city",
            defaultValue = "",
            getter = { it.profile?.address?.city ?: "" },
            updater = { proto, value ->
                val currentProfile = proto.profile ?: TestNullableProfile()
                val currentAddress = currentProfile.address ?: TestNullableAddress()
                proto.copy(
                    profile = currentProfile.copy(
                        address = currentAddress.copy(city = value),
                    ),
                )
            },
        )
        var city: String by cityPref
        assertEquals("", city)
        city = "Berlin"
        assertEquals("Berlin", city)
    }
}
