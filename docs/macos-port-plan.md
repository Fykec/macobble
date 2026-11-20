# macOS 移植计划（CoreApp）

> 状态：已与用户逐题确认（grill-me 共 10 题）。本文档是执行依据，执行顺序见文末。

## 目标

为 CoreApp 增加 macOS 支持，编译产出**可独立运行的 `.app`**（双击即可使用，不依赖 Xcode 工程）。

## 已确认的决策

| # | 决策点 | 结论 |
|---|---|---|
| 1 | 技术路线 | Kotlin/Native `macosArm64` 目标 + Compose Multiplatform 原生 macOS（复用 iOS 的 CoreBluetooth/Darwin 生态），不做 JVM Desktop |
| 2 | 芯片架构 | 仅 Apple Silicon（arm64）。CMP 1.12.2 与 sqlite-bundled 2.7.1 均无 macosX64 产物 |
| 3 | v1 功能范围 | 仅手表核心功能（BLE 连接、通知、健康数据、表盘、配置）；Ring 不进 v1 |
| 4 | Ring 编译处理 | experimental 模块排除出 macOS 编译图；composeApp commonMain 中 ring 引用通过源码集重组隔离 |
| 5 | Firebase | macOS 走"桩"方案：本地新建 firebase 桩模块，commonMain 零改动；macOS 运行时无 Firebase（无登录、无云端后端） |
| 6 | 源码集策略 | 各模块抽 `appleMain` 共享层（纯 Darwin、无 UIKit 代码上移），`macosMain` 写 AppKit 专属 |
| 7 | 打包 | 纯 Gradle 打包 `.app`（Info.plist + entitlements + ad-hoc codesign），不建 Xcode 工程 |
| 8 | UI 布局 | v1 沿用手机竖屏布局，固定 iPhone 比例窗口 |
| 9 | 降级清单 | 见下表 |

## v1 功能降级清单（已确认）

| 项目 | iOS 现状 | macOS v1 处理 |
|---|---|---|
| 登录/账号 | Firebase Auth（Apple/Google/GitHub） | 无登录，Firebase 桩呈现未登录态 |
| PebbleWebServices（表盘商店等后端） | id token 鉴权 | 桩/no-op，本地 sideload 表盘可用 |
| 远端推送 | FCM + kmpnotifier | 桩/no-op；BLE 本地方向的来电/通知转发到手表不受影响 |
| 后台扫描/同步 | BGTaskScheduler + 后台模式 | 无后台模式；macOS 前台 CoreBluetooth 扫描不受 iOS 10 秒限制 |
| 崩溃/统计 | Crashlytics + Analytics | 随 Firebase 桩 no-op |
| 手机电量上报 | UIDevice（UIKit） | 换 macOS IOPowerSources 或 v1 先桩 |
| 日历/提醒同步 | EventKit | macOS EventKit 可用，走 appleMain 共享 |
| 蓝牙权限 | iOS 应用内弹窗 | macOS 走系统设置授权；复用现有 permission-nag UI 指引 |
| Ring 全部功能 | 完整 | 不在编译图 |
| MetricKit / 深链等杂项 | 有 | v1 桩，深链仅保留 URL scheme 声明 |

## 关键事实（已核实）

- CMP 1.12.2、Room 2.8.5、sqlite-bundled 2.7.1、koin-core 4.2.2、kable 0.45.0 均有 `macosArm64` 产物。
- gitlive firebase-kotlin 2.8.0：common/firestore/crashlytics/storage **有** macOS 产物；**auth 没有** → 桩模块需覆盖 auth，其余模块按需替换。
- macOS 编译图模块（experimental 排除后）：`composeApp`、`util`、`pebble`、`libpebble3`（+ `blobannotations`/`blobdbgen` KSP 处理器）。
- 需要适配的 iOS 侧代码规模：libpebble3 ~3.5k 行、util ~1.9k、pebble ~1.5k、composeApp ~1.7k；其中 UIKit 强依赖文件仅占一部分，其余可上移 appleMain。

## 执行阶段

1. **构建配置与源码集重组**
   - 新建 `:firebase-stubs` 桩模块（macosArm64，覆盖 `dev.gitlive.firebase` 在用 API：auth 必备，firestore/crashlytics/storage/common 按需）。
   - composeApp/util/pebble/libpebble3 加 `macosArm64()` 目标。
   - composeApp 新建 ring 中间源码集，隔离 ring 引用（App.kt、AppNavHost、CoreDeepLinkHandler、CommonAppDelegate、utilModule、BugReportProcessor、logging、AppstorePluginStore、ringonboarding/）。
   - 验证：`:composeApp:compileKotlinMacosArm64` 编译通过，Android/iOS 编译不回归。
2. **libpebble3 appleMain 迁移**（BLE CoreBluetooth 栈为核心）
   - 验证：iOS klib 编译不回归 + macOS 编译链接通过。
3. **util appleMain 迁移**（EventKit、UserNotifications、WebKit、FileManager 等）
4. **pebble appleMain 迁移**
5. **composeApp macosMain**（入口、DI、权限、各 expect actual）
6. **`:macosApp` 打包模块**（NSApplication 入口 + .app 组装 + Info.plist + entitlements + ad-hoc codesign）
   - 验证：产出可双击启动的 `.app`。
7. **真机联调**（本机蓝牙 + Pebble 手表：连接、通知转发、健康同步）

## 验证标准

- `gradlew` 一条命令产出 `.app`，本机双击可启动。
- 可完成手表配对/连接、通知转发、健康数据同步等核心链路。
- Android/iOS 现有构建与测试不受影响（以 iOS framework 编译 + Android assembleDebug 为回归门槛）。

---

## 实施结果（已完成）

### 产出

- `:macosApp` 模块：`./gradlew :macosApp:bundleApp` 产出
  `macosApp/build/macosApp/CoreApp.app`（Info.plist + entitlements + ad-hoc 签名 + 合并的
  Compose 资源），双击/`open` 即可运行。
- `:macos-stubs` 模块：无 macOS 产物的第三方库的 no-op 实现（firebase common/auth/firestore/
  crashlytics、health-kmp、kmpnotifier、compose-webview-multiplatform、compass mobile、haversine）。
  通过各消费模块 `configurations.configureEach` 的 `dependencySubstitution` 仅在 macOS 配置上生效，
  commonMain 源码零改动。

### 源码集重组

- `libpebble3`（54 个文件）、`util`（38）、`pebble`（27）的 `iosMain` → `appleMain`；UIKit 专属
  文件（10 个）回落 `iosMain`，各模块新增 `macosMain` 实现。
- `composeApp` 新增 `ringMain` 中间源集（experimental 依赖 + ring UI 入口），`androidMain`/
  `iosMain` 依赖它，`macosMain` 不依赖；commonMain 中 14 处 ring 引用改为 `expect`，macOS 提供
  no-op actual（`RingSupport`、`RingNavSupport`、`RingDeepLinks`、`RingBugReportData`、
  `RingTraceSessions`、`generateRingDeviceSummary`、`utilRingModule`）。
- `composeApp/macosMain`：`MacosApp()` 入口 + `macosDefaultModule`（Platform/PermissionRequester/
  OAuthLauncher/SyncScheduler/AppUpdate/WatchNotifier/CompanionDevice/AnalyticsBackend/
  CactusModelPathProvider 的 macOS 实现）。

### 关键实现事实（踩过的坑）

1. Compose Multiplatform 1.12.2 macOS 目标需要 `org.jetbrains.compose.experimental.macos.enabled=true`。
2. 中间源集（appleMain）不能引用 expect 类的构造函数，需要 `expect fun createX(): X` 工厂。
3. macOS 的 Compose 窗口 API 是 `androidx.compose.ui.window.Window(title, size) { }`（无
   `application {}`，那是 JVM desktop）；`main()` 末尾必须 `NSApplication.run()`，否则进程渲染
   一帧后退出。
4. Compose 资源在 macOS 的查找路径是
   `Contents/Resources/compose-resources/composeResources/<namespace>/...`（双层目录），
   由 `:macosApp:bundleApp` 合并各模块 `assembleMacosArm64MainResources` 产物生成。
5. 各模块 macosArm64 需要 `kspMacosArm64(room.compiler)`，否则 Room 找不到
   `RoomDatabaseConstructor`。
6. `:composeApp` 的 macOS 构建不需要 CocoaPods：`podGenMacos`/`podInstallSynthetic*`/`podBuild*`
   被禁用，两个 pod cinterop 换成空 def。
7. `:macosApp` 的 `binaries.executable.entryPoint` 要写函数全名（`coredevices.coreapp.main`），
   不是 `MainKt`。
8. Room/`paging`/`compass` 的 macOS 构件必须从 mavenCentral 取（阿里云镜像缺 klib），
   仓库顺序为 jitpack → central → aliyun public → aliyun google。

### 验证状态

| 验证项 | 结果 |
|---|---|
| 5 个模块 `compileKotlinMacosArm64` | ✅ 通过 |
| `linkDebugExecutableMacosArm64` | ✅ 产出 137 MB 原生二进制 |
| `bundleApp` 产出 `.app` + 签名 | ✅ `codesign` 校验通过（ad-hoc） |
| 应用启动 | ✅ 进程常驻，无异常输出，Compsoe 资源加载成功，Metal shader 编译通过 |
| 窗口 | ✅ 420×900，onscreen=1（NSWindow frame 显式设置） |
| iOS 编译（libpebble3/util/pebble/libindex） | ✅ 无回归 |
| Android 编译（util/libindex `assembleAndroidMain`） | ✅ 无回归 |
| iOS `composeApp` 编译 | ⚠️ 未验证：需要 `pod install` 拉取 GitHub 上的 Firestore 二进制，本机网络无法访问 github.com |
| UI 视觉验证 | ⚠️ 需人工在本机解锁屏幕后确认（自动化截图时屏幕已锁定） |

### 后续可做

- BLE 真机联调（连接 Pebble、通知转发、健康同步）。
- macOS 上恢复可选功能：文件/照片选择器、分享、内嵌 WebView（配置页）、Account 登录。
- macOS 侧 `CactusModelPathProvider` 目前是 no-op；若要本地转写需要 cactus 的 macOS 原生库。
- 若要分发：Developer ID 签名 + 公证（`notarytool`）+ 版本号注入。
