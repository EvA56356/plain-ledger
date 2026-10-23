package cn.plainledger;

import android.app.*;
import android.content.*;
import android.os.Build;

/** A recoverable review entry point; notification text never exposes payment details. */
final class AutoBillReviewNotification {
 private static final int ID=3802;
 static void update(Context context,int count){
  try{
   NotificationManager manager=context.getSystemService(NotificationManager.class);
   if(count<=0){manager.cancel(ID);return;}
   if(Build.VERSION.SDK_INT>=33&&context.checkSelfPermission("android.permission.POST_NOTIFICATIONS")!=android.content.pm.PackageManager.PERMISSION_GRANTED)return;
   if(!manager.areNotificationsEnabled())return;
   String channel="auto_bill_review";
   manager.createNotificationChannel(new NotificationChannel(channel,"待核对账单",NotificationManager.IMPORTANCE_DEFAULT));
   Intent open=new Intent(context,MainActivity.class).setAction(context.getPackageName()+".AUTO_BILL").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_SINGLE_TOP|Intent.FLAG_ACTIVITY_CLEAR_TOP);
   PendingIntent tap=PendingIntent.getActivity(context,3802,open,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
   int icon=context.getResources().getIdentifier("ic_launcher","drawable",context.getPackageName());
   Notification notification=new Notification.Builder(context,channel).setSmallIcon(icon).setContentTitle("简账 · 有账单待核对")
    .setContentText("待核对 "+count+" 笔 · 点击打开简账").setVisibility(Notification.VISIBILITY_PRIVATE)
    .setContentIntent(tap).setOnlyAlertOnce(true).setAutoCancel(false).setCategory(Notification.CATEGORY_REMINDER).build();
   manager.notify(ID,notification);
  }catch(RuntimeException ignored){/* Permission or provider failures never prevent saving a draft. */}
 }
}
