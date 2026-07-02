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
 * 组合规则解析器
 *
 * 根据规则类型自动路由到对应的解析器实现：
 * - css / xpath → HtmlParser
 * - jsonpath → JsonParser
 *
 * 这种设计遵循策略模式，使得新增解析规则类型时
 * 只需添加新的解析器实现，无需修改调用方代码。
 *
 * @property htmlParser HTML 解析器（处理 CSS / XPath 规则）
 * @property jsonParser JSON 解析器（处理 JSONPath 规则）
 */
class CompositeRuleParser(
    private val htmlParser: HtmlParser,
    private val jsonParser: JsonParser
) : RuleParser {

    /**
     * 选择列表
     *
     * @param content 内容
     * @param rule 规则
     * @param ruleType 规则类型（css / xpath / jsonpath）
     * @return 匹配元素列表
     */
    override fun selectList(content: String, rule: String, ruleType: String): List<String> {
        return resolveParser(ruleType).selectList(content, rule, ruleType)
    }

    /**
     * 解析字段
     *
     * @param content 内容
     * @param rule 规则
     * @param ruleType 规则类型
     * @return 提取的值
     */
    override fun parseField(content: String, rule: String, ruleType: String): String {
        return resolveParser(ruleType).parseField(content, rule, ruleType)
    }

    /**
     * 解析第一个匹配
     *
     * @param content 内容
     * @param rule 规则
     * @param ruleType 规则类型
     * @return 第一个匹配值
     */
    override fun parseFirst(content: String, rule: String, ruleType: String): String {
        return resolveParser(ruleType).parseFirst(content, rule, ruleType)
    }

    /**
     * 根据规则类型选择解析器
     *
     * @param ruleType 规则类型
     * @return 对应的解析器实例
     */
    private fun resolveParser(ruleType: String): RuleParser {
        return when (ruleType.lowercase()) {
            "jsonpath" -> jsonParser
            else -> htmlParser // css / xpath 默认使用 HTML 解析器
        }
    }
}
