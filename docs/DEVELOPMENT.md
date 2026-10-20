# 开发指南

[项目首页](../README.md) · [版本记录](../CHANGELOG.md) · [分支说明](BRANCHES.md)

## 项目结构

```text
android/              Android 容器、SQLite 桥接、自动识别服务和主题资源
web/                  App 与网页共享的界面和业务逻辑
docs/                 使用、下载、隐私、开发和排查文档
server.py             本机 SQLite 预览服务
build.ps1             APK 编译、对齐和开发签名
start-preview.ps1     Windows 预览启动入口
test-*.cjs            记账、分类、项目、统计和草稿投递检查
test-storage.py       SQLite 存储检查
```

协作分支另含 `cloud_server.py`、`test-cloud.py` 与 `test-native-cloud.cjs`。界面通过 APK 资产打包，Android 运行时不依赖 Python 服务。

## Android 构建

准备 JDK 17 或以上、Android SDK Platform 35、Build Tools 35.0.0，以及 Windows PowerShell 5.1 或 PowerShell 7。

```powershell
$env:JAVA_HOME = '<JDK 安装目录>'
$env:ANDROID_SDK_ROOT = '<Android SDK 目录>'
./build.ps1
```

非标准 SDK 布局可显式指定：

```powershell
./build.ps1 -JavaHome '<JDK 目录>' -BuildToolsDirectory '<Build Tools 目录>' -AndroidJar '<Platform 35 的 android.jar>'
```

APK 输出到 `dist/`，具体文件名由当前分支构建脚本决定。脚本在本机生成开发签名密钥，密钥不纳入 Git；保留自己的密钥，以便对已安装应用覆盖升级。此签名方案用于本地开发测试。

## 本机预览

离线分支使用 Python 3.9 或以上，仅依赖标准库：

```bash
python server.py --port 8765
```

打开 `http://127.0.0.1:8765/`。服务仅监听本机回环地址；数据库为项目目录中的 `preview.sqlite3`，与 APK 数据库独立。Windows 也可运行 `./start-preview.ps1`。

协作网页需使用 `python cloud_server.py --dev-sms --port 8766`，不能用离线预览服务替代。详见 [协作服务器指南](https://github.com/EvA56356/plain-ledger/blob/feature/shared-ledger/docs/SERVER.md)。

## 检查

Node.js 18 或以上和 Python 用于以下检查：

```bash
node test-accounting.cjs
node test-calendar.cjs
node test-reports.cjs
node test-projects.cjs
node test-project-statistics.cjs
node test-fixes.cjs
node test-auto-bill.cjs
python test-storage.py
```

金额识别逻辑不依赖 Android 运行时，可用 JDK 单独检查：

```bash
javac -encoding UTF-8 -d build/parser-tests android/src/cn/plainledger/PaymentParser.java test/PaymentParserTest.java
java -cp build/parser-tests PaymentParserTest
```

协作分支额外运行：

```bash
python test-cloud.py
node test-native-cloud.cjs
```

模拟器验证不能替代真机兼容性验证；自动记账的验证范围见 [使用与排查说明](AUTO_BILL.md)。

## 数据与开发边界

金额以整数“分”存储；账户间转账保留独立流水，不计入收入和支出。SQLite 以事务保存应用状态和账单表。

不要提交数据库、个人备份、真实账号、服务器密钥或签名材料。协作短信、公网 HTTPS、备份和运维需部署者配置，开发验证码模式只供本机测试。

## 文档维护

- `README.md`：稳定的项目介绍、功能汇总、安装和使用入口。仅当整体功能、安装方式或使用流程发生实质变化时更新，不逐版追加更新摘要或硬编码安装包版本号。
- `CHANGELOG.md`：按版本追加功能变化、修复和重要验证边界；尚未发版的变化放在“未发布”。
- `docs/DOWNLOADS.md`：维护最新三个版本的实际 APK 下载入口。
- `docs/APK_HISTORY.md`：归档更早版本，只链接 Release 中真实存在的文件。
- 专项指南：只在对应使用方式变化时更新；发布说明可引用版本记录，避免把同一份增量日志重复写入首页。
