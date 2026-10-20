# 开发与验证

## Android 构建

安装 JDK 17 或以上（当前验证 JDK 24）、Android SDK Platform 35 与 Build Tools 35.0.0。Gradle 8.14.3 + Android Gradle Plugin 8.10.1 管理中文 OCR、资源、清单合并与签名。

```powershell
./build.ps1 -JavaHome C:/path/to/jdk -AndroidSdkRoot C:/path/to/android-sdk
```

可传 `-GradleHome` 使用已有 Gradle；否则首次从官方分发下载并校验 SHA-256，存于用户本机构建缓存。首次构建需要下载 Maven 依赖，运行时不需要下载 OCR 模型。APK 输出到 `dist/`。本机开发密钥和 local.properties 不入库；公开源码不包含发行版签名密钥。

## 业务与界面测试

运行根目录 `test-*.cjs`、`test-storage.py`、`test-theme.py`。协作版另有 `test-cloud.py` 与 `test-native-cloud.cjs`。纯 Java 金额规则可用 `test/PaymentParserTest.java` 测试。

Android OCR 测试位于 `android/test/`：合成图片识别、聊天/未完成付款排除、多渠道草稿及去重。在测试设备上安装 debug 和 androidTest APK 后运行 AndroidJUnitRunner。debug 使用 `.ocrtest` 后缀，不覆盖正式版本。

私有截图测试输入仅在本机构建目录 `build/private-test-images/receipt1.jpg` 至 `receipt4.jpg` 临时提供；公开源码不提供原图，缺少输入时该项跳过，其余合成测试照常运行。androidTest APK 不得作为正式安装包发布。

## 发布检查

四个分支保留同一功能和各自配色。构建并核对 APK 资源、内置模型、无测试专用包名、非调试、离线版无互联网权限；源码压缩包排除构建缓存、数据库、凭据、签名密钥及私有图片。

逐版记录在 CHANGELOG.md，下载索引只保留最新三个版本。README 保持整体介绍。真实手机后台权限、系统截图和弹窗验证须单独记录，不能用合成页面或截图回放测试替代真机结论。
