# 喵喵记账 5.1.6：Manifest 与内置配置功能清单

## 证据边界

- 原始证据：`evidence/manifest.json`、`decoded/assets/cats.json`、`decoded/assets/billcats.json`、`evidence/analysis-summary.json`（本目录）。组件位置以 manifest 数组的从零开始索引标记。
- APK：喵喵记账_5.1.6_APKPure.apk；包名 `com.youqi.miaomiao`；版本 5.1.6 / 516；SHA-256 `e1fb3f4433a1494f26bba8b8e95feeb15adad77875d2aeccd4ebd637bb09777f`。minSdk 21，targetSdk 35。
- 【声明】只表示 APK 注册了组件或配置包含对应字段；【推断】表示由组件类名/包路径解释其用途，不证明当前账号可见、网络服务可用或功能已运行。
- `evidence/analysis-summary.json` 的 DEX 清单只识别到 5 个 `com.secneo.apkwrapper` 类，存在加固包装；Manifest 功能声明不能替代业务代码或运行验证。
- manifest 共 320 个 activity、37 个 service；本清单枚举全部自有前缀及两个明显集成业务模块，其他 SDK 单列。

## 全量自有与集成业务组件

### 账单、分类、搜索、转账与统计（11）

用途归类：【推断】。下列类名、组件类型、进程/权限均为【声明】。

| 类型 | 完整组件名 | manifest 索引 | 声明附注 |
|---|---|---:|---|
| activity | `com.lanniser.kittykeeping.ui.bill.SceneCateBillStatisticsActivity` | 228 | exported=false |
| activity | `com.lanniser.kittykeeping.ui.bill.SceneContactStatisticsActivity` | 229 | exported=false |
| activity | `com.lanniser.kittykeeping.ui.bill.TransferDetailsActivity` | 240 | exported=false |
| activity | `com.lanniser.kittykeeping.ui.bill.CateStatisticsActivity` | 292 | exported=false |
| activity | `com.lanniser.kittykeeping.ui.bill.BillActivity` | 293 | exported=false |
| activity | `com.lanniser.kittykeeping.ui.bill.BillEditActivity` | 305 | exported=false |
| activity | `com.lanniser.kittykeeping.ui.bill.cate.CateManagerAct` | 325 | exported=false |
| activity | `com.lanniser.kittykeeping.ui.bill.cate.CustomCateAct` | 326 | exported=false |
| activity | `com.lanniser.kittykeeping.ui.bill.search.BillSearchActivity` | 327 | exported=false |
| activity | `com.lanniser.kittykeeping.ui.bill.BillDetailActivity` | 328 | exported=false |
| activity | `com.lanniser.kittykeeping.ui.bill.BillTableActivity` | 353 | exported=false |

### 预算（4）

用途归类：【推断】。下列类名、组件类型、进程/权限均为【声明】。

| 类型 | 完整组件名 | manifest 索引 | 声明附注 |
|---|---|---:|---|
| activity | `com.lanniser.kittykeeping.ui.budget.BudgetBillListActivity` | 294 | exported=false |
| activity | `com.lanniser.kittykeeping.ui.budget.CateBudgetActivity` | 295 | exported=false |
| activity | `com.lanniser.kittykeeping.ui.budget.AddBudgetActivity` | 296 | exported=false |
| activity | `com.lanniser.kittykeeping.ui.budget.BudgetActivity` | 297 | exported=false |

### 账本管理（3）

用途归类：【推断】。下列类名、组件类型、进程/权限均为【声明】。

| 类型 | 完整组件名 | manifest 索引 | 声明附注 |
|---|---|---:|---|
| activity | `com.lanniser.kittykeeping.ui.activity.book.BillBookDetailActivity` | 289 | exported=false |
| activity | `com.lanniser.kittykeeping.ui.activity.book.BillBookEditActivity` | 290 | exported=false |
| activity | `com.lanniser.kittykeeping.ui.activity.book.BillBookListActivity` | 291 | exported=false |

### 资金账户与借贷（5）

用途归类：【推断】。下列类名、组件类型、进程/权限均为【声明】。

| 类型 | 完整组件名 | manifest 索引 | 声明附注 |
|---|---|---:|---|
| activity | `com.lanniser.kittykeeping.ui.fund.FundAccountTypeActivity` | 333 | exported=false |
| activity | `com.lanniser.kittykeeping.ui.fund.FundAccountAddActivity` | 334 | exported=false |
| activity | `com.lanniser.kittykeeping.ui.fund.FundAccountAddBorrowActivity` | 335 | exported=false |
| activity | `com.lanniser.kittykeeping.ui.fund.FundAccountTransferActivity` | 336 | exported=false |
| activity | `com.lanniser.kittykeeping.ui.fund.FundAccountDetailActivity` | 337 | exported=false |

### 场景管理（3）

用途归类：【推断】。下列类名、组件类型、进程/权限均为【声明】。

| 类型 | 完整组件名 | manifest 索引 | 声明附注 |
|---|---|---:|---|
| activity | `com.lanniser.kittykeeping.ui.activity.scene.SceneAddCateActivity` | 235 | exported=false |
| activity | `com.lanniser.kittykeeping.ui.activity.scene.SceneCateManagerActivity` | 236 | exported=false |
| activity | `com.lanniser.kittykeeping.ui.activity.scene.SceneManagerActivity` | 237 | exported=false |

### 联系人（3）

用途归类：【推断】。下列类名、组件类型、进程/权限均为【声明】。

| 类型 | 完整组件名 | manifest 索引 | 声明附注 |
|---|---|---:|---|
| activity | `com.lanniser.kittykeeping.ui.activity.contact.ContactDetailActivity` | 232 | exported=false |
| activity | `com.lanniser.kittykeeping.ui.activity.contact.AddContactActivity` | 233 | exported=false |
| activity | `com.lanniser.kittykeeping.ui.activity.contact.ContactProfileActivity` | 234 | exported=false |

### 自动记账（4）

用途归类：【推断】。下列类名、组件类型、进程/权限均为【声明】。

| 类型 | 完整组件名 | manifest 索引 | 声明附注 |
|---|---|---:|---|
| activity | `com.lanniser.kittykeeping.ui.activity.autobill.AutoBillDataActivity` | 245 | 进程 :autoBill；exported=false |
| activity | `com.lanniser.kittykeeping.ui.activity.autobill.BillIdentifiedActivity` | 250 | exported=false |
| activity | `com.lanniser.kittykeeping.ui.activity.autobill.SceneIdentifiedActivity` | 252 | exported=false |
| activity | `com.lanniser.kittykeeping.ui.activity.autobill.AutoBillSettingActivity` | 253 | exported=false |

### 截图与OCR记账（3）

用途归类：【推断】。下列类名、组件类型、进程/权限均为【声明】。

| 类型 | 完整组件名 | manifest 索引 | 声明附注 |
|---|---|---:|---|
| activity | `com.lanniser.kittykeeping.ui.activity.ocrbill.NewBillIdentifiedActivity` | 251 | exported=false |
| activity | `com.lanniser.kittykeeping.ui.activity.ocrbill.OcrBillSettingActivity` | 254 | exported=false |
| activity | `com.lanniser.kittykeeping.ui.activity.ocrbill.OcrBillScreenCaptureActivity` | 400 | exported=true |

### AI聊天记账（4）

用途归类：【推断】。下列类名、组件类型、进程/权限均为【声明】。

| 类型 | 完整组件名 | manifest 索引 | 声明附注 |
|---|---|---:|---|
| activity | `com.lanniser.kittykeeping.ui.activity.chat.AiCatSettingActivity` | 241 | exported=false |
| activity | `com.lanniser.kittykeeping.ui.activity.chat.AiSettingActivity` | 242 | exported=false |
| activity | `com.lanniser.kittykeeping.ui.activity.chat.AiHistoryBillActivity` | 243 | exported=false |
| activity | `com.lanniser.kittykeeping.ui.activity.chat.ChatActivity` | 244 | exported=false |

### 定时账单（4）

用途归类：【推断】。下列类名、组件类型、进程/权限均为【声明】。

| 类型 | 完整组件名 | manifest 索引 | 声明附注 |
|---|---|---:|---|
| activity | `com.lanniser.kittykeeping.ui.activity.timedbill.TimedBillDetailActivity` | 275 | exported=false |
| activity | `com.lanniser.kittykeeping.ui.activity.timedbill.TimedBillActivity` | 287 | exported=false |
| activity | `com.lanniser.kittykeeping.ui.activity.timedbill.CreateTimedBillActivity` | 348 | exported=false |
| activity | `com.lanniser.kittykeeping.ui.activity.timedbill.CateChooseActivity` | 349 | exported=false |

### 提醒（2）

用途归类：【推断】。下列类名、组件类型、进程/权限均为【声明】。

| 类型 | 完整组件名 | manifest 索引 | 声明附注 |
|---|---|---:|---|
| activity | `com.lanniser.kittykeeping.ui.activity.remind.RemindActivity` | 272 | exported=false |
| activity | `com.lanniser.kittykeeping.ui.activity.remind.BillRemindSettingActivity` | 273 | exported=false |

### 账单导入（3）

用途归类：【推断】。下列类名、组件类型、进程/权限均为【声明】。

| 类型 | 完整组件名 | manifest 索引 | 声明附注 |
|---|---|---:|---|
| activity | `com.lanniser.kittykeeping.ui.activity.importbill.ImportBillActivity` | 264 | exported=false |
| activity | `com.lanniser.kittykeeping.ui.activity.importbill.ImportBillDetailActivity` | 265 | exported=false |
| activity | `com.lanniser.kittykeeping.ui.activity.importbill.ImportBillHelpActivity` | 266 | exported=false |

### 账单导出（1）

用途归类：【推断】。下列类名、组件类型、进程/权限均为【声明】。

| 类型 | 完整组件名 | manifest 索引 | 声明附注 |
|---|---|---:|---|
| activity | `com.lanniser.kittykeeping.ui.activity.exportbill.ExportBillActivity` | 286 | exported=false |

### 储蓄计划与打卡（7）

用途归类：【推断】。下列类名、组件类型、进程/权限均为【声明】。

| 类型 | 完整组件名 | manifest 索引 | 声明附注 |
|---|---|---:|---|
| activity | `com.lanniser.kittykeeping.ui.activity.save.ChangeRecordActivity` | 255 | exported=false |
| activity | `com.lanniser.kittykeeping.ui.activity.save.SaveMoneyUpgradeActivity` | 256 | exported=false |
| activity | `com.lanniser.kittykeeping.ui.activity.save.SaveMoneyActivity` | 354 | exported=false |
| activity | `com.lanniser.kittykeeping.ui.activity.save.PlanDetailActivity` | 355 | exported=false |
| activity | `com.lanniser.kittykeeping.ui.activity.save.CreateSavePlanActivity` | 356 | exported=false |
| activity | `com.lanniser.kittykeeping.ui.activity.save.ModifySavePlanActivity` | 357 | exported=false |
| activity | `com.lanniser.kittykeeping.ui.dialog.save.SaveClockActivityDialog` | 367 | exported=false |

### 打卡认证与支付（7）

用途归类：【推断】。下列类名、组件类型、进程/权限均为【声明】。

| 类型 | 完整组件名 | manifest 索引 | 声明附注 |
|---|---|---:|---|
| activity | `com.lanniser.kittykeeping.ui.activity.clocking.ClockingActivity` | 370 | exported=false |
| activity | `com.lanniser.kittykeeping.ui.activity.clocking.ClockingAuthActivity` | 371 | exported=false |
| activity | `com.lanniser.kittykeeping.ui.activity.clocking.ClockingAuditActivity` | 372 | exported=false |
| activity | `com.lanniser.kittykeeping.ui.activity.clocking.ClockingPaymentSuccessActivity` | 373 | exported=false |
| activity | `com.lanniser.kittykeeping.ui.activity.clocking3.ClockingAuthV3Activity` | 374 | exported=false |
| activity | `com.lanniser.kittykeeping.ui.activity.clocking3.ClockingAuditV3Activity` | 375 | exported=false |
| activity | `com.lanniser.kittykeeping.ui.activity.clocking3.ClockingPaymentSuccessV3Activity` | 376 | exported=false |

### 购物清单（4）

用途归类：【推断】。下列类名、组件类型、进程/权限均为【声明】。

| 类型 | 完整组件名 | manifest 索引 | 声明附注 |
|---|---|---:|---|
| activity | `com.lanniser.kittykeeping.ui.activity.shopping.GoodsListPurchasedActivity` | 267 | exported=false |
| activity | `com.lanniser.kittykeeping.ui.activity.shopping.CreateShoppingListActivity` | 269 | exported=false |
| activity | `com.lanniser.kittykeeping.ui.activity.shopping.ShoppingListActivity` | 270 | exported=false |
| activity | `com.lanniser.kittykeeping.ui.activity.shopping.GoodsListActivity` | 271 | exported=false |

### 回收站（4）

用途归类：【推断】。下列类名、组件类型、进程/权限均为【声明】。

| 类型 | 完整组件名 | manifest 索引 | 声明附注 |
|---|---|---:|---|
| activity | `com.lanniser.kittykeeping.ui.activity.recycle.RecycleFundAccountActivity` | 278 | exported=false |
| activity | `com.lanniser.kittykeeping.ui.activity.recycle.RecycleBillBookActivity` | 279 | exported=false |
| activity | `com.lanniser.kittykeeping.ui.activity.recycle.RecycleBillActivity` | 280 | exported=false |
| activity | `com.lanniser.kittykeeping.ui.activity.recycle.RecycleBinActivity` | 281 | exported=false |

### 月度报告（1）

用途归类：【推断】。下列类名、组件类型、进程/权限均为【声明】。

| 类型 | 完整组件名 | manifest 索引 | 声明附注 |
|---|---|---:|---|
| activity | `com.lanniser.kittykeeping.ui.activity.report.MonthlyReportActivity` | 399 | exported=false |

### 桌面组件（20）

用途归类：【推断】。下列类名、组件类型、进程/权限均为【声明】。

| 类型 | 完整组件名 | manifest 索引 | 声明附注 |
|---|---|---:|---|
| activity | `com.lanniser.kittykeeping.ui.activity.widget.AppWidgetClickActivity` | 246 | 进程 :appWidget；exported=false |
| activity | `com.lanniser.kittykeeping.ui.activity.widget.WidgetMineActivity` | 257 | exported=false |
| activity | `com.lanniser.kittykeeping.ui.activity.widget.HourglassWidgetSettingActivity` | 258 | exported=false |
| activity | `com.lanniser.kittykeeping.ui.activity.widget.AppWidgetSmallActivity` | 339 | exported=false |
| activity | `com.lanniser.kittykeeping.ui.activity.widget.AppWidgetMiddleActivity` | 340 | exported=false |
| activity | `com.lanniser.kittykeeping.ui.activity.widget.AppWidgetLargeActivity` | 341 | exported=false |
| activity | `com.lanniser.kittykeeping.ui.activity.widget.TicketWidgetSettingActivity` | 342 | exported=false |
| activity | `com.lanniser.kittykeeping.ui.activity.widget.CalendarWidgetSettingActivity` | 343 | exported=false |
| activity | `com.lanniser.kittykeeping.ui.activity.widget.QuickBookkeepingWidgetSettingActivity` | 344 | exported=false |
| activity | `com.lanniser.kittykeeping.ui.activity.widget.WidgetBillSmallSettingActivity` | 359 | exported=false |
| activity | `com.lanniser.kittykeeping.ui.activity.widget.SaveMoneyWidgetSettingActivity` | 360 | exported=false |
| activity | `com.lanniser.kittykeeping.ui.activity.widget.TagsWidgetSettingActivity` | 361 | exported=false |
| activity | `com.lanniser.kittykeeping.ui.activity.widget.LineChartMidWidgetSettingActivity` | 362 | exported=false |
| activity | `com.lanniser.kittykeeping.ui.activity.widget.LineChartLargeWidgetSettingActivity` | 363 | exported=false |
| activity | `com.lanniser.kittykeeping.ui.activity.widget.WebViewWidgetVideoActivity` | 364 | exported=false |
| activity | `com.lanniser.kittykeeping.ui.activity.widget.WidgetMainActivity` | 365 | exported=false |
| activity | `com.lanniser.kittykeeping.ui.activity.widget.SaveMoneyPotWidgetSettingActivity` | 369 | exported=false |
| receiver | `com.lanniser.kittykeeping.appwidget.AppWidgetSmall` | 404 | exported=false |
| receiver | `com.lanniser.kittykeeping.appwidget.AppWidgetMiddle` | 408 | exported=false |
| receiver | `com.lanniser.kittykeeping.appwidget.AppWidgetLarge` | 412 | exported=false |

### 密码锁（2）

用途归类：【推断】。下列类名、组件类型、进程/权限均为【声明】。

| 类型 | 完整组件名 | manifest 索引 | 声明附注 |
|---|---|---:|---|
| activity | `com.lanniser.kittykeeping.ui.activity.password.PasswordActivity` | 350 | exported=false |
| activity | `com.lanniser.kittykeeping.ui.activity.password.PasswordInputActivity` | 351 | exported=false |

### 主题与个性化（3）

用途归类：【推断】。下列类名、组件类型、进程/权限均为【声明】。

| 类型 | 完整组件名 | manifest 索引 | 声明附注 |
|---|---|---:|---|
| activity | `com.lanniser.kittykeeping.ui.activity.setting.IndividualizationActivity` | 263 | exported=false |
| activity | `com.lanniser.kittykeeping.ui.theme.ThemeActivity` | 321 | exported=false |
| activity | `com.lanniser.kittykeeping.ui.theme.ThemeAllActivity` | 329 | exported=false |

### 小票外观与预览（4）

用途归类：【推断】。下列类名、组件类型、进程/权限均为【声明】。

| 类型 | 完整组件名 | manifest 索引 | 声明附注 |
|---|---|---:|---|
| activity | `com.lanniser.kittykeeping.ui.activity.ticket.TicketSkinListActivity` | 259 | exported=false |
| activity | `com.lanniser.kittykeeping.ui.activity.ticket.TicketDialogActivity` | 283 | exported=false |
| activity | `com.lanniser.kittykeeping.ui.activity.ticket.TicketHintDialogActivity` | 284 | exported=false |
| activity | `com.lanniser.kittykeeping.ui.activity.ticket.TicketPreviewDialogActivity` | 285 | exported=false |

### 首页和猫咪家园（3）

用途归类：【推断】。下列类名、组件类型、进程/权限均为【声明】。

| 类型 | 完整组件名 | manifest 索引 | 声明附注 |
|---|---|---:|---|
| activity | `com.lanniser.kittykeeping.ui.activity.home.HomeBillMenuActivity` | 260 | exported=false |
| activity | `com.lanniser.kittykeeping.ui.activity.home.CatHomeActivity` | 261 | exported=false |
| activity | `com.lanniser.kittykeeping.ui.activity.home.MainActivity` | 262 | exported=false |

### 猫咪喂养（1）

用途归类：【推断】。下列类名、组件类型、进程/权限均为【声明】。

| 类型 | 完整组件名 | manifest 索引 | 声明附注 |
|---|---|---:|---|
| activity | `com.lanniser.kittykeeping.ui.home.FeedActivity` | 322 | exported=false |

### 商城与仓库（2）

用途归类：【推断】。下列类名、组件类型、进程/权限均为【声明】。

| 类型 | 完整组件名 | manifest 索引 | 声明附注 |
|---|---|---:|---|
| activity | `com.lanniser.kittykeeping.ui.shop.ShopActivity` | 306 | exported=false |
| activity | `com.lanniser.kittykeeping.ui.shop.WarehouseActivity` | 320 | exported=false |

### 多币种与汇率换算（4）

用途归类：【推断】。下列类名、组件类型、进程/权限均为【声明】。

| 类型 | 完整组件名 | manifest 索引 | 声明附注 |
|---|---|---:|---|
| activity | `com.lanniser.kittykeeping.ui.rate.OptionRateActivity` | 299 | exported=false |
| activity | `com.lanniser.kittykeeping.ui.rate.ConverterActivity` | 300 | exported=false |
| activity | `com.lanniser.kittykeeping.ui.rate.ExchangeRateUpdateActivity` | 301 | exported=false |
| activity | `com.lanniser.kittykeeping.ui.rate.MyCurrencyActivity` | 302 | exported=false |

### 账号、会员与设置（13）

用途归类：【推断】。下列类名、组件类型、进程/权限均为【声明】。

| 类型 | 完整组件名 | manifest 索引 | 声明附注 |
|---|---|---:|---|
| activity | `com.lanniser.kittykeeping.ui.user.VipPrivilegeActivity` | 268 | exported=false |
| activity | `com.lanniser.kittykeeping.ui.user.MineActivity` | 307 | exported=false |
| activity | `com.lanniser.kittykeeping.ui.user.AboutActivity` | 308 | exported=false |
| activity | `com.lanniser.kittykeeping.ui.user.LoginActivity` | 309 | exported=false |
| activity | `com.lanniser.kittykeeping.ui.user.PhoneLoginActivity` | 310 | exported=false |
| activity | `com.lanniser.kittykeeping.ui.user.UpdateNameActivity` | 312 | exported=false |
| activity | `com.lanniser.kittykeeping.ui.user.UserInfoAct` | 323 | exported=false |
| activity | `com.lanniser.kittykeeping.ui.user.WebViewActivity` | 324 | exported=false |
| activity | `com.lanniser.kittykeeping.ui.user.VipActivity` | 338 | exported=false |
| activity | `com.lanniser.kittykeeping.ui.user.SettingActivity` | 352 | exported=false |
| activity | `com.lanniser.kittykeeping.ui.user.PhoneBindActivity` | 358 | exported=false |
| activity | `com.lanniser.kittykeeping.ui.user.VideoPlayerActivity` | 366 | exported=false |
| activity | `com.lanniser.kittykeeping.ui.user.OauthActivity` | 368 | exported=false |

### 成就（1）

用途归类：【推断】。下列类名、组件类型、进程/权限均为【声明】。

| 类型 | 完整组件名 | manifest 索引 | 声明附注 |
|---|---|---:|---|
| activity | `com.lanniser.kittykeeping.ui.achievement.AchievementActivity` | 313 | exported=false |

### 明信片与采风（1）

用途归类：【推断】。下列类名、组件类型、进程/权限均为【声明】。

| 类型 | 完整组件名 | manifest 索引 | 声明附注 |
|---|---|---:|---|
| activity | `com.lanniser.kittykeeping.ui.postcard.CollectingWindActivity` | 298 | exported=false |

### 相册（2）

用途归类：【推断】。下列类名、组件类型、进程/权限均为【声明】。

| 类型 | 完整组件名 | manifest 索引 | 声明附注 |
|---|---|---:|---|
| activity | `com.lanniser.kittykeeping.ui.album.AlbumActivity` | 331 | exported=false |
| activity | `com.lanniser.kittykeeping.ui.album.AlbumDetailActivity` | 332 | exported=false |

### 信箱（1）

用途归类：【推断】。下列类名、组件类型、进程/权限均为【声明】。

| 类型 | 完整组件名 | manifest 索引 | 声明附注 |
|---|---|---:|---|
| activity | `com.lanniser.kittykeeping.ui.mailbox.MailboxActivity` | 330 | exported=false |

### 微信登录/分享与支付回调（2）

用途归类：【推断】。下列类名、组件类型、进程/权限均为【声明】。

| 类型 | 完整组件名 | manifest 索引 | 声明附注 |
|---|---|---:|---|
| activity | `com.youqi.miaomiao.wxapi.WXEntryActivity` | 311 | exported=true |
| activity | `com.youqi.miaomiao.wxapi.WXPayEntryActivity` | 315 | exported=true |

### 春节活动（2）

用途归类：【推断】。下列类名、组件类型、进程/权限均为【声明】。

| 类型 | 完整组件名 | manifest 索引 | 声明附注 |
|---|---|---:|---|
| activity | `com.lanniser.kittykeeping.ui.activity.act.NewYearActivity` | 238 | exported=false |
| activity | `com.lanniser.kittykeeping.ui.activity.act.NewYearRecordActivity` | 239 | exported=false |

### 奖励中心、收益、提现和补贴（集成模块）（8）

用途归类：【推断】。下列类名、组件类型、进程/权限均为【声明】。

| 类型 | 完整组件名 | manifest 索引 | 声明附注 |
|---|---|---:|---|
| activity | `com.ioio.rewardhub.ui.activity.WzHomeActivity` | 640 | exported=true |
| activity | `com.ioio.rewardhub.ui.activity.WzExtractMoneyActivity` | 641 | exported=true |
| activity | `com.ioio.rewardhub.ui.activity.WzRulesAtivity` | 642 | exported=true |
| activity | `com.ioio.rewardhub.ui.activity.WzRevenueListActivity` | 643 | exported=true |
| activity | `com.ioio.rewardhub.ui.activity.WzTakeoutSubsidyActivity` | 644 | exported=true |
| activity | `com.ioio.rewardhub.ui.activity.PermissionWZActivity` | 645 | exported=false |
| activity | `com.ioio.rewardhub.ui.activity.WzWidgetSettingActivity` | 646 | exported=true |
| receiver | `com.ioio.rewardhub.widget.WzAppWidgetProvider` | 647 | exported=true |

### 塔罗小游戏与结果（集成模块）（2）

用途归类：【推断】。下列类名、组件类型、进程/权限均为【声明】。

| 类型 | 完整组件名 | manifest 索引 | 声明附注 |
|---|---|---:|---|
| activity | `cn.aiyo.fne.tarot.ui.TarotGameActivity` | 651 |  |
| activity | `cn.aiyo.fne.tarot.ui.TarotInterpretResultActivity` | 652 |  |

### 后台服务、接收器与保活（10）

用途归类：【推断】。下列类名、组件类型、进程/权限均为【声明】。

| 类型 | 完整组件名 | manifest 索引 | 声明附注 |
|---|---|---:|---|
| receiver | `com.lanniser.kittykeeping.receiver.CloseScreenCaptureReceiver` | 403 |  |
| receiver | `com.lanniser.kittykeeping.receiver.AutoBillBroadcastReceiver` | 416 | 进程 :autoBill；exported=false |
| service | `com.lanniser.kittykeeping.service.ScreenCaptureTileService` | 419 | 进程 :autoBill；权限 android.permission.BIND_QUICK_SETTINGS_TILE；exported=true |
| service | `com.lanniser.kittykeeping.service.ScreenCaptureService` | 423 | 权限 android.permission.FOREGROUND_SERVICE；exported=false |
| service | `com.lanniser.kittykeeping.service.PetFloatService` | 426 | exported=false |
| service | `com.lanniser.kittykeeping.service.AutoBillService` | 427 | 进程 :autoBill |
| service | `com.lanniser.kittykeeping.service.GTPushService` | 449 | 进程 :pushservice；exported=false |
| service | `com.lanniser.kittykeeping.service.MmIntentService` | 450 |  |
| activity | `com.lanniser.kittykeeping.ui.activity.getui.KeepAliveActivity` | 451 | 进程 :pushservice；exported=true |
| service | `com.lanniser.kittykeeping.service.KeepAliveService` | 452 | 进程 :pushservice；exported=true |

### 广告相关自有界面（2）

用途归类：【推断】。下列类名、组件类型、进程/权限均为【声明】。

| 类型 | 完整组件名 | manifest 索引 | 声明附注 |
|---|---|---:|---|
| activity | `com.lanniser.kittykeeping.ui.activity.AdPersonalRecommendActivity` | 247 | exported=false |
| activity | `com.lanniser.kittykeeping.ui.activity.InterstitialAdActivity` | 347 | exported=false |

### 其他自有页面与入口（14）

用途归类：【推断】。下列类名、组件类型、进程/权限均为【声明】。

| 类型 | 完整组件名 | manifest 索引 | 声明附注 |
|---|---|---:|---|
| activity | `com.lanniser.kittykeeping.ui.activity.SystemPermissionActivity` | 230 | exported=false |
| activity | `com.lanniser.kittykeeping.ui.activity.RevokePolicyActivity` | 231 | exported=false |
| activity | `com.lanniser.kittykeeping.ui.activity.IntroduceActivity` | 248 | exported=false |
| activity | `com.lanniser.kittykeeping.ui.activity.CourseActivity` | 249 | exported=false |
| activity | `com.lanniser.kittykeeping.ui.activity.ReceiveSuccessActivity` | 274 | exported=false |
| activity | `com.lanniser.kittykeeping.ui.activity.ImagePreviewActivity` | 276 | exported=false |
| activity | `com.lanniser.kittykeeping.ui.activity.StatisticsActivity` | 277 | exported=false |
| activity | `com.lanniser.kittykeeping.ui.activity.PayActivity` | 282 | exported=false |
| activity | `com.lanniser.kittykeeping.ui.activity.CatAndIllustratedActivity` | 288 | exported=false |
| activity | `com.lanniser.kittykeeping.ui.activity.FieldGuildActivity` | 314 | exported=false |
| activity | `com.lanniser.kittykeeping.ui.activity.HelpActivity` | 345 | exported=false |
| activity | `com.lanniser.kittykeeping.ui.activity.PermissionActivity` | 346 | exported=false |
| activity | `com.lanniser.kittykeeping.ui.activity.SplashActivity` | 377 | exported=true |
| activity | `com.lanniser.kittykeeping.ui.activity.SchemeStartActivity` | 398 | exported=true |

## 内置分类（配置直接观察）

- `billcats.json.appear`：34 个条目，标题为 餐饮、购物、日用、交通、蔬菜、水果、零食、运动、娱乐、通讯、服饰、美容、住房、家庭、社交、旅行、烟酒、数码、汽车、医疗、书籍、学习、宠物、礼金、礼品、办公、维修、捐赠、彩票、红包、快递、其它、还款、借出。appear 作为支出侧的解释依赖数据语义，收入/支出实际规则需代码确认。
- `billcats.json.income`：10 个条目，标题为 工资、红包、租金、礼金、分红、理财、年终奖、其它、借入、收款。
- 条目包含 id、key、title、isCustom、type 等字段，存在自定义分类标记；不能据默认值推定用户分类上限。

## 猫咪配置（配置直接观察）

- `cats.json.cats` 共 48 条；type=1 有 36 条，type=2 有 12 条。type 的业务含义未证实。
- 字段支持的候选功能：猫咪名称/徽章/故事介绍、分类绑定、欢迎和互动台词、喂食动画、喜爱/普通/讨厌食物与礼物、好感升级阈值、勋章灰显/可用状态、衣柜/等级/价格/拥有标记、图鉴及主题资源。字段存在属于观察，实际玩法为推断。
- 资源 URL 指向 `meow-image.aiyouaiyou.cn`，本分析未访问远程资源，也未验证服务可用。

| id | key | 名称 | 分类 | 衣柜条目数 |
|---:|---|---|---|---:|
| 48 | ad_mao | 招财猫 |  | 3 |
| 49 | npc_mao | 帮助猫 |  | 3 |
| 7 | qiche_mao | 汽车猫 | 汽车 | 3 |
| 9 | yanjiu_mao | 烟酒猫 | 烟酒 | 3 |
| 16 | tongxun_mao | 通讯猫 | 通讯 | 3 |
| 12 | jiating_mao | 家庭猫 | 家庭 | 3 |
| 6 | yiliao_mao | 医疗猫 | 医疗 | 3 |
| 10 | lvxing_mao | 旅行猫 | 旅行 | 3 |
| 21 | shucai_mao | 蔬菜猫 | 蔬菜 | 3 |
| 5 | shuji_mao | 书籍猫 | 书籍 | 3 |
| 1 | lipin_mao | 礼品猫 | 礼品 | 3 |
| 27 | hongbao_mao | 红包猫 | 红包 | 4 |
| 15 | fushi_mao | 服饰猫 | 服饰 | 4 |
| 24 | gouwu_mao | 购物猫 | 购物 | 4 |
| 34 | sr_hongbao_mao | 红包猫 | 红包 | 4 |
| 26 | bangong_mao | 办公猫 | 办公 | 3 |
| 17 | yule_mao | 娱乐猫 | 娱乐 | 3 |
| 23 | riyong_mao | 日用猫 | 日用 | 4 |
| 11 | shejiao_mao | 社交猫 | 社交 | 3 |
| 22 | jiaotong_mao | 交通猫 | 交通 | 4 |
| 38 | sr_licai_mao | 理财猫 | 理财 | 3 |
| 31 | kuaidi_mao | 快递猫 | 快递 | 3 |
| 33 | sr_gongzi_mao | 工资猫 | 工资 | 4 |
| 8 | shuma_mao | 数码猫 | 数码 | 3 |
| 32 | qita_mao | 其它猫 | 其它 | 4 |
| 29 | weixiu_mao | 维修猫 | 维修 | 3 |
| 39 | sr_nianzhongjiang_mao | 年终奖猫 | 年终奖 | 3 |
| 2 | lijin_mao | 礼金猫 | 礼金 | 3 |
| 28 | juanzeng_mao | 捐赠猫 | 捐赠 | 3 |
| 30 | caipiao_mao | 彩票猫 | 彩票 | 3 |
| 35 | sr_zujin_mao | 租金猫 | 租金 | 3 |
| 40 | sr_qita_mao | 其它猫 | 其它 | 4 |
| 36 | sr_lijin_mao | 礼金猫 | 礼金 | 3 |
| 37 | sr_fenhong_mao | 分红猫 | 分红 | 3 |
| 13 | zhufang_mao | 住房猫 | 住房 | 3 |
| 18 | yundong_mao | 健身猫 | 运动 | 3 |
| 45 | yinpin_mao | 饮品猫 | 饮品 | 4 |
| 47 | zhuixing_mao | 追星猫 | 追星 | 3 |
| 4 | xuexi_mao | 学习猫 | 学习 | 3 |
| 44 | sr_shoukuan_mao | 收款猫 | 收款 | 3 |
| 43 | sr_jieru_mao | 借入猫 | 借入 | 3 |
| 20 | shuiguo_mao | 水果猫 | 水果 | 3 |
| 14 | meirong_mao | 美容猫 | 美容 | 3 |
| 42 | jiechu_mao | 借出猫 | 借出 | 3 |
| 19 | lingshi_mao | 零食猫 | 零食 | 4 |
| 25 | canyin_mao | 餐饮猫 | 餐饮 | 4 |
| 41 | huankuan_mao | 还款猫 | 还款 | 3 |
| 3 | chongwu_mao | 宠物猫 | 宠物 | 3 |

## 广告、推广、直播 SDK（不计入记账业务功能）

快手登录别名 `com.youqi.miaomiao.kwad.KwadHandlerActivity`（activity-alias，manifest[519]）指向 `com.kwad.auth.login.KwadRouteHandlerActivity`，属于 SDK 登录接入，不计入记账功能。

以下组件说明集成基础设施存在，不证明用户会遇到每种广告形态。广告激励与奖励中心间的实际联动尚未建立。

- 【声明】activity `com.lanniser.kittykeeping.ui.activity.AdPersonalRecommendActivity`（manifest[247]）。
- 【声明】activity `com.lanniser.kittykeeping.ui.activity.InterstitialAdActivity`（manifest[347]）。
- 【声明】provider `com.bytedance.sdk.openadsdk.TTFileProvider`（manifest[443]）。
- 【声明】receiver `com.byazt.za.Collector`（manifest[561]）。
- 【声明】activity `com.byazt.bm.JumpKllkActivity`（manifest[563]）。
- 【声明】receiver `com.byazt.zs.DownloadReceiver`（manifest[564]）。
- 【声明】service `com.byazt.z.DownloadHandlerService`（manifest[566]）。
- 【声明】activity `com.byazt.y.JumpUnknownSourceActivity`（manifest[568]）。
- 【声明】service `com.byazt.z.RetryJobSchedulerService`（manifest[569]）。
- 【声明】service `com.byazt.n.DownloadNotificationService`（manifest[570]）。
- 【声明】service `com.byazt.zz.CSJDownloadService`（manifest[571]）。
- 【声明】service `com.byazt.b.DownloadHandleService`（manifest[572]）。
- 【声明】service `com.byazt.zz.SqlDownloadCacheService`（manifest[573]）。
- 【声明】service `com.byazt.zz.CSJIndependentProcessDownloadService`（manifest[574]）。
- 【声明】activity `com.bytedance.sdk.openadsdk.core.activity.base.TTNativePageActivity`（manifest[577]）。
- 【声明】activity `com.bytedance.sdk.openadsdk.core.activity.base.TTWebPageActivity`（manifest[578]）。
- 【声明】activity `com.bytedance.sdk.openadsdk.core.activity.base.TTSevenScreenWebPageActivity`（manifest[579]）。
- 【声明】activity `com.bytedance.sdk.openadsdk.core.activity.base.TTVideoWebPageActivity`（manifest[580]）。
- 【声明】activity `com.bytedance.sdk.openadsdk.core.activity.base.TTVideoScrollWebPageActivity`（manifest[581]）。
- 【声明】activity `com.bytedance.sdk.openadsdk.core.activity.base.TTDelegateActivity`（manifest[582]）。
- 【声明】activity `com.bytedance.sdk.openadsdk.core.component.reward.activity.TTRewardVideoActivity`（manifest[583]）。
- 【声明】activity `com.bytedance.sdk.openadsdk.core.component.reward.activity.TTFullScreenVideoActivity`（manifest[584]）。
- 【声明】activity `com.bytedance.sdk.openadsdk.core.component.reward.activity.TTRewardVideoLandscapeActivity`（manifest[585]）。
- 【声明】activity `com.bytedance.sdk.openadsdk.core.component.reward.activity.TTFullScreenVideoLandscapeActivity`（manifest[586]）。
- 【声明】service `com.byazt.wzi.BinderPoolService`（manifest[587]）。
- 【声明】activity `com.byazt.qde.Stub_Standard_Activity`（manifest[588]）。
- 【声明】activity `com.byazt.qde.Stub_Standard_Portrait_Activity`（manifest[589]）。
- 【声明】activity `com.byazt.qde.Stub_Standard_Activity_T`（manifest[590]）。
- 【声明】activity `com.byazt.qde.Stub_Standard_Landscape_Activity`（manifest[591]）。
- 【声明】activity `com.byazt.qde.Stub_Activity`（manifest[592]）。
- 【声明】activity `com.byazt.qde.Stub_SingleTask_Activity_T`（manifest[593]）。
- 【声明】activity `com.byazt.qde.Stub_SingleTask_Activity`（manifest[594]）。
- 【声明】provider `com.byazt.gg.DownloaderServerManager`（manifest[595]）。
- 【声明】provider `com.byazt.gg.MainServerManager`（manifest[596]）。
- 【声明】provider `com.byazt.vf.TTMultiProvider`（manifest[597]）。
- 【声明】service `com.byazt.nu.ApiDownloadHandlerService`（manifest[598]）。
- 【声明】activity `com.byazt.nu.ApiDownloadHandleNotificationActivity`（manifest[599]）。
- 【声明】provider `com.byazt.quv.MainServerManager`（manifest[600]）。
- 【声明】provider `com.byazt.sj.MainProcessProviderProxy`（manifest[601]）。
- 【声明】provider `com.byazt.uk.FileProvider`（manifest[602]）。
- 【声明】activity `com.bytedance.android.openliveplugin.stub.activity.DouyinAuthorizeActivityProxy`（manifest[603]）。
- 【声明】activity `com.bytedance.android.openliveplugin.stub.activity.DouyinAuthorizeActivityLiveProcessProxy`（manifest[604]）。
- 【声明】provider `com.bytedance.android.openliveplugin.process.server.LiveServerManager`（manifest[605]）。
- 【声明】activity `com.bytedance.android.openliveplugin.stub.activity.StubActivity$Activity_Portrait`（manifest[606]）。
- 【声明】activity `com.bytedance.android.openliveplugin.stub.activity.StubActivity$Activity_Portrait2`（manifest[607]）。
- 【声明】activity `com.bytedance.android.openliveplugin.stub.activity.StubActivity$Activity_Portrait3`（manifest[608]）。
- 【声明】activity `com.bytedance.android.openliveplugin.stub.activity.StubActivity$Activity_Portrait4`（manifest[609]）。
- 【声明】activity `com.bytedance.android.openliveplugin.stub.activity.StubActivity$Activity_Portrait5`（manifest[610]）。
- 【声明】activity `com.byted.live.lite.Activity_bytelive_singleTask4`（manifest[611]）。
- 【声明】activity `com.byted.live.lite.Activity_bytelive_singleTop3`（manifest[612]）。
- 【声明】activity `com.byted.live.lite.Activity_bytelive_singleTop2`（manifest[613]）。
- 【声明】activity `com.byted.live.lite.Activity_bytelive_singleTop5`（manifest[614]）。
- 【声明】activity `com.byted.live.lite.Activity_bytelive_singleTop4`（manifest[615]）。
- 【声明】activity `com.byted.live.lite.Activity_bytelive_standard`（manifest[616]）。
- 【声明】activity `com.byted.live.lite.Activity_bytelive_singleTop1`（manifest[617]）。
- 【声明】activity `com.byted.live.lite.Activity_bytelive_singleTask1`（manifest[618]）。
- 【声明】activity `com.byted.live.lite.Activity_bytelive_singleTop6`（manifest[619]）。
- 【声明】activity `com.byted.live.lite.Activity_bytelive_singleTask2`（manifest[620]）。
- 【声明】activity `com.byted.live.lite.Activity_bytelive_singleTask3`（manifest[621]）。
- 【声明】activity `com.byted.live.lite.Activity_main_singleTask1`（manifest[622]）。
- 【声明】activity `com.byted.live.lite.Activity_main_singleTask2`（manifest[623]）。
- 【声明】activity `com.byted.live.lite.Activity_main_singleTask3`（manifest[624]）。
- 【声明】activity `com.byted.live.lite.Activity_main_singleTask4`（manifest[625]）。
- 【声明】activity `com.byted.live.lite.Activity_main_standard`（manifest[626]）。
- 【声明】activity `com.byted.live.lite.Activity_main_singleTop1`（manifest[627]）。
- 【声明】activity `com.byted.live.lite.Activity_main_singleTop2`（manifest[628]）。
- 【声明】activity `com.byted.live.lite.Activity_main_singleInstance1`（manifest[629]）。
- 【声明】activity `com.byted.live.lite.Activity_main_singleTop5`（manifest[630]）。
- 【声明】activity `com.byted.live.lite.Activity_main_singleTop6`（manifest[631]）。
- 【声明】activity `com.byted.live.lite.Activity_main_singleTop3`（manifest[632]）。
- 【声明】activity `com.byted.live.lite.Activity_main_singleTop4`（manifest[633]）。
- 【声明】activity `com.byted.live.lite.Activity_bytelive_singleInstance1`（manifest[634]）。
- 【声明】provider `com.byted.live.lite.ServerManager_bytelive`（manifest[635]）。
- 【声明】provider `com.byted.live.lite.ServerManager_push`（manifest[636]）。
- 【声明】provider `com.byted.live.lite.ServerManager_downloader`（manifest[637]）。
- 【声明】activity `com.bytedance.msdk.api.activity.TTDelegateActivity`（manifest[638]）。
- 【声明】activity `com.bytedance.msdk.api.activity.TTTransparentActivity`（manifest[639]）。
- 【声明】activity `com.bytedance.ads.convert.BDBridgeActivity`（manifest[666]）。
- 【声明】activity `com.library.zt.ad.activity.ZTDouYinMallActivity`（manifest[766]）。
- 【声明】activity `com.library.zt.ad.activity.ZTInteractionAdActivity`（manifest[767]）。
- 【声明】activity `com.library.zt.ad.activity.ZTJTWebViewActivity`（manifest[768]）。
- 【声明】activity `com.library.zt.ad.activity.ZTWebViewActivity`（manifest[769]）。
- 【声明】provider `com.library.zt.ad.provider.DownloadFileProvider`（manifest[770]）。
- 【声明】activity `com.huawei.openalliance.ad.activity.FeedbackActivity`（manifest[777]）。
- 【声明】activity `com.huawei.openalliance.ad.activity.PPSLauncherActivity`（manifest[792]）。
- 【声明】activity `com.huawei.openalliance.ad.activity.PPSBridgeActivity`（manifest[800]）。
- 【声明】activity `com.huawei.openalliance.ad.activity.PPSNotificationActivity`（manifest[803]）。
- 【声明】activity `com.huawei.openalliance.ad.activity.AgProtocolActivity`（manifest[806]）。
- 【声明】activity `com.huawei.openalliance.ad.activity.TemplateStubActivity`（manifest[810]）。
- 【声明】activity `com.huawei.openalliance.ad.activity.ComplianceActivity`（manifest[816]）。
- 【声明】activity `com.huawei.openalliance.ad.activity.PPSShareActivity`（manifest[819]）。
- 【声明】provider `com.huawei.openalliance.ad.provider.PPSECProvider`（manifest[822]）。

## 其他第三方与框架组件（全量补充）

- 【声明】service `com.google.android.accessibility.selecttospeak.SelectToSpeakService`（manifest[428]）。
- 【声明】provider `androidx.core.content.FileProvider`（manifest[432]）。
- 【声明】activity `com.tencent.tauth.AuthActivity`（manifest[436]）。
- 【声明】activity `com.tencent.connect.common.AssistActivity`（manifest[442]）。
- 【声明】provider `com.qq.e.comm.GDTFileProvider`（manifest[445]）。
- 【声明】provider `com.meishu.sdk.core.service.MeishuFileProvider`（manifest[447]）。
- 【声明】receiver `com.mlethe.library.accessibility.auto.MainProcessBroadcastReceiver`（manifest[454]）。
- 【声明】provider `com.baidu.mobads.sdk.api.BdFileProvider`（manifest[459]）。
- 【声明】activity `com.baidu.mobads.sdk.api.AppActivity`（manifest[461]）。
- 【声明】activity `com.baidu.oauth.sdkbqt.view.WebViewActivity`（manifest[462]）。
- 【声明】activity `com.baidu.mobads.sdk.api.MobRewardVideoActivity`（manifest[463]）。
- 【声明】activity `com.baidu.mobads.sdk.api.BdShellActivity`（manifest[464]）。
- 【声明】service `com.qq.e.comm.DownloadService`（manifest[465]）。
- 【声明】activity `com.qq.e.ads.ADActivity`（manifest[466]）。
- 【声明】activity `com.qq.e.ads.PortraitADActivity`（manifest[467]）。
- 【声明】activity `com.qq.e.ads.LandscapeADActivity`（manifest[468]）。
- 【声明】activity `com.qq.e.ads.RewardvideoPortraitADActivity`（manifest[469]）。
- 【声明】activity `com.qq.e.ads.RewardvideoLandscapeADActivity`（manifest[471]）。
- 【声明】activity `com.qq.e.ads.DialogActivity`（manifest[473]）。
- 【声明】activity `com.jd.ad.sdk.bl.adinteraction.deeplink.JADWebViewActivity`（manifest[474]）。
- 【声明】provider `com.jd.ad.sdk.dl.cache.multiprocess.JADMultiProvider`（manifest[475]）。
- 【声明】activity `com.kwad.sdk.api.proxy.app.AdWebViewActivity`（manifest[476]）。
- 【声明】activity `com.kwad.sdk.api.proxy.app.KsFullScreenVideoActivity`（manifest[477]）。
- 【声明】activity `com.kwad.sdk.api.proxy.app.KsFullScreenLandScapeVideoActivity`（manifest[478]）。
- 【声明】activity `com.kwad.sdk.api.proxy.app.KsRewardVideoActivity`（manifest[479]）。
- 【声明】activity `com.kwad.sdk.api.proxy.app.KSRewardLandScapeVideoActivity`（manifest[480]）。
- 【声明】activity `com.kwad.sdk.api.proxy.app.FeedDownloadActivity`（manifest[481]）。
- 【声明】activity `com.kwad.sdk.api.proxy.app.BaseFragmentActivity$KsTrendsActivity`（manifest[482]）。
- 【声明】activity `com.kwad.sdk.api.proxy.app.BaseFragmentActivity$ProfileHomeActivity`（manifest[483]）。
- 【声明】activity `com.kwad.sdk.api.proxy.app.BaseFragmentActivity$ProfileVideoDetailActivity`（manifest[484]）。
- 【声明】activity `com.kwad.sdk.api.proxy.app.BaseFragmentActivity$TubeProfileActivity`（manifest[485]）。
- 【声明】activity `com.kwad.sdk.api.proxy.app.BaseFragmentActivity$ChannelDetailActivity`（manifest[486]）。
- 【声明】activity `com.kwad.sdk.api.proxy.app.BaseFragmentActivity$TubeDetailActivity`（manifest[487]）。
- 【声明】activity `com.kwad.sdk.api.proxy.app.BaseFragmentActivity$EpisodeDetailActivity`（manifest[488]）。
- 【声明】activity `com.kwad.sdk.api.proxy.app.BaseFragmentActivity$RequestInstallPermissionActivity`（manifest[489]）。
- 【声明】activity `com.kwad.sdk.api.proxy.app.BaseFragmentActivity$FragmentActivity1`（manifest[493]）。
- 【声明】activity `com.kwad.sdk.api.proxy.app.BaseFragmentActivity$GoodsPlayBackActivity`（manifest[494]）。
- 【声明】activity `com.kwad.sdk.api.proxy.app.BaseFragmentActivity$FragmentActivity2`（manifest[495]）。
- 【声明】activity `com.kwad.sdk.api.proxy.app.BaseFragmentActivity$FragmentActivity3`（manifest[496]）。
- 【声明】activity `com.kwad.sdk.api.proxy.app.BaseFragmentActivity$FragmentActivity4`（manifest[497]）。
- 【声明】activity `com.kwad.sdk.api.proxy.app.BaseFragmentActivity$FragmentActivity5`（manifest[498]）。
- 【声明】activity `com.kwad.sdk.api.proxy.app.BaseFragmentActivity$FragmentActivity6`（manifest[499]）。
- 【声明】activity `com.kwad.sdk.api.proxy.app.BaseFragmentActivity$FragmentActivity7`（manifest[500]）。
- 【声明】activity `com.kwad.sdk.api.proxy.app.BaseFragmentActivity$FragmentActivity8`（manifest[501]）。
- 【声明】activity `com.kwad.sdk.api.proxy.app.BaseFragmentActivity$FragmentActivity9`（manifest[502]）。
- 【声明】activity `com.kwad.sdk.api.proxy.app.BaseFragmentActivity$FragmentActivity10`（manifest[503]）。
- 【声明】activity `com.kwad.sdk.api.proxy.app.BaseFragmentActivity$FragmentActivity11`（manifest[504]）。
- 【声明】activity `com.kwad.sdk.api.proxy.app.BaseFragmentActivity$FragmentActivity12`（manifest[505]）。
- 【声明】activity `com.kwad.sdk.api.proxy.app.BaseFragmentActivity$FragmentActivity13`（manifest[506]）。
- 【声明】activity `com.kwad.sdk.api.proxy.app.BaseFragmentActivity$FragmentActivity14`（manifest[507]）。
- 【声明】activity `com.kwad.sdk.api.proxy.app.BaseFragmentActivity$FragmentActivity15`（manifest[508]）。
- 【声明】activity `com.kwad.sdk.api.proxy.app.BaseFragmentActivity$FragmentActivity16`（manifest[509]）。
- 【声明】activity `com.kwad.sdk.api.proxy.app.BaseFragmentActivity$FragmentActivity17`（manifest[510]）。
- 【声明】activity `com.kwad.sdk.api.proxy.app.BaseFragmentActivity$FragmentActivity18`（manifest[511]）。
- 【声明】activity `com.kwad.sdk.api.proxy.app.BaseFragmentActivity$FragmentActivitySingleTop1`（manifest[512]）。
- 【声明】activity `com.kwad.sdk.api.proxy.app.BaseFragmentActivity$FragmentActivitySingleTop2`（manifest[513]）。
- 【声明】activity `com.kwad.sdk.api.proxy.app.BaseFragmentActivity$FragmentActivitySingleTop3`（manifest[514]）。
- 【声明】activity `com.kwad.sdk.api.proxy.app.BaseFragmentActivity$FragmentActivitySingleInstance1`（manifest[515]）。
- 【声明】activity `com.kwad.sdk.api.proxy.app.BaseFragmentActivity$FragmentActivitySingleInstance2`（manifest[516]）。
- 【声明】activity `com.kwad.sdk.api.proxy.app.BaseFragmentActivity$FragmentActivitySingleInstance3`（manifest[517]）。
- 【声明】activity `com.kwad.auth.login.KwadRouteHandlerActivity`（manifest[518]）。
- 【声明】activity `com.kwad.sdk.api.proxy.app.BaseFragmentActivity$DeveloperConfigActivity`（manifest[520]）。
- 【声明】activity `com.kwad.sdk.api.proxy.app.BaseFragmentActivity$LandscapeFragmentActivity`（manifest[521]）。
- 【声明】activity `com.kwad.sdk.api.proxy.app.BaseFragmentActivity$LandscapeFragmentActivitySingleTop1`（manifest[522]）。
- 【声明】activity `com.kwad.sdk.api.proxy.app.BaseFragmentActivity$LandscapeFragmentActivitySingleTop2`（manifest[523]）。
- 【声明】activity `com.kwad.sdk.api.proxy.app.BaseFragmentActivity$LandscapeFragmentActivitySingleTask1`（manifest[524]）。
- 【声明】activity `com.kwad.sdk.api.proxy.app.BaseFragmentActivity$LandscapeFragmentActivitySingleTask2`（manifest[525]）。
- 【声明】activity `com.kwad.sdk.api.proxy.app.BaseFragmentActivity$LandscapeFragmentActivitySingleInstance1`（manifest[526]）。
- 【声明】activity `com.kwad.sdk.api.proxy.app.BaseFragmentActivity$LandscapeFragmentActivitySingleInstance2`（manifest[527]）。
- 【声明】activity `com.kwad.sdk.api.proxy.app.WebPageActivity`（manifest[528]）。
- 【声明】activity `com.kwad.sdk.api.proxy.app.PayResultActivity`（manifest[529]）。
- 【声明】service `com.kwad.sdk.api.proxy.app.FileDownloadService$SharedMainProcessService`（manifest[530]）。
- 【声明】service `com.kwad.sdk.api.proxy.app.FileDownloadService$SeparateProcessService`（manifest[531]）。
- 【声明】service `com.kwad.sdk.api.proxy.app.DownloadService`（manifest[532]）。
- 【声明】service `com.kwad.sdk.api.proxy.app.ServiceProxyRemote`（manifest[533]）。
- 【声明】provider `com.kwad.sdk.api.proxy.app.AdSdkFileProvider`（manifest[534]）。
- 【声明】activity `com.meishu.sdk.activity.SdkInterstitialActivity`（manifest[536]）。
- 【声明】activity `com.meishu.sdk.activity.MeishuDownloadDetailActivity`（manifest[537]）。
- 【声明】activity `com.meishu.sdk.activity.MeishuOpenDeepLinkActivity`（manifest[538]）。
- 【声明】activity `com.meishu.sdk.activity.MeishuRewardVideoPlayerActivity`（manifest[539]）。
- 【声明】activity `com.meishu.sdk.activity.MeishuRewardH5Activity`（manifest[540]）。
- 【声明】activity `com.meishu.sdk.activity.MeishuWebviewActivity`（manifest[541]）。
- 【声明】activity `com.meishu.sdk.meishu_ad.view.DownLoadDialogActivity`（manifest[542]）。
- 【声明】activity `com.meishu.sdk.activity.MeishuAlertDialogActivity`（manifest[543]）。
- 【声明】activity `com.meishu.sdk.activity.MeishuDetailActivity`（manifest[544]）。
- 【声明】provider `com.meishu.sdk.core.service.MsLifeCycleContent`（manifest[545]）。
- 【声明】activity `com.insightvision.openadsdk.view.web.WebViewActivity`（manifest[546]）。
- 【声明】activity `com.insightvision.openadsdk.download.DownloadAppActivity`（manifest[552]）。
- 【声明】activity `com.insightvision.openadsdk.template.rendering.reward.activity.RewardAdActivity`（manifest[557]）。
- 【声明】activity `com.insightvision.openadsdk.template.rendering.table.activity.TableScreenActivity`（manifest[558]）。
- 【声明】provider `com.insightvision.openadsdk.FanTiApkProvider`（manifest[559]）。
- 【声明】activity `com.ss.android.downloadlib.activity.TTDelegateActivity`（manifest[562]）。
- 【声明】activity `com.ss.android.socialbase.appdownloader.view.DownloadHandleNotificationActivity`（manifest[565]）。
- 【声明】activity `com.ss.android.socialbase.appdownloader.view.DownloadTaskDeleteActivity`（manifest[567]）。
- 【声明】provider `com.luck.picture.lib.basic.PictureFileProvider`（manifest[653]）。
- 【声明】service `com.luck.picture.lib.service.ForegroundService`（manifest[655]）。
- 【声明】activity `com.luck.picture.lib.basic.PictureSelectorSupporterActivity`（manifest[656]）。
- 【声明】activity `com.luck.picture.lib.basic.PictureSelectorTransparentActivity`（manifest[657]）。
- 【声明】activity `com.yalantis.ucrop.UCropActivity`（manifest[658]）。
- 【声明】activity `com.yalantis.ucrop.UCropMultipleActivity`（manifest[659]）。
- 【声明】provider `androidx.startup.InitializationProvider`（manifest[660]）。
- 【声明】service `androidx.room.MultiInstanceInvalidationService`（manifest[664]）。
- 【声明】provider `com.squareup.picasso.PicassoProvider`（manifest[667]）。
- 【声明】service `com.igexin.sdk.OppoPushService`（manifest[670]）。
- 【声明】service `com.igexin.sdk.OppoAppPushService`（manifest[673]）。
- 【声明】receiver `androidx.profileinstaller.ProfileInstallReceiver`（manifest[678]）。
- 【声明】activity `com.alipay.sdk.app.H5PayActivity`（manifest[687]）。
- 【声明】activity `com.alipay.sdk.app.H5AuthActivity`（manifest[688]）。
- 【声明】activity `com.alipay.sdk.app.PayResultActivity`（manifest[689]）。
- 【声明】activity `com.alipay.sdk.app.AlipayResultActivity`（manifest[690]）。
- 【声明】activity `com.alipay.sdk.app.H5OpenAuthActivity`（manifest[691]）。
- 【声明】activity `com.alipay.sdk.app.APayEntranceActivity`（manifest[692]）。
- 【声明】activity `com.mlethe.library.imagepreview.GPreviewActivity`（manifest[694]）。
- 【声明】activity `com.mlethe.library.imagepreview.GPVideoPlayerActivity`（manifest[695]）。
- 【声明】service `com.igexin.sdk.PushService`（manifest[699]）。
- 【声明】service `com.igexin.sdk.GTIntentService`（manifest[700]）。
- 【声明】service `com.igexin.sdk.GService`（manifest[701]）。
- 【声明】activity `com.igexin.sdk.GetuiActivity`（manifest[704]）。
- 【声明】activity `com.igexin.sdk.PopupActivity`（manifest[707]）。
- 【声明】service `com.getui.gtc.GtcService`（manifest[710]）。
- 【声明】provider `com.getui.gtc.base.GtcProvider`（manifest[713]）。
- 【声明】service `com.igexin.sdk.HmsPushMessageService`（manifest[715]）。
- 【声明】receiver `com.huawei.hms.support.api.push.PushMsgReceiver`（manifest[718]）。
- 【声明】receiver `com.huawei.hms.support.api.push.PushReceiver`（manifest[722]）。
- 【声明】service `com.huawei.hms.support.api.push.service.HmsMsgService`（manifest[726]）。
- 【声明】provider `com.huawei.hms.support.api.push.PushProvider`（manifest[730]）。
- 【声明】activity `com.huawei.hms.support.api.push.TransActivity`（manifest[733]）。
- 【声明】service `com.xiaomi.mipush.sdk.PushMessageHandler`（manifest[738]）。
- 【声明】service `com.xiaomi.mipush.sdk.MessageHandleService`（manifest[739]）。
- 【声明】receiver `com.igexin.sdk.MiuiPushReceiver`（manifest[740]）。
- 【声明】activity `com.xiaomi.mipush.sdk.NotificationClickedActivity`（manifest[747]）。
- 【声明】receiver `com.igexin.sdk.VivoPushMessageReceiver`（manifest[751]）。
- 【声明】service `com.vivo.push.sdk.service.CommandClientService`（manifest[755]）。
- 【声明】service `com.igexin.assist.control.stp.StpService`（manifest[758]）。
- 【声明】provider `cn.thinkingdata.analytics.persistence.storage.contentProvider.ThinkingDataContentProvider`（manifest[765]）。
- 【声明】provider `com.huawei.hms.aaid.InitProvider`（manifest[772]）。
- 【声明】activity `com.huawei.hms.activity.BridgeActivity`（manifest[823]）。
- 【声明】activity `com.huawei.hms.activity.EnableServiceActivity`（manifest[825]）。
- 【声明】provider `com.huawei.agconnect.core.provider.AGConnectInitializeProvider`（manifest[826]）。
- 【声明】service `com.huawei.agconnect.core.ServiceDiscovery`（manifest[827]）。
- 【声明】provider `com.secneo.apkwrapper.CP`（manifest[828]）。

## 尚不能由这些证据回答

- 自动记账支持哪些支付软件、识别规则和准确度；OCR 本地/云端实现；AI 模型与接口；账单同步与备份；导入导出格式及上限；会员权益与定价；打卡认证收费规则；账户数据加密；奖励提现条件。
- 组件是否在当前版本可到达，哪些属于遗留/实验/服务器开关；截图记账是否经过完整授权流程；具体分类/账本/预算/借贷算法。
- 因此本文件是组件与配置范围内的完整枚举，不应称为经过运行验证的完整产品能力。
