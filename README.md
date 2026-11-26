# QRCodeGenerator（二维码生成器）

[![Android CI](https://github.com/leaf-wai/QRCodeGenerator/actions/workflows/android-ci.yml/badge.svg)](https://github.com/leaf-wai/QRCodeGenerator/actions/workflows/android-ci.yml)
[![Android Release](https://github.com/leaf-wai/QRCodeGenerator/actions/workflows/android-release.yml/badge.svg)](https://github.com/leaf-wai/QRCodeGenerator/actions/workflows/android-release.yml)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)

一款基于 **Kotlin**、**Compose** 与 **Miuix** 打造的简洁轻量级 Android 二维码工具应用，支持二维码生成、相册保存、实时扫码、图库识别及历史记录管理。

## 功能特性

- **文本生成二维码**：支持任意 UTF-8 文本快速生成二维码（M 级纠错），展示高清二维码并支持保存 PNG 图片至系统相册（`Pictures/QRCodeGenerator`）。
- **剪贴板快捷生成**：应用进入前台时自动检测剪贴板文本，点击卡片即可一键生成二维码。
- **相机实时扫描与相册识别**：基于 CameraX 与 Google ML Kit，支持后置相机实时扫描（QR Code / Aztec）及通过系统相册选取图片识别二维码，识别结果支持一键复制到剪贴板。
- **历史记录管理**：使用 MMKV 本地持久化生成记录，支持点击再次查看二维码、长按复制内容、多选批量删除与全选/取消全选。
- **现代视觉体验**：采用 Miuix 设计语言与 MiSans 可变字体，支持系统深浅色主题切换、Edge-to-Edge 全面屏、Android 12+ SplashScreen 启动动画及 Android 13 主题图标。

## 环境要求

| 项目 | 配置 |
| --- | --- |
| 最低系统版本 (`minSdk`) | Android 7.0 (API 24) |
| 目标系统版本 (`targetSdk`) | Android 16 (API 36) |
| 编译版本 (`compileSdk`) | API 37 |
| 支持架构 (`abiFilters`) | `armeabi-v7a`, `arm64-v8a` |
| Gradle / AGP | Gradle 9.6.0 / AGP 9.5.2 |
| Kotlin / JDK | Kotlin 2.4.21 / JDK 21（Daemon JVM），JVM Target 17 |

## 项目结构

本项目采用轻量 MVVM + 单向数据流架构：

```text
app/src/main/java/com/leaf/qrcodegenerator/
├── App.kt                    # Application 入口
├── ApplicationHolder.kt      # 初始化并持有 Application、MMKV
├── MainActivity.kt           # 生成页与历史记录页；权限、导航等系统交互
├── QrCodeActivity.kt         # 二维码展示页；保存权限与展示
├── ScanActivity.kt           # 扫描页；CameraX、ML Kit 与相册选图
├── data/
│   ├── HistoryRepository.kt  # 历史记录接口与 MMKV 实现
│   └── QrCodeImageStore.kt   # 二维码图片存储接口与 MediaStore 实现
├── viewmodel/
│   ├── MainViewModel.kt      # 历史记录、剪贴板和权限弹窗状态
│   ├── QrCodeViewModel.kt    # 二维码保存状态与提示消息
│   └── ScanViewModel.kt      # 扫描结果、图库识别进度与提示消息
├── ui/
│   ├── AppTheme.kt           # Miuix 主题与 MiSans 字体配置
│   └── AppSnackbarHost.kt    # 通用 SnackbarHost
└── utils/
    ├── ClipboardUtils.kt     # 剪贴板系统操作
    ├── QrCodeUtils.kt        # ZXing 二维码位图生成
    └── SPUtils.kt            # MMKV 底层读写封装
```

## 本地构建与测试

```bash
# 运行 JVM 单元测试
./gradlew testDebugUnitTest

# 构建 Debug APK
./gradlew assembleDebug

# 构建 Release APK
./gradlew assembleRelease
```

### Release 签名配置

`app/build.gradle.kts` 支持通过 `local.properties` 或环境变量读取签名配置：

- `RELEASE_STORE_FILE`：签名密钥库（`.jks` / `.keystore`）路径
- `RELEASE_STORE_PASSWORD`：密钥库密码
- `RELEASE_KEY_ALIAS`：密钥别名
- `RELEASE_KEY_PASSWORD`：密钥密码

未提供签名配置时，`assembleRelease` 将自动输出未签名包 `app-release-unsigned.apk`。

## GitHub Actions 自动构建与发布

项目内置两条工作流：

1. **Android CI** ([`.github/workflows/android-ci.yml`](.github/workflows/android-ci.yml))
   - 在推送至 `master`/`main` 或提交 Pull Request 时自动触发，也支持手动触发。
   - 执行 JVM 单元测试（`testDebugUnitTest`）并构建上传 Debug APK 产物。
2. **Android Release** ([`.github/workflows/android-release.yml`](.github/workflows/android-release.yml))
   - 在推送版本标签（如 `2.0.2` 或 `v2.0.2`）或手动触发时运行。
   - 执行单元测试、构建 Release APK 及对应 `.sha256` 校验文件，并在标签推送时自动创建 GitHub Release 并附带发布产物。

如需在 GitHub Actions 中生成正式签名的 Release APK，请在仓库 **Settings → Secrets and variables → Actions** 中配置以下 Secrets：

| Secret 名称 | 说明 |
| --- | --- |
| `RELEASE_KEYSTORE_BASE64` | 密钥库文件的 Base64 编码内容（macOS 可执行 `base64 -i your.jks \| pbcopy`，Linux 可执行 `base64 -w 0 your.jks`） |
| `RELEASE_STORE_PASSWORD` | 密钥库密码 |
| `RELEASE_KEY_ALIAS` | 密钥别名 |
| `RELEASE_KEY_PASSWORD` | 密钥密码 |

## 使用的开源库与资源

感谢以下开源项目、SDK 与字体资源对本项目的支持：

| 项目 / 资源 | 用途 | 许可证 |
| --- | --- | --- |
| [Miuix](https://github.com/compose-miuix-ui/miuix) (`miuix-ui-android`, `miuix-icons-android`) | HyperOS / MIUI 风格 Compose UI 组件库与图标库 | [Apache-2.0](https://github.com/compose-miuix-ui/miuix/blob/main/LICENSE) |
| [Compose Multiplatform / Jetpack Compose](https://github.com/JetBrains/compose-multiplatform) | 声明式 UI 框架 | [Apache-2.0](https://github.com/JetBrains/compose-multiplatform/blob/master/LICENSE.txt) |
| [AndroidX](https://developer.android.com/jetpack/androidx) (`core-ktx`, `core-splashscreen`, `activity-compose`, `lifecycle`) | Android 基础扩展、启动页动画与生命周期组件 | [Apache-2.0](https://www.apache.org/licenses/LICENSE-2.0) |
| [AndroidX CameraX](https://developer.android.com/training/camerax) (`camera-core`, `camera2`, `lifecycle`, `compose`, `mlkit-vision`) | 相机预览与图像分析框架 | [Apache-2.0](https://www.apache.org/licenses/LICENSE-2.0) |
| [ZXing (Zebra Crossing)](https://github.com/zxing/zxing) (`com.google.zxing:core`) | 二维码编码生成 | [Apache-2.0](https://github.com/zxing/zxing/blob/master/LICENSE) |
| [Tencent MMKV](https://github.com/Tencent/MMKV) (`com.tencent:mmkv-static`) | 高性能轻量级本地键值存储 | [BSD-3-Clause](https://github.com/Tencent/MMKV/blob/master/LICENSE.TXT) |
| [Google ML Kit Barcode Scanning](https://developers.google.com/ml-kit/vision/barcode-scanning) (`com.google.mlkit:barcode-scanning`) | 端侧二维码与条码识别引擎 | [Google APIs Terms of Service](https://developers.google.com/ml-kit/terms) |
| [JUnit 4](https://github.com/junit-team/junit4) & [AndroidX Test](https://developer.android.com/training/testing) | 单元测试与仪器化测试框架 | [EPL-1.0](https://github.com/junit-team/junit4/blob/main/LICENSE-junit.txt) / [Apache-2.0](https://www.apache.org/licenses/LICENSE-2.0) |
| [MiSans](https://hyperos.mi.com/font/zh/) (`misans_vf.ttf`) | 应用内置可变无衬线字体 | MiSans 字体知识产权许可协议（版权归小米科技有限责任公司所有） |

## 开源协议与版权声明

本项目基于 [**Apache License 2.0**](LICENSE) 开源，详情请查阅根目录下的 [LICENSE](LICENSE) 文件。

```text
Copyright 2021-2026 leaf-wai

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

> 本项目所引用的第三方开源库、SDK 与字体资源（包括但不限于 Miuix、ZXing、MMKV、AndroidX、Google ML Kit 与 MiSans 字体）的版权均归其各自原作者或版权方所有，并遵循其原始许可协议。
