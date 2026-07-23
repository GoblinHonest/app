/*
 * Copyright 2024 Stark Industries
 */

package com.stark.miuix

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.compose.rememberNavController
import com.stark.miuix.di.AppContainer
import com.stark.miuix.navigation.AppNavigation
import com.stark.miuix.theme.AppTheme
import com.stark.miuix.theme.ThemeState
import miuix_app.shared.generated.resources.Res
import org.jetbrains.compose.resources.ExperimentalResourceApi

@OptIn(ExperimentalResourceApi::class)
@Composable
fun App() {
    // 尽早恢复主题，避免首帧闪烁错误模式
    ThemeState.init(AppContainer.localStorage)

    LaunchedEffect(Unit) {
        try {
            val bytes = Res.readBytes("files/example_source.json")
            AppContainer.sourceRepository.initWithDefaultsIfEmpty(bytes.decodeToString())
        } catch (_: Exception) { }
        try {
            val bytes = Res.readBytes("files/feifan_source.json")
            AppContainer.sourceRepository.initWithDefaultsIfEmpty(bytes.decodeToString())
        } catch (_: Exception) { }
    }

    val navController = rememberNavController()

    AppTheme {
        AppNavigation(
            navController = navController,
            videoRepository = AppContainer.videoRepository,
            sourceRepository = AppContainer.sourceRepository,
            userDataRepository = AppContainer.userDataRepository
        )
    }
}
