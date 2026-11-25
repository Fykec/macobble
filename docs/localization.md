# 本地化状态

简体中文（`values-zh`）与平台级文案的当前进度、已确认的取舍，以及后续继续做时需要知道的东西。

## 现状

语言跟随系统，没有应用内切换器。系统语言为中文时读取 `values-zh`，否则读 `values`。

| 模块 | 已抽取文案 | 中文 |
|---|---|---|
| `:util` | 24 | 24 |
| `:pebble` | 596 | 596 |
| `:experimental` | 375 | 375 |
| `:composeApp` | 76 | 76 |
| **合计** | **1071** | **1071** |

平台级（不走 Compose 资源）：

- Android：`androidApp/src/main/res/values-zh/strings.xml`、通知渠道名（`composeApp/src/androidMain/res/values-zh`）
- iOS：`iosApp/iosApp/zh-Hans.lproj/InfoPlist.strings`（权限弹窗）
- macOS：`macosApp/src/macosMain/resources/zh-Hans.lproj/InfoPlist.strings`，由 `bundleApp` 拷进 bundle

## 验收命令

```bash
./scripts/check_localization.py
```

检查三件事，任一不满足即退出码 1：

1. **key 完整性**：`values/strings.xml` 与 `values-zh/strings.xml` 的 key 集合必须一致
2. **界面内联文案**：扫描 `Text("…")`、`name = "…"`、`-> "…"` 三种写法
3. **枚举显示文案**：`enum class X(val title: String)` 这类声明直接报警

当前输出中剩余 208 条内联文案，全部落在下面"已确认跳过"的范围里。

## 已确认的取舍

### 不翻译（界面上保持英文）

这些字符串虽然出现在扫描结果里，但是**数据或标识符**，翻译会破坏功能。它们在代码里带 `// not-localized` 标记，检查器会跳过。

| 类别 | 例子 | 位置 |
|---|---|---|
| 产品名与缩写 | `Pebble Time 2`、`PT2`、`OG` | `LockerUtil.kt` |
| 天气词汇表 | `Partly cloudy`、`Sunny` | `WeatherPlugin.kt`（供表盘按代码取图） |
| 健康平台类型 | `Walk`、`Run`、`Workout` | `PlatformHealthSync.kt`（写入系统健康平台） |
| ASR 错误码 | `empty_result`、`network_error` | `RebbleAsrTranscription.kt` |
| 分析事件名 | `not_enough_memory`、`service_unavailable` | `TranscriptionAnalytics.kt` |
| Agent/工具标识 | `Nenya`、`Built-in`、servlet 标题 | `experimental/ring/agent/**` |
| 语言区映射 | `zh_tw` → `zh_hant` | `PublishedLanguagePack.kt` |
| 页面滚动/动画技术名 | `child-row-fade`、`shimmer-progress` | `experimental` |

共 119 处标记。

### 暂不做（本次范围外）

| 范围 | 剩余条数 | 原因 |
|---|---|---|
| Ring / Index（`:experimental`） | 115 | 没有戒指设备；macOS 不编译该模块 |
| 离线语音模型界面 | 12 | 当前 `STTConfig(mode=RemoteOnly)`，相关界面不出现 |
| 权限名称与安装审核 | 62 | 见下节，需要类型改造 |

## 后续要继续的部分

### 1. 权限名称 / 安装审核（62 条）

`pebble` 里剩下 47 条中的 33 条、`util` 里 40 条中的 34 条都属于这块。

卡点是 `InstallReview.kt` 的这个函数：

```kotlin
fun permissionLabel(key: String, defined: Map<String, PluginDefinedPermission>): String = when (permissionOf(key)) {
    PluginPermissions.INTERNET -> domainOf(key)?.let { "Connect to $it" } ?: "Connect to any website"
    PluginPermissions.NOTIFICATIONS -> packageOf(key)?.let { "Read your notifications from $it" } ?: …
    PluginPermissions.LOCAL_NETWORK -> "Connect to devices on your local network"   // 静态
    …
}
```

**一半分支要拼域名/包名，一半是静态文案**，无法直接换成 `StringResource`。建议引入：

```kotlin
sealed interface PermissionText {
    data class Plain(val text: String) : PermissionText       // 已拼好的动态文案
    data class Resource(val res: StringResource) : PermissionText
}
```

渲染处 `when` 解析即可。涉及调用点：

- `InstallReview.kt`：`PermissionRow`（label 参数）、`requestLabels`、`requestSummary`、`PendingPermissionRequests`
- `WatchappSettingsScreen.kt:347`（snackbar，在 `launch { }` 内，可用 `getString`）
- `PermissionRequestNotifier.kt:93`（`private suspend fun`，可用 `getString`）
- `PermissionUsageScreens.kt:54`

### 2. 语音模式选择器（8 条）

`WatchSettingsScreen.kt` 里 `CactusSTTMode.*` 的下拉标签（`Cloud Only`、`Local (with Cloud Fallback)`…）。
界面会出现，但如果确认长期只用云端识别，可以继续放着。

### 3. Ring（115 条）

`experimental` 全部。等真的有戒指设备再做，或者按产品决定是否维护。

## 继续时的工作方式

1. 跑 `./scripts/check_localization.py`，看目标文件剩余条数
2. 判断该处的上下文：
   - `Text(...)` 或 `@Composable` 参数里 → 直接 `stringResource(Res.string.x)`
   - 普通函数返回值 → 加 `@Composable`（前提是全部调用点都在 Composable 上下文），或返回 `StringResource` 交给调用处解析
   - `suspend` 函数或协程里 → `getString(Res.string.x)`
   - 数据/标识符 → 加 `// not-localized` 注释
3. 补 `values` 与 `values-zh` 的 key（两边都要，否则 key 完整性检查会失败）
4. 编译三端：`:pebble:compileKotlinMacosArm64`、`:pebble:compileAndroidMain`、`:pebble:compileKotlinIosArm64`
5. 改完再跑一次检查器

### 注意

- Compose Resources 生成的是**顶层扩展属性**，每个 key 都要单独 `import coreapp.<module>.generated.resources.<key>`
- 有的文件把模块的 `Res` 导入成别名（`import …Res as PebbleRes`），此时用 `PebbleRes.string.x`
- `remember { }` 的 lambda **不能**调 Composable；设置列表为此刻意改成内联 `run { }`
- 枚举里的显示字段用 `StringResource`，不要用 `String`（检查器会对 `String` 报警）

## 术语与风格

| 英文 | 译法 |
|---|---|
| watchface | 表盘 |
| watchapp / app | 手表应用 / 应用 |
| App Store | 应用商店 |
| Timeline | 时间线 |
| Devices | 设备 |
| Pair / Unpair | 配对 / 解除配对 |
| Sync | 同步 |
| Firmware | 固件 |
| Complication | 复杂功能 |
| Rebble / Pebble / Core / Index 01 / Ring | 不译 |

- 用「你」，不用「您」
- 中英文之间加空格
- 中文用全角标点
- 按钮文案不带句号，错误提示带句号
