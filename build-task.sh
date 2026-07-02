#!/bin/bash
cd /mnt/workspace/apps/miuix-app
claude --dangerously-skip-permissions --max-turns 50 -p "请按照 CLAUDE.md 中的完整规范，创建一个完整的 Compose Multiplatform 跨平台视频聚合播放器项目。这是一个类似开源阅读（legado）的视频版本：通过导入通用 JSON 格式视频源来获取视频内容。

需要创建的所有文件：

1. Gradle 构建文件：gradle/libs.versions.toml（Kotlin 2.1.10, Compose Multiplatform 1.7.3, Miuix 0.9.2, AGP 8.7.3）、build.gradle.kts、shared/build.gradle.kts、androidApp/build.gradle.kts、desktopApp/build.gradle.kts、webApp/build.gradle.kts、gradle/wrapper/gradle-wrapper.properties（Gradle 8.11.1）

2. 数据层 data/：VideoSource.kt、Video.kt、Episode.kt、SearchResult.kt、SourceEngine.kt、SourceEngineImpl.kt、RuleParser.kt、HtmlParser.kt、JsonParser.kt、VideoRepository.kt、SourceRepository.kt

3. UI层 ui/：App.kt、MiuixAppTheme.kt、AppNavigation.kt、Screen.kt、HomeScreen+VM、CategoryScreen+VM、SearchScreen+VM、DetailScreen+VM、PlayerScreen+VideoPlayer(expect)、SourceManageScreen+VM、SettingsScreen+VM、VideoCard.kt、VideoGrid.kt、EpisodeList.kt、SourceCard.kt

4. 工具层 util/：NetworkClient.kt、JsonUtils.kt、StringUtils.kt

5. 平台实现：androidMain VideoPlayer.android.kt + MainActivity.kt + AndroidManifest.xml、desktopMain VideoPlayer.desktop.kt + Main.kt、wasmJsMain VideoPlayer.wasmJs.kt + Main.kt + index.html

6. 示例：example_source.json

代码质量要求：每个类和公共函数有详细 KDoc 注释、sealed class 管理 UI 状态、Result 类型封装错误、协程+Flow、Apache-2.0 license 头、import 有序、代码整洁。完成后直接执行所有文件创建，不要 git 操作。"
