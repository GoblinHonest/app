package com.stark.miuix.data.parser

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HtmlParserTest {

    private val parser = HtmlParser()

    @Test
    fun selectByTag() {
        val html = "<div><p>Hello</p><p>World</p></div>"
        val result = parser.selectList(html, "p", "css")
        assertEquals(2, result.size)
        assertTrue(result[0].contains("Hello"))
    }

    @Test
    fun selectByClass() {
        val html = """<div class="item">A</div><div class="other">B</div><div class="item">C</div>"""
        val result = parser.selectList(html, ".item", "css")
        assertEquals(2, result.size)
    }

    @Test
    fun selectById() {
        val html = """<span id="target">Found</span><span>Not</span>"""
        val result = parser.selectList(html, "#target", "css")
        assertEquals(1, result.size)
        assertTrue(result[0].contains("Found"))
    }

    @Test
    fun parseFieldText() {
        val html = "<a href=\"/url\">Link Text</a>"
        val text = parser.parseField(html, "@text", "css")
        assertEquals("Link Text", text)
    }

    @Test
    fun parseFieldAttr() {
        val html = """<img src="pic.jpg" alt="desc"/>"""
        val src = parser.parseField(html, "@attr:src", "css")
        assertEquals("pic.jpg", src)
    }

    @Test
    fun parseFieldHref() {
        val html = """<a href="/page/1">Click</a>"""
        val href = parser.parseField(html, "@href", "css")
        assertEquals("/page/1", href)
    }

    @Test
    fun descendantSelector() {
        val html = """<div class="list"><li>A</li><li>B</li></div><li>C</li>"""
        val result = parser.selectList(html, ".list li", "css")
        assertEquals(2, result.size)
    }

    @Test
    fun nestedSameTag() {
        val html = "<div><div>Inner</div></div>"
        val result = parser.selectList(html, "div", "css")
        assertTrue(result.isNotEmpty())
        assertTrue(result[0].contains("Inner"))
    }
}
