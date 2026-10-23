# APK 下载

[项目首页](../README.md) · [版本记录](../CHANGELOG.md) · [历史 APK](APK_HISTORY.md)

## 最新三个版本

1.4.2（2026-10-24）是 GitHub prerelease 测试版，包含支付页面补扫、草稿恢复及代码审查修复。1.4.2 / 1.4.1 / 1.4.0 均保留自动 OCR 测试边界，真实手机支付及系统截图兼容性待复测；不标记为稳定版。

下载本次版本请打开 [1.4.2 测试发布页](https://github.com/EvA56356/plain-ledger/releases/tag/v1.4.2)，或使用下表四个 APK 的直接下载入口。[稳定版发布页](https://github.com/EvA56356/plain-ledger/releases/latest) 不会指向 prerelease 测试版。

| 版本 | 绿色版 | 纸感版 | 蓝色版 | 协作版 |
| --- | --- | --- | --- | --- |
| [1.4.2（测试版）](https://github.com/EvA56356/plain-ledger/releases/tag/v1.4.2) | [下载](https://github.com/EvA56356/plain-ledger/releases/download/v1.4.2/plain-ledger-green-1.4.2.apk) | [下载](https://github.com/EvA56356/plain-ledger/releases/download/v1.4.2/plain-ledger-paper-1.4.2.apk) | [下载](https://github.com/EvA56356/plain-ledger/releases/download/v1.4.2/plain-ledger-sky-1.4.2.apk) | [下载](https://github.com/EvA56356/plain-ledger/releases/download/v1.4.2/plain-ledger-shared-1.4.2.apk) |
| [1.4.1](https://github.com/EvA56356/plain-ledger/releases/tag/v1.4.1) | [下载](https://github.com/EvA56356/plain-ledger/releases/download/v1.4.1/plain-ledger-green-1.4.1.apk) | [下载](https://github.com/EvA56356/plain-ledger/releases/download/v1.4.1/plain-ledger-paper-1.4.1.apk) | [下载](https://github.com/EvA56356/plain-ledger/releases/download/v1.4.1/plain-ledger-sky-1.4.1.apk) | [下载](https://github.com/EvA56356/plain-ledger/releases/download/v1.4.1/plain-ledger-shared-1.4.1.apk) |
| [1.4.0](https://github.com/EvA56356/plain-ledger/releases/tag/v1.4.0) | [下载](https://github.com/EvA56356/plain-ledger/releases/download/v1.4.0/plain-ledger-green-1.4.0.apk) | [下载](https://github.com/EvA56356/plain-ledger/releases/download/v1.4.0/plain-ledger-paper-1.4.0.apk) | [下载](https://github.com/EvA56356/plain-ledger/releases/download/v1.4.0/plain-ledger-sky-1.4.0.apk) | [下载](https://github.com/EvA56356/plain-ledger/releases/download/v1.4.0/plain-ledger-shared-1.4.0.apk) |

绿色、纸感、蓝色是离线版；协作版可离线使用，按需连接自己的服务器。四个安装包可以同时安装，数据库彼此独立。

## 安装与升级

支持 Android 8.0 及以上；自动截图 OCR 需要 Android 11 及以上。中文模型随安装包内置，运行时无需下载。下载 `.apk` 后在手机上安装；更新已有应用时使用对应版本的 APK 覆盖安装，保留原有应用数据。卸载会清除应用数据，迁移前请从设置导出 JSON 备份。

需要旧版本时查看 [历史 APK](APK_HISTORY.md)。部分早期补丁只发布了部分风格，表中的“—”表示未提供对应安装包。

## 源码与校验

Release 附件中的 `*-source-版本号.zip` 对应各风格源码；GitHub 自动生成的 Source code 对应标签所在分支，不代表同时包含所有风格。

提供 `SHA256SUMS` 的版本可用该文件核对下载内容。附件不包含用户数据库、真实账单、服务器凭据或私有签名密钥。
