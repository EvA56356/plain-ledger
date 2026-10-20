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
  check("支付成功\n￥12.34\n优惠￥1.00",0);
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
  System.out.println("28 payment recognition checks passed");
 }
}
