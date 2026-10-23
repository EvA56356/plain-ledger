# 开发与验证

## Android 构建

安装 JDK 17 或以上（当前验证 JDK 24）、Android SDK Platform 35 与 Build Tools 35.0.0。Gradle 8.14.3 + Android Gradle Plugin 8.10.1 管理中文 OCR、资源、清单合并与签名。

```powershell
./build.ps1 -JavaHome C:/path/to/jdk -AndroidSdkRoot C:/path/to/android-sdk
```

可传 `-GradleHome` 使用已有 Gradle；否则首次从官方分发下载并校验 SHA-256，存于用户本机构建缓存。首次构建需要下载 Maven 依赖，运行时不需要下载 OCR 模型。APK 输出到 `dist/`。本机开发密钥和 local.properties 不入库；公开源码不包含发行版签名密钥。

正式构建必须显式提供既有发行签名，缺少任一项会失败，不会生成或替换发行密钥，也不会自动使用 `local-debug.keystore`。可用用户级 `~/.gradle/gradle.properties` 或环境变量配置；仓库自带的 `gradle.properties` 受版本控制，不能写入密钥、口令或个人路径。口令不通过命令行参数传入。

| 本机 Gradle 属性 | 环境变量 |
| --- | --- |
| `ledgerKey` | `LEDGER_KEY` |
| `ledgerStorePassword` | `LEDGER_STORE_PASSWORD` |
| `ledgerKeyAlias` | `LEDGER_KEY_ALIAS` |
| `ledgerKeyPassword` | `LEDGER_KEY_PASSWORD` |

`build.ps1` 的 `-KeyStore`、`-KeyAlias` 可覆盖路径及别名；口令仍从上述本地配置读取。升级验证必须比对历史 APK 的签名证书，不能因为构建成功就声称仍可覆盖安装。

## 业务与界面测试

以下命令在仓库根目录运行。JDK、Node.js、Python 需已加入 `PATH`。

```powershell
Get-ChildItem -File -Filter 'test-*.cjs' | ForEach-Object {
    node $_.FullName
    if ($LASTEXITCODE -ne 0) { throw "JavaScript 测试失败：$($_.Name)" }
}
python test-storage.py
python test-theme.py
```

协作版另运行 `python test-cloud.py`；其 `test-native-cloud.cjs` 已包含在上述 JavaScript 全套命令中。

### 纯 Java 金额与补扫会话回归

这两组测试不依赖 Android 设备或 Gradle。补扫会话测试使用可控的单调时间，覆盖事件文字有效期、窗口隔离、实时页面优先、窗口归属、页面切换和过期回调。

```powershell
New-Item -ItemType Directory -Force build/java-tests | Out-Null
javac -encoding UTF-8 -d build/java-tests `
    android/src/cn/plainledger/PaymentParser.java `
    android/src/cn/plainledger/PaymentScanSession.java `
    android/src/cn/plainledger/PaymentWindowOwnership.java `
    test/PaymentParserTest.java test/PaymentScanSessionTest.java
if ($LASTEXITCODE -ne 0) { throw 'Java 测试编译失败' }
java -cp build/java-tests PaymentParserTest
if ($LASTEXITCODE -ne 0) { throw '金额规则测试失败' }
java -cp build/java-tests cn.plainledger.PaymentScanSessionTest
if ($LASTEXITCODE -ne 0) { throw '补扫会话测试失败' }
```

预期分别输出 `53 payment recognition checks passed` 和 `57 observation, window ownership, event-storm, OCR freshness and delivery suppression scenarios passed`。

### Android instrumentation

测试源码位于 `android/test/`：

- `LocalOcrTest`：合成图片 OCR（含优惠及“完成”布局）、聊天和未完成付款排除、多渠道草稿及去重，以及可选的私有截图布局。
- `AutoBillDraftQueueTest`：顺序交付、跨渠道去重、旧草稿兼容、90 秒后同额付款、20 笔容量上限、并发入队与完成确认；展示不消费、重建后继续核对、测试草稿让位、已提交固定账单 ID 的完成回执恢复。
- `PaymentNodePrivacyTest`：隐藏密码节点不误阻止识别，可见密码节点阻止识别，两者均不采集密码文字；可见内容描述可以提供金额。
- `PendingExportTest`：文件选择期间缓存及目标 URI 恢复、UTF-8 字节 10MB 上限、未完成导出不被新内容覆盖、中断后重写及失败后重新导出；全新 Activity 仅清理没有目标 URI 的遗留等待状态，恢复中的 Activity 和已选择目标的写入状态保留。缓存被系统回收或目标文档权限失效时仍需重新导出。

先配置 JDK、SDK 和 Gradle。debug 仅使用本机开发密钥；新检出目录可运行下方 `keytool` 命令生成开发密钥，不能将其作为正式签名。以下使用 `build.ps1` 默认的 Gradle 缓存路径，使用自备 Gradle 时修改 `$gradle`。`JAVA_HOME` 需指向 JDK，`local.properties` 需已配置 SDK 路径，`adb` 需加入 `PATH`，多设备时为每条命令加 `-s <设备序列号>`。

```powershell
$gradle = Join-Path $env:LOCALAPPDATA 'PlainLedger/build-tools/gradle-8.14.3/bin/gradle.bat'
if (!(Test-Path -LiteralPath local-debug.keystore)) {
    & "$env:JAVA_HOME/bin/keytool.exe" -genkeypair -keystore local-debug.keystore `
        -storepass android -keypass android -alias plainledger -keyalg RSA -keysize 2048 `
        -validity 10000 -dname 'CN=Plain Ledger Local Development'
    if ($LASTEXITCODE -ne 0) { throw '开发密钥生成失败' }
}
& $gradle assembleDebug assembleDebugAndroidTest --console=plain
if ($LASTEXITCODE -ne 0) { throw 'Android 测试构建失败' }
adb install -r -t build/outputs/apk/debug/PlainLedger-debug.apk
adb install -r -t build/outputs/apk/androidTest/debug/PlainLedger-debug-androidTest.apk
$testApp = ([xml](Get-Content android/AndroidManifest.xml -Raw)).manifest.package + '.ocrtest'
$runner = $testApp + '.test/androidx.test.runner.AndroidJUnitRunner'
adb shell am instrument -w $runner
```

debug 使用 `.ocrtest` 包名后缀，不覆盖正式版本。上述命令从当前分支清单推导测试包名，四种风格均适用。若只运行指定回归，可在最后一条命令的 `-w` 后加入 `-e class cn.plainledger.AutoBillDraftQueueTest,cn.plainledger.PaymentNodePrivacyTest,cn.plainledger.PendingExportTest`。

私有截图测试输入仅在本机构建目录 `build/private-test-images/receipt1.jpg` 至 `receipt4.jpg` 临时提供；公开源码不提供原图，缺少输入时该项跳过，其余合成测试照常运行。androidTest APK 不得作为正式安装包发布。

### 隔离的无障碍事件端到端验证

单元测试和 instrumentation 不足以证明服务能收到真实窗口事件并打开核对界面。端到端验证使用单独安装的虚构支付页面和隔离简账构建，保留 Android 无障碍事件、节点读取、草稿交付、Activity 启动及网页核对界面的实际调用链。隔离构建临时将测试页面包名加入监听范围，使用 `.ocrtest` 或 `.reliabilityqa` 应用 ID；这些修改和安装包不得混入正式构建或发布资产。

7 项场景分别为内容描述金额、无后续事件的延迟渲染、连续事件更新、支付失败、聊天文字、识别前离开页面、无后续事件时离开页面。正例同时核对识别金额、前台核对窗口标题和金额；反例确认没有生成草稿。此组验证关闭 OCR，只验证无障碍文字链路。运行前后恢复原无障碍服务配置，仅清理隔离应用的测试设置，不读取或修改正式账本；页面内容不进入日志或公开报告。

### 本次未发布改动的验证记录

- 已通过：JavaScript 全套与本地预览专项；包括保存失败不完成草稿、固定账单 ID、已提交保存恢复、同日同来源同金额重复提示、CSV 及请求校验回归。
- 已通过：53 项 Java 金额规则及 68 项会话/窗口/事件风暴/OCR 时序场景。
- Android instrumentation 报告 `OK (23 tests)`；其中 `privateReceiptLayouts` 缺少私有图片输入，由 `Assume` 跳过，另外 22 项执行通过。实际随包 WebView 已验证禁用文件访问后离线加载、已展示草稿重开后保留、保存一次及明确取消不入账。
- 已通过：MuMu 的 7 项真实无障碍事件合成页面场景；正例均自动打开金额正确的核对窗口，反例均未生成草稿。四分支构建、签名及源码一致性检查待本轮收尾记录。Android 13 及以上返回回调已编译，尚未在相应系统实机验证；实际文档选择器中的进程死亡恢复仍需单独测试，不能用缓存类回归代替。
- 尚未验证：小米真机、真实支付、厂商后台策略和本次系统截图兼容性。历史 MuMu 黑帧与私有截图回放不能替代本次真机验证；合成页面通过也不能证明真实支付应用全部兼容。90 秒同来源同金额去重仍可能合并短时间内两笔真实同额付款。

## 发布检查

四个分支保留同一功能和各自配色。构建并核对 APK 资源、内置模型、无测试专用包名、非调试、离线版无互联网权限；源码压缩包排除构建缓存、数据库、凭据、签名密钥及私有图片。

逐版记录在 CHANGELOG.md，下载索引只保留最新三个版本。README 保持整体介绍。真实手机后台权限、系统截图和弹窗验证须单独记录，不能用合成页面或截图回放测试替代真机结论。
