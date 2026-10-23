# 喵喵记账 5.1.6：静态逆向资料

这是 `EvA56356/plain-ledger` 的独立分析分支 `reverse/miaomiao-5.1.6`，归档此前分析中实际得到的可读代码、资源 XML、内置配置与功能清单。**核心业务代码仍受 SecNeo 加固保护；这里不是完整业务源码，也不能直接构建成原应用。**

目标包名：`com.youqi.miaomiao`，版本：`5.1.6 / 516`。原 APK SHA-256：

```text
e1fb3f4433a1494f26bba8b8e95feeb15adad77875d2aeccd4ebd637bb09777f
```

| 内容 | 位置 | 已恢复范围 |
| --- | --- | --- |
| Java 反编译文件 | [decompiled/java](decompiled/java) | 全部 7 个现有输出：5 个壳类、生成的 R.java、Kotlin 库辅助类 |
| smali 反汇编 | [decompiled/smali](decompiled/smali) | 标准 classes.dex 中的全部 5 个类 |
| 解码清单与资源 | [decoded](decoded) | AndroidManifest.xml、3,356 个资源 XML、13 个内置文本配置/脚本文件 |
| 功能清单 | [FEATURES.md](FEATURES.md) | 按字符串、组件声明、布局和配置归纳；不是运行验证 |
| 组件清单 | [COMPONENTS.md](COMPONENTS.md) | 声明的业务组件及集成 SDK |
| 证据摘要与文件目录 | [evidence](evidence) | 原 APK 6,798 个归档条目、类表、原始 REA 证据引用和脱敏记录 |

未上传原 APK、DEX、SO、SDK JAR、模型、图片/音频/字体、密钥或证书文件。非文本资源保留在 APK 条目索引中；未反编译的二进制不能作为“源码”展示。内嵌 SDK key/secret、部分应用标识及潜在密钥片段以占位符替换，详见 [脱敏记录](evidence/redactions.json)。没有读取或上传用户的实际账本、支付截图、登录会话或简账签名密钥。

## 查看与下载

GitHub 的 **Code → Download ZIP** 下载的是当前所选分支。也可以仅克隆此分支：

```sh
git clone --single-branch --branch reverse/miaomiao-5.1.6 https://github.com/EvA56356/plain-ledger.git miaomiao-analysis
cd miaomiao-analysis
python tools/verify_bundle.py
```

验证脚本检查文件清单与 SHA-256、恢复数量、XML/JSON 可解析性，以及常见凭据模式。通过验证不等于已恢复业务逻辑或已证明不存在任何潜在敏感信息。

分析方法、工具与恢复边界见 [PROVENANCE.md](PROVENANCE.md) 和 [LIMITATIONS.md](LIMITATIONS.md)。喵喵及相关 SDK 的代码/资源保留其原有权利归属，本分支没有将它们重新授权为简账的开源代码，见 [NOTICE.md](NOTICE.md)。

需要可构建、可使用的简账源码和安装包，请切换到 [main](https://github.com/EvA56356/plain-ledger/tree/main)。本分析分支不作为第五个简账界面版本，不发布 APK。
