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
  System.out.println("20 payment recognition checks passed");
 }
}
