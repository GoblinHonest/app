# Requirements Document

## Introduction

在现有 JSON 规则视频源（类 legado）基础上，增强「复杂源」表达与执行能力，使系统能够稳定支持：多线路/多清晰度、二次解析播放地址、自定义请求头与 Cookie、POST/分页、多步请求链等真实站点常见场景。

本需求保持跨平台（Android / Desktop / Web / iOS）兼容，优先在声明式规则与引擎层增强；可选支持进程内代码插件源，作为规则表达力不足时的扩展通道。

## Glossary

- **System**：CineHub / miuix 视频聚合播放器
- **VideoSource**：JSON 格式视频源配置
- **RuleSource**：基于声明式规则的源（当前主路径）
- **PluginSource**：进程内代码插件源（可选扩展路径）
- **PlayLine**：播放线路（如「线路1」「量子」）
- **Episode**：剧集/清晰度项
- **VideoType**：播放解析类型（Direct / Decode / Regex / Sniff）
- **Decode**：详情阶段仅拿到中间标识，播放前需二次请求解析出真实流地址
- **RequestConfig**：源级请求配置（headers、Cookie、method、body、timeout）
- **SourceEngine**：统一源执行引擎接口

## Requirements

### Requirement 1 — 播放类型与二次解析

**User Story:** AS 用户, I want 复杂站点的视频也能正确播放, so that 我不必因签名/中间页/接口加密而无法观影

#### Acceptance Criteria

1. WHEN 剧集的 `videoType` 为 `Direct`，the System SHALL 将 `episode.url` 视为可直接播放的地址（或仅需附加 headers）
2. WHEN 剧集的 `videoType` 为 `Decode`，the System SHALL 在播放前调用引擎的二次解析流程，并返回最终流地址与播放 headers
3. WHEN 剧集的 `videoType` 为 `Regex`，the System SHALL 使用源配置中的正则规则从响应中提取播放地址
4. WHEN 二次解析成功，the System SHALL 向播放器提供 `url` 与可选 `headers`、`audioUrl`
5. IF 二次解析失败，the System SHALL 返回可读错误信息，并允许用户切换其他线路/剧集重试

### Requirement 2 — 多线路与清晰度模型

**User Story:** AS 用户, I want 在详情页选择线路与清晰度, so that 我能在不同线路失败时快速切换

#### Acceptance Criteria

1. WHEN 详情解析完成且存在多条线路，the System SHALL 在详情页展示全部 `PlayLine`
2. WHEN 用户切换线路，the System SHALL 展示该线路下的剧集列表
3. WHEN 同一集存在多个清晰度项，the System SHALL 以独立 `Episode` 或清晰度列表形式供用户选择
4. WHEN 详情仅有单线路，the System SHALL 隐藏线路切换控件并直接展示剧集
5. WHERE 源为 MacCMS 格式，the System SHALL 继续兼容 `vod_play_from` / `vod_play_url` 的 `$$$` 多线路解析

### Requirement 3 — 源级请求配置

**User Story:** AS 源维护者, I want 为源配置 headers/Cookie/请求方法, so that 需要鉴权或反爬的站点可正常访问

#### Acceptance Criteria

1. WHEN 源配置了 `requestHeaders`，the System SHALL 在搜索、分类、详情请求中附带这些 headers
2. WHEN 源配置了 `cookie`（或用户在源设置中填写 Cookie），the System SHALL 在后续请求中附带 `Cookie` 头
3. WHEN 规则 URL 指定 `method=POST` 或源规则声明 POST，the System SHALL 使用 POST 发送请求（含可选 body 模板）
4. WHEN 播放规则配置了 `playerHeaders`，the System SHALL 在解析播放地址与实际播放请求中应用这些 headers
5. IF Cookie 失效导致业务失败，the System SHALL 向用户展示可操作的错误提示（例如引导更新 Cookie）

### Requirement 4 — 分页与 hasMore

**User Story:** AS 用户, I want 分类与搜索支持稳定翻页, so that 我可以浏览完整结果

#### Acceptance Criteria

1. WHEN 分类/搜索 URL 模板包含 `{{page}}` 或 `{{keyword}}`，the System SHALL 正确替换占位符
2. WHEN 解析结果声明 `hasMore=true` 或结果数量达到页大小阈值，the System SHALL 允许加载下一页
3. WHEN 当前页无结果或 `hasMore=false`，the System SHALL 停止自动加载更多
4. WHILE 正在加载更多，the System SHALL 展示加载状态并防止重复请求同一页

### Requirement 5 — 增强规则 DSL 与多步请求

**User Story:** AS 源维护者, I want 用更强的规则描述复杂页面, so that 多数站点无需写代码插件

#### Acceptance Criteria

1. WHEN 字段规则使用 `||` 分隔多个候选规则，the System SHALL 按顺序尝试并采用第一个非空结果
2. WHEN 字段规则使用 `&&` 连接多个规则，the System SHALL 合并多个匹配结果
3. WHEN 规则类型为 `regex`，the System SHALL 支持捕获组提取
4. WHEN 播放规则配置了 `decodeSteps`（多步请求），the System SHALL 按顺序执行：请求 → 解析中间值 → 注入下一步 URL/body → 直至得到最终播放地址
5. WHERE 规则需要引用上一步结果，the System SHALL 支持占位符替换（例如 `{{step1.vid}}`、`{{episode.url}}`）

### Requirement 6 — 源级用户偏好设置

**User Story:** AS 用户, I want 为单个源配置 Cookie/开关等参数, so that 同一 App 内不同源可独立配置

#### Acceptance Criteria

1. WHEN 源定义了 preference 项（如 Cookie 文本框、启用搜索开关），the System SHALL 在源详情/设置中展示对应配置 UI
2. WHEN 用户修改 preference，the System SHALL 持久化到本地，并在后续该源请求中生效
3. WHEN 源未定义 preference，the System SHALL 仅展示通用开关（启用/禁用源）

### Requirement 7 — 统一引擎接口与兼容

**User Story:** AS 开发者, I want 复杂源能力挂在统一引擎上, so that UI 与仓库层无需感知规则源/插件源差异

#### Acceptance Criteria

1. WHEN UI 请求搜索/详情/分类/播放地址，the System SHALL 通过统一的 `SourceEngine`（或扩展接口）完成
2. WHEN 导入旧版 `VideoSource` JSON，the System SHALL 保持向后兼容并可正常工作
3. WHEN 新字段缺失，the System SHALL 使用安全默认值（例如 `videoType=Direct`、空 headers）
4. WHERE 选择实现代码插件扩展，the System SHALL 仅支持进程内插件注册，不加载外部不可信 DEX/JAR 动态代码

### Requirement 8 — 错误处理与可观测性

**User Story:** AS 用户与开发者, I want 复杂源失败时看到明确原因, so that 我能判断是网络、规则还是 Cookie 问题

#### Acceptance Criteria

1. WHEN 任一引擎步骤失败，the System SHALL 将错误包装为 `Result.failure` 并附带阶段信息（search/detail/decode/play）
2. WHEN 开启调试日志，the System SHALL 记录请求 URL、规则类型、解析结果摘要（脱敏，不含完整 Cookie）
3. IF 解析结果为空，the System SHALL 返回明确错误（例如「无法解析播放地址」），避免静默失败

## Out of Scope（本阶段不做）

- 资源猫 DEX 动态加载与外部 jar 热插拔
- 完整 JS 沙箱规则引擎（可在后续 P1 独立需求推进）
- 与资源猫插件生态二进制兼容
- Web 端嗅探（Sniff）依赖浏览器扩展能力的方案
