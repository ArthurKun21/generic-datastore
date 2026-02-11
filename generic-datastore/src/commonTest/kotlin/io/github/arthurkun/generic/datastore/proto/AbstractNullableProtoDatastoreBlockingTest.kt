package io.github.arthurkun.generic.datastore.proto

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

abstract class AbstractNullableProtoDatastoreBlockingTest {

    abstract val protoDatastore: GenericProtoDatastore<TestNullableProtoData>

    // ── data() blocking tests ───────────────────────────────────────────

    @Test
    fun data_getBlockingReturnsDefault() {
        val pref = protoDatastore.data()
        assertEquals(TestNullableProtoData(), pref.getBlocking())
    }

    @Test
    fun data_setBlockingAndGetBlocking() {
        val pref = protoDatastore.data()
        val value = TestNullableProtoData(id = 1, name = "BlockingUser", label = "tag")
        pref.setBlocking(value)
        assertEquals(value, pref.getBlocking())
    }

    @Test
    fun data_setBlockingWithNullableFields() {
        val pref = protoDatastore.data()
        val value = TestNullableProtoData(
            id = 2,
            name = "NullFields",
            label = null,
            profile = null,
        )
        pref.setBlocking(value)
        assertEquals(value, pref.getBlocking())
        assertNull(pref.getBlocking().label)
        assertNull(pref.getBlocking().profile)
    }

    @Test
    fun data_setBlockingWithAllFieldsPopulated() {
        val pref = protoDatastore.data()
        val value = TestNullableProtoData(
            id = 3,
            name = "Full",
            label = "admin",
            profile = TestNullableProfile(
                nickname = "fullnick",
                age = 40,
                address = TestNullableAddress(
                    street = "100 Main",
                    city = "Boston",
                    coordinates = TestCoordinates(42.3601, -71.0589),
                ),
            ),
        )
        pref.setBlocking(value)
        assertEquals(value, pref.getBlocking())
    }

    @Test
    fun data_resetToDefaultBlocking() {
        val pref = protoDatastore.data()
        pref.setBlocking(
            TestNullableProtoData(
                id = 99,
                name = "ToReset",
                label = "temp",
                profile = TestNullableProfile(nickname = "reset"),
            ),
        )
        pref.resetToDefaultBlocking()
        assertEquals(TestNullableProtoData(), pref.getBlocking())
    }

    @Test
    fun data_delegation() {
        val pref = protoDatastore.data()
        var delegated: TestNullableProtoData by pref

        val newValue = TestNullableProtoData(id = 42, name = "Delegated", label = "delegate")
        delegated = newValue
        assertEquals(newValue, delegated)
        assertEquals(newValue, pref.getBlocking())

        pref.resetToDefaultBlocking()
        assertEquals(TestNullableProtoData(), delegated)
    }

    // ── field() blocking – non-nullable fields ──────────────────────────

    @Test
    fun field_idGetBlockingDefault() {
        val idPref = protoDatastore.field(
            key = "id",
            defaultValue = 0,
            getter = { it.id },
            updater = { proto, value -> proto.copy(id = value) },
        )
        assertEquals(0, idPref.getBlocking())
    }

    @Test
    fun field_idSetBlockingAndGetBlocking() {
        val idPref = protoDatastore.field(
            key = "id",
            defaultValue = 0,
            getter = { it.id },
            updater = { proto, value -> proto.copy(id = value) },
        )
        idPref.setBlocking(55)
        assertEquals(55, idPref.getBlocking())
    }

    @Test
    fun field_nameSetBlockingAndGetBlocking() {
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
    fun field_nameResetToDefaultBlocking() {
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
    fun field_nameDelegation() {
        val namePref = protoDatastore.field(
            key = "name",
            defaultValue = "",
            getter = { it.name },
            updater = { proto, value -> proto.copy(name = value) },
        )
        var delegated: String by namePref

        delegated = "DelegatedName"
        assertEquals("DelegatedName", delegated)
        assertEquals("DelegatedName", namePref.getBlocking())

        namePref.resetToDefaultBlocking()
        assertEquals("", delegated)
    }

    // ── field() blocking – nullable fields ──────────────────────────────

    @Test
    fun field_labelBlockingDefaultIsNull() {
        val labelPref = protoDatastore.field(
            key = "label",
            defaultValue = null as String?,
            getter = { it.label },
            updater = { proto, value -> proto.copy(label = value) },
        )
        assertNull(labelPref.getBlocking())
    }

    @Test
    fun field_labelBlockingSetAndGet() {
        val labelPref = protoDatastore.field(
            key = "label",
            defaultValue = null as String?,
            getter = { it.label },
            updater = { proto, value -> proto.copy(label = value) },
        )
        labelPref.setBlocking("blockLabel")
        assertEquals("blockLabel", labelPref.getBlocking())
    }

    @Test
    fun field_labelBlockingSetToNull() {
        val labelPref = protoDatastore.field(
            key = "label",
            defaultValue = null as String?,
            getter = { it.label },
            updater = { proto, value -> proto.copy(label = value) },
        )
        labelPref.setBlocking("temp")
        labelPref.setBlocking(null)
        assertNull(labelPref.getBlocking())
    }

    @Test
    fun field_labelBlockingResetToDefaultNull() {
        val labelPref = protoDatastore.field(
            key = "label",
            defaultValue = null as String?,
            getter = { it.label },
            updater = { proto, value -> proto.copy(label = value) },
        )
        labelPref.setBlocking("willReset")
        labelPref.resetToDefaultBlocking()
        assertNull(labelPref.getBlocking())
    }

    @Test
    fun field_labelBlockingDelegation() {
        val labelPref = protoDatastore.field(
            key = "label",
            defaultValue = null as String?,
            getter = { it.label },
            updater = { proto, value -> proto.copy(label = value) },
        )
        var delegated: String? by labelPref

        assertNull(delegated)
        delegated = "delegateLabel"
        assertEquals("delegateLabel", delegated)

        delegated = null
        assertNull(delegated)
    }

    // ── field() blocking – nullable nested object ───────────────────────

    @Test
    fun field_profileBlockingSetAndGet() {
        val profilePref = protoDatastore.field(
            key = "profile",
            defaultValue = null as TestNullableProfile?,
            getter = { it.profile },
            updater = { proto, value -> proto.copy(profile = value) },
        )
        assertNull(profilePref.getBlocking())

        val profile = TestNullableProfile(nickname = "block", age = 28)
        profilePref.setBlocking(profile)
        assertEquals(profile, profilePref.getBlocking())

        profilePref.setBlocking(null)
        assertNull(profilePref.getBlocking())
    }

    // ── field() blocking – deep nesting ─────────────────────────────────

    @Test
    fun field_coordinatesBlockingRoundTrip() {
        val coordsPref = protoDatastore.field(
            key = "coords",
            defaultValue = null as TestCoordinates?,
            getter = { it.profile?.address?.coordinates },
            updater = { proto, value ->
                val profile = proto.profile ?: TestNullableProfile()
                val address = profile.address ?: TestNullableAddress()
                proto.copy(profile = profile.copy(address = address.copy(coordinates = value)))
            },
        )
        assertNull(coordsPref.getBlocking())

        coordsPref.setBlocking(TestCoordinates(51.5074, -0.1278))
        assertEquals(TestCoordinates(51.5074, -0.1278), coordsPref.getBlocking())

        coordsPref.setBlocking(null)
        assertNull(coordsPref.getBlocking())
    }

    // ── cross-field isolation blocking ──────────────────────────────────

    @Test
    fun field_blockingCrossFieldIsolation() {
        val idPref = protoDatastore.field(
            key = "id",
            defaultValue = 0,
            getter = { it.id },
            updater = { proto, value -> proto.copy(id = value) },
        )
        val namePref = protoDatastore.field(
            key = "name",
            defaultValue = "",
            getter = { it.name },
            updater = { proto, value -> proto.copy(name = value) },
        )

        idPref.setBlocking(10)
        namePref.setBlocking("alpha")

        assertEquals(10, idPref.getBlocking())
        assertEquals("alpha", namePref.getBlocking())

        idPref.setBlocking(20)
        assertEquals(20, idPref.getBlocking())
        assertEquals("alpha", namePref.getBlocking())
    }
}
