/*
 * Copyright 2024 Starter
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
 * 使用正则表达式实现简化的 CSS 选择器解析。
 * 在 KMP 环境下作为轻量级 HTML 解析方案，
 * 支持基本的标签选择、class 选择和属性提取。
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
                if (selector.isBlank()) {
                    extractText(content)
                } else {
                    val elements = selectByCss(content, selector)
                    elements.firstOrNull()?.let { extractText(it) } ?: ""
                }
            }
            rule.contains("@attr:") -> {
                val parts = rule.split("@attr:")
                val selector = parts[0].trim()
                val attrName = parts[1].trim()
                if (selector.isBlank()) {
                    extractAttribute(content, attrName)
                } else {
                    val elements = selectByCss(content, selector)
                    elements.firstOrNull()?.let { extractAttribute(it, attrName) } ?: ""
                }
            }
            rule == "@src" -> extractAttribute(content, "src")
            rule == "@href" -> extractAttribute(content, "href")
            else -> {
                val elements = selectByCss(content, rule)
                elements.firstOrNull()?.let { extractText(it) } ?: ""
            }
        }
    }

    override fun parseFirst(content: String, rule: String, ruleType: String): String {
        return parseField(content, rule, ruleType)
    }

    /**
     * 简化 CSS 选择器实现
     *
     * 支持标签名、class (.classname)、id (#id) 选择器。
     * 使用正则表达式匹配，适用于结构简单的 HTML 内容。
     */
    private fun selectByCss(content: String, selector: String): List<String> {
        val tagPattern = when {
            selector.startsWith(".") -> {
                val className = selector.substring(1)
                """<\w+[^>]*class\s*=\s*["'][^"']*\b${Regex.escape(className)}\b[^"']*["'][^>]*>.*?</\w+>""".toRegex(RegexOption.DOT_MATCHES_ALL)
            }
            selector.startsWith("#") -> {
                val id = selector.substring(1)
                """<\w+[^>]*id\s*=\s*["']${Regex.escape(id)}["'][^>]*>.*?</\w+>""".toRegex(RegexOption.DOT_MATCHES_ALL)
            }
            selector.contains(" ") -> {
                val parts = selector.split(" ")
                val lastTag = parts.last()
                """<${Regex.escape(lastTag)}[^>]*>.*?</${Regex.escape(lastTag)}>""".toRegex(RegexOption.DOT_MATCHES_ALL)
            }
            else -> {
                """<${Regex.escape(selector)}[^>]*>.*?</${Regex.escape(selector)}>""".toRegex(RegexOption.DOT_MATCHES_ALL)
            }
        }
        return tagPattern.findAll(content).map { it.value }.toList()
    }

    /**
     * 简化 XPath 选择器实现（基本支持）
     */
    private fun selectByXpath(content: String, xpath: String): List<String> {
        val tagMatch = """//?(\w+)""".toRegex().find(xpath) ?: return emptyList()
        val tag = tagMatch.groupValues[1]
        val pattern = """<${Regex.escape(tag)}[^>]*>.*?</${Regex.escape(tag)}>""".toRegex(RegexOption.DOT_MATCHES_ALL)
        return pattern.findAll(content).map { it.value }.toList()
    }

    /**
     * 提取 HTML 元素的纯文本内容
     */
    private fun extractText(html: String): String {
        return html.replace("""<[^>]+>""".toRegex(), "").trim()
    }

    /**
     * 提取 HTML 元素的指定属性值
     */
    private fun extractAttribute(html: String, attrName: String): String {
        val pattern = """${Regex.escape(attrName)}\s*=\s*["']([^"']+)["']""".toRegex()
        return pattern.find(html)?.groupValues?.get(1) ?: ""
    }
}
