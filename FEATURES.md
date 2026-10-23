# 喵喵记账 5.1.6 功能清单（静态逆向）

## 范围与可信度

对象：`喵喵记账_5.1.6_APKPure.apk`，包名 `com.youqi.miaomiao`，版本 5.1.6 / 516；SHA-256 `e1fb3f4433a1494f26bba8b8e95feeb15adad77875d2aeccd4ebd637bb09777f`。

本清单综合 Manifest、`decoded/res/values/strings.xml` 的全部资源项、`decoded/res/layout/`、`decoded/assets/cats.json`、`decoded/assets/billcats.json`。列出的资源键和组件声明是直接观察；从它们组合出的用户流程是静态推断。没有安装运行目标，没有验证后台接口、到账、识别准确度或当前会员策略。组件的全限定类名及 manifest 数组位置见 [COMPONENTS.md](COMPONENTS.md)。下表 Activity 使用唯一短类名；布局名相对于 `decoded/res/layout/`；字符串键均相对于 `decoded/res/values/strings.xml`。

证据类型：**S** 字符串资源；**M** Manifest 声明；**L** 布局结构或名称；**J** 内置 JSON。仅 M/L 的条目置信度低于多种证据一致的条目。加固导致业务 DEX 尚未恢复，不能把本清单理解为执行验证结果。

## 1. 手动记账、分类、账本

| 用户功能与细项 | 字符串键或其他资源证据 | Activity / 类型 |
|---|---|---|
| 手动记录收入、支出，保存账单 | `manual_bookkeeping`, `income`, `expend`, `bill_complete` | BillEditActivity / S+M |
| 选择金额、类别、账本、账户、时间、备注 | `please_select_category_hint`, `please_select_bill_book_hint`, `amount_must_be_greater_than_hint`；activity_bill_edit.xml | BillEditActivity / S+L+M |
| 长备注、账单图片、拍照/相册上传、图片预览 | `add_remark_and_img`, `camera_storage_permission_desc`, `vip_bill_image_hint` | BillDetailActivity、ImagePreviewActivity / S+M |
| 按天/分钟设置记账时间精度 | `accuracy_of_accounting_time`, `accuracy_of_accounting_time_hint` | IndividualizationActivity / S+M，具体控件归属待运行确认 |
| 账单列表、详情、编辑、表格视图 | `bill_list`, `bill_detail`；activity_bill_table.xml | BillActivity、BillDetailActivity、BillEditActivity、BillTableActivity / S+L+M |
| 多选账单、批量编辑、选中总收支 | `string_edit_bills_num`, `string_edit_bills_money`；dialog_edit_bills.xml | BillActivity / S+L+M |
| 搜索账单、结果笔数与收支汇总 | `string_search_bills_num`, `string_search_bills_money`；activity_bill_search.xml | BillSearchActivity / S+L+M |
| 分类管理、自定义分类 | `create_cate_by_name`, `unified_entry_cate_other`；billcats.json 的 isCustom | CateManagerAct、CustomCateAct / S+J+M |
| 默认支出侧34项、收入10项，含借入/借出/还款/收款 | billcats.json；完整类别枚举见组件清单 | CateManagerAct / J+M；appear 解释为支出侧属于语义推断 |
| 多账本创建、编辑、详情、切换、封面 | `bill_list_book_guide_one`, `vip_bill_book_hint`；item_bill_book_cover.xml | BillBookListActivity、BillBookEditActivity、BillBookDetailActivity / S+L+M |
| 示例账本与会员解锁提示 | `vip_bill_book_sample_hint` | BillBookListActivity / S+M |

## 2. 自动记账、截图、语音与图片

| 用户功能与细项 | 字符串键或布局 | Activity / 类型 |
|---|---|---|
| 支付宝、微信等支付成功后识别账单，弹窗供用户确认保存 | `auto_bill_open_success_msg`, `identify_a_bill_hint` | AutoBillDataActivity、BillIdentifiedActivity / S+M；并非证明无确认自动写账 |
| 自动记账开关、暂停、重启、状态与试用余量 | `auto_bill_closed`, `auto_bill_paused`, `auto_bill_restart`, `remaining_experience_transactions_number` | AutoBillSettingActivity / S+M |
| 识别场景设置与配置更新 | `identify_scenarios`, `auto_bill_scene_update_success`, `auto_bill_restart_hint` | SceneIdentifiedActivity / S+M |
| 无障碍、悬浮窗/后台弹窗、通知授权引导 | `auto_bill_guide_second_title`, `suspended_window_permissions_desc`, `notification_permissions_desc` | AutoBillSettingActivity、PermissionActivity / S+M |
| 华为/荣耀/OPPO/一加/VIVO/小米后台保活与系统差异帮助 | `allow_app_auto_start_path_huawei`, `allow_full_background_running_path_honor`, `color_os_15_desc`, `lock_app_to_background` | AutoBillSettingActivity / S+M；是帮助文本，不能当兼容性保证 |
| 截屏记账：桌面萌宠悬浮按钮或状态栏磁贴触发 | `ocr_bill_pet_title`, `ocr_bill_capture_title`, `capture_bill_open_success_msg` | OcrBillSettingActivity、OcrBillScreenCaptureActivity、NewBillIdentifiedActivity / S+M |
| 截屏授权、识别进度、未识别有效数据及关闭权限提醒 | `ocr_invalid_data`, `screen_capture_notifiy_info`, `ocr_bill_setting_step1_desc` | OcrBillScreenCaptureActivity；ScreenCaptureService、ScreenCaptureTileService / S+M |
| Android 15 截屏范围与字体/系统配置帮助 | `ocr_bill_setting_capture_float_msg` | OcrBillSettingActivity / S+M；记录目标提示，不建议照搬其中降低系统防护的操作 |
| 长按发送语音、录音用于语音记账 | `long_press_to_send_voice`, `record_audio_permission_desc`, `vip_voice_bill_hint`；dialog_voice_bill.xml | BillEditActivity 或 ChatActivity 的入口归属未证实 / S+L |
| 宠物图片记账权益名称 | `vip_privilege_image_bill` | ChatActivity / S+M；具体交互与识别实现待证实 |

## 3. AI猫咪聊天记账

| 用户功能与细项 | 字符串键 / 布局 | Activity / 类型 |
|---|---|---|
| 自然语言聊天与聊天记账 | `pet_chat_and_accounting`, `ai_compliance_hint`, `chat_msg_hint` | ChatActivity / S+M |
| 查看聊天生成的历史账单 | `chat_bill`；activity_ai_history_bill.xml | AiHistoryBillActivity / S+L+M |
| 设置猫咪姓名、性格、与用户关系、对用户称呼 | `cat_name`, `cat_personality`, `cat_relationship`, `cat_call_you` | AiCatSettingActivity、AiSettingActivity / S+M |
| 情景补充：动作、表情语气、心理活动 | `scenario_supplement_box`, `scenario_supplement_box_desc` | AiSettingActivity / S+M |
| 清除当前聊天内容，提示不可恢复且已记账账单不受影响 | `clear_current_chat_content_text` | AiSettingActivity 或 ChatActivity / S+M |
| 默认记账入口切换为聊天或手动 | `default_accounting_page_settings_hint`, `pet_chat_and_accounting_setting_hint` | IndividualizationActivity / S+M |
| AI使用须知、AI生成标记、内容拒答/举报线索 | `ai_use_notice_hint`, `message_is_generated_by_ai`, `message_violation_hint`, `report` | ChatActivity / S+M；举报具体对象待确认 |
| 聊天互动与喵饼回馈 | `chat_interact_reward_hint`；dialog_chat_interact_reward.xml | ChatActivity / S+L+M |

## 4. 场景记账、亲友与人情往来

| 用户功能与细项 | 字符串键 / 布局 | Activity / 类型 |
|---|---|---|
| 我的情景、通用情景、情景管理 | `string_my_scene`, `general_scenario`, `string_scene_using` | SceneManagerActivity / S+M |
| 情景分类增删管理、选择情景、自定义类别提示 | `bill_edit_scene_hint`；activity_scene_add_cate.xml、dialog_scene_choose.xml | SceneAddCateActivity、SceneCateManagerActivity / S+L+M |
| 情景下分类账单统计 | activity_cate_statistics.xml；Manifest | SceneCateBillStatisticsActivity / M+L |
| 亲友档案、姓名、电话、关系、自定义关系、备注、编辑 | `string_add_contact_name`, `string_add_contact_phone`, `string_relation_custom`, `string_edit_contact` | AddContactActivity、ContactProfileActivity、ContactDetailActivity / S+M |
| 往来账单、往来笔数与人情统计 | `string_contact_bill_title`, `string_friend_record_bill_num`；fragment_human_feelings_statistics.xml | SceneContactStatisticsActivity、ContactDetailActivity / S+L+M |
| 删除亲友时可选只删档案，或连相关数据一起清空 | `string_delete_contact_only`, `string_delete_contact_with_bill` | ContactDetailActivity / S+M |

## 5. 资产、借贷、多币种

| 用户功能与细项 | 字符串键 / 布局 | Activity / 类型 |
|---|---|---|
| 资产账户类型选择、新建、详情、余额与变动明细 | `assets_account`, `account_balance`, `deposit_details` | FundAccountTypeActivity、FundAccountAddActivity、FundAccountDetailActivity / S+M |
| 借贷账户/借款添加线索 | billcats.json 的借入、借出、还款、收款 | FundAccountAddBorrowActivity / J+M；不能推定自动利息/分期算法 |
| 账户间转账、转账详情 | `transfer`, `transfer_out` | FundAccountTransferActivity、TransferDetailsActivity / S+M |
| 币种选择、汇率调整、换算器 | `select_currency`, `base_currency_and_exchange_rate`, `exchange_rate_conversion`, `exchange_rate_update` | MyCurrencyActivity、OptionRateActivity、ExchangeRateUpdateActivity、ConverterActivity / S+M |

## 6. 预算、统计、报表与小票

| 用户功能与细项 | 字符串键 / 布局 | Activity / 类型 |
|---|---|---|
| 总预算设置、预算详情与相关账单 | `guide_budget`；activity_budget_bill_list.xml | BudgetActivity、AddBudgetActivity、BudgetBillListActivity / S+L+M |
| 分类预算 | `vip_cate_budget_hint` | CateBudgetActivity / S+M |
| 分类统计、图表、明细、自定义统计 | `vip_privilege_custom_statistics`；header_statistics_pie.xml、item_statistics_category.xml、item_statistics_detailed.xml | StatisticsActivity、CateStatisticsActivity / S+L+M |
| 日历视图、按日期查看收支、自定义查看周期 | `title_bill_calendar`, `string_calendar_custom_vip`；header_bill_calendar.xml | BillActivity / S+L+M |
| 月起始日设置 | `mine_mouth_origin` | SettingActivity / S+M；设置入口推断 |
| 每月专属报告、上月总收入/总支出、日均支出、与前月比较 | `string_month_report_introduce`, `string_report_total_income`, `string_report_total_exp`, `string_report_daily_exp_title`, `string_compared_to_last` | MonthlyReportActivity / S+M |
| 下拉查看当日收支小票、预览、分享、皮肤 | `ticket_hint_one`, `ticket_hint_two`, `ticket_skin`；dialog_share_today_bill.xml | TicketDialogActivity、TicketHintDialogActivity、TicketPreviewDialogActivity、TicketSkinListActivity / S+L+M |

## 7. 定时账单与提醒

| 用户功能与细项 | 字符串键 / 布局 | Activity / 类型 |
|---|---|---|
| 创建定时账单、列表、详情 | `time_bookkeeping`, `vip_timed_bill_hint` | TimedBillActivity、CreateTimedBillActivity、TimedBillDetailActivity / S+M |
| 周期、起止/结束日期、永久手动结束、金额、类别、账本、入账/出账账户、备注 | `create_timed_bill_time`, `create_timed_bill_end_time_hint`, `create_timed_bill_enter_fund`, `create_timed_bill_out_fund` | CreateTimedBillActivity、CateChooseActivity / S+M；具体周期选项未恢复 |
| 固定时间记账提醒、专属提醒、通知权限帮助 | `bookkeeping_reminder_hint`, `vip_privilege_remind`, `notifications_permission_desc` | RemindActivity、BillRemindSettingActivity / S+M |

## 8. 导入、导出、回收站

| 用户功能与细项 | 字符串键 / 布局 | Activity / 类型 |
|---|---|---|
| 选择账单来源平台，导入 Excel/CSV 文件，查看教程 | `string_import_bill_source_hint`, `string_import_bill_support_text` | ImportBillActivity、ImportBillHelpActivity / S+M；资源未给出完整支持平台名单 |
| 文件大小不超过1MB、单次不超过3000条，超出需分批 | `string_import_bill_support_text` | ImportBillActivity / S+M；静态提示约束 |
| 通过 QQ/微信文件“导入到喵喵记账”打开 | `string_import_way_text` | ImportBillActivity / S+M |
| 预览、选择导入记录、修改类型/账户/金额/备注/时间 | `string_import_bill_cate`, `string_import_bill_fund`, `string_import_bill_money`, `string_import_bill_remark`, `string_import_bill_time`；activity_import_bill_detail.xml | ImportBillDetailActivity / S+L+M |
| 缺失分类可创建或统一归入其它；同步修改/仅当前账单；重复账单提示 | `import_cate_tips`, `create_cate_by_name`, `unified_entry_cate_other`, `string_modify_all`, `string_modify_only`, `duplicate_bill` | ImportBillDetailActivity / S+M；自动去重行为未证实 |
| 导出时选择账本和起止日期、分享文件 | `export_bill_book`, `export_start_time`, `export_end_time`, `share_file` | ExportBillActivity / S+M；导出具体格式未证实 |
| 已删除明细、账本、资产账户的回收站；恢复和彻底删除 | `recycle_bill`, `recycle_bill_book`, `recycle_fund_account`, `recovery_confirmation`, `delete_completely` | RecycleBinActivity、RecycleBillActivity、RecycleBillBookActivity、RecycleFundAccountActivity / S+M |

## 9. 攒钱计划与支付宝小荷包

| 用户功能与细项 | 字符串键 / 布局 | Activity / 类型 |
|---|---|---|
| 新建、修改、查看储蓄计划和进度；选择计划类型/封面/方式 | `string_new_save_money_plan`, `edit_plan`；dialog_save_plan_type.xml、dialog_save_plan_cover.xml、dialog_save_plan_way.xml | SaveMoneyActivity、CreateSavePlanActivity、ModifySavePlanActivity、PlanDetailActivity / S+L+M |
| 手动攒、自动攒、仅打卡记录三类线索 | `auto_fail_hint`, `string_save_way_only_title`, `string_save_way_only_content` | PlanDetailActivity / S+M |
| 攒入打卡、备注、重复确认、自动攒失败后手动补打卡 | `save_money_punch_clock_hint`, `string_save_this_time_remark_hint`, `string_save_failed_reason` | SaveClockActivityDialog、PlanDetailActivity / S+M |
| 单次攒入不超过2万元，金额不足可能影响后续目标 | `string_save_plan_money_much`, `string_average_money`, `string_suggest_min_money` | CreateSavePlanActivity、PlanDetailActivity / S+M；文案约束 |
| 小荷包账户跳转、转出、消费、账户变动记录 | `common_problem_content`, `plan_detail_help_content`, `change_record` | OauthActivity、ChangeRecordActivity、PlanDetailActivity / S+M |
| 区分“已攒入金额”与“账户余额”：外部转入/转出/消费不变更计划进度 | `plan_detail_help_content` | PlanDetailActivity / S+M；不能当资金安全审计结论 |
| 结束/恢复计划、暂停后续自动攒、历史缺卡需手动补 | `end_automatic_plan_hint`, `recovery_plan_expired_hint`, `recovery_plan_not_expired_hint` | PlanDetailActivity / S+M |
| 删除计划不可恢复，提示删除关联接收账户与历史攒钱明细 | `delete_plan_hint`, `delete_plan_xhb_hint` | PlanDetailActivity / S+M |
| 打卡/完成计划分享、攒钱功能升级说明 | dialog_save_clock_share.xml、dialog_save_finish_share.xml；Manifest | SaveMoneyUpgradeActivity、SaveClockActivityDialog / L+M |

## 10. 365天挑战（与普通攒钱打卡分开）

| 用户功能与细项 | 字符串键 / 布局 | Activity / 类型 |
|---|---|---|
| 挑战规则、购买门票档位、选择支付方式、参与记录 | `clock_365_buy_ticket_type`, `clock_365_need_look_complete_rules`, `participation_record` | ClockingActivity / S+M |
| 每日完成任务后打卡、打卡日历、断签与补签卡 | `complete_task_fail_hint`, `complete_today_check_in`, `clocking_day_has_repair`, `fill_sign_number` | ClockingActivity / S+M |
| 同设备当日不能为其他账号重复打卡、境外IP风险提示、北京时间 | `device_has_been_clocked_in_hint`, `clocking_365_ip_hint`, `clocking_365_time_hint` | ClockingActivity / S+M |
| 多阶段身份验证、到期认证、超时/补签截止失败提示 | `clocking_not_auth_hint`, `clocking_complete_auth_hint`, `clocking_complete_fail_hint` | ClockingAuthActivity、ClockingAuthV3Activity / S+M |
| 实名、手机号、身份证、银行卡、开户行、脸部认证、资料提交 | `string_please_input_name`, `string_participant_id_card`, `string_credit_card_numbers`, `string_opening_bank`, `string_triggering_facial_recognition_failed` | ClockingAuthActivity、ClockingAuthV3Activity / S+M |
| 审核、失败原因、打款结果、完成分享、放弃挑战 | `clock_365_payment_failed`, `string_give_up_challenge`；dialog_clocking_complete_share.xml | ClockingAuditActivity、ClockingAuditV3Activity、ClockingPaymentSuccessActivity、ClockingPaymentSuccessV3Activity / S+L+M |
| 两套普通/V3界面同时打包 | activity_clocking_auth.xml、activity_clocking_auth_v3.xml | 上述 V3 与非 V3 组件 / L+M；不能证明均对当前用户开放 |

## 11. 购物与物品清单

| 用户功能与细项 | 字符串键 / 布局 | Activity / 类型 |
|---|---|---|
| 创建清单、添加商品、商品列表、已购列表 | `add_goods`；Manifest | CreateShoppingListActivity、ShoppingListActivity、GoodsListActivity、GoodsListPurchasedActivity / S+M |
| 修改名称、移动至其他清单、清空/删除清单 | `update_shopping_name_title`, `string_move_shopping_list`, `clear_list`, `delete_list` | ShoppingListActivity / S+M |
| 按创建时间、数量、价格、优先级、自定义拖动排序 | `by_creation_time`, `by_goods_number`, `by_price`, `by_priority`, `by_custom_sort_hint` | GoodsListActivity / S+M |
| 生产日期、到期日期、设置过期日线索 | `manufacture_date`, `due_date`, `shopping_share_setting_expire_time` | GoodsListActivity / S+M；具体提醒机制未证实 |
| 分享购物清单 | `share_list`, `shopping_share_app_name` | ShoppingListActivity / S+M |

## 12. 桌面小组件与快捷入口

| 用户功能与细项 | 字符串键 / 布局 | Activity / 类型 |
|---|---|---|
| 组件广场、我的组件、大小规格、添加指导、自定义 | `mine_widget`, `my_widget`, `custom_widget`；activity_widget_mine.xml | WidgetMainActivity、WidgetMineActivity、AppWidgetSmallActivity、AppWidgetMiddleActivity、AppWidgetLargeActivity / S+L+M |
| 收支总览、月预算、预算沙漏、余额、结余、总支出 | app_widget_income_and_expense.xml、app_widget_budget_middle.xml、app_widget_large_budget_hourglass_pink.xml、app_widget_balance_middle.xml、app_widget_surplus_large.xml、app_widget_total_expense.xml | HourglassWidgetSettingActivity、WidgetBillSmallSettingActivity / L+M；各布局对应的产品名称属推断 |
| 小票、日历、折线图、中/大图、贴纸、自定义标签 | app_widget_middle_print_ticket.xml、app_widget_vip_calendar.xml、app_widget_line_chart_mid.xml、app_widget_large_sticker.xml；dialog_widget_tags.xml | TicketWidgetSettingActivity、CalendarWidgetSettingActivity、LineChartMidWidgetSettingActivity、LineChartLargeWidgetSettingActivity、TagsWidgetSettingActivity / L+M |
| 攒钱/存钱罐组件及外观配置 | app_widget_save_money.xml、app_widget_save_money_pot.xml；item_rlv_widget_setting_color.xml、item_rlv_widget_setting_pot.xml | SaveMoneyWidgetSettingActivity、SaveMoneyPotWidgetSettingActivity / L+M |
| 点击分类桌面快捷记账 | `add_quick_bookkeeping_widget_hint`；app_widget_quick_bookkeeping.xml | QuickBookkeepingWidgetSettingActivity、AppWidgetClickActivity / S+L+M |
| 微信/支付宝扫一扫、付款码快捷入口 | `app_widget_extend_alipay_payment_code`, `app_widget_extend_alipay_scan`, `app_widget_extend_wechat_payment_code`, `app_widget_extend_wechat_scan` | WidgetMainActivity；app_widget_middle_tripartite_quick_payment.xml / S+L+M |
| 组件视频教程与激励视频线索 | activity_webview_widget_video.xml、dialog_widget_video.xml | WebViewWidgetVideoActivity / L+M；使用条件待运行确认 |

## 13. 猫咪养成、喵城与装扮

| 用户功能与细项 | 字符串键 / JSON / 布局 | Activity / 类型 |
|---|---|---|
| 喵城/猫咪家园、点击猫咪互动、道具长按操作 | `main_cat_guide`, `main_opt_guide` | MainActivity、CatHomeActivity / S+M |
| 猫咪图鉴、介绍、分类绑定、勋章与解锁展示 | cats.json 的 handBookImg、background、categoryKey、medalImg、medalGreyImg | CatAndIllustratedActivity、FieldGuildActivity / J+M |
| 48条内置猫咪配置，互动台词、好感阈值、喜好食物/礼物 | cats.json 的 tickling、favorabilityUpgrade、favoriteFoodId、favoriteGiftIds、hateGiftIds | FeedActivity、CatHomeActivity / J+M；实际获得方式与算法未证实 |
| 喂食、梳毛及当日次数提示 | `already_fed_cat_today_hint`, `already_combed_cat_today_hint` | FeedActivity / S+M |
| 猫咪移动到指定场景 | `string_cat_list_hint`, `string_cat_list_title` | CatHomeActivity、ThemeActivity / S+M |
| 衣柜/装扮等级/价格/拥有状态、场景主题选择 | cats.json 的 wardrobes；`kitty_theme`, `vip_privilege_exclusive_look` | ThemeActivity、ThemeAllActivity / J+S+M |
| 道具商城、购买、仓库与喵饼虚拟货币 | `kitty_prop`, `kitty_coin`, `buy` | ShopActivity、WarehouseActivity / S+M |
| 成就、相册及详情、喵咪邮局 | `mine_mail_box`；Manifest | AchievementActivity、AlbumActivity、AlbumDetailActivity、MailboxActivity / S+M，具体成就条件未知 |
| 采风与明信片、历史收藏线索 | `what_is_wind`, `what_postcards_do`, `how_to_look`, `answer_wind_count` | CollectingWindActivity、MailboxActivity / S+M |
| 采风每日5次：前3次免费，后2次喵饼购买或激励视频 | `answer_wind_count` | CollectingWindActivity / S+M；打包帮助文本 |
| 七日签到赠场景礼包，招财猫激励奖励、抽奖次数上限 | `sign_desc`, `ad_cat_gold`, `ad_cat_time`, `daily_lottery_limit_has_been_reached_hint` | MainActivity、CatHomeActivity / S+M；与福利现金签到分开 |
| 春节活动与活动记录 | Manifest | NewYearActivity、NewYearRecordActivity / M；规则/是否仍在运营未知 |

## 14. 塔罗小游戏

| 用户功能与细项 | 字符串键 / 布局 | Activity / 类型 |
|---|---|---|
| 问题选择/自定义问题入口 | `other_option_hint`；item_tarot_problem_adapter.xml | TarotGameActivity / S+L+M，字符串与此功能关联属推断 |
| 洗牌、抽牌、翻牌动画与卡名展示 | activity_tarot_game.xml 的 TarotShuffleLayout、TarotDrawCardView、cardFlop、tvCardName；layout_tarot_shuffle.xml | TarotGameActivity / L+M |
| 三张牌、牌名、正/逆位展示区域、问题与解答 | tarot_interpret_result.xml 的 image1/2/3、image1Name、image1Plaques、questionTv、interpret | TarotInterpretResultActivity / L+M；示例文本不等于实际牌面 |
| 继续占卜、AI生成内容提示 | tarot_interpret_result.xml 中硬编码文本与 ai_tips_tv | TarotInterpretResultActivity / L+M；模型/算法/次数/收费未知 |

## 15. 福利中心、金币、现金与提现（集成业务）

| 用户功能与细项 | 字符串键 / 布局 | Activity / 类型 |
|---|---|---|
| 福利首页、现金/金币余额、累计奖励、收益说明、活动规则 | `wz_cash_balance`, `wz_accumulated_rewards`, `wz_income_statement`, `wz_activity_rules_title` | WzHomeActivity、WzRulesAtivity / S+M |
| 新人现金红包、首次提现任务 | `wz_home_task_user_title`, `wz_home_task_firstwithdraw_title` | WzHomeActivity / S+M；“最高68元”只属宣传资源，不能承诺实得 |
| 每日签到、连续7天神秘奖励、断签重置、日历提醒 | `wz_sign_in_consecutively`, `wz_sign_in_interrupted`, `wz_calendar_reminder_title` | WzHomeActivity / S+M |
| 每隔1小时打开App领取奖励、宝箱、红包雨 | `wz_once_every_hour`, `wz_treasure_chest_rewards`, `wz_home_task_rain_title` | WzHomeActivity / S+M |
| 激励视频、翻卡、金币/现金翻倍、补领 | `wz_home_task_flip_card_title`, `wz_continue_to_double`, `wz_watch_video_make_up_once` | WzHomeActivity / S+M |
| 浏览指定时长得金币、下载并打开应用任务、任务进度 | `wz_home_task_getgold_subtitle`, `wz_home_task_getgold_subtitle_down`, `wz_click_view_browsing_time` | WzHomeActivity / S+M；实际下载/打开验证未观察 |
| 逛商城奖励，跳京东/美团/淘宝等外部App任务 | `wz_shop_mall_browsing_task`, `wz_public_click_jd_title`, `wz_public_click_mt_title`, `wz_public_click_tb_title` | WzHomeActivity / S+M |
| 福利桌面组件、从桌面点击完成任务、组件奖励/翻倍 | `wz_home_task_widget_title`, `wz_home_task_widget_subtitle_2`, `wz_widget_accept_award`；wz_app_widget.xml | WzWidgetSettingActivity；WzAppWidgetProvider / S+L+M |
| 打开通知获取奖励提醒、奖励预约、余额/兑换提醒 | `wz_home_task_notification_title`, `wz_todays_appointmen`, `wz_balance_reminder`, `wz_exchange_reminder` | WzHomeActivity、PermissionWZActivity / S+M |
| 每日凌晨按动态汇率把通过审核金币兑换现金 | `wz_every_day`, `wz_income_statement_details` | WzHomeActivity / S+M；汇率不固定，具体到账未知 |
| 绑定收款账号/微信、更换微信、占用检测提示 | `wz_bind_receivable_account`, `wz_bind_other_wx`, `wz_bind_wx_failure_occupied` | WzExtractMoneyActivity / S+M |
| 提现金额档位、每日次数、提现套餐前置申请提示 | `wz_extract_money_can_select`, `wz_the_meeting_is_now_over`, `wz_apply_prepayment_amount_gearshift` | WzExtractMoneyActivity / S+M；金额和次数为动态占位，不据资源猜值 |
| 现金/金币收益记录、提现详情、渠道/账号/状态/时间/失败原因 | `wz_revenue_list`, `wz_gold_coin_income`, `wz_cash_income`, `wz_withdrawal_status`, `wz_reason_for_failure` | WzRevenueListActivity、WzExtractMoneyActivity / S+M |
| 实名收款要求、奖励审核最长7工作日、失败款8天后退回说明 | `wz_income_statement_details` | WzRulesAtivity、WzExtractMoneyActivity / S+M；均为打包规则文本，未验证履行 |
| 反作弊规则线索：多账号、模拟器、分身、群控等可能取消奖励 | `wz_income_statement_details` | WzRulesAtivity / S+M；只证明有规则，不证明检测实现 |

## 16. 外卖补贴（福利子模块）

| 用户功能与细项 | 字符串键 / 布局 | Activity / 类型 |
|---|---|---|
| 到饭点领券/下单补贴、外部平台跳转 | `wz_home_task_eat_subsidy_title`, `wz_home_task_eat_subsidy_subtitle`, `wz_meituan`, `wz_tabao` | WzTakeoutSubsidyActivity / S+M |
| 早餐/午餐/晚餐/夜宵时段、下时段领取、未到时间提示 | `wz_breakfast`, `wz_lunch`, `wz_dinner`, `wz_late_night`, `wz_not_subsidy_time`, `wz_next_time_slot` | WzTakeoutSubsidyActivity / S+M |
| 补贴补领、视频补领、次日预约领取 | `wz_watch_video_make_up_once`, `wz_one_click_collection`；wz_dialog_subsidy_replacement.xml | WzTakeoutSubsidyActivity / S+L+M；具体金额/门槛/佣金规则未知 |

## 17. 账号、设置、隐私与支持

| 用户功能与细项 | 字符串键 / 布局 | Activity / 类型 |
|---|---|---|
| 游客、账号、手机号验证码、微信、QQ登录 | `login_tourist`, `login_account`, `login_by_tel`, `login_get_code`, `login_by_wx`, `login_by_qq` | LoginActivity、PhoneLoginActivity、WXEntryActivity / S+M |
| 游客绑定账号、切换账号、手机号绑定、名称与头像修改 | `bind_hint`, `change_account`, `avatar`, `user_info` | PhoneBindActivity、UpdateNameActivity、UserInfoAct / S+M |
| 多设备/跨平台同步宣传；QQ登录不支持安卓/iOS互通的提示 | `bind_hint`, `login_other_hint` | LoginActivity / S+M；没有验证同步协议、冲突策略、备份恢复 |
| 我的页面显示记账天数/笔数、会员、关于、版本 | `mine_bill_counts`, `mine_bill_days`, `about_us_version` | MineActivity、AboutActivity / S+M |
| 首页可设喵城或日历、默认记账页、音效/背景乐/震动 | `home_setting_hint`, `default_accounting_page_settings_hint`, `background_music`, `sound_effect`, `vibration_feeling` | IndividualizationActivity、SettingActivity / S+M |
| 密码锁；指纹验证候选 | `password_lock`, `vip_password_hint`, `biometricprompt_verify_fingerprint` | PasswordActivity、PasswordInputActivity / S+M；指纹项可能来自依赖，接入未证实 |
| 系统权限管理及用途说明、相机/照片/录音/通知/辅助功能/悬浮窗 | `system_permission`, `permission_usage_instructions`, `record_audio_permission_desc`, `notifications_permission_desc` | SystemPermissionActivity、PermissionActivity / S+M |
| 隐私政策、用户协议、撤回同意并退出登录、账号注销 | `agree_content`, `revoke_of_consent_content`, `logout`, `cancellation_hint_content` | RevokePolicyActivity、SettingActivity / S+M；关联小荷包计划存在时有注销限制提示 |
| 广告个性化开关，关闭不减少数量的说明 | `ad_personal_recommend`, `ad_personal_recommend_hint` | AdPersonalRecommendActivity / S+M |
| 帮助/使用教程/视频、客服/用户反馈（支持图片）、版本更新 | `using_tutorials`, `video_tutorials`, `user_feedback`, `about_wechat_service`, `right_now_update` | HelpActivity、CourseActivity、IntroduceActivity、WebViewActivity、VideoPlayerActivity / S+M |
| 微信登录/分享与支付回调、支付宝支付/授权集成 | `pay_alipay`, `pay_wechat`；WXEntryActivity/WXPayEntryActivity 与 Alipay SDK 组件 | PayActivity、OauthActivity / S+M；无法推定当前商品价格 |
| 深链接启动、启动页与接收成功提示页 | Manifest、`app_name` | SplashActivity、SchemeStartActivity、ReceiveSuccessActivity / M；完整路由行为待代码确认 |

## 18. 会员、试用与额度限制汇总

所有限制均来自打包提示，不能证明当前服务器仍执行相同政策；旧/新提示可能共存。固定数值仅在资源明确给出时列出。

| 功能 | 静态限制/权益说明 | 字符串证据键 |
|---|---|---|
| 多账本 | 免费账本数量有限，具体数未知；VIP称可任意数量；示例账本需VIP | `vip_bill_book_hint`, `vip_bill_book_sample_hint` |
| 账单图片 | 非VIP至多给3笔账单加图片 | `vip_bill_image_hint` |
| 日历、自定义日期周期 | VIP | `vip_calendar_hint`, `string_calendar_custom_vip` |
| 分类预算 | VIP | `vip_cate_budget_hint` |
| 数据导出、密码锁、定时账单 | VIP | `vip_export_hint`, `vip_password_hint`, `vip_timed_bill_hint` |
| 回收站恢复 | VIP任意恢复的提示；免费恢复额度未知 | `vip_recycle_hint` |
| 情景记账 | 非VIP有动态试用次数，次数占位未赋值 | `vip_scene_hint` |
| 语音记账 | VIP无限使用的提示 | `vip_voice_bill_hint` |
| AI聊天 | 非SVIP动态试用次数；耗尽需SVIP；部分人格设置选项需SVIP | `chat_experience_x_times_hint`, `vip_chat_free_attempts_exhausted_hint`, `svip_chat_unlock_all_options_hint` |
| 自动记账 | 旧提示：非会员有限试用、VIP耗喵饼、SVIP免费无限；新提示：非SVIP有限试用、SVIP免费无限。不能选其中一套作当前定论 | `auto_bill_count_exhaust_hint`, `auto_bill_count_exhaust_hint_new`, `auto_bill_unit_price`, `auto_bill_coin_insufficient_hint` |
| 其他标为权益的能力 | 资产账户、自定义首页/统计、清单、装扮、专属组件、攒钱、专属提醒、情景记账、小票换肤、宠物图片记账、免广告；只有权益标签时，具体免费额度未知 | `vip_privilege_assets`, `vip_privilege_custom_home`, `vip_privilege_custom_statistics`, `vip_privilege_detailed_list`, `vip_privilege_exclusive_look`, `vip_privilege_proprietary_widget`, `vip_privilege_save_money`, `vip_privilege_remind`, `vip_privilege_scene_accounting`, `vip_privilege_ticket_skin`, `vip_privilege_image_bill`, `vip_privilege_ad` |
| 会员形态 | 永久VIP、永久SVIP、一次付费永久权益提示；价格、是否订阅、退款政策未获证实 | `forever_vip`, `forever_svip`, `forever_vip_hint` |
| 导入 | Excel/CSV、≤1MB、≤3000条/次；支持平台完整名单未知 | `string_import_bill_support_text` |
| 攒钱 | 单笔≤2万元；部分计划会员专属；与支付宝关联计划影响注销 | `string_save_plan_money_much`, `common_problem_content`, `cancellation_hint_content` |
| 采风 | 每日5次，前3免费，后2喵饼/视频 | `answer_wind_count` |
| 福利提现 | 动态每日次数与套餐档位；实名账号；动态金币汇率；规则文本称审核≤7工作日、失败款8天退回 | `wz_the_meeting_is_now_over`, `wz_apply_prepayment_amount_gearshift`, `wz_income_statement_details` |

## 19. 广告和技术基础设施的处理

APK 包含穿山甲/聚合广告、快手、华为广告、推广下载与直播插件，以及个推和厂商推送、图片选择/裁剪、Room、Picasso、统计和加固组件。它们的组件全量列表见配套 inventory。SDK 自带音视频选择、下载管理、广告 WebView 等通用能力不自动计为喵喵记账面向用户的独立功能。福利中心与塔罗具有独立业务 Activity 和布局，已单独列出。

## 20. 尚待动态或代码验证

- 实际入口、账号/地区/版本开关、遗留界面和当前活动可用性。
- 自动记账场景完整列表、OCR/语音/AI的本地或云端实现、准确率、数据出网范围。
- 导入支持平台清单、去重规则、导出格式、字段和限额；预算统计与账户借贷算法。
- 同步、离线行为、备份恢复、存储加密、服务端接口与权限校验。
- 当前会员价格、额度、免广告范围；福利收益、提现与365挑战是否真实可完成及当前规则。

**覆盖结论：**这是当前 APK 静态资源及声明范围内尽可能全面的功能清单；功能条目均附追溯依据，尚未恢复的加固业务代码及未运行的服务端行为明确保留为未知。
### 证据索引与覆盖核验

| 证据 | 文件/标识 | 覆盖与限制 |
|---|---|---|
| REA Android证据 | `ev_c489921daddd90accbd61d3542e942066d21809c062c4cb7ec9ee1f7b03aeaf0`；android-evidence.json、android-jadx-evidence.json | APK身份与Android静态分析；REA/JADX metadata类计数为7，可能包含synthetic类，不能等同直接DEX类定义数 |
| Manifest原始解码 | manifest.json；apktool/AndroidManifest.xml | 320 activity、1 activity-alias、37 service、14 receiver、30 provider。两种解码的业务组件计数一致；uses-sdk在Apktool中存入yaml，造成该tag计数差异 |
| 默认字符串全集 | apktool/res/values/strings.xml | 1762项，包含业务和SDK资源；本清单所引字符串key已逐项校验存在 |
| 布局全集 | apktool/res/layout/ | 1307个布局；名称只支持候选功能，具体控件结构可提供更强静态证据 |
| 内置配置 | cats.json；billcats.json | 48条猫咪、34条appear分类、10条income分类；不等于用户实时配置 |
| 包与DEX摘要 | summary.json；classes.dex.classes.txt；jadx.log | 直接DEX定义5个SecNeo包装类，JADX CLI导出5个；没有业务实现恢复，不将其与REA metadata的7混淆 |
| 全量组件逐项映射 | COMPONENTS.md | 原始171个自有前缀/集成业务候选，含2个自有广告页与1个快手SDK别名；别名已归入SDK说明。全部402个组件均有清单记录（广告交叉列举不重复计产品能力） |

**Activity模块覆盖声明：**已覆盖全部自有前缀业务Activity，以及`com.ioio.rewardhub`福利和`cn.aiyo.fne.tarot`塔罗模块。其细分功能按上文1—18节整理；没有足够文案的入口/活动保留M级候选。完整类名、manifest索引与后台服务/接收器均可在配套inventory追溯。自有广告页面、SDK别名和第三方框架组件归入第19节，不作为额外记账功能扩张。本声明是“声明组件枚举覆盖”，不是“全部运行路径或服务端功能验证”。