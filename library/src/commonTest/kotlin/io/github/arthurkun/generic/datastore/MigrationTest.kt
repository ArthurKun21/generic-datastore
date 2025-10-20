package io.github.arthurkun.generic.datastore

import kotlinx.serialization.json.JsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

/**
 * Tests for JSON migration utilities.
 */
class MigrationTest {

    @Test
    fun toJsonElement_convertsNullToJsonNull() {
        val result = null.toJsonElement()
        assertTrue(result.toString().contains("null"))
    }

    @Test
    fun toJsonElement_convertsStringToJsonPrimitive() {
        val result = "test".toJsonElement()
        assertEquals(JsonPrimitive("test"), result)
    }

    @Test
    fun toJsonElement_convertsBooleanToJsonPrimitive() {
        val result = true.toJsonElement()
        assertEquals(JsonPrimitive(true), result)
    }

    @Test
    fun toJsonElement_convertsIntToJsonPrimitive() {
        val result = 42.toJsonElement()
        assertEquals(JsonPrimitive(42), result)
    }

    @Test
    fun toJsonElement_convertsLongToJsonPrimitive() {
        val result = 999L.toJsonElement()
        assertEquals(JsonPrimitive(999L), result)
    }

    @Test
    fun toJsonElement_convertsFloatToJsonPrimitive() {
        val result = 3.14f.toJsonElement()
        assertEquals(JsonPrimitive(3.14f), result)
    }

    @Test
    fun toJsonElement_convertsDoubleToJsonPrimitive() {
        val result = 2.718.toJsonElement()
        assertEquals(JsonPrimitive(2.718), result)
    }

    @Test
    fun toJsonElement_convertsListToJsonArray() {
        val list = listOf("a", "b", "c")
        val result = list.toJsonElement()
        assertTrue(result.toString().contains("a"))
        assertTrue(result.toString().contains("b"))
        assertTrue(result.toString().contains("c"))
    }

    @Test
    fun toJsonElement_convertsMapToJsonObject() {
        val map = mapOf("key1" to "value1", "key2" to 42)
        val result = map.toJsonElement()
        assertTrue(result.toString().contains("key1"))
        assertTrue(result.toString().contains("value1"))
        assertTrue(result.toString().contains("key2"))
    }

    @Test
    fun toJsonElement_convertsNestedStructures() {
        val nested = mapOf(
            "string" to "test",
            "number" to 42,
            "list" to listOf(1, 2, 3),
            "map" to mapOf("inner" to "value"),
        )
        val result = nested.toJsonElement()
        assertTrue(result.toString().contains("string"))
        assertTrue(result.toString().contains("test"))
        assertTrue(result.toString().contains("inner"))
    }

    @Test
    fun toJsonMap_parsesValidJsonString() {
        val json = """{"key1":"value1","key2":42,"key3":true}"""
        val result = json.toJsonMap()

        assertEquals("value1", result["key1"])
        assertEquals(42L, result["key2"]) // Numbers are parsed as Long
        assertEquals(true, result["key3"])
    }

    @Test
    fun toJsonMap_parsesNestedJson() {
        val json = """{"outer":{"inner":"value"}}"""
        val result = json.toJsonMap()

        assertTrue(result.containsKey("outer"))
        val outer = result["outer"] as? Map<*, *>
        assertEquals("value", outer?.get("inner"))
    }

    @Test
    fun toJsonMap_parsesJsonArray() {
        val json = """{"list":[1,2,3]}"""
        val result = json.toJsonMap()

        assertTrue(result.containsKey("list"))
        val list = result["list"] as? List<*>
        assertEquals(3, list?.size)
    }

    @Test
    fun toJsonMap_handlesNullValues() {
        val json = """{"key1":"value1","key2":null}"""
        val result = json.toJsonMap()

        // Null values should be filtered out
        assertEquals(1, result.size)
        assertEquals("value1", result["key1"])
    }

    @Test
    fun toJsonMap_throwsOnInvalidJson() {
        val invalidJson = "not a valid json"
        assertFailsWith<IllegalArgumentException> {
            invalidJson.toJsonMap()
        }
    }

    @Test
    fun toJsonMap_throwsOnNonObjectJson() {
        val arrayJson = """[1,2,3]"""
        assertFailsWith<IllegalArgumentException> {
            arrayJson.toJsonMap()
        }
    }

    @Test
    fun toJsonMap_handlesEmptyObject() {
        val json = "{}"
        val result = json.toJsonMap()
        assertEquals(0, result.size)
    }

    @Test
    fun toJsonMap_parsesComplexTypes() {
        val json = """{"string":"test","int":42,"long":999,"double":3.14,"bool":true,"array":[1,2,3],"object":{"nested":"value"}}"""
        val result = json.toJsonMap()

        assertEquals("test", result["string"])
        assertEquals(42L, result["int"])
        assertEquals(999L, result["long"])
        assertEquals(3.14, result["double"])
        assertEquals(true, result["bool"])

        val array = result["array"] as? List<*>
        assertEquals(3, array?.size)

        val obj = result["object"] as? Map<*, *>
        assertEquals("value", obj?.get("nested"))
    }
}
