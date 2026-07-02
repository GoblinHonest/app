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

package com.stark.miuix.data.source

import com.stark.miuix.data.model.SearchResult
import com.stark.miuix.data.model.Video
import com.stark.miuix.data.model.VideoSource

/**
 * 视频源引擎接口
 *
 * 定义从单个视频源获取数据的四种核心能力。
 * 实现类 [SourceEngineImpl] 通过 [NetworkClient] + [RuleParser]
 * 完成「请求页面 → 规则解析 → 结构化数据」的完整链路。
 *
 * 所有方法均为 suspend，返回 [Result] 封装成功/失败，
 * 调用方无需 try-catch。
 */
interface SourceEngine {

    /**
     * 使用指定视频源搜索视频
     *
     * @param source 视频源配置
     * @param keyword 搜索关键词
     * @return 搜索结果列表，失败时返回 Result.failure
     */
    suspend fun search(source: VideoSource, keyword: String): Result<List<SearchResult>>

    /**
     * 获取视频详情信息
     *
     * @param source 视频源配置
     * @param url 详情页 URL
     * @return 视频详情，失败时返回 Result.failure
     */
    suspend fun getDetail(source: VideoSource, url: String): Result<Video>

    /**
     * 解析播放地址
     *
     * @param source 视频源配置
     * @param episodeUrl 剧集页面 URL
     * @return 可播放的视频 URL，失败时返回 Result.failure
     */
    suspend fun getPlayerUrl(source: VideoSource, episodeUrl: String): Result<String>

    /**
     * 获取分类视频列表
     *
     * @param source 视频源配置
     * @param categoryUrl 分类页面 URL，为空时使用源默认分类 URL
     * @param page 页码
     * @return 视频搜索结果列表，失败时返回 Result.failure
     */
    suspend fun getCategoryVideos(
        source: VideoSource,
        categoryUrl: String = "",
        page: Int = 1
    ): Result<List<SearchResult>>
}
