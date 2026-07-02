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

package com.stark.miuix.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

/**
 * 通用空状态组件
 *
 * 在列表为空、加载失败等场景下展示友好提示，
 * 可选配重试按钮引导用户操作。
 *
 * @param title 主标题
 * @param message 副标题/描述
 * @param actionText 操作按钮文字（为空不展示）
 * @param onAction 操作回调
 */
@Composable
fun EmptyStateView(
    title: String,
    message: String = "",
    actionText: String = "",
    onAction: () -> Unit = {}
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = title,
                style = MiuixTheme.textStyles.headline1,
                color = MiuixTheme.colorScheme.onSurface
            )
            if (message.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = message,
                    style = MiuixTheme.textStyles.footnote1,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                )
            }
            if (actionText.isNotBlank()) {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier.clickable(onClick = onAction),
                    cornerRadius = 12.dp
                ) {
                    Text(
                        text = actionText,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                        style = MiuixTheme.textStyles.body1,
                        color = MiuixTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

/**
 * 通用错误状态组件
 *
 * @param message 错误信息
 * @param onRetry 重试回调
 */
@Composable
fun ErrorStateView(
    message: String,
    onRetry: () -> Unit = {}
) {
    EmptyStateView(
        title = "出错了",
        message = message,
        actionText = "重试",
        onAction = onRetry
    )
}
