package io.github.arthurkun.generic.datastore.proto.core

import io.github.arthurkun.generic.datastore.proto.GenericProtoDatastore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertSame

abstract class AbstractProtoCachedFieldTest {

    abstract val protoDatastore: GenericProtoDatastore<TestProtoData>
    abstract val testDispatcher: TestDispatcher

    @Test
    fun cached_returnsSameInstanceForSameName() {
        val first = protoDatastore.cached("age") {
            protoDatastore.field(0, getter = {
                it.profile.age
            }, updater = { p, v -> p.copy(profile = p.profile.copy(age = v)) })
        }
        val second = protoDatastore.cached("age") {
            protoDatastore.field(99, getter = {
                it.profile.age
            }, updater = { p, v -> p.copy(profile = p.profile.copy(age = v)) })
        }

        assertSame(first, second)
        assertEquals(0, second.defaultValue)
    }

    @Test
    fun cached_distinguishesNames() = runTest(testDispatcher) {
        val age = protoDatastore.cached("cache_age") {
            protoDatastore.field(0, getter = {
                it.profile.age
            }, updater = { p, v -> p.copy(profile = p.profile.copy(age = v)) })
        }
        val id = protoDatastore.cached("cache_id") {
            protoDatastore.field(0, getter = { it.id }, updater = { p, v -> p.copy(id = v) })
        }

        id.set(5)
        assertEquals(5, id.get())
        assertEquals(0, age.get())
    }

    @Test
    fun cached_rejectsBlankName() {
        assertFailsWith<IllegalArgumentException> {
            protoDatastore.cached<Int>(" ") {
                protoDatastore.field(0, getter = { it.id }, updater = { p, v -> p.copy(id = v) })
            }
        }
    }

    @Test
    fun field_stateInCurrent_seedsWithPersistedValue() = runTest(testDispatcher) {
        val age = protoDatastore.field(0, getter = {
            it.profile.age
        }, updater = { p, v -> p.copy(profile = p.profile.copy(age = v)) })
        age.set(33)

        val state: StateFlow<Int> = age.stateInCurrent(CoroutineScope(testDispatcher), SharingStarted.Eagerly)

        assertEquals(33, state.value)
    }
}
