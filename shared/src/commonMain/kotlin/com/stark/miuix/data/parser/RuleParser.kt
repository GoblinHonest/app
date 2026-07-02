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
 * 规则解析器接口
 *
 * 提供对 HTML/JSON 内容的规则化解析能力，
 * 支持 CSS 选择器、XPath 和 JSONPath 三种解析规则类型。
 */
interface RuleParser {

    /**
     * 根据规则从内容中选择多个元素
     *
     * @param content 待解析的内容（HTML 或 JSON 字符串）
     * @param rule 选择规则表达式
     * @param ruleType 规则类型：css / xpath / jsonpath
     * @return 匹配的元素列表（以字符串形式返回各元素的 HTML/JSON 片段）
     */
    fun selectList(content: String, rule: String, ruleType: String): List<String>

    /**
     * 根据规则从内容中提取单个字段值
     *
     * @param content 待解析的内容片段
     * @param rule 提取规则表达式
     * @param ruleType 规则类型
     * @return 提取的字段值，未匹配时返回空字符串
     */
    fun parseField(content: String, rule: String, ruleType: String): String

    /**
     * 从完整内容中提取第一个匹配的值
     *
     * @param content 待解析的完整内容
     * @param rule 提取规则表达式
     * @param ruleType 规则类型
     * @return 第一个匹配值，未匹配时返回空字符串
     */
    fun parseFirst(content: String, rule: String, ruleType: String): String
}
