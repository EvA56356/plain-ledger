package cn.plainledger;
import android.app.Notification;
import android.service.notification.*;
import android.os.Bundle;

/** Optional notification listener: only explicitly supported payment apps and recent arrivals. */
public class PaymentNotifications extends NotificationListenerService {
 public static String source(String pkg){return "com.tencent.mm".equals(pkg)?"微信":"com.eg.android.AlipayGphone".equals(pkg)?"支付宝":("com.sankuai.meituan".equals(pkg)||"com.sankuai.meituan.takeoutnew".equals(pkg))?"美团":null;}
 @Override public void onNotificationPosted(StatusBarNotification sbn){
  android.content.SharedPreferences p=getSharedPreferences("auto_bill",0);
  if(!p.getBoolean("enabled",false)||!p.getBoolean("notifications",false))return;
  String origin=source(sbn.getPackageName());if(origin==null||System.currentTimeMillis()-sbn.getPostTime()>300000)return;
  Bundle e=sbn.getNotification().extras;if(e==null)return;StringBuilder text=new StringBuilder();
  for(String key:new String[]{Notification.EXTRA_TITLE,Notification.EXTRA_TEXT,Notification.EXTRA_BIG_TEXT}){CharSequence t=e.getCharSequence(key);if(t!=null&&text.length()<10000)text.append(t).append('\n');}
  long amount=PaymentParser.parseNotification(text.toString());if(amount==0){AutoBillDrafts.status(this,"收到支付应用通知，但未找到明确的支出金额");return;}
  AutoBillService.notification(this,amount,origin);
 }
}
