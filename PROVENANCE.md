# 来源、工具与复现

## 目标身份

| 属性 | 值 |
| --- | --- |
| 提供方文件名 | 喵喵记账_5.1.6_APKPure.apk |
| 包名 | com.youqi.miaomiao |
| 版本 | 5.1.6 / 516 |
| APK 字节数 | 154416947 |
| SHA-256 | e1fb3f4433a1494f26bba8b8e95feeb15adad77875d2aeccd4ebd637bb09777f |

本分支沿用此前本地静态分析产物，2026-10-23 补充标准 DEX 的 smali 并进行公开归档审查。未重新下载其他 APK，也未启动该 APK。

## 工具

- JADX CLI 1.5.6：Java 反编译输出。
- Apktool 3.0.3（内置 smali / baksmali 3.0.9-dev）：Manifest、资源 XML 解码及标准 DEX 的 smali。
- REA 6.5.0 与 jadx-headless-mcp 0.7.1：此前归档/Android 静态检查的证据来源。
- Python：原包结构读取、文件索引、去敏和归档验证。

已记录的工具分发哈希：

| 工具 | 分发文件 SHA-256 |
| --- | --- |
| JADX 1.5.6 ZIP | 545ea2be9c242511bc145755cf4bda2485ade42966e096f8b4d3da2a230e8974 |
| Apktool 3.0.3 JAR | dbf930b076c6b9be08d57c449cacefc3bdd6b71ebd59b3066fc0e1f5b14f9423 |
| jadx-headless-mcp 0.7.1 JAR | 6e5eacf500b64292bfb73c49797c1958f6ee44646e43e868039ae7feb573ff75 |

## 复现解码

先自行提供与上述哈希一致的 APK 和对应工具；这些命令仅读取目标，不运行应用。输出到新目录，避免覆盖其他资料。

```sh
jadx --no-res -d raw-jadx target.apk
java -jar apktool_3.0.3.jar d -s -o raw-res target.apk
java -jar apktool_3.0.3.jar d -r --no-assets -j 2 -o raw-smali target.apk
```

命令产生的是本机原始解码结果，可能含嵌入式服务凭据及大体积二进制；公开提交前应按 [脱敏记录](evidence/redactions.json) 对应字段移除值，且仅选取本文档列出的文本范围。不要将未审查的 raw-* 目录直接上传。

原始文件已经保存在本地分析目录，本次没有将原始 APK 或敏感值加入任何待推送 Git 提交。

## 证据身份

- `ev_c489921daddd90accbd61d3542e942066d21809c062c4cb7ec9ee1f7b03aeaf0`：inspect_android_package / JADX headless 0.7.1。
- `ev_066ba32033b5e587b3dfdc38ec446ad78c3a42274f59bbafdf97bc90616e2788`：inspect_artifact。
- `ev_d980ee3e94c52ba084a34c0ad38659bf0a1f5c7b71b5c30e0e5d24e6bda01cce`：ZIP 条目 inventory_artifact；范围为中央目录，不覆盖 APK 签名块等非条目字节。

这些 ID 引用此前保存的本地 REA 证据。本分支的 [analysis-summary.json](evidence/analysis-summary.json) 是经过筛选的分析摘要，不冒充原始不可变证据。反编译和静态声明不构成运行时行为证明。
