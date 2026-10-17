# 版本与分支

| 分支 | 定位 | 本地预览 |
|---|---|---|
| `main` | 当前绿色离线版；桌面增强统计 | `python server.py --port 8765` |
| `ui/paper` | 温暖纸感、细线边框、书刊字体 | `python server.py --port 8767` |
| `ui/mono` | 黑白极简、桌面横向导航 | `python server.py --port 8768` |
| `ui/sky` | 清透蓝色、大圆角与悬浮侧栏 | `python server.py --port 8769` |
| `feature/shared-ledger` | 基于当前绿色 UI，增加服务器、账号、共享与同步 | `python cloud_server.py --dev-sms --port 8766` |

前三个 UI 分支与 main 的业务代码、数据库格式一致，仅修改样式、HTML 样式引用及分支说明。协作分支从 main 建立，不依赖任何其他 UI 分支。

离线分支不申请联网权限。协作版是独立 Android 包 `cn.plainledger.shared`，可与原版同时安装；个人离线数据库不会自动迁移或上传。开发短信模式只供本机测试，真实短信和公网 HTTPS 部署需自行配置。共享邀请是应用内邀请，不会自动发送短信或联系真实用户。

网页版与 Android 共用 web 源码。网页的“收支统计”提供日期范围、等长上期对比、每日收支折线、分类环图、支出热力、账户汇总、大额排行和范围 CSV 导出；Android 保持较紧凑的月度统计布局。

切换分支前先提交或备份自己的源码修改。各分支在独立目录运行时，数据库互相独立。不要提交数据库、备份、服务器密钥或真实手机号。
