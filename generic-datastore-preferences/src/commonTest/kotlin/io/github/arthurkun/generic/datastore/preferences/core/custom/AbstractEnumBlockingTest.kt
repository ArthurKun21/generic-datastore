package io.github.arthurkun.generic.datastore.preferences.core.data.custom

import io.github.arthurkun.generic.datastore.preferences.GenericPreferencesDatastore
import io.github.arthurkun.generic.datastore.preferences.enum
import io.github.arthurkun.generic.datastore.preferences.enumSet
import kotlin.test.Test
import kotlin.test.assertEquals

abstract class AbstractEnumBlockingTest {

    abstract val preferenceDatastore: GenericPreferencesDatastore

    @Test
    fun enumPreference_defaultValueWhenNotSetBlocking() {
        val pref = preferenceDatastore.enum("enumBlockingDefault", EnumTestTheme.SYSTEM)
        assertEquals(EnumTestTheme.SYSTEM, pref.getBlocking())
    }

    @Test
    fun enumPreference_setAndGetBlocking() {
        val pref = preferenceDatastore.enum("enumBlockingSetGet", EnumTestTheme.SYSTEM)
        pref.setBlocking(EnumTestTheme.DARK)
        assertEquals(EnumTestTheme.DARK, pref.getBlocking())
    }

    @Test
    fun enumPreference_resetToDefaultBlocking() {
        val pref = preferenceDatastore.enum("enumBlockingReset", EnumTestTheme.SYSTEM)
        pref.setBlocking(EnumTestTheme.DARK)
        pref.resetToDefaultBlocking()
        assertEquals(EnumTestTheme.SYSTEM, pref.getBlocking())
    }

    @Test
    fun enumPreference_delegation() {
        val pref = preferenceDatastore.enum("enumBlockingDelegate", EnumTestTheme.SYSTEM)
        var delegated: EnumTestTheme by pref

        assertEquals(EnumTestTheme.SYSTEM, delegated)
        delegated = EnumTestTheme.LIGHT
        assertEquals(EnumTestTheme.LIGHT, delegated)
        assertEquals(EnumTestTheme.LIGHT, pref.getBlocking())

        pref.resetToDefaultBlocking()
        assertEquals(EnumTestTheme.SYSTEM, delegated)
    }

    @Test
    fun enumSetPreference_setAndGetBlocking() {
        val pref = preferenceDatastore.enumSet<EnumTestTheme>("enumSetBlockingSetGet")
        pref.setBlocking(setOf(EnumTestTheme.DARK))
        assertEquals(setOf(EnumTestTheme.DARK), pref.getBlocking())
    }

    @Test
    fun enumSetPreference_resetToDefaultBlocking() {
        val default = setOf(EnumTestTheme.SYSTEM)
        val pref = preferenceDatastore.enumSet("enumSetBlockingReset", default)
        pref.setBlocking(setOf(EnumTestTheme.LIGHT))
        pref.resetToDefaultBlocking()
        assertEquals(default, pref.getBlocking())
    }

    @Test
    fun enumSetPreference_delegation() {
        val default = setOf(EnumTestTheme.DARK)
        val pref = preferenceDatastore.enumSet("enumSetBlockingDelegate", default)
        var delegated: Set<EnumTestTheme> by pref

        delegated = setOf(EnumTestTheme.LIGHT)
        assertEquals(setOf(EnumTestTheme.LIGHT), delegated)
        assertEquals(setOf(EnumTestTheme.LIGHT), pref.getBlocking())

        pref.resetToDefaultBlocking()
        assertEquals(default, delegated)
    }
}
