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

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * JSON 解析器
 *
 * 实现简化的 JSONPath 解析，支持从 JSON 内容中按路径提取数据。
 * 支持数组遍历（$..items[*]）和嵌套对象访问（$.data.list）。
 */
class JsonParser : RuleParser {

    private val json = Json { ignoreUnknownKeys = true }

    override fun selectList(content: String, rule: String, ruleType: String): List<String> {
        if (rule.isBlank() || ruleType != "jsonpath") return emptyList()

        return runCatching {
            val root = json.parseToJsonElement(content)
            val elements = navigateToArray(root, rule)
            elements.map { it.toString() }
        }.getOrDefault(emptyList())
    }

    override fun parseField(content: String, rule: String, ruleType: String): String {
        if (rule.isBlank() || ruleType != "jsonpath") return ""

        return runCatching {
            val root = json.parseToJsonElement(content)
            extractValue(root, rule)
        }.getOrDefault("")
    }

    override fun parseFirst(content: String, rule: String, ruleType: String): String {
        return parseField(content, rule, ruleType)
    }

    /**
     * 沿 JSONPath 导航到目标数组
     *
     * 支持的路径格式：
     * - $.data.list → 访问嵌套对象中的数组
     * - $..items → 递归查找名为 items 的数组
     * - $.list[*] → 展开数组中的所有元素
     */
    private fun navigateToArray(root: JsonElement, path: String): List<JsonElement> {
        val normalizedPath = path.removePrefix("$").removePrefix(".")
        val segments = normalizedPath.split(".")

        var current: JsonElement = root
        for (segment in segments) {
            val cleanSegment = segment.removeSuffix("[*]")
            if (cleanSegment.isBlank()) continue

            current = when (current) {
                is JsonObject -> current.jsonObject[cleanSegment] ?: return emptyList()
                is JsonArray -> {
                    val results = mutableListOf<JsonElement>()
                    for (element in current.jsonArray) {
                        if (element is JsonObject) {
                            element.jsonObject[cleanSegment]?.let { results.add(it) }
                        }
                    }
                    if (results.size == 1) results[0] else return results
                }
                else -> return emptyList()
            }
        }

        return when (current) {
            is JsonArray -> current.jsonArray.toList()
            else -> listOf(current)
        }
    }

    /**
     * 从 JSON 元素中提取单个值
     */
    private fun extractValue(root: JsonElement, path: String): String {
        val normalizedPath = path.removePrefix("$").removePrefix(".")
        val segments = normalizedPath.split(".")

        var current: JsonElement = root
        for (segment in segments) {
            if (segment.isBlank()) continue
            current = when (current) {
                is JsonObject -> current.jsonObject[segment] ?: return ""
                else -> return ""
            }
        }

        return when (current) {
            is JsonPrimitive -> current.jsonPrimitive.content
            else -> current.toString()
        }
    }
}
