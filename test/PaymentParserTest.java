import cn.plainledger.PaymentParser;
public class PaymentParserTest {
 static void check(String page,long expected){if(PaymentParser.parse(page)!=expected)throw new AssertionError(page);}
 public static void main(String[] args){
  check("支付成功\n￥12.34",1234);check("付款成功\n实付 9.90元",990);check("支付成功\n￥12.34\n支付金额12.34",1234);
  check("确认付款\n￥12.34",0);check("支付失败\n￥12.34",0);check("退款成功\n支付成功\n￥12.34",0);check("转账\n支付成功\n￥12.34",0);
  check("支付成功\n￥12.34\n￥15.00",0);check("收款成功\n￥12.34",0);check("支付成功\n￥0.00",0);check("支付成功\n￥12.345",0);
  System.out.println("11 payment recognition checks passed");
 }
}
