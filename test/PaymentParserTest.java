import cn.plainledger.PaymentParser;
public class PaymentParserTest {
 static void check(String page,long expected){if(PaymentParser.parse(page)!=expected)throw new AssertionError(page);}
 public static void main(String[] args){
  check("支付成功\n￥12.34",1234);check("付款成功\n实付 9.90元",990);check("支付成功\n￥12.34\n支付金额12.34",1234);
  check("确认付款\n￥12.34",0);check("支付失败\n￥12.34",0);check("退款成功\n支付成功\n￥12.34",0);check("转账\n支付成功\n￥12.34",0);
  check("支付成功\n￥12.34\n￥15.00",0);check("收款成功\n￥12.34",0);check("支付成功\n￥0.00",0);check("支付成功\n￥12.345",0);
  check("支付成功\n实付金额：12.34\n原价￥15.00\n红包￥2.66",1234);
  check("支付成功\n12.34\n申请退款",1234);
  check("交易成功\n实际支付\n￥１２．３４",1234);
  check("支付成功\n实付款\n12.34元\n优惠1.00元",1234);
  check("支付成功\n退款金额￥12.34",0);
  check("支付成功\n实付12.34\n付款金额10.00",0);
  check("支付成功\n￥12.34\n优惠￥1.00",1234);
  check("支付成功\n12345678",0);
  if(!PaymentParser.inspect("支付成功").status.contains("未找到"))throw new AssertionError("Missing diagnostic");
  // Synthetic examples reproduce the four layouts without real merchants, card numbers or order IDs.
  check("支付成功\n¥50.00\n示例餐厅 ¥50.00\n付款方式\n测试账户\n通勤券0.6元\n红包3元\n优惠1元\n外卖券8元",5000);
  check("支付成功\n示例面馆\n¥19.90\n返回商家",1990);
  check("支付成功 ¥21.30\n付款方式\n测试账户\n本单使用信用卡支付成功\n奖励27.24元\n商品¥3.33 原价¥5.90",2130);
  check("示例面馆\n-19.90\n当前状态\n支付成功\n支付时间\n2026年1月1日 12:00:00\n商品 示例餐品\n支付方式 测试账户\n交易单号 DEMO-ORDER",1990);
  check("支付成功 ¥21.30\n支付成功 ¥22.30",0);
  check("-19.90\n支付成功",0);
  check("示例商户\n-19.90\n当前状态 支付失败\n支付时间\n交易单号 DEMO",0);
  check("支付成功\n¥50.00\n¥60.00\n付款方式\n测试账户\n优惠3元",0);
  if(PaymentParser.inspectOcr("支付成功\n*19.90\n返回商家").amount!=1990)throw new AssertionError("OCR currency correction");
  if(PaymentParser.inspectOcr("聊天\n支付成功 19.90元\n按住说话").amount!=0)throw new AssertionError("Chat rejected");
  if(PaymentParser.inspectOcr("¥19.90\n付款方式").amount!=0)throw new AssertionError("Incomplete payment rejected");
  if(PaymentParser.parseNotification("微信支付\n向示例商家付款19.90元")!=1990)throw new AssertionError("Notification expense");
  if(PaymentParser.parseNotification("你有一笔 12.34元 的支出")!=1234)throw new AssertionError("Notification expense amount");
  if(PaymentParser.parseNotification("收款成功 12.34元")!=0)throw new AssertionError("Income excluded");
  if(PaymentParser.parseNotification("退款成功 12.34元")!=0)throw new AssertionError("Refund excluded");
  if(PaymentParser.parseNotification("消费12.34元，余额50.00元")!=0)throw new AssertionError("Ambiguous notification");
  if(PaymentParser.inspectOcr("支付戌功\n¥12.34\n付款方式 测试账户").amount!=1234)throw new AssertionError("OCR success glyph correction");
  check("支付成功\n¥19.90\n随机立减 ¥0.10\n红包2.00元\n付款方式\n测试账户",1990);
  check("支付成功\n¥19.90\n原价¥25.00\n奖励¥1.00\n完成",1990);
  check("支付成功\n¥19.90\n优惠\n1.00\n付款方式",1990);
  check("支付成功\n¥19.90\n¥25.00\n优惠¥5.10",0);
  check("支付成功\n优惠¥1.00\n付款方式",0);
  check("支付成功\n¥19.90\n实付金额18.90\n优惠¥1.00",1890);
  check("支付失败\n¥19.90\n优惠¥1.00\n完成",0);
  if(PaymentParser.inspectOcr("支付成功\n¥19.90\n完成").amount!=1990)throw new AssertionError("Completion button receipt");
  if(PaymentParser.inspectOcr("支付失败\n¥19.90\n完成").amount!=0)throw new AssertionError("Completion is not proof of payment");
  if(PaymentParser.inspectOcr("朋友：支付成功¥19.90\n完成\n按住说话").amount!=0)throw new AssertionError("Completion does not allow chat");
  check("支付成功\n优惠¥1.00，实付金额19.90\n商品金额¥20.90\n付款方式\n测试账户",1990);
  check("支付成功\n19.90\n付款方式\n测试账户\n商品¥3.33",1990);
  check("支付成功\n¥19.90\n优惠 -¥1.00\n付款方式",1990);
  check("示例商户\n-19.90\n当前状态 支付成功\n优惠\n-1.00\n支付时间\n交易单号 DEMO",1990);
  if(PaymentParser.inspectOcr("支付成功\n优惠¥1.00，实付金额19.90\n商品金额¥20.90\n付款方式\n测试账户").amount!=1990)throw new AssertionError("Auxiliary row must not erase paid label");
  if(PaymentParser.inspectOcr("支付成功\n19.90\n付款方式\n测试账户\n商品¥3.33").amount!=1990)throw new AssertionError("Receipt amount must outrank later product price");
  System.out.println("53 payment recognition checks passed");
 }
}
