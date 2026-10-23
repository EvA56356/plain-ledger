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
 private static final Pattern AUXILIARY=Pattern.compile("^\\s*(?:优惠(?:金额)?|已优惠|随机立减|立减|原价|奖励|红包(?:抵扣)?|(?:商家|店铺|平台|通用|满减|支付)?券(?:抵扣)?|抵扣金额)(?:\\s*[:：]?\\s*[-−－]?\\s*[¥￥]?\\s*[-−－]?[0-9].*|\\s*[:：]?\\s*)$");
 private static final Pattern AMOUNT_ROW=Pattern.compile("^\\s*[-−－]?\\s*[¥￥]?\\s*[-−－]?[0-9]{1,9}(?:,[0-9]{3})*(?:\\.[0-9]{1,2})?\\s*元?\\s*$");
 public static long parse(String text){return inspect(text).amount;}
 public static Result inspectOcr(String text){
  if(text==null)return new Result(0,"页面文字不可用");
  // Observed Chinese-font OCR confusion; still require the receipt structure below.
  text=text.replaceAll("(支付|付款|交易)戌功","$1成功");
  if(!Pattern.compile("付款方式|支付方式|返回商家|回首页|当前状态[\\s\\S]{0,40}支付成功|(?m)^\\s*完成\\s*$").matcher(text).find())
   return new Result(0,"未确认支付完成页，不生成账单");
  if(Pattern.compile("按住说话|切换到按住说话|发送给朋友").matcher(text).find())
   return new Result(0,"当前为聊天或分享页面，不生成账单");
  // The bundled Chinese model can read the large yuan glyph as an asterisk.
  // Restrict correction to a standalone amount row on an already-confirmed receipt.
  text=text.replaceAll("(?m)^\\s*\\*\\s*([0-9]{1,9}\\.[0-9]{2})\\s*$","¥$1\n");
  return inspect(text);
 }
 public static long parseNotification(String text){
  if(text==null||text.length()>10000||EXCLUDE.matcher(text).find())return 0;
  long paid=parse(text);if(paid>0)return paid;
  if(!Pattern.compile("支出|消费|向[^\\n]{1,80}付款").matcher(text).find())return 0;
  return choose(amounts(MONEY,text)).amount;
 }
 public static Result inspect(String text){
  if(text==null||text.length()>65000)return new Result(0,"页面文字不可用");StringBuilder b=new StringBuilder();
  for(char c:text.toCharArray())b.append(c>='０'&&c<='９'?(char)(c-'０'+'0'):c=='．'?'.':c=='\u00a0'||c=='\u3000'?' ':c);
  text=b.toString().replaceAll("[\\t ]+"," ");
  if(Pattern.compile("按住说话|切换到按住说话|发送给朋友").matcher(text).find())return new Result(0,"当前为聊天或分享页面，不生成账单");
  if(!SUCCESS.matcher(text).find())return new Result(0,"已读取页面，等待支付成功标记");
  if(EXCLUDE.matcher(text).find())return new Result(0,"退款、转账、收款或未完成支付页面不识别");
  String candidates=withoutAuxiliaryAmounts(text);
  Set<Long> amounts=amounts(PAID,candidates);if(!amounts.isEmpty())return choose(amounts);
  // A success heading with its own amount is distinct from reward/product prices below it.
  amounts=amounts(HEADING,candidates);if(!amounts.isEmpty())return choose(amounts);
  // Receipt layout: the payment section ends at the payment-method label.
  int method=candidates.indexOf("付款方式");if(method<0)method=candidates.indexOf("支付方式");
  if(method>=0){String receipt=candidates.substring(0,method);if(SUCCESS.matcher(receipt).find()){amounts=amounts(MONEY,receipt);amounts.addAll(amounts(ISOLATED,receipt));if(!amounts.isEmpty())return choose(amounts);}}
  // A negative standalone amount is an expense only on a completed bill-detail page.
  if(Pattern.compile("当前状态\\s*支付成功").matcher(text).find()&&text.contains("支付时间")&&text.contains("交易单号")){
   amounts=amounts(DEBIT,candidates);if(!amounts.isEmpty())return choose(amounts);
  }
  amounts=amounts(MONEY,candidates);return choose(amounts.isEmpty()?amounts(ISOLATED,candidates):amounts);
 }
 private static String withoutAuxiliaryAmounts(String text){
  StringBuilder kept=new StringBuilder();boolean previousLabel=false;
  for(String row:text.split("\\n",-1)){
   if(previousLabel&&AMOUNT_ROW.matcher(row).matches()){previousLabel=false;continue;}
   previousLabel=false;
   if(AUXILIARY.matcher(row).matches()&&!PAID.matcher(row).find()){previousLabel=!Pattern.compile("[0-9]").matcher(row).find();continue;}
   kept.append(row).append('\n');
  }
  return kept.toString();
 }
 private static Set<Long> amounts(Pattern p,String text){Set<Long> values=new HashSet<>();Matcher m=p.matcher(text);while(m.find())for(int i=1;i<=m.groupCount();i++)if(m.group(i)!=null)try{long n=new BigDecimal(m.group(i).replace(",","")).movePointRight(2).longValueExact();if(n>0&&n<=99999999999L)values.add(n);}catch(Exception ignored){}return values;}
 private static Result choose(Set<Long> values){return values.size()==1?new Result(values.iterator().next(),"已识别付款金额"):new Result(0,values.isEmpty()?"支付成功，但未找到可确认的金额":"支付成功，但存在多个金额，未自动选择");}
}
