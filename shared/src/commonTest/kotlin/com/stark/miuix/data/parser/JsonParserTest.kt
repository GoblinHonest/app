package com.stark.miuix.data.parser

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class JsonParserTest {

    private val parser = JsonParser()

    @Test
    fun parseSimpleField() {
        val json = """{"data":{"url":"https://example.com/video.mp4"}}"""
        val result = parser.parseField(json, "$.data.url", "jsonpath")
        assertEquals("https://example.com/video.mp4", result)
    }

    @Test
    fun selectArray() {
        val json = """{"list":[{"name":"A"},{"name":"B"}]}"""
        val items = parser.selectList(json, "$.list", "jsonpath")
        assertEquals(2, items.size)
    }

    @Test
    fun nestedArrayAccess() {
        val json = """{"data":{"items":[{"title":"T1"},{"title":"T2"}]}}"""
        val items = parser.selectList(json, "$.data.items", "jsonpath")
        assertEquals(2, items.size)
        assertTrue(items[0].contains("T1"))
    }

    @Test
    fun blankRuleReturnsEmpty() {
        val json = """{"a":1}"""
        assertEquals("", parser.parseField(json, "", "jsonpath"))
        assertEquals(emptyList(), parser.selectList(json, "", "jsonpath"))
    }

    @Test
    fun wrongRuleTypeReturnsEmpty() {
        val json = """{"a":1}"""
        assertEquals("", parser.parseField(json, "$.a", "css"))
    }
}
