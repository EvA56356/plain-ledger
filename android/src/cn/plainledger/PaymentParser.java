package cn.plainledger;
import java.math.BigDecimal;
import java.util.*;
import java.util.regex.*;
/** Local recognition only. Page text is never retained. */
public final class PaymentParser {
 public static final class Result {public final long amount;public final String status;Result(long a,String s){amount=a;status=s;}}
 private static final String NUM="([0-9]{1,9}(?:,[0-9]{3})*(?:\\.[0-9]{1,2})?)(?![0-9.,])";
 private static final Pattern SUCCESS=Pattern.compile("支付成功|付款成功|交易成功");
 private static final Pattern EXCLUDE=Pattern.compile("退款成功|退款中|退款金额|退回金额|转账|收款成功|收钱到账|支付失败|付款失败|等待支付|待支付");
 private static final Pattern PAID=Pattern.compile("(?:实际支付(?:金额)?|实付(?:金额|款)?|支付金额|付款金额)\\s*[:：]?\\s*(?:人民币\\s*)?[¥￥]?\\s*"+NUM);
 private static final Pattern MONEY=Pattern.compile("[¥￥]\\s*"+NUM+"|"+NUM+"\\s*元");
 private static final Pattern HEADING=Pattern.compile("(?:支付成功|付款成功|交易成功)[ \t:：]*[¥￥][ \t]*"+NUM);
 private static final Pattern DEBIT=Pattern.compile("(?m)^\\s*[-−－]\\s*([0-9]{1,9}\\.[0-9]{2})\\s*$");
 private static final Pattern ISOLATED=Pattern.compile("(?m)^\\s*([0-9]{1,9}\\.[0-9]{2})\\s*$");
 public static long parse(String text){return inspect(text).amount;}
 public static Result inspect(String text){
  if(text==null||text.length()>65000)return new Result(0,"页面文字不可用");StringBuilder b=new StringBuilder();
  for(char c:text.toCharArray())b.append(c>='０'&&c<='９'?(char)(c-'０'+'0'):c=='．'?'.':c=='\u00a0'||c=='\u3000'?' ':c);
  text=b.toString().replaceAll("[\\t ]+"," ");
  if(!SUCCESS.matcher(text).find())return new Result(0,"已读取页面，等待支付成功标记");
  if(EXCLUDE.matcher(text).find())return new Result(0,"退款、转账、收款或未完成支付页面不识别");
  Set<Long> amounts=amounts(PAID,text);if(!amounts.isEmpty())return choose(amounts);
  // A success heading with its own amount is distinct from reward/product prices below it.
  amounts=amounts(HEADING,text);if(!amounts.isEmpty())return choose(amounts);
  // Receipt layout: the payment section ends at the payment-method label.
  int method=text.indexOf("付款方式");if(method<0)method=text.indexOf("支付方式");
  if(method>=0){String receipt=text.substring(0,method);if(SUCCESS.matcher(receipt).find()){amounts=amounts(MONEY,receipt);if(!amounts.isEmpty())return choose(amounts);}}
  // A negative standalone amount is an expense only on a completed bill-detail page.
  if(Pattern.compile("当前状态\\s*支付成功").matcher(text).find()&&text.contains("支付时间")&&text.contains("交易单号")){
   amounts=amounts(DEBIT,text);if(!amounts.isEmpty())return choose(amounts);
  }
  amounts=amounts(MONEY,text);return choose(amounts.isEmpty()?amounts(ISOLATED,text):amounts);
 }
 private static Set<Long> amounts(Pattern p,String text){Set<Long> values=new HashSet<>();Matcher m=p.matcher(text);while(m.find())for(int i=1;i<=m.groupCount();i++)if(m.group(i)!=null)try{long n=new BigDecimal(m.group(i).replace(",","")).movePointRight(2).longValueExact();if(n>0&&n<=99999999999L)values.add(n);}catch(Exception ignored){}return values;}
 private static Result choose(Set<Long> values){return values.size()==1?new Result(values.iterator().next(),"已识别付款金额"):new Result(0,values.isEmpty()?"支付成功，但未找到可确认的金额":"支付成功，但存在多个金额，未自动选择");}
}
