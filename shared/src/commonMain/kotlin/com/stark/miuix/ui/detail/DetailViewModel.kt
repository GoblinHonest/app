/*
 * Copyright 2024 Stark Industries
 */

package com.stark.miuix.ui.detail

import com.stark.miuix.data.model.Video
import com.stark.miuix.data.repository.SourceRepository
import com.stark.miuix.data.repository.VideoRepository
import com.stark.miuix.util.AppLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope

sealed interface DetailUiState {
    data object Loading : DetailUiState
    data class Success(
        val video: Video,
        /** 所有可用源的解析结果 (sourceName -> Video) */
        val allSources: Map<String, Video> = emptyMap(),
        /** 当前选中的源名称 */
        val currentSource: String = ""
    ) : DetailUiState
    data class Error(val message: String) : DetailUiState
}

/**
 * 详情页 ViewModel — 支持多源切换
 *
 * 首先加载指定源的详情，同时后台搜索其他源中的同名视频。
 * 用户可在 UI 中切换不同源的剧集列表。
 */
class DetailViewModel(
    private val videoRepository: VideoRepository,
    private val sourceRepository: SourceRepository
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val _uiState = MutableStateFlow<DetailUiState>(DetailUiState.Loading)
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    /**
     * 加载视频详情 — 先加载主源，再后台查找其他源
     */
    fun loadDetail(sourceName: String, detailUrl: String) {
        scope.launch {
            _uiState.value = DetailUiState.Loading
            val source = sourceRepository.getSourceByName(sourceName)
            if (source == null) {
                _uiState.value = DetailUiState.Error("视频源不存在")
                return@launch
            }

            val result = videoRepository.getDetail(source, detailUrl)
            result.fold(
                onSuccess = { video ->
                    val sources = mutableMapOf(sourceName to video)
                    _uiState.value = DetailUiState.Success(
                        video = video,
                        allSources = sources,
                        currentSource = sourceName
                    )
                    // 后台搜索其他源中的同名视频
                    loadOtherSources(video.title, sourceName, sources)
                },
                onFailure = { error ->
                    _uiState.value = DetailUiState.Error(error.message ?: "加载失败")
                }
            )
        }
    }

    /**
     * 后台从其他启用的源搜索同名视频并加载详情
     */
    private fun loadOtherSources(
        title: String,
        primarySource: String,
        currentSources: MutableMap<String, Video>
    ) {
        if (title.isBlank()) return
        scope.launch {
            val otherSources = sourceRepository.getEnabledSources()
                .filter { it.sourceName != primarySource }

            supervisorScope {
                otherSources.map { source ->
                    async {
                        try {
                            val searchResults = videoRepository.search(title)
                                .getOrDefault(emptyList())
                                .filter { it.sourceName == source.sourceName }
                                .firstOrNull { it.title.trim().equals(title.trim(), ignoreCase = true) }

                            if (searchResults != null) {
                                val video = videoRepository.getDetail(source, searchResults.url).getOrNull()
                                if (video != null) {
                                    AppLogger.d("Detail", "找到其他源: ${source.sourceName}")
                                    synchronized(currentSources) {
                                        currentSources[source.sourceName] = video
                                    }
                                    val current = _uiState.value
                                    if (current is DetailUiState.Success) {
                                        _uiState.value = current.copy(allSources = currentSources.toMap())
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            AppLogger.e("Detail", "搜索源[${source.sourceName}]失败", e)
                        }
                    }
                }
            }
        }
    }

    /**
     * 切换播放源
     */
    fun switchSource(sourceName: String) {
        val current = _uiState.value
        if (current is DetailUiState.Success) {
            val video = current.allSources[sourceName] ?: return
            _uiState.value = current.copy(
                video = video,
                currentSource = sourceName
            )
        }
    }

    suspend fun getPlayerUrl(sourceName: String, episodeUrl: String): Result<String> {
        val source = sourceRepository.getSourceByName(sourceName)
            ?: return Result.failure(Exception("视频源不存在"))
        return videoRepository.getPlayerUrl(source, episodeUrl)
    }
}
