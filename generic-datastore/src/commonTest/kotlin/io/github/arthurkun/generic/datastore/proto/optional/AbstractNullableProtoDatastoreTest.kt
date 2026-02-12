package io.github.arthurkun.generic.datastore.proto.optional

import io.github.arthurkun.generic.datastore.proto.GenericProtoDatastore
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.plus
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

abstract class AbstractNullableProtoDatastoreTest {

    abstract val protoDatastore: GenericProtoDatastore<TestNullableProtoData>
    abstract val testDispatcher: TestDispatcher

    // ── data() whole-object tests ────────────────────────────────────────

    @Test
    fun data_defaultValueWhenNotSet() = runTest(testDispatcher) {
        val pref = protoDatastore.data()
        assertEquals(TestNullableProtoData(), pref.get())
    }

    @Test
    fun data_setAndGetValue() = runTest(testDispatcher) {
        val pref = protoDatastore.data()
        val value = TestNullableProtoData(id = 1, name = "Alice")
        pref.set(value)
        assertEquals(value, pref.get())
    }

    @Test
    fun data_setWithNullableFieldsPopulated() = runTest(testDispatcher) {
        val pref = protoDatastore.data()
        val value = TestNullableProtoData(
            id = 1,
            name = "Bob",
            label = "admin",
            profile = TestNullableProfile(
                nickname = "bobby",
                age = 30,
                address = TestNullableAddress(
                    street = "123 Main St",
                    city = "Springfield",
                    coordinates = TestCoordinates(40.0, -89.0),
                ),
            ),
        )
        pref.set(value)
        assertEquals(value, pref.get())
    }

    @Test
    fun data_setWithNullableFieldsNull() = runTest(testDispatcher) {
        val pref = protoDatastore.data()
        val value = TestNullableProtoData(
            id = 2,
            name = "Charlie",
            label = null,
            profile = null,
        )
        pref.set(value)
        assertEquals(value, pref.get())
        assertNull(pref.get().label)
        assertNull(pref.get().profile)
    }

    @Test
    fun data_observeDefaultValue() = runTest(testDispatcher) {
        val pref = protoDatastore.data()
        val value = pref.asFlow().first()
        assertEquals(TestNullableProtoData(), value)
    }

    @Test
    fun data_observeSetValue() = runTest(testDispatcher) {
        val pref = protoDatastore.data()
        val value = TestNullableProtoData(id = 3, name = "Dave", label = "test")
        pref.set(value)
        val observed = pref.asFlow().first()
        assertEquals(value, observed)
    }

    @Test
    fun data_deleteResetsToDefault() = runTest(testDispatcher) {
        val pref = protoDatastore.data()
        pref.set(TestNullableProtoData(id = 4, name = "Eve", label = "temp"))
        assertEquals(TestNullableProtoData(id = 4, name = "Eve", label = "temp"), pref.get())

        pref.delete()
        assertEquals(TestNullableProtoData(), pref.get())
    }

    @Test
    fun data_updateValue() = runTest(testDispatcher) {
        val pref = protoDatastore.data()
        pref.set(TestNullableProtoData(id = 1, name = "Start"))
        pref.update { it.copy(id = it.id + 10, name = "Updated") }
        assertEquals(TestNullableProtoData(id = 11, name = "Updated"), pref.get())
    }

    @Test
    fun data_updateNullableFieldFromNullToValue() = runTest(testDispatcher) {
        val pref = protoDatastore.data()
        pref.set(TestNullableProtoData(id = 1, name = "Test", label = null))
        assertNull(pref.get().label)

        pref.update { it.copy(label = "assigned") }
        assertEquals("assigned", pref.get().label)
    }

    @Test
    fun data_updateNullableFieldFromValueToNull() = runTest(testDispatcher) {
        val pref = protoDatastore.data()
        pref.set(TestNullableProtoData(id = 1, name = "Test", label = "hasValue"))
        assertEquals("hasValue", pref.get().label)

        pref.update { it.copy(label = null) }
        assertNull(pref.get().label)
    }

    @Test
    fun data_resetToDefault() = runTest(testDispatcher) {
        val pref = protoDatastore.data()
        pref.set(
            TestNullableProtoData(
                id = 5,
                name = "Changed",
                label = "label",
                profile = TestNullableProfile(nickname = "nick"),
            ),
        )
        pref.resetToDefault()
        assertEquals(TestNullableProtoData(), pref.get())
    }

    @Test
    fun data_keyReturnsConfiguredKey() = runTest(testDispatcher) {
        val pref = protoDatastore.data()
        assertEquals("proto_datastore", pref.key())
    }

    @Test
    fun data_customKey() = runTest(testDispatcher) {
        val customDatastore = GenericProtoDatastore(
            datastore = protoDatastore.datastore,
            defaultValue = TestNullableProtoData(),
            key = "custom_key",
        )
        val pref = customDatastore.data()
        assertEquals("custom_key", pref.key())
    }

    @Test
    fun protoPreference_blankKeyThrows() = runTest(testDispatcher) {
        assertFailsWith<IllegalArgumentException> {
            GenericProtoDatastore(
                datastore = protoDatastore.datastore,
                defaultValue = TestNullableProtoData(),
                key = " ",
            ).data()
        }
    }

    // ── field() – top-level non-nullable fields ─────────────────────────

    @Test
    fun field_idDefaultValue() = runTest(testDispatcher) {
        val idPref = protoDatastore.field(
            defaultValue = 0,
            getter = { it.id },
            updater = { proto, value -> proto.copy(id = value) },
        )
        assertEquals(0, idPref.get())
    }

    @Test
    fun field_idSetAndGet() = runTest(testDispatcher) {
        val idPref = protoDatastore.field(
            defaultValue = 0,
            getter = { it.id },
            updater = { proto, value -> proto.copy(id = value) },
        )
        idPref.set(42)
        assertEquals(42, idPref.get())
    }

    @Test
    fun field_nameSetAndGet() = runTest(testDispatcher) {
        val namePref = protoDatastore.field(
            defaultValue = "",
            getter = { it.name },
            updater = { proto, value -> proto.copy(name = value) },
        )
        namePref.set("Alice")
        assertEquals("Alice", namePref.get())
    }

    @Test
    fun field_nameObserveFlow() = runTest(testDispatcher) {
        val namePref = protoDatastore.field(
            defaultValue = "",
            getter = { it.name },
            updater = { proto, value -> proto.copy(name = value) },
        )
        namePref.set("Observed")
        assertEquals("Observed", namePref.asFlow().first())
    }

    @Test
    fun field_nameUpdate() = runTest(testDispatcher) {
        val namePref = protoDatastore.field(
            defaultValue = "",
            getter = { it.name },
            updater = { proto, value -> proto.copy(name = value) },
        )
        namePref.set("Hello")
        namePref.update { "$it World" }
        assertEquals("Hello World", namePref.get())
    }

    @Test
    fun field_nameDeleteResetsToDefault() = runTest(testDispatcher) {
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
    fun field_nameResetToDefault() = runTest(testDispatcher) {
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
    fun field_usesDatastoreKey() = runTest(testDispatcher) {
        val namePref = protoDatastore.field(
            defaultValue = "",
            getter = { it.name },
            updater = { proto, value -> proto.copy(name = value) },
        )
        assertEquals("proto_datastore", namePref.key())
    }

    // ── field() – top-level nullable fields ─────────────────────────────

    @Test
    fun field_labelDefaultIsNull() = runTest(testDispatcher) {
        val labelPref = protoDatastore.field(
            defaultValue = null as String?,
            getter = { it.label },
            updater = { proto, value -> proto.copy(label = value) },
        )
        assertNull(labelPref.get())
    }

    @Test
    fun field_labelSetNonNull() = runTest(testDispatcher) {
        val labelPref = protoDatastore.field(
            defaultValue = null as String?,
            getter = { it.label },
            updater = { proto, value -> proto.copy(label = value) },
        )
        labelPref.set("important")
        assertEquals("important", labelPref.get())
    }

    @Test
    fun field_labelSetNullExplicitly() = runTest(testDispatcher) {
        val labelPref = protoDatastore.field(
            defaultValue = null as String?,
            getter = { it.label },
            updater = { proto, value -> proto.copy(label = value) },
        )
        labelPref.set("temp")
        assertEquals("temp", labelPref.get())

        labelPref.set(null)
        assertNull(labelPref.get())
    }

    @Test
    fun field_labelObserveNull() = runTest(testDispatcher) {
        val labelPref = protoDatastore.field(
            defaultValue = null as String?,
            getter = { it.label },
            updater = { proto, value -> proto.copy(label = value) },
        )
        assertNull(labelPref.asFlow().first())
    }

    @Test
    fun field_labelUpdateFromNullToValue() = runTest(testDispatcher) {
        val labelPref = protoDatastore.field(
            defaultValue = null as String?,
            getter = { it.label },
            updater = { proto, value -> proto.copy(label = value) },
        )
        assertNull(labelPref.get())
        labelPref.update { "assigned" }
        assertEquals("assigned", labelPref.get())
    }

    @Test
    fun field_labelUpdateFromValueToNull() = runTest(testDispatcher) {
        val labelPref = protoDatastore.field(
            defaultValue = null as String?,
            getter = { it.label },
            updater = { proto, value -> proto.copy(label = value) },
        )
        labelPref.set("exists")
        labelPref.update { null }
        assertNull(labelPref.get())
    }

    @Test
    fun field_labelDeleteResetsToDefaultNull() = runTest(testDispatcher) {
        val labelPref = protoDatastore.field(
            defaultValue = null as String?,
            getter = { it.label },
            updater = { proto, value -> proto.copy(label = value) },
        )
        labelPref.set("willBeDeleted")
        labelPref.delete()
        assertNull(labelPref.get())
    }

    // ── field() – nullable nested object (profile) ──────────────────────

    @Test
    fun field_profileDefaultIsNull() = runTest(testDispatcher) {
        val profilePref = protoDatastore.field(
            defaultValue = null as TestNullableProfile?,
            getter = { it.profile },
            updater = { proto, value -> proto.copy(profile = value) },
        )
        assertNull(profilePref.get())
    }

    @Test
    fun field_profileSetAndGet() = runTest(testDispatcher) {
        val profilePref = protoDatastore.field(
            defaultValue = null as TestNullableProfile?,
            getter = { it.profile },
            updater = { proto, value -> proto.copy(profile = value) },
        )
        val profile = TestNullableProfile(nickname = "bobby", age = 25)
        profilePref.set(profile)
        assertEquals(profile, profilePref.get())
    }

    @Test
    fun field_profileSetToNull() = runTest(testDispatcher) {
        val profilePref = protoDatastore.field(
            defaultValue = null as TestNullableProfile?,
            getter = { it.profile },
            updater = { proto, value -> proto.copy(profile = value) },
        )
        profilePref.set(TestNullableProfile(nickname = "temp"))
        profilePref.set(null)
        assertNull(profilePref.get())
    }

    @Test
    fun field_profileUpdateNested() = runTest(testDispatcher) {
        val profilePref = protoDatastore.field(
            defaultValue = null as TestNullableProfile?,
            getter = { it.profile },
            updater = { proto, value -> proto.copy(profile = value) },
        )
        profilePref.set(TestNullableProfile(nickname = "old", age = 20))
        profilePref.update { it?.copy(nickname = "new", age = 21) }
        assertEquals(TestNullableProfile(nickname = "new", age = 21), profilePref.get())
    }

    @Test
    fun field_profileResetToDefaultNull() = runTest(testDispatcher) {
        val profilePref = protoDatastore.field(
            defaultValue = null as TestNullableProfile?,
            getter = { it.profile },
            updater = { proto, value -> proto.copy(profile = value) },
        )
        profilePref.set(TestNullableProfile(nickname = "test"))
        profilePref.resetToDefault()
        assertNull(profilePref.get())
    }

    // ── field() – deeply nested nullable field (profile.address) ────────

    @Test
    fun field_nestedAddressDefaultIsNull() = runTest(testDispatcher) {
        val addressPref = protoDatastore.field(
            defaultValue = null as TestNullableAddress?,
            getter = { it.profile?.address },
            updater = { proto, value ->
                proto.copy(
                    profile = (proto.profile ?: TestNullableProfile()).copy(address = value),
                )
            },
        )
        assertNull(addressPref.get())
    }

    @Test
    fun field_nestedAddressSetWhenProfileIsNull() = runTest(testDispatcher) {
        val addressPref = protoDatastore.field(
            defaultValue = null as TestNullableAddress?,
            getter = { it.profile?.address },
            updater = { proto, value ->
                proto.copy(
                    profile = (proto.profile ?: TestNullableProfile()).copy(address = value),
                )
            },
        )
        val address = TestNullableAddress(street = "123 Elm", city = "Portland")
        addressPref.set(address)
        assertEquals(address, addressPref.get())
    }

    @Test
    fun field_nestedAddressPreservesExistingProfileFields() = runTest(testDispatcher) {
        // First set a profile with no address
        val dataPref = protoDatastore.data()
        dataPref.set(
            TestNullableProtoData(
                id = 1,
                name = "Test",
                profile = TestNullableProfile(
                    nickname = "nick",
                    age = 30,
                    address = null,
                ),
            ),
        )

        val addressPref = protoDatastore.field(
            defaultValue = null as TestNullableAddress?,
            getter = { it.profile?.address },
            updater = { proto, value ->
                proto.copy(
                    profile = (proto.profile ?: TestNullableProfile()).copy(address = value),
                )
            },
        )

        addressPref.set(TestNullableAddress(street = "456 Oak", city = "Seattle"))

        // Verify address was set
        assertEquals("456 Oak", addressPref.get()?.street)

        // Verify existing profile fields are preserved
        val fullData = dataPref.get()
        assertEquals("nick", fullData.profile?.nickname)
        assertEquals(30, fullData.profile?.age)
    }

    // ── field() – 3-level nested nullable (profile.address.coordinates) ─

    @Test
    fun field_coordinatesDefaultIsNull() = runTest(testDispatcher) {
        val coordsPref = protoDatastore.field(
            defaultValue = null as TestCoordinates?,
            getter = { it.profile?.address?.coordinates },
            updater = { proto, value ->
                val profile = proto.profile ?: TestNullableProfile()
                val address = profile.address ?: TestNullableAddress()
                proto.copy(
                    profile = profile.copy(
                        address = address.copy(coordinates = value),
                    ),
                )
            },
        )
        assertNull(coordsPref.get())
    }

    @Test
    fun field_coordinatesSetWhenAllParentsNull() = runTest(testDispatcher) {
        val coordsPref = protoDatastore.field(
            defaultValue = null as TestCoordinates?,
            getter = { it.profile?.address?.coordinates },
            updater = { proto, value ->
                val profile = proto.profile ?: TestNullableProfile()
                val address = profile.address ?: TestNullableAddress()
                proto.copy(
                    profile = profile.copy(
                        address = address.copy(coordinates = value),
                    ),
                )
            },
        )
        coordsPref.set(TestCoordinates(48.8566, 2.3522))
        assertEquals(TestCoordinates(48.8566, 2.3522), coordsPref.get())
    }

    @Test
    fun field_coordinatesSetToNullClearsOnly() = runTest(testDispatcher) {
        val dataPref = protoDatastore.data()
        dataPref.set(
            TestNullableProtoData(
                id = 1,
                name = "Paris",
                profile = TestNullableProfile(
                    nickname = "tourist",
                    address = TestNullableAddress(
                        street = "Champs-Élysées",
                        city = "Paris",
                        coordinates = TestCoordinates(48.8566, 2.3522),
                    ),
                ),
            ),
        )

        val coordsPref = protoDatastore.field(
            defaultValue = null as TestCoordinates?,
            getter = { it.profile?.address?.coordinates },
            updater = { proto, value ->
                val profile = proto.profile ?: TestNullableProfile()
                val address = profile.address ?: TestNullableAddress()
                proto.copy(
                    profile = profile.copy(
                        address = address.copy(coordinates = value),
                    ),
                )
            },
        )

        coordsPref.set(null)
        assertNull(coordsPref.get())

        // Address and profile should still exist
        val data = dataPref.get()
        assertEquals("Champs-Élysées", data.profile?.address?.street)
        assertEquals("tourist", data.profile?.nickname)
    }

    // ── field() – nested non-nullable through nullable parent ───────────

    @Test
    fun field_nicknameViaProfile() = runTest(testDispatcher) {
        val nicknamePref = protoDatastore.field(
            defaultValue = "",
            getter = { it.profile?.nickname ?: "" },
            updater = { proto, value ->
                proto.copy(
                    profile = (proto.profile ?: TestNullableProfile()).copy(nickname = value),
                )
            },
        )
        assertEquals("", nicknamePref.get())

        nicknamePref.set("bobby")
        assertEquals("bobby", nicknamePref.get())
    }

    @Test
    fun field_ageViaProfile() = runTest(testDispatcher) {
        val agePref = protoDatastore.field(
            defaultValue = null as Int?,
            getter = { it.profile?.age },
            updater = { proto, value ->
                proto.copy(
                    profile = (proto.profile ?: TestNullableProfile()).copy(age = value),
                )
            },
        )
        assertNull(agePref.get())

        agePref.set(25)
        assertEquals(25, agePref.get())

        agePref.set(null)
        assertNull(agePref.get())
    }

    // ── field() – cross-field isolation ─────────────────────────────────

    @Test
    fun field_settingOneFieldDoesNotAffectAnother() = runTest(testDispatcher) {
        val idPref = protoDatastore.field(
            defaultValue = 0,
            getter = { it.id },
            updater = { proto, value -> proto.copy(id = value) },
        )
        val namePref = protoDatastore.field(
            defaultValue = "",
            getter = { it.name },
            updater = { proto, value -> proto.copy(name = value) },
        )
        val labelPref = protoDatastore.field(
            defaultValue = null as String?,
            getter = { it.label },
            updater = { proto, value -> proto.copy(label = value) },
        )

        idPref.set(100)
        namePref.set("Isolated")
        labelPref.set("tag")

        assertEquals(100, idPref.get())
        assertEquals("Isolated", namePref.get())
        assertEquals("tag", labelPref.get())

        // Update only id, others should remain
        idPref.set(200)
        assertEquals(200, idPref.get())
        assertEquals("Isolated", namePref.get())
        assertEquals("tag", labelPref.get())
    }

    // ── field() – full round-trip with all nesting levels ───────────────

    @Test
    fun field_fullRoundTrip() = runTest(testDispatcher) {
        val dataPref = protoDatastore.data()
        val fullData = TestNullableProtoData(
            id = 99,
            name = "FullTest",
            label = "complete",
            profile = TestNullableProfile(
                nickname = "tester",
                age = 35,
                address = TestNullableAddress(
                    street = "100 Broadway",
                    city = "New York",
                    coordinates = TestCoordinates(40.7128, -74.0060),
                ),
            ),
        )
        dataPref.set(fullData)

        // Read individual fields
        val idPref = protoDatastore.field(
            defaultValue = 0,
            getter = { it.id },
            updater = { proto, v -> proto.copy(id = v) },
        )
        val labelPref = protoDatastore.field(
            defaultValue = null as String?,
            getter = { it.label },
            updater = { proto, v -> proto.copy(label = v) },
        )
        val coordsPref = protoDatastore.field(
            defaultValue = null as TestCoordinates?,
            getter = { it.profile?.address?.coordinates },
            updater = { proto, v ->
                val profile = proto.profile ?: TestNullableProfile()
                val address = profile.address ?: TestNullableAddress()
                proto.copy(profile = profile.copy(address = address.copy(coordinates = v)))
            },
        )

        assertEquals(99, idPref.get())
        assertEquals("complete", labelPref.get())
        assertEquals(TestCoordinates(40.7128, -74.0060), coordsPref.get())
    }

    // ── field() – stateIn ───────────────────────────────────────────────

    @Test
    fun field_stateIn() = runTest(testDispatcher) {
        val namePref = protoDatastore.field(
            defaultValue = "",
            getter = { it.name },
            updater = { proto, v -> proto.copy(name = v) },
        )
        val childScope = this + Job()
        val state = namePref.stateIn(childScope)
        assertEquals("", state.value)

        namePref.set("StateIn")
        val observed = state.first { it == "StateIn" }
        assertEquals("StateIn", observed)
        childScope.cancel()
    }

    // ── data() – stateIn ────────────────────────────────────────────────

    @Test
    fun data_stateIn() = runTest(testDispatcher) {
        val pref = protoDatastore.data()
        val childScope = this + Job()
        val state = pref.stateIn(childScope)
        assertEquals(TestNullableProtoData(), state.value)

        val value = TestNullableProtoData(id = 7, name = "StateIn")
        pref.set(value)
        val observed = state.first { it == value }
        assertEquals(value, observed)
        childScope.cancel()
    }

    // ── edge: set entire object, then read fields ───────────────────────

    @Test
    fun field_readAfterWholeObjectSet() = runTest(testDispatcher) {
        protoDatastore.data().set(
            TestNullableProtoData(id = 5, name = "Whole", label = "tag"),
        )

        val idPref = protoDatastore.field(
            defaultValue = 0,
            getter = { it.id },
            updater = { proto, v -> proto.copy(id = v) },
        )
        assertEquals(5, idPref.get())
    }

    // ── edge: set field, then read whole object ─────────────────────────

    @Test
    fun data_reflectsFieldUpdate() = runTest(testDispatcher) {
        val idPref = protoDatastore.field(
            defaultValue = 0,
            getter = { it.id },
            updater = { proto, v -> proto.copy(id = v) },
        )
        idPref.set(77)

        val wholeData = protoDatastore.data().get()
        assertEquals(77, wholeData.id)
    }

    // ── edge: nullable field with non-null default ──────────────────────

    @Test
    fun field_nullableFieldWithNonNullDefault() = runTest(testDispatcher) {
        val labelPref = protoDatastore.field(
            defaultValue = "fallback",
            getter = { it.label ?: "fallback" },
            updater = { proto, value -> proto.copy(label = value) },
        )
        // Default proto has label = null, but getter returns "fallback"
        assertEquals("fallback", labelPref.get())

        labelPref.set("custom")
        assertEquals("custom", labelPref.get())

        // Delete resets proto to default, getter returns "fallback"
        labelPref.delete()
        assertEquals("fallback", labelPref.get())
    }
}
