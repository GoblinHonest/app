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

package com.stark.miuix.data.dlna.parser

/**
 * 轻量 XML 解析工具
 *
 * DLNA 设备描述与 SOAP 响应都是 XML，但结构相对固定。
 * 为避免引入完整 XML 库（KMP 中可选项有限且体积大），
 * 这里基于正则 + 字符串查找实现仅满足本模块需要的提取能力。
 *
 * 局限：不支持命名空间前缀转换、CDATA、嵌套同名标签等复杂场景。
 * 在 DLNA 标准响应范围内足够使用。
 */
internal object XmlParser {

    /**
     * 提取首个指定标签的文本内容
     *
     * @param xml 原始 XML
     * @param tag 标签名（不含尖括号，如 "friendlyName"）
     * @return 标签内文本，未找到返回空字符串
     */
    fun tagContent(xml: String, tag: String): String {
        val regex = Regex("<$tag[^>]*>([\\s\\S]*?)</$tag>", RegexOption.IGNORE_CASE)
        return regex.find(xml)?.groupValues?.getOrNull(1)?.trim()?.let { decodeEntities(it) } ?: ""
    }

    /**
     * 提取所有匹配标签的文本内容
     */
    fun allTagContents(xml: String, tag: String): List<String> {
        val regex = Regex("<$tag[^>]*>([\\s\\S]*?)</$tag>", RegexOption.IGNORE_CASE)
        return regex.findAll(xml).map { it.groupValues[1].trim() }.toList()
    }

    /**
     * 提取标签的某个属性值
     *
     * @param xml 原始 XML
     * @param tag 标签名
     * @param attr 属性名
     * @return 属性值，未找到返回空字符串
     */
    fun tagAttr(xml: String, tag: String, attr: String): String {
        val regex = Regex("<$tag[^>]*\\s$attr\\s*=\\s*\"([^\"]*)\"", RegexOption.IGNORE_CASE)
        return regex.find(xml)?.groupValues?.getOrNull(1)?.trim() ?: ""
    }

    /**
     * 提取所有匹配标签的原始片段（含标签本身）
     *
     * 用于在设备描述 XML 中提取所有 `<service>` 节点。
     */
    fun allTagBlocks(xml: String, tag: String): List<String> {
        val regex = Regex("<$tag[\\s\\S]*?</$tag>", RegexOption.IGNORE_CASE)
        return regex.findAll(xml).map { it.value }.toList()
    }

    /** XML 实体解码（仅处理 DLNA 响应中常见的几种） */
    private fun decodeEntities(text: String): String {
        return text
            .replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&quot;", "\"")
            .replace("&apos;", "'")
    }
}
