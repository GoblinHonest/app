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

package com.stark.miuix.data.subtitle

/**
 * 字幕条目
 *
 * @property startMs 开始时间（毫秒）
 * @property endMs 结束时间（毫秒）
 * @property text 字幕文本（支持多行）
 */
data class SubtitleEntry(
    val startMs: Long,
    val endMs: Long,
    val text: String
)

/**
 * 字幕解析器
 *
 * 支持 SRT 和 VTT 两种常见字幕格式的解析。
 * ASS 格式暂做简化处理（提取 Dialogue 行文本）。
 */
class SubtitleParser {

    /**
     * 解析字幕内容为条目列表
     *
     * @param content 字幕文件内容
     * @param format 格式标识：srt / vtt / ass
     * @return 按时间排序的字幕条目列表
     */
    fun parse(content: String, format: String): List<SubtitleEntry> {
        return when (format.lowercase()) {
            "srt" -> parseSrt(content)
            "vtt" -> parseVtt(content)
            "ass", "ssa" -> parseAss(content)
            else -> parseSrt(content)
        }
    }

    /**
     * 根据播放位置查找当前应显示的字幕
     *
     * @param entries 字幕条目列表
     * @param positionMs 当前播放位置（毫秒）
     * @param offsetMs 时间偏移（毫秒），正值为延后
     * @return 当前应显示的字幕文本，无则返回 null
     */
    fun findCurrentSubtitle(entries: List<SubtitleEntry>, positionMs: Long, offsetMs: Long = 0L): String? {
        val adjustedPos = positionMs - offsetMs
        return entries.firstOrNull { adjustedPos in it.startMs..it.endMs }?.text
    }

    /** 解析 SRT 格式 */
    private fun parseSrt(content: String): List<SubtitleEntry> {
        val entries = mutableListOf<SubtitleEntry>()
        val blocks = content.replace("\r\n", "\n").split(Regex("\n\n+"))

        for (block in blocks) {
            val lines = block.trim().lines()
            if (lines.size < 3) continue

            val timeLine = lines.find { it.contains("-->") } ?: continue
            val times = timeLine.split("-->").map { it.trim() }
            if (times.size != 2) continue

            val startMs = parseSrtTime(times[0])
            val endMs = parseSrtTime(times[1])
            val textLines = lines.dropWhile { !it.contains("-->") }.drop(1)
            val text = textLines.joinToString("\n").trim()

            if (text.isNotBlank() && startMs >= 0 && endMs > startMs) {
                entries.add(SubtitleEntry(startMs, endMs, text))
            }
        }
        return entries.sortedBy { it.startMs }
    }

    /** 解析 VTT 格式 */
    private fun parseVtt(content: String): List<SubtitleEntry> {
        val cleaned = content
            .replace("WEBVTT", "")
            .replace(Regex("NOTE[\\s\\S]*?\n\n"), "")
            .trim()
        return parseSrt(cleaned.replace(".", ","))
    }

    /** 解析 ASS/SSA 格式（简化：仅提取 Dialogue 行） */
    private fun parseAss(content: String): List<SubtitleEntry> {
        val entries = mutableListOf<SubtitleEntry>()
        val dialogueRegex = Regex("^Dialogue:\\s*\\d+,(\\d+:\\d{2}:\\d{2}\\.\\d{2}),(\\d+:\\d{2}:\\d{2}\\.\\d{2}),[^,]*,[^,]*,\\d+,\\d+,\\d+,(.*)$", RegexOption.MULTILINE)

        for (match in dialogueRegex.findAll(content)) {
            val startMs = parseAssTime(match.groupValues[1])
            val endMs = parseAssTime(match.groupValues[2])
            val text = match.groupValues[3]
                .replace(Regex("\\{[^}]*\\}"), "")
                .replace("\\N", "\n")
                .replace("\\n", "\n")
                .trim()

            if (text.isNotBlank() && startMs >= 0 && endMs > startMs) {
                entries.add(SubtitleEntry(startMs, endMs, text))
            }
        }
        return entries.sortedBy { it.startMs }
    }

    /** 解析 SRT 时间格式：00:01:23,456 */
    private fun parseSrtTime(time: String): Long {
        val parts = time.replace(",", ".").split(":")
        if (parts.size != 3) return -1
        return runCatching {
            val h = parts[0].toLong()
            val m = parts[1].toLong()
            val sParts = parts[2].split(".")
            val s = sParts[0].toLong()
            val ms = if (sParts.size > 1) sParts[1].take(3).padEnd(3, '0').toLong() else 0L
            h * 3600000 + m * 60000 + s * 1000 + ms
        }.getOrDefault(-1)
    }

    /** 解析 ASS 时间格式：0:01:23.45 */
    private fun parseAssTime(time: String): Long {
        val parts = time.split(":")
        if (parts.size != 3) return -1
        return runCatching {
            val h = parts[0].toLong()
            val m = parts[1].toLong()
            val sParts = parts[2].split(".")
            val s = sParts[0].toLong()
            val ms = if (sParts.size > 1) (sParts[1].take(2).padEnd(3, '0').toLong()) else 0L
            h * 3600000 + m * 60000 + s * 1000 + ms
        }.getOrDefault(-1)
    }
}
