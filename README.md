# 简账协作版 · Shared Ledger

## 1.2.0 更新

- 分类采用纯文字，两级结构；每个一级分类有预设二级分类，可在设置中添加、改名。
- 旅行包含交通、住宿、餐饮、购物、门票；账单显示完整分类路径。
- 旧账单继续关联原分类，不自动改分类、不改变金额。备份和协作同步保留层级。
- 手机端分类管理标题与添加按钮保持同一行。
- APK 下载见 [Releases](https://github.com/EvA56356/plain-ledger/releases/latest)。


基于绿色 main UI 的独立协作分支，不依赖其他 UI 分支。具备原有记账、分类、账户、预算、备份与网页增强统计，并加入手机号账号、密码管理、共享账本和同步。

## 本机体验

```powershell
python cloud_server.py --dev-sms --port 8766
```

打开 http://127.0.0.1:8766/ 。开发验证码显示在页面，未发送真实短信。真实短信、公网域名与 HTTPS 尚需部署者配置。

[服务器配置、使用流程与限制](docs/SERVER.md) · [协作版隐私说明](docs/PRIVACY_SHARED.md)

创建账号后，顶部“账号与共享”可以修改昵称和密码、新建共享账本及邀请已注册用户。对方接受邀请后，共享成员可编辑或只读访问。打开或回到客户端时自动同步，也可点击刷新图标。版本冲突会保留待保存副本供下载，避免覆盖别人修改。

Android 协作版包名 `cn.plainledger.shared`，可与离线版同时安装。使用 Android SDK Platform 35、Build Tools 35、JDK 17+，运行 `./build.ps1`；产物 `dist/简账-协作版-1.1.0.apk`。首次启动填写 HTTPS 服务器地址，本机模拟器可通过 adb reverse 使用回环地址。详细构建参数与原版相同。

## 验证

```powershell
python test-cloud.py
node test-reports.cjs
node test-accounting.cjs
node test-calendar.cjs
```

服务器数据、验证码密钥、签名密钥和个人账单不会纳入 Git。当前未做真实短信或公网部署验证；网络中断时不支持离线写入队列。每个共享空间对应一个账本，可新建多个空间。

## APK 下载

四种风格/用途的安装包统一发布在 [GitHub Releases](https://github.com/EvA56356/plain-ledger/releases/latest)，可分别安装，不通过源码文件上传页下载。发布附件不包含个人数据库、备份、会话或签名私钥。
