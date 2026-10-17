package cn.plainledger;
import java.math.BigDecimal;
import java.util.*;
import java.util.regex.*;
/** Conservative local parser. Ambiguous pages are intentionally ignored. */
public final class PaymentParser {
 public static long parse(String text){
  if(text==null||text.length()>20000)return 0;
  if(!(text.contains("支付成功")||text.contains("付款成功")))return 0;
  if(Pattern.compile("退款|转账|收款成功|收钱到账|支付失败|付款失败|等待支付|待支付").matcher(text).find())return 0;
  Pattern p=Pattern.compile("(?:[¥￥]\\s*|(?:实付|实付款|支付金额|付款金额)\\s*[:：]?\\s*[¥￥]?\\s*)([0-9]{1,9}(?:,[0-9]{3})*(?:\\.[0-9]{1,2})?)(?![0-9.])|([0-9]{1,9}(?:\\.[0-9]{1,2})?)\\s*元");
  Matcher m=p.matcher(text);Set<Long> amounts=new HashSet<>();
  while(m.find())try{long n=new BigDecimal((m.group(1)!=null?m.group(1):m.group(2)).replace(",","")).movePointRight(2).longValueExact();if(n>0&&n<=99999999999L)amounts.add(n);}catch(Exception ignored){}
  return amounts.size()==1?amounts.iterator().next():0;
 }
}
