请按照项目中 CLAUDE.md 的完整规范，创建一个完整的 Compose Multiplatform 跨平台视频聚合播放器项目。

这是一个类似开源阅读（legado）的视频版本：通过导入通用 JSON 格式视频源来获取视频内容。

请创建以下所有文件：

## 1. Gradle 构建文件
- gradle/libs.versions.toml - Version Catalog（Kotlin 2.1.10, Compose Multiplatform 1.7.3, Miuix 0.9.2, AGP 8.7.3）
- build.gradle.kts - 根构建文件
- shared/build.gradle.kts - 共享模块（KMP, 依赖 Miuix 全部模块）
- androidApp/build.gradle.kts - Android 应用模块
- desktopApp/build.gradle.kts - Desktop JVM 应用
- webApp/build.gradle.kts - Web WasmJs 应用
- gradle/wrapper/gradle-wrapper.properties - Gradle 8.11.1

## 2. 数据层 (shared/src/commonMain/kotlin/com/stark/miuix/data/)
- model/VideoSource.kt - 视频源数据模型（含 search/category/detail/player 规则）
- model/Video.kt - 视频数据模型
- model/Episode.kt - 剧集数据模型
- model/SearchResult.kt - 搜索结果模型
- source/SourceEngine.kt - 视频源引擎接口
- source/SourceEngineImpl.kt - 引擎实现
- parser/RuleParser.kt - 规则解析器接口
- parser/HtmlParser.kt - HTML 解析
- parser/JsonParser.kt - JSON 解析
- repository/VideoRepository.kt
- repository/SourceRepository.kt

## 3. UI层 (shared/src/commonMain/kotlin/com/stark/miuix/)
- App.kt - 根 Composable
- theme/MiuixAppTheme.kt - MiuixTheme + ThemeController + Monet动态取色
- navigation/AppNavigation.kt + Screen.kt
- ui/home/HomeScreen.kt + HomeViewModel.kt
- ui/category/CategoryScreen.kt + CategoryViewModel.kt
- ui/search/SearchScreen.kt + SearchViewModel.kt
- ui/detail/DetailScreen.kt + DetailViewModel.kt
- ui/player/PlayerScreen.kt + VideoPlayer.kt (expect声明)
- ui/source/SourceManageScreen.kt + SourceManageViewModel.kt
- ui/settings/SettingsScreen.kt + SettingsViewModel.kt
- ui/components/VideoCard.kt, VideoGrid.kt, EpisodeList.kt, SourceCard.kt

## 4. 工具层 (shared/src/commonMain/kotlin/com/stark/miuix/util/)
- NetworkClient.kt - Ktor HTTP 客户端
- JsonUtils.kt, StringUtils.kt

## 5. 平台实现
- shared/src/androidMain/kotlin/com/stark/miuix/ui/player/VideoPlayer.android.kt
- shared/src/desktopMain/kotlin/com/stark/miuix/ui/player/VideoPlayer.desktop.kt
- shared/src/wasmJsMain/kotlin/com/stark/miuix/ui/player/VideoPlayer.wasmJs.kt
- androidApp/src/main/kotlin/com/stark/miuix/MainActivity.kt
- androidApp/src/main/AndroidManifest.xml
- desktopApp/src/main/kotlin/com/stark/miuix/Main.kt
- webApp/src/wasmJsMain/kotlin/com/stark/miuix/Main.kt
- webApp/src/wasmJsMain/resources/index.html

## 6. 示例数据
- shared/src/commonMain/composeResources/files/example_source.json

要求：每个类和公共函数有详细 KDoc 注释、sealed class 管理 UI 状态、Result 封装错误、协程+Flow、Apache-2.0 license 头。
