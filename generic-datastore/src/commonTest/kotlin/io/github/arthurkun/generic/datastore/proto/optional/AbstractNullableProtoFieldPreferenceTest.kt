package io.github.arthurkun.generic.datastore.proto.optional

import io.github.arthurkun.generic.datastore.proto.GenericProtoDatastore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

abstract class AbstractNullableProtoFieldPreferenceTest {

    abstract val nullableProtoDatastore: GenericProtoDatastore<TestNullableProtoData>
    abstract val testDispatcher: TestDispatcher

    // ---- Nullable top-level scalar (label: String?) ----

    @Test
    fun field_getNullLabelWhenNotSet() = runTest(testDispatcher) {
        val labelPref = nullableProtoDatastore.field(
            key = "label",
            defaultValue = null as String?,
            getter = { it.label },
            updater = { proto, value -> proto.copy(label = value) },
        )
        assertNull(labelPref.get())
    }

    @Test
    fun field_setLabelToNonNull() = runTest(testDispatcher) {
        val labelPref = nullableProtoDatastore.field(
            key = "label",
            defaultValue = null as String?,
            getter = { it.label },
            updater = { proto, value -> proto.copy(label = value) },
        )
        labelPref.set("hello")
        assertEquals("hello", labelPref.get())
    }

    @Test
    fun field_setLabelToNull() = runTest(testDispatcher) {
        val labelPref = nullableProtoDatastore.field(
            key = "label",
            defaultValue = null as String?,
            getter = { it.label },
            updater = { proto, value -> proto.copy(label = value) },
        )
        labelPref.set("hello")
        labelPref.set(null)
        assertNull(labelPref.get())
    }

    @Test
    fun field_deleteLabelResetsToNull() = runTest(testDispatcher) {
        val labelPref = nullableProtoDatastore.field(
            key = "label",
            defaultValue = null as String?,
            getter = { it.label },
            updater = { proto, value -> proto.copy(label = value) },
        )
        labelPref.set("toDelete")
        labelPref.delete()
        assertNull(labelPref.get())
    }

    @Test
    fun field_asFlowLabelNullTransitions() = runTest(testDispatcher) {
        val labelPref = nullableProtoDatastore.field(
            key = "label",
            defaultValue = null as String?,
            getter = { it.label },
            updater = { proto, value -> proto.copy(label = value) },
        )
        assertNull(labelPref.asFlow().first())
        labelPref.set("hello")
        assertEquals("hello", labelPref.asFlow().first())
        labelPref.set(null)
        assertNull(labelPref.asFlow().first())
    }

    // ---- Nullable message field (profile: TestNullableProfile?) ----

    @Test
    fun field_getNullProfileWhenNotSet() = runTest(testDispatcher) {
        val profilePref = nullableProtoDatastore.field(
            key = "profile",
            defaultValue = null as TestNullableProfile?,
            getter = { it.profile },
            updater = { proto, value -> proto.copy(profile = value) },
        )
        assertNull(profilePref.get())
    }

    @Test
    fun field_setProfileToNonNull() = runTest(testDispatcher) {
        val profilePref = nullableProtoDatastore.field(
            key = "profile",
            defaultValue = null as TestNullableProfile?,
            getter = { it.profile },
            updater = { proto, value -> proto.copy(profile = value) },
        )
        val profile = TestNullableProfile(nickname = "Nick", age = 25)
        profilePref.set(profile)
        assertEquals(profile, profilePref.get())
    }

    @Test
    fun field_setProfileToNull() = runTest(testDispatcher) {
        val profilePref = nullableProtoDatastore.field(
            key = "profile",
            defaultValue = null as TestNullableProfile?,
            getter = { it.profile },
            updater = { proto, value -> proto.copy(profile = value) },
        )
        profilePref.set(TestNullableProfile(nickname = "Nick"))
        profilePref.set(null)
        assertNull(profilePref.get())
    }

    @Test
    fun field_updateNullableMessage() = runTest(testDispatcher) {
        val profilePref = nullableProtoDatastore.field(
            key = "profile",
            defaultValue = null as TestNullableProfile?,
            getter = { it.profile },
            updater = { proto, value -> proto.copy(profile = value) },
        )
        profilePref.set(TestNullableProfile(nickname = "Original"))
        profilePref.update { it?.copy(nickname = "updated") }
        assertEquals("updated", profilePref.get()?.nickname)
    }

    // ---- Non-null scalar inside nullable parent (profile?.nickname) ----

    @Test
    fun field_getNicknameDefaultWhenProfileNull() = runTest(testDispatcher) {
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
        assertEquals("", nicknamePref.get())
    }

    @Test
    fun field_setNicknameAutoCreatesProfile() = runTest(testDispatcher) {
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
        nicknamePref.set("AutoCreated")
        assertEquals("AutoCreated", nicknamePref.get())
    }

    @Test
    fun field_setNicknameDoesNotAffectAge() = runTest(testDispatcher) {
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
        val agePref = nullableProtoDatastore.field(
            key = "profile_age",
            defaultValue = null as Int?,
            getter = { it.profile?.age },
            updater = { proto, value ->
                proto.copy(
                    profile = (proto.profile ?: TestNullableProfile()).copy(age = value),
                )
            },
        )
        agePref.set(30)
        nicknamePref.set("Nick")
        assertEquals(30, agePref.get())
    }

    // ---- Nullable scalar inside nullable parent (profile?.age: Int?) ----

    @Test
    fun field_getNullAgeWhenProfileNull() = runTest(testDispatcher) {
        val agePref = nullableProtoDatastore.field(
            key = "profile_age",
            defaultValue = null as Int?,
            getter = { it.profile?.age },
            updater = { proto, value ->
                proto.copy(
                    profile = (proto.profile ?: TestNullableProfile()).copy(age = value),
                )
            },
        )
        assertNull(agePref.get())
    }

    @Test
    fun field_setAgeAutoCreatesProfile() = runTest(testDispatcher) {
        val agePref = nullableProtoDatastore.field(
            key = "profile_age",
            defaultValue = null as Int?,
            getter = { it.profile?.age },
            updater = { proto, value ->
                proto.copy(
                    profile = (proto.profile ?: TestNullableProfile()).copy(age = value),
                )
            },
        )
        agePref.set(25)
        assertEquals(25, agePref.get())
    }

    @Test
    fun field_setAgeNullWithoutClearingProfile() = runTest(testDispatcher) {
        val agePref = nullableProtoDatastore.field(
            key = "profile_age",
            defaultValue = null as Int?,
            getter = { it.profile?.age },
            updater = { proto, value ->
                proto.copy(
                    profile = (proto.profile ?: TestNullableProfile()).copy(age = value),
                )
            },
        )
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
        nicknamePref.set("KeepMe")
        agePref.set(25)
        agePref.set(null)
        assertNull(agePref.get())
        assertEquals("KeepMe", nicknamePref.get())
    }

    @Test
    fun field_deleteAgeResetsToNull() = runTest(testDispatcher) {
        val agePref = nullableProtoDatastore.field(
            key = "profile_age",
            defaultValue = null as Int?,
            getter = { it.profile?.age },
            updater = { proto, value ->
                proto.copy(
                    profile = (proto.profile ?: TestNullableProfile()).copy(age = value),
                )
            },
        )
        agePref.set(25)
        agePref.delete()
        assertNull(agePref.get())
    }

    // ---- Deeply nested non-null scalar through nullable chain (profile?.address?.city) ----

    @Test
    fun field_getCityDefaultWhenChainNull() = runTest(testDispatcher) {
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
        assertEquals("", cityPref.get())
    }

    @Test
    fun field_setCityAutoCreatesBothParents() = runTest(testDispatcher) {
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
        cityPref.set("Tokyo")
        assertEquals("Tokyo", cityPref.get())
    }

    @Test
    fun field_setCityDoesNotAffectStreetOrCoordinates() = runTest(testDispatcher) {
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
        val streetPref = nullableProtoDatastore.field(
            key = "profile_address_street",
            defaultValue = "",
            getter = { it.profile?.address?.street ?: "" },
            updater = { proto, value ->
                val currentProfile = proto.profile ?: TestNullableProfile()
                val currentAddress = currentProfile.address ?: TestNullableAddress()
                proto.copy(
                    profile = currentProfile.copy(
                        address = currentAddress.copy(street = value),
                    ),
                )
            },
        )
        val coordsPref = nullableProtoDatastore.field(
            key = "profile_address_coordinates",
            defaultValue = null as TestCoordinates?,
            getter = { it.profile?.address?.coordinates },
            updater = { proto, value ->
                val currentProfile = proto.profile ?: TestNullableProfile()
                val currentAddress = currentProfile.address ?: TestNullableAddress()
                proto.copy(
                    profile = currentProfile.copy(
                        address = currentAddress.copy(coordinates = value),
                    ),
                )
            },
        )
        streetPref.set("123 Main")
        coordsPref.set(TestCoordinates(1.0, 2.0))
        cityPref.set("Berlin")
        assertEquals("123 Main", streetPref.get())
        assertEquals(TestCoordinates(1.0, 2.0), coordsPref.get())
    }

    @Test
    fun field_updateCityAppendsSuffix() = runTest(testDispatcher) {
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
        cityPref.set("New")
        cityPref.update { "$it York" }
        assertEquals("New York", cityPref.get())
    }

    @Test
    fun field_deleteCityWithoutNullifyingParents() = runTest(testDispatcher) {
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
        val streetPref = nullableProtoDatastore.field(
            key = "profile_address_street",
            defaultValue = "",
            getter = { it.profile?.address?.street ?: "" },
            updater = { proto, value ->
                val currentProfile = proto.profile ?: TestNullableProfile()
                val currentAddress = currentProfile.address ?: TestNullableAddress()
                proto.copy(
                    profile = currentProfile.copy(
                        address = currentAddress.copy(street = value),
                    ),
                )
            },
        )
        streetPref.set("KeepStreet")
        cityPref.set("DeleteCity")
        cityPref.delete()
        assertEquals("", cityPref.get())
        assertEquals("KeepStreet", streetPref.get())
    }

    // ---- Nullable message at deepest level (profile?.address?.coordinates) ----

    @Test
    fun field_getCoordinatesNullWhenNotSet() = runTest(testDispatcher) {
        val coordsPref = nullableProtoDatastore.field(
            key = "profile_address_coordinates",
            defaultValue = null as TestCoordinates?,
            getter = { it.profile?.address?.coordinates },
            updater = { proto, value ->
                val currentProfile = proto.profile ?: TestNullableProfile()
                val currentAddress = currentProfile.address ?: TestNullableAddress()
                proto.copy(
                    profile = currentProfile.copy(
                        address = currentAddress.copy(coordinates = value),
                    ),
                )
            },
        )
        assertNull(coordsPref.get())
    }

    @Test
    fun field_setCoordinatesAutoCreatesParents() = runTest(testDispatcher) {
        val coordsPref = nullableProtoDatastore.field(
            key = "profile_address_coordinates",
            defaultValue = null as TestCoordinates?,
            getter = { it.profile?.address?.coordinates },
            updater = { proto, value ->
                val currentProfile = proto.profile ?: TestNullableProfile()
                val currentAddress = currentProfile.address ?: TestNullableAddress()
                proto.copy(
                    profile = currentProfile.copy(
                        address = currentAddress.copy(coordinates = value),
                    ),
                )
            },
        )
        coordsPref.set(TestCoordinates(1.0, 2.0))
        assertEquals(TestCoordinates(1.0, 2.0), coordsPref.get())
    }

    @Test
    fun field_setCoordinatesNullWithoutAffectingCity() = runTest(testDispatcher) {
        val coordsPref = nullableProtoDatastore.field(
            key = "profile_address_coordinates",
            defaultValue = null as TestCoordinates?,
            getter = { it.profile?.address?.coordinates },
            updater = { proto, value ->
                val currentProfile = proto.profile ?: TestNullableProfile()
                val currentAddress = currentProfile.address ?: TestNullableAddress()
                proto.copy(
                    profile = currentProfile.copy(
                        address = currentAddress.copy(coordinates = value),
                    ),
                )
            },
        )
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
        cityPref.set("KeepCity")
        coordsPref.set(TestCoordinates(1.0, 2.0))
        coordsPref.set(null)
        assertNull(coordsPref.get())
        assertEquals("KeepCity", cityPref.get())
    }

    @Test
    fun field_deleteCoordinatesResetsToNull() = runTest(testDispatcher) {
        val coordsPref = nullableProtoDatastore.field(
            key = "profile_address_coordinates",
            defaultValue = null as TestCoordinates?,
            getter = { it.profile?.address?.coordinates },
            updater = { proto, value ->
                val currentProfile = proto.profile ?: TestNullableProfile()
                val currentAddress = currentProfile.address ?: TestNullableAddress()
                proto.copy(
                    profile = currentProfile.copy(
                        address = currentAddress.copy(coordinates = value),
                    ),
                )
            },
        )
        coordsPref.set(TestCoordinates(1.0, 2.0))
        coordsPref.delete()
        assertNull(coordsPref.get())
    }

    // ---- Cross-level isolation with nullable parents ----

    @Test
    fun field_crossLevelIsolation() = runTest(testDispatcher) {
        val labelPref = nullableProtoDatastore.field(
            key = "label",
            defaultValue = null as String?,
            getter = { it.label },
            updater = { proto, value -> proto.copy(label = value) },
        )
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
        labelPref.set("MyLabel")
        nicknamePref.set("Nick")
        cityPref.set("Paris")
        assertEquals("MyLabel", labelPref.get())
        assertEquals("Nick", nicknamePref.get())
        assertEquals("Paris", cityPref.get())
    }

    @Test
    fun field_setProfileNullClearsNestedButNotLabel() = runTest(testDispatcher) {
        val labelPref = nullableProtoDatastore.field(
            key = "label",
            defaultValue = null as String?,
            getter = { it.label },
            updater = { proto, value -> proto.copy(label = value) },
        )
        val profilePref = nullableProtoDatastore.field(
            key = "profile",
            defaultValue = null as TestNullableProfile?,
            getter = { it.profile },
            updater = { proto, value -> proto.copy(profile = value) },
        )
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
        labelPref.set("KeepLabel")
        nicknamePref.set("Nick")
        profilePref.set(null)
        assertEquals("KeepLabel", labelPref.get())
        assertEquals("", nicknamePref.get())
    }
}
