# CineHub 视频聚合播放器 — 产品需求文档 (PRD)

> 版本: v2.0 | 日期: 2026-07-19 | 状态: 待评审

## 一、产品定位

CineHub 是一款跨平台视频聚合播放器，核心理念是"规则引擎 + 多源聚合"——用户通过导入 JSON 视频源即可观看全网视频内容，无需为每个平台单独安装 APP。

**目标用户**: 影视爱好者、动漫追番党、多平台内容消费者
**核心价值**: 一个 APP 看全网，规则驱动、源可自定义、跨平台一致体验

---

## 二、现状盘点

### 已有能力 (v1.0)

| 模块 | 能力 | 完成度 |
|------|------|--------|
| 首页 | 分类 Tab（推荐/电视剧/动漫/电影）、多源聚合、交错去重 | 80% |
| 搜索 | 多源并行搜索、搜索历史 | 60% |
| 详情页 | 视频信息、剧集列表、多源切换、收藏 | 75% |
| 播放器 | ExoPlayer、全屏手势（亮度/音量/进度/倍速/锁屏）、DLNA 投屏 | 70% |
| 视频源 | JSON 导入/导出、启用/禁用、CSS/JSONPath/XPath 解析 | 70% |
| 个人中心 | 观看历史、收藏列表 | 50% |
| 设置 | 基础设置项 | 30% |
| 跨平台 | Android / Desktop / Web(WasmJs) / iOS(条件编译) | 85% |

### 核心差距（对标成熟视频 APP）

1. **播放体验**: 无字幕、无弹幕、无画质切换、无断点续播、无后台/画中画
2. **内容发现**: 无个性化推荐、无热搜、无搜索联想
3. **源管理**: 无在线源仓库、无源健康检测、无 JS 规则引擎
4. **离线能力**: 无下载管理、无离线播放
5. **社交属性**: 无评论、无评分、无观看进度同步
6. **数据管理**: 无备份/恢复、无多设备同步

---

## 三、需求总览与优先级

### P0 — 核心体验（不做就不是成熟产品）

| # | 需求 | 模块 | 预估工期 |
|---|------|------|----------|
| 1 | 断点续播 + "继续观看"卡片 | 播放器/首页 | 3d |
| 2 | 字幕系统（内嵌/外挂/在线） | 播放器 | 5d |
| 3 | 画质/线路切换 | 播放器/详情 | 3d |
| 4 | 搜索联想 + 热搜榜 | 搜索 | 3d |
| 5 | 源健康检测 + 一键修复 | 源管理 | 3d |
| 6 | 后台播放 + 通知栏控制 | 播放器/Android | 4d |
| 7 | 画中画 (PiP) | 播放器/Android | 2d |
| 8 | 数据备份与恢复 | 设置 | 2d |

### P1 — 竞争力功能（做了显著领先）

| # | 需求 | 模块 | 预估工期 |
|---|------|------|----------|
| 9 | 弹幕系统 | 播放器 | 7d |
| 10 | 在线源仓库 + 一键订阅 | 源管理 | 5d |
| 11 | JS 规则引擎（兼容 legado 语法） | 解析器 | 7d |
| 12 | 下载管理 + 离线播放 | 新模块 | 7d |
| 13 | 个性化推荐（基于观看历史） | 首页 | 5d |
| 14 | 多设备进度同步 | 数据层 | 5d |
| 15 | 平板/TV 自适应布局 | 全局 UI | 5d |

### P2 — 差异化功能（锦上添花）

| # | 需求 | 模块 | 预估工期 |
|---|------|------|----------|
| 16 | 一起看（同步播放房间） | 新模块 | 7d |
| 17 | AI 智能匹配（跨源自动找同一部剧） | 详情页 | 5d |
| 18 | 语音搜索 | 搜索 | 3d |
| 19 | 自定义主题 + 图标包 | 设置 | 3d |
| 20 | Widget 小组件（继续观看） | Android | 3d |
| 21 | 无障碍适配（TalkBack/VoiceOver） | 全局 | 5d |

---

## 四、P0 需求详细设计

### 需求 1: 断点续播 + "继续观看"

**用户故事**: 作为一个追剧用户，我昨天看到第 5 集 23:15 关掉了 APP，今天打开后希望首页直接告诉我"继续观看 第5集"，点一下就从 23:15 接着播。

**功能描述**:
- 播放过程中每 5 秒持久化一次进度（videoId + episodeIndex + positionMs + durationMs）
- 首页顶部新增"继续观看"横向卡片（最多 6 条），显示封面 + 标题 + 进度条 + "第X集"
- 点击卡片直接跳转到对应剧集的对应进度
- 详情页打开时，如果该剧有观看记录，自动定位到上次观看的集数并高亮
- 播放器全屏时，顶部标题栏显示"上次看到 23:15"提示（3 秒后消失）

**验收标准**:
- [ ] 播放 5 秒后杀进程，重新打开能从 ±1 秒内恢复
- [ ] 首页"继续观看"卡片正确显示进度百分比
- [ ] 切换剧集后进度独立记录
- [ ] 观看完成的视频（进度 > 95%）从"继续观看"中移除
- [ ] 支持手动清除单条/全部继续观看记录

**数据模型变更**:
```kotlin
data class WatchProgress(
    val videoId: String,
    val episodeIndex: Int,
    val episodeName: String,
    val positionMs: Long,
    val durationMs: Long,
    val timestamp: Long,
    val sourceName: String,
    val detailUrl: String,
    val title: String,
    val cover: String
)
```

---

### 需求 2: 字幕系统

**用户故事**: 作为一个看外语片/生肉动漫的用户，我需要加载字幕才能看懂内容。

**功能描述**:
- **内嵌字幕**: 自动检测视频流中的字幕轨道（ExoPlayer 原生支持），在播放器设置中列出可选字幕
- **外挂字幕**: 支持从本地文件加载 .srt / .ass / .vtt 字幕
- **在线字幕**: 集成 OpenSubtitles / SubHD API，按视频标题自动搜索匹配字幕
- **字幕渲染**: 支持字号/颜色/背景/位置调整，ASS 格式支持样式渲染
- **字幕偏移**: 支持 ±0.1s 步进的时间偏移调整
- **字幕记忆**: 同一部剧的字幕选择跨集记忆

**验收标准**:
- [ ] 内嵌字幕自动检测并在播放器菜单中列出
- [ ] 外挂 .srt 文件能正确渲染，支持 UTF-8/GBK 编码
- [ ] .ass 字幕支持基础样式（颜色/字号/位置）
- [ ] 在线字幕搜索返回结果列表，支持手动选择
- [ ] 字幕偏移调整后实时生效
- [ ] 字幕设置（字号/颜色/偏移）持久化

**技术方案**:
- Android: ExoPlayer `SubtitleView` + `SubtitleDecoderFactory`
- Desktop: VLCJ 字幕渲染 或 自绘 Compose Text overlay
- 字幕解析: commonMain 实现 SRT/VTT 解析器，ASS 用简化渲染

---

### 需求 3: 画质/线路切换

**用户故事**: 作为一个网络环境不稳定的用户，当 1080P 卡顿时，我希望能快速切到 720P 或换一条线路。

**功能描述**:
- 详情页解析时提取所有可用线路（MacCMS `vod_play_from` 多线路 / HTML 多播放源）
- 播放器全屏时，底部按钮区新增"线路"按钮，弹出线路选择面板
- 切换线路时保持当前播放进度（seek 到相同位置）
- 线路列表显示名称 + 当前选中态
- 支持"自动"模式：当前线路加载失败时自动尝试下一条

**验收标准**:
- [ ] 多线路视频正确解析并显示所有线路
- [ ] 切换线路后进度无缝衔接（误差 < 2s）
- [ ] 线路加载失败时 Toast 提示并自动切换
- [ ] 单线路视频不显示线路按钮

**数据模型变更**:
```kotlin
data class PlayLine(
    val name: String,        // 线路名（如"线路1""量子云"）
    val episodes: List<Episode>,
    val playerHeaders: Map<String, String> = emptyMap()
)

// Video 模型扩展
data class Video(
    // ... 现有字段
    val playLines: List<PlayLine> = emptyList()  // 多线路
)
```

---

### 需求 4: 搜索联想 + 热搜榜

**用户故事**: 作为一个用户，我输入"庆"的时候希望看到"庆余年""庆余年2"等联想词，不用打完全名。

**功能描述**:
- **搜索联想**: 输入时实时请求各源的搜索建议接口（MacCMS `suggest` API / HTML 联想），去重合并后下拉展示
- **热搜榜**: 聚合各源首页推荐/排行榜数据，展示"热搜 TOP 10"
- **搜索历史**: 已有，增加"清空历史"按钮和单条删除（左滑）
- **搜索过滤**: 搜索结果支持按源筛选（Tab 或 Chip）
- **空结果引导**: 无结果时推荐"换个关键词试试" + 热门源推荐

**验收标准**:
- [ ] 输入 2 个字符后 300ms 内返回联想结果
- [ ] 联想结果去重，最多显示 8 条
- [ ] 热搜榜每次打开搜索页刷新
- [ ] 搜索结果按源分组，支持单源筛选
- [ ] 搜索历史支持单条删除和全部清空

**技术方案**:
- 联想: 各源 `searchRule` 新增 `suggestUrl` 字段，SourceEngine 新增 `getSuggestions()` 方法
- 热搜: 各源 `categoryRule` 新增 `hotUrl` 字段，或复用分类接口取前 N 条
- 防抖: 输入框 300ms debounce，取消上一次请求

---

### 需求 5: 源健康检测 + 一键修复

**用户故事**: 作为一个导入了 10 个源的用户，我不知道哪些源还能用，希望一键检测并禁用失效源。

**功能描述**:
- 源管理页新增"全部检测"按钮
- 检测逻辑: 对每个启用源发起一次分类请求（取第一页），判断 HTTP 状态码 + 响应内容是否包含预期结构
- 检测结果: 每个源显示状态（正常 / 超时 / 解析失败 / 不可达）+ 响应时间
- 一键操作: "禁用全部失效源" / "重新启用全部"
- 单源检测: 源卡片长按弹出"检测此源"
- 检测历史: 记录最近一次检测时间和结果

**验收标准**:
- [ ] 全部检测在 30 秒内完成（并行请求）
- [ ] 正确识别 4 种状态
- [ ] 检测过程中显示进度（已检测 X/Y）
- [ ] 禁用失效源后首页不再加载该源

**VideoSource 模型扩展**:
```kotlin
data class VideoSource(
    // ... 现有字段
    val lastCheckTime: Long = 0,
    val lastCheckStatus: String = "",  // ok / timeout / parse_error / unreachable
    val lastCheckLatencyMs: Long = 0
)
```

---

### 需求 6: 后台播放 + 通知栏控制

**用户故事**: 作为一个听歌/听书/听广播剧的用户，我锁屏后希望音频继续播放，并能通过通知栏控制暂停/下一集。

**功能描述**:
- 播放器设置中新增"后台播放"开关（默认关闭）
- 开启后，退出详情页/锁屏时 ExoPlayer 不释放，转为后台音频模式
- Android 通知栏显示: 封面 + 标题 + 集数 + 播放/暂停/上一集/下一集
- 通知栏点击回到播放器页面
- 支持 MediaSession，兼容车机/蓝牙耳机按键
- 退出 APP（从最近任务清除）时停止播放

**验收标准**:
- [ ] 开启后台播放后锁屏，音频不中断
- [ ] 通知栏控制按钮全部可用
- [ ] 蓝牙耳机播放/暂停键生效
- [ ] 关闭后台播放后行为与当前一致（退出即停止）
- [ ] 从最近任务清除 APP 后播放停止

**技术方案**:
- Android: `MediaSessionService` + `MediaLibrarySession` (Media3)
- 前台服务通知: `MediaStyle` 通知
- 生命周期: 绑定到 Application 级别，不随 Activity 销毁

---

### 需求 7: 画中画 (PiP)

**用户故事**: 作为一个边看视频边聊微信的用户，我希望把视频缩小为悬浮窗继续播放。

**功能描述**:
- 播放器全屏时，底部按钮区新增"小窗"按钮
- 点击后进入 PiP 模式，视频以 16:9 小窗悬浮在其他 APP 上方
- PiP 模式下支持: 播放/暂停、上一集/下一集（通过 PiP 操作按钮）
- 点击小窗回到 APP 播放器页面
- Android 12+ 支持自动 PiP（按 Home 键自动进入）

**验收标准**:
- [ ] PiP 小窗正确显示视频画面
- [ ] PiP 操作按钮（播放/暂停/上下集）可用
- [ ] 点击小窗回到 APP 且播放状态一致
- [ ] AndroidManifest 已声明 `supportsPictureInPicture`（已有）

---

### 需求 8: 数据备份与恢复

**用户故事**: 作为一个换了新手机的用户，我希望把旧手机上的源、收藏、历史一键迁移过来。

**功能描述**:
- 设置页新增"数据管理"入口
- **备份**: 将 sources.json + favorites.json + history.json + settings 打包为单个 `.cinehub` 文件（ZIP 格式），支持保存到本地/分享
- **恢复**: 从 `.cinehub` 文件导入，支持覆盖/合并两种模式
- **云备份（可选）**: 支持 WebDAV 同步到坚果云/Nextcloud
- 备份文件包含版本号，恢复时做兼容性检查

**验收标准**:
- [ ] 备份文件包含所有用户数据
- [ ] 恢复后数据完整，无丢失
- [ ] 合并模式不覆盖已有数据
- [ ] 备份文件可通过系统分享发送

---

## 五、P1 需求概要

### 需求 9: 弹幕系统
- 集成弹弹play开放API / Bilibili弹幕接口
- 按视频标题+集数匹配弹幕
- 支持弹幕开关/透明度/字号/速度/区域调整
- 播放器 Canvas 层渲染，不影响视频性能

### 需求 10: 在线源仓库 + 一键订阅
- 内置 2-3 个公开源仓库地址（GitHub Raw / 自建 CDN）
- 源仓库页面: 分类浏览、搜索、一键导入
- 订阅机制: 订阅的源仓库定期拉取更新，新源自动提示
- 源评分: 用户可标记"好用/失效"，聚合显示

### 需求 11: JS 规则引擎
- 集成 QuickJS (KMP 绑定) 或 JsEngine
- 兼容 legado 的 JS 规则语法（`js:` 前缀）
- 规则执行沙箱: 限制网络访问范围、超时控制
- 调试模式: 规则编辑器 + 实时预览解析结果

### 需求 12: 下载管理 + 离线播放
- 下载队列: 支持多任务并行（可配置并发数）
- 下载格式: 原始流（m3u8 → ts 合并 / mp4 直下）
- 下载管理页: 进度/速度/暂停/删除
- 离线播放: 已下载视频在详情页显示"离线可用"标记
- 存储空间管理: 显示已用空间，支持批量清理

### 需求 13: 个性化推荐
- 基于观看历史的标签提取（类型/年份/地区）
- 首页"猜你喜欢"模块，从已启用源中匹配相似内容
- 冷启动: 首次使用时展示各分类热门内容
- 负反馈: "不感兴趣"按钮，过滤同类内容

### 需求 14: 多设备进度同步
- 基于 WebDAV / 自建轻量 API
- 同步内容: 观看进度 + 收藏 + 源列表
- 冲突策略: 以最新时间戳为准
- 同步频率: 播放进度实时同步，其他数据启动时同步

### 需求 15: 平板/TV 自适应布局
- 平板: 详情页左右分栏（左视频+右剧集列表）
- TV: D-Pad 焦点导航、大字体、简化手势
- 响应式断点: 600dp / 840dp / 1200dp
- Android TV: Leanback 风格首页

---

## 六、P2 需求概要

### 需求 16: 一起看
- 创建/加入房间（房间号或链接）
- 基于 WebSocket 的播放状态同步（播放/暂停/seek）
- 房间内文字聊天
- 房主控制模式 / 自由模式

### 需求 17: AI 智能匹配
- 详情页加载后，用标题+年份+类型做模糊匹配
- 跨源自动关联同一部剧的不同源版本
- 显示"其他源也有此剧 (3)"，一键切换

### 需求 18: 语音搜索
- 集成系统语音识别（Android SpeechRecognizer / Web Speech API）
- 搜索页麦克风按钮，识别结果填入搜索框
- 支持连续对话: "播放庆余年第二集"

### 需求 19: 自定义主题
- 预设主题: 深色/浅色/纯黑(AMOLED)/跟随系统
- 自定义主色调（取色器）
- 图标包切换（线性/填充/圆角）
- 字体大小全局调整

### 需求 20: Widget 小组件
- Android: 4x1 / 4x2 继续观看 Widget
- 显示最近 1-3 条观看记录 + 进度
- 点击直接跳转到对应剧集

### 需求 21: 无障碍适配
- 所有交互元素添加 contentDescription
- 焦点顺序合理（Tab 键可遍历）
- 字幕支持屏幕阅读器朗读
- 高对比度模式

---

## 七、非功能性需求

### 性能
| 指标 | 目标 |
|------|------|
| 冷启动到首页可交互 | < 1.5s |
| 搜索结果首屏 | < 2s |
| 详情页加载 | < 1.5s |
| 播放器首帧 | < 2s |
| 内存占用（播放中） | < 200MB |
| APK 体积 | < 30MB |

### 稳定性
- 崩溃率 < 0.1%
- ANR 率 < 0.05%
- 源解析失败不崩溃，静默降级

### 安全
- 源规则执行沙箱（JS 引擎限制文件/网络访问）
- 不收集用户观看数据（纯本地）
- 备份文件不含敏感信息

### 兼容性
- Android: minSdk 26 (Android 8.0)，targetSdk 35
- Desktop: Windows 10+ / macOS 12+ / Ubuntu 20.04+
- Web: Chrome 90+ / Firefox 90+ / Safari 15+
- iOS: 15.0+

---

## 八、版本规划

```
v2.0 (P0 核心体验)          ← 当前目标
  ├── 断点续播 + 继续观看
  ├── 字幕系统
  ├── 画质/线路切换
  ├── 搜索联想 + 热搜
  ├── 源健康检测
  ├── 后台播放 + 通知栏
  ├── 画中画
  └── 数据备份恢复

v2.5 (P1 竞争力)
  ├── 弹幕系统
  ├── 在线源仓库
  ├── JS 规则引擎
  ├── 下载管理
  └── 个性化推荐

v3.0 (P2 差异化)
  ├── 一起看
  ├── AI 智能匹配
  ├── 语音搜索
  ├── 平板/TV 适配
  └── 无障碍
```

---

## 九、数据模型变更汇总

```kotlin
// 新增: 观看进度（断点续播）
data class WatchProgress(
    val videoId: String,
    val episodeIndex: Int,
    val episodeName: String,
    val positionMs: Long,
    val durationMs: Long,
    val timestamp: Long,
    val sourceName: String,
    val detailUrl: String,
    val title: String,
    val cover: String
)

// 新增: 播放线路
data class PlayLine(
    val name: String,
    val episodes: List<Episode>,
    val playerHeaders: Map<String, String> = emptyMap()
)

// 新增: 字幕
data class Subtitle(
    val name: String,
    val url: String,
    val language: String = "",
    val format: String = "srt"  // srt / ass / vtt
)

// 新增: 下载任务
data class DownloadTask(
    val id: String,
    val videoId: String,
    val title: String,
    val episodeName: String,
    val url: String,
    val status: DownloadStatus,
    val progress: Float,
    val totalBytes: Long,
    val downloadedBytes: Long,
    val filePath: String = ""
)

enum class DownloadStatus { PENDING, DOWNLOADING, PAUSED, COMPLETED, FAILED }

// 扩展: VideoSource
data class VideoSource(
    // ... 现有字段
    val lastCheckTime: Long = 0,
    val lastCheckStatus: String = "",
    val lastCheckLatencyMs: Long = 0
)

// 扩展: SearchRule
data class SearchRule(
    // ... 现有字段
    val suggestUrl: String = "",     // 搜索联想 URL
    val suggestRule: String = ""     // 联想结果解析规则
)

// 扩展: Video
data class Video(
    // ... 现有字段
    val playLines: List<PlayLine> = emptyList(),
    val subtitles: List<Subtitle> = emptyList()
)
```

---

## 十、开放问题

| # | 问题 | 影响 | 建议 |
|---|------|------|------|
| 1 | JS 引擎选型: QuickJS vs Hermes vs 自研 | P1-11 工期 | 优先 QuickJS（成熟、体积小、KMP 有现成绑定） |
| 2 | 弹幕数据源: 弹弹play API 是否稳定 | P1-9 可行性 | 先做框架，数据源可插拔 |
| 3 | 下载 m3u8 合并是否需要 FFmpeg | P1-12 体积 | 用 Kotlin 原生 TS 合并，避免引入 FFmpeg |
| 4 | 多设备同步用 WebDAV 还是自建服务 | P1-14 运维 | 先 WebDAV（零运维），后续可选自建 |
| 5 | Android minSdk 是否从 33 降到 26 | 用户覆盖 | 建议降到 26，Media3 支持 minSdk 21 |
| 6 | iOS 播放器选型: AVPlayer vs VLC Mobile | 跨平台一致性 | AVPlayer（原生、体积小），VLC 作为备选 |
