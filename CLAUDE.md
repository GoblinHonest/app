# Miuix Video App - 开发指南

## 项目概述
一个跨平台视频聚合播放器，使用 Compose Multiplatform + Miuix UI 库，纯小米 HyperOS 设计风格。
核心理念：类似开源阅读（legado）的规则引擎模式，通过导入通用 JSON 格式视频源来获取视频内容。

## 技术栈
- **语言**: Kotlin
- **UI框架**: Compose Multiplatform 1.11.1
- **UI组件库**: Miuix (top.yukonga.miuix.kmp) v0.9.2
- **构建**: Gradle (Kotlin DSL) + Version Catalog
- **目标平台**: Android, Desktop (JVM), iOS, Web (WasmJs)

## 代码规范

### 架构
- 采用 MVVM + Clean Architecture
- 模块划分清晰：数据层 / 领域层 / 表现层
- 依赖注入使用简单的 ServiceLocator（KMP 兼容）
- 所有跨平台代码放 commonMain，平台特定实现用 expect/actual

### 命名规范
- 包名: `com.stark.miuix`
- 类名: PascalCase（如 `VideoPlayer`, `SourceManager`）
- 函数名: camelCase（如 `loadVideo`, `parseEpisode`）
- 常量: SCREAMING_SNAKE_CASE
- 资源文件: snake_case

### 代码质量
- 每个类和公共函数必须有 KDoc 注释
- 关键业务逻辑要有详细注释说明"为什么这样做"
- 使用 Kotlin 协程处理异步操作
- 错误处理要完善，不能有未捕获异常崩溃
- 使用密封类（sealed class）管理状态

### Miuix 使用规范
- 所有 UI 组件优先使用 Miuix 提供的组件
- 主题使用 MiuixTheme + ThemeController（Monet 动态取色）
- 圆角统一使用 Squircle 形状
- 列表使用 Miuix 的 TopAppBar + 滚动联动
- 设置页使用 miuix-preference 组件
- 暗色模式必须适配

### 视频源格式
参考开源阅读的 bookSource 概念，设计 VideoSource JSON 格式：
- sourceName, sourceUrl, version, enabled
- search: 搜索规则（URL模板 + XPath/CSS/JSONPath 解析规则）
- category: 分类规则
- detail: 详情页解析规则
- player: 播放地址解析规则

### 依赖管理
Miuix 依赖：
```kotlin
implementation("top.yukonga.miuix.kmp:miuix-ui:0.9.2")
implementation("top.yukonga.miuix.kmp:miuix-preference:0.9.2")
implementation("top.yukonga.miuix.kmp:miuix-icons:0.9.2")
implementation("top.yukonga.miuix.kmp:miuix-blur:0.9.2")
implementation("top.yukonga.miuix.kmp:miuix-squircle:0.9.2")
```

## 项目结构
```
miuix-app/
├── build.gradle.kts          # 根构建文件
├── settings.gradle.kts       # 项目设置
├── gradle.properties         # Gradle 属性
├── gradle/
│   └── libs.versions.toml    # Version Catalog
├── shared/                   # 共享模块（核心逻辑 + UI）
│   ├── build.gradle.kts
│   └── src/
│       ├── commonMain/       # 跨平台代码
│       │   ├── kotlin/com/stark/miuix/
│       │   │   ├── App.kt              # App 入口 Composable
│       │   │   ├── theme/              # 主题定义
│       │   │   ├── navigation/         # 导航
│       │   │   ├── ui/                 # UI 组件
│       │   │   │   ├── home/           # 首页
│       │   │   │   ├── category/       # 分类
│       │   │   │   ├── search/         # 搜索
│       │   │   │   ├── detail/         # 详情
│       │   │   │   ├── player/         # 播放器
│       │   │   │   ├── source/         # 视频源管理
│       │   │   │   └── settings/       # 设置
│       │   │   ├── data/               # 数据层
│       │   │   │   ├── model/          # 数据模型
│       │   │   │   ├── source/         # 视频源引擎
│       │   │   │   ├── parser/         # 解析器
│       │   │   │   └── repository/     # 仓库
│       │   │   └── util/               # 工具类
│       │   └── composeResources/       # Compose 资源
│       ├── androidMain/      # Android 特定实现
│       ├── desktopMain/      # Desktop 特定实现
│       ├── iosMain/          # iOS 特定实现
│       └── wasmJsMain/       # Web 特定实现
├── androidApp/               # Android 应用入口
├── desktopApp/               # Desktop 应用入口
└── webApp/                   # Web 应用入口
```

## 注意事项
- 不要使用 Material 3 组件，全部用 Miuix
- KMP 项目，注意 sourceSet 的正确使用
- 使用 libs.version catalog 管理依赖版本
- 构建文件使用 Kotlin DSL（.kts）
- 所有文件使用 UTF-8 编码
- 行尾使用 LF 换行符
