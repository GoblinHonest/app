/*
 * Copyright 2024 Stark Industries
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.stark.miuix.data.parser

/**
 * HTML 解析器
 *
 * 在 KMP 环境下使用正则表达式实现 CSS/XPath 选择器解析。
 * 采用标签栈匹配策略处理嵌套标签，比简单的 `.*?` 更可靠。
 *
 * 支持的 CSS 选择器：
 * - `tag` — 标签名匹配
 * - `.class` — class 属性包含
 * - `#id` — id 精确匹配
 * - `tag.class` — 标签 + class 组合
 * - `parent child` — 后代选择器（在父元素内查找子元素）
 * - `[attr=value]` — 属性选择器
 *
 * 字段提取后缀：
 * - `@text` — 提取纯文本
 * - `@attr:name` — 提取指定属性值
 * - `@src` / `@href` — 快捷属性提取
 */
class HtmlParser : RuleParser {

    override fun selectList(content: String, rule: String, ruleType: String): List<String> {
        if (rule.isBlank()) return emptyList()
        return when (ruleType) {
            "css" -> selectByCss(content, rule)
            "xpath" -> selectByXpath(content, rule)
            else -> emptyList()
        }
    }

    override fun parseField(content: String, rule: String, ruleType: String): String {
        if (rule.isBlank()) return ""
        return when {
            rule.endsWith("@text") -> {
                val selector = rule.removeSuffix("@text").trim()
                if (selector.isBlank()) extractText(content)
                else selectByCss(content, selector).firstOrNull()?.let { extractText(it) } ?: ""
            }
            rule.contains("@attr:") -> {
                val parts = rule.split("@attr:")
                val selector = parts[0].trim()
                val attrName = parts[1].trim()
                if (selector.isBlank()) extractAttribute(content, attrName)
                else selectByCss(content, selector).firstOrNull()?.let { extractAttribute(it, attrName) } ?: ""
            }
            rule == "@src" -> extractAttribute(content, "src")
            rule == "@href" -> extractAttribute(content, "href")
            else -> selectByCss(content, rule).firstOrNull()?.let { extractText(it) } ?: ""
        }
    }

    override fun parseFirst(content: String, rule: String, ruleType: String): String {
        return parseField(content, rule, ruleType)
    }

    /**
     * CSS 选择器匹配
     *
     * 支持后代选择器：先匹配最外层，再在结果内递归匹配子选择器。
     */
    private fun selectByCss(content: String, selector: String): List<String> {
        val parts = selector.trim().split(DESCENDANT_SPLIT_REGEX)
        if (parts.isEmpty()) return emptyList()

        var results = listOf(content)
        for (part in parts) {
            results = results.flatMap { html -> matchSingleSelector(html, part.trim()) }
            if (results.isEmpty()) break
        }
        return results
    }

    /**
     * 匹配单个选择器（tag / .class / #id / tag.class / [attr=val]）
     *
     * 使用标签栈跟踪嵌套深度，确保提取完整的元素（包括嵌套的同名标签）。
     */
    private fun matchSingleSelector(content: String, selector: String): List<String> {
        val parsed = parseSelectorParts(selector)
        val results = mutableListOf<String>()

        val openTagRegex = if (parsed.tagName != null) {
            """<(${Regex.escape(parsed.tagName)})(\s[^>]*)?>""".toRegex(RegexOption.IGNORE_CASE)
        } else {
            """<(\w+)(\s[^>]*)?>""".toRegex(RegexOption.IGNORE_CASE)
        }

        var searchFrom = 0
        while (searchFrom < content.length) {
            val openMatch = openTagRegex.find(content, searchFrom) ?: break
            val tagName = openMatch.groupValues[1]
            val attrs = openMatch.groupValues[2]

            if (!matchesFilters(attrs, openMatch.value, parsed)) {
                searchFrom = openMatch.range.last + 1
                continue
            }

            val elementEnd = findClosingTag(content, tagName, openMatch.range.first)
            if (elementEnd < 0) {
                searchFrom = openMatch.range.last + 1
                continue
            }

            results.add(content.substring(openMatch.range.first, elementEnd))
            searchFrom = elementEnd
        }
        return results
    }

    /**
     * 通过标签栈找到匹配的闭合标签位置
     *
     * 处理嵌套同名标签的情况（如 `<div><div>...</div></div>`）。
     */
    private fun findClosingTag(content: String, tagName: String, startIdx: Int): Int {
        val openPattern = """<${Regex.escape(tagName)}[\s>/]""".toRegex(RegexOption.IGNORE_CASE)
        val closePattern = """</${Regex.escape(tagName)}\s*>""".toRegex(RegexOption.IGNORE_CASE)

        var depth = 0
        var pos = startIdx

        while (pos < content.length) {
            val nextOpen = openPattern.find(content, pos + 1)
            val nextClose = closePattern.find(content, pos + 1)

            if (nextClose == null) return -1

            if (nextOpen != null && nextOpen.range.first < nextClose.range.first) {
                // 遇到同名的嵌套开标签
                val selfClosing = content.substring(nextOpen.range.first).let {
                    it.indexOf('>').let { end -> end >= 0 && it[end - 1] == '/' }
                }
                if (!selfClosing) depth++
                pos = nextOpen.range.first
            } else {
                if (depth == 0) {
                    return nextClose.range.last + 1
                }
                depth--
                pos = nextClose.range.first
            }
        }
        return -1
    }

    /** 检查元素属性是否满足选择器过滤条件 */
    private fun matchesFilters(attrs: String, fullOpenTag: String, parsed: SelectorParts): Boolean {
        val attrText = attrs.ifBlank { fullOpenTag }
        if (parsed.className != null) {
            val classAttr = extractAttribute(attrText, "class")
            if (!classAttr.split(WHITESPACE_REGEX).contains(parsed.className)) return false
        }
        if (parsed.id != null) {
            val idAttr = extractAttribute(attrText, "id")
            if (idAttr != parsed.id) return false
        }
        if (parsed.attrName != null) {
            val value = extractAttribute(attrText, parsed.attrName)
            if (parsed.attrValue != null && value != parsed.attrValue) return false
            if (parsed.attrValue == null && value.isBlank()) return false
        }
        return true
    }

    /**
     * 解析 CSS 选择器为结构化部分
     *
     * 示例：`div.content` → SelectorParts(tagName="div", className="content")
     */
    private fun parseSelectorParts(selector: String): SelectorParts {
        var s = selector
        var attrName: String? = null
        var attrValue: String? = null

        // [attr=value]
        val attrMatch = """\[(\w+)(?:=["']?([^"'\]]+)["']?)?\]""".toRegex().find(s)
        if (attrMatch != null) {
            attrName = attrMatch.groupValues[1]
            attrValue = attrMatch.groupValues[2].takeIf { it.isNotBlank() }
            s = s.removeRange(attrMatch.range)
        }

        return when {
            s.startsWith("#") -> SelectorParts(id = s.substring(1), attrName = attrName, attrValue = attrValue)
            s.startsWith(".") -> SelectorParts(className = s.substring(1), attrName = attrName, attrValue = attrValue)
            s.contains(".") -> {
                val (tag, cls) = s.split(".", limit = 2)
                SelectorParts(tagName = tag, className = cls, attrName = attrName, attrValue = attrValue)
            }
            s.contains("#") -> {
                val (tag, id) = s.split("#", limit = 2)
                SelectorParts(tagName = tag, id = id, attrName = attrName, attrValue = attrValue)
            }
            s.isNotBlank() -> SelectorParts(tagName = s, attrName = attrName, attrValue = attrValue)
            else -> SelectorParts(attrName = attrName, attrValue = attrValue)
        }
    }

    private fun selectByXpath(content: String, xpath: String): List<String> {
        val segments = xpath.split("/").filter { it.isNotBlank() }
        if (segments.isEmpty()) return emptyList()

        var results = listOf(content)
        for (segment in segments) {
            val tagName = segment.replace("""[\[\]@\w='"]""".toRegex(), "").takeIf { it.isNotBlank() } ?: continue
            results = results.flatMap { html -> matchSingleSelector(html, tagName) }
            if (results.isEmpty()) break
        }
        return results
    }

    private fun extractText(html: String): String {
        return html.replace("""<[^>]+>""".toRegex(), "").trim()
    }

    private fun extractAttribute(html: String, attrName: String): String {
        val pattern = """${Regex.escape(attrName)}\s*=\s*["']([^"']+)["']""".toRegex()
        return pattern.find(html)?.groupValues?.get(1) ?: ""
    }

    private data class SelectorParts(
        val tagName: String? = null,
        val className: String? = null,
        val id: String? = null,
        val attrName: String? = null,
        val attrValue: String? = null
    )

    companion object {
        private val DESCENDANT_SPLIT_REGEX = """\s+""".toRegex()
        private val WHITESPACE_REGEX = """\s+""".toRegex()
    }
}
