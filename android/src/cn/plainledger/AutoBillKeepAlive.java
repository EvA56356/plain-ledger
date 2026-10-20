package cn.plainledger;
import android.app.*;
import android.content.*;
import android.os.*;

/** Visible, user-enabled foreground operation; no timer or wake lock. */
public class AutoBillKeepAlive extends Service {
 public static void update(Context c){
  Intent i=new Intent(c,AutoBillKeepAlive.class);
  if(!c.getSharedPreferences("auto_bill",0).getBoolean("enabled",false)){c.stopService(i);return;}
  try{c.startForegroundService(i);}catch(Exception e){AutoBillDrafts.status(c,"后台常驻未启动，请打开简账并检查系统后台运行限制");}
 }
 @Override public int onStartCommand(Intent intent,int flags,int id){
  if(!getSharedPreferences("auto_bill",0).getBoolean("enabled",false)){stopSelf();return START_NOT_STICKY;}
  NotificationManager manager=getSystemService(NotificationManager.class);
  manager.createNotificationChannel(new NotificationChannel("auto_bill_running","自动记账运行状态",NotificationManager.IMPORTANCE_LOW));
  Intent open=new Intent(this,MainActivity.class);
  PendingIntent tap=PendingIntent.getActivity(this,0,open,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
  int icon=getResources().getIdentifier("ic_launcher","drawable",getPackageName());
  Notification n=new Notification.Builder(this,"auto_bill_running").setSmallIcon(icon).setContentTitle("简账 · 自动记账运行中")
   .setContentText(AutoBillService.isConnected()?"支付后自动识别并提示核对 · 点击管理":"等待系统无障碍授权 · 点击完成首次配置").setContentIntent(tap).setOngoing(true).setOnlyAlertOnce(true).setCategory(Notification.CATEGORY_SERVICE).build();
  if(Build.VERSION.SDK_INT>=34)startForeground(3801,n,android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE);else startForeground(3801,n);
  return START_STICKY;
 }
 @Override public IBinder onBind(Intent i){return null;}
 public static class Restart extends BroadcastReceiver {public void onReceive(Context c,Intent i){if(Intent.ACTION_BOOT_COMPLETED.equals(i.getAction())||Intent.ACTION_MY_PACKAGE_REPLACED.equals(i.getAction()))update(c);}}
}
