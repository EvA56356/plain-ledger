package cn.plainledger;
import android.accessibilityservice.AccessibilityService;
import android.view.accessibility.*;
import android.content.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import android.graphics.Color;
import org.json.JSONObject;
import java.util.*;
public class AutoBillService extends AccessibilityService {
 private static volatile boolean connected;
 public static boolean isConnected(){return connected;}
 private final Handler handler=new Handler(Looper.getMainLooper());
 private View overlay;private String pendingPackage="",eventText="";private long eventTextAt;private boolean queued;private int retries;
 private final Map<String,Long> recent=new HashMap<>();
 private final Runnable scan=()->{queued=false;scanPage();};
 private SharedPreferences prefs(){return getSharedPreferences("auto_bill",0);}
 private boolean paymentApp(String p){return "com.eg.android.AlipayGphone".equals(p)||"com.tencent.mm".equals(p);}
 private final SharedPreferences.OnSharedPreferenceChangeListener changes=(p,key)->{
  if("enabled".equals(key)&&!p.getBoolean("enabled",false)){handler.removeCallbacks(scan);queued=false;dismiss();status("应用内识别已暂停");}
  if("draft_opened".equals(key))dismiss();
  if("test_request".equals(key))handler.postDelayed(()->{if(prefs().getBoolean("enabled",false))recognize(1234,"测试",true);},3000);
 };
 protected void onServiceConnected(){super.onServiceConnected();connected=true;prefs().registerOnSharedPreferenceChangeListener(changes);status(prefs().getBoolean("enabled",false)?"服务已连接，等待支付宝或微信页面":"系统服务已连接，但应用内识别已暂停");}
 private void status(String text){if(!text.equals(prefs().getString("status","")))prefs().edit().putString("status",text).putLong("status_at",System.currentTimeMillis()).apply();}
 public void onAccessibilityEvent(AccessibilityEvent event){
  if(!prefs().getBoolean("enabled",false))return;String pkg=String.valueOf(event.getPackageName());if(!paymentApp(pkg))return;
  if(!pkg.equals(pendingPackage)){handler.removeCallbacks(scan);queued=false;eventText="";pendingPackage=pkg;}
  AccessibilityNodeInfo source=event.getSource();if(source!=null)try{StringBuilder out=new StringBuilder();collect(source,out,0,new int[]{0});eventText=out.toString();eventTextAt=SystemClock.elapsedRealtime();}catch(Exception ignored){}finally{source.recycle();}
  retries=0;status("已收到"+(pkg.equals("com.tencent.mm")?"微信":"支付宝")+"页面事件，正在读取");schedule(180);
 }
 // Do not restart this delay on every event: animated payment pages can starve a trailing debounce.
 private void schedule(long delay){if(!queued){queued=true;handler.postDelayed(scan,delay);}}
 private void scanPage(){
  if(!prefs().getBoolean("enabled",false)||!paymentApp(pendingPackage))return;StringBuilder text=new StringBuilder();
  try{
   AccessibilityNodeInfo root=getRootInActiveWindow();if(root!=null)try{if(pendingPackage.equals(String.valueOf(root.getPackageName())))collect(root,text,0,new int[]{0});}finally{root.recycle();}
   for(AccessibilityWindowInfo w:getWindows())try{AccessibilityNodeInfo n=w.getRoot();if(n!=null)try{if(pendingPackage.equals(String.valueOf(n.getPackageName())))collect(n,text,0,new int[]{0});}finally{n.recycle();}}finally{w.recycle();}
   if(SystemClock.elapsedRealtime()-eventTextAt<2000&&text.length()<30000)text.append('\n').append(eventText);
  }catch(Exception e){status("页面读取失败，请重新连接无障碍服务");return;}
  if(text.length()==0){status("已收到页面事件，但系统未提供可读文字");if(retries++<4)schedule(450);return;}
  PaymentParser.Result result=PaymentParser.inspect(text.toString());status(result.status);
  if(result.amount==0){if(retries++<4)schedule(450);return;}
  handler.removeCallbacks(scan);queued=false;long now=SystemClock.elapsedRealtime();String key=pendingPackage+":"+result.amount;Long prev=recent.get(key);
  if(prev!=null&&now-prev<90000){status("同一来源和金额在 90 秒内已提示，已忽略重复页面");return;}
  recent.entrySet().removeIf(e->now-e.getValue()>90000);if(recognize(result.amount,pendingPackage.equals("com.tencent.mm")?"微信":"支付宝",false))recent.put(key,now);
 }
 private void collect(AccessibilityNodeInfo n,StringBuilder out,int depth,int[] count){
  if(depth>28||count[0]++>700||out.length()>30000||n.isPassword())return;
  if(n.isVisibleToUser()){CharSequence t=n.getText(),d=n.getContentDescription();if(t!=null)out.append(t).append('\n');if(d!=null&&!d.equals(t))out.append(d).append('\n');}
  for(int i=0;i<n.getChildCount();i++){AccessibilityNodeInfo c=n.getChild(i);if(c!=null)try{collect(c,out,depth+1,count);}finally{c.recycle();}}
 }
 private boolean recognize(long amount,String source,boolean test){
  try{
   String pending=prefs().getString("draft","null");if(!"null".equals(pending)){JSONObject old=new JSONObject(pending);if(System.currentTimeMillis()-old.optLong("created")<300000){show(old.optLong("amount"),old.optString("source"),old.optBoolean("test"));status("已有待核对账单，请先处理，未覆盖原草稿");openDraft();return false;}}
   JSONObject draft=new JSONObject();draft.put("id",UUID.randomUUID().toString());draft.put("amount",amount);draft.put("source",source);draft.put("test",test);draft.put("created",System.currentTimeMillis());
   if(!prefs().edit().putString("draft",draft.toString()).commit()){status("识别成功，但草稿保存失败");return false;}
   status("已识别，正在打开核对窗口；未弹出时点击悬浮提示");show(amount,source,test);openDraft();handler.postDelayed(()->{if(!"null".equals(prefs().getString("draft","null")))status("核对窗口尚未打开或已有编辑中记录；请点悬浮提示，小米请检查后台弹出权限");},2000);return true;
  }catch(Exception e){status("识别成功，但核对窗口准备失败");return false;}
 }
 private void openDraft(){try{startActivity(new Intent(this,MainActivity.class).setAction(getPackageName()+".AUTO_BILL").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_SINGLE_TOP|Intent.FLAG_ACTIVITY_CLEAR_TOP));}catch(Exception e){status("后台打开被系统限制，请点击悬浮提示并检查应用后台弹出设置");}}
 private void show(long amount,String source,boolean test){
  dismiss();LinearLayout box=new LinearLayout(this);box.setOrientation(1);int pad=(int)(20*getResources().getDisplayMetrics().density);box.setPadding(pad,pad,pad,pad);
  android.graphics.drawable.GradientDrawable bg=new android.graphics.drawable.GradientDrawable();bg.setColor(MainActivity.uiColor(this,"paper",Color.WHITE));bg.setCornerRadius(MainActivity.uiRadius(this));box.setBackground(bg);box.setElevation(12);
  TextView title=new TextView(this);title.setText((test?"测试弹窗 · 虚构金额":"识别到"+source+"支出")+" ¥ "+String.format(java.util.Locale.ROOT,"%.2f",amount/100.0)+"\n核对后保存，尚未写入账本");title.setTextColor(MainActivity.uiColor(this,"ink",Color.rgb(33,78,67)));title.setTextSize(18);box.addView(title);
  Button confirm=new Button(this);confirm.setText("打开记账核对");confirm.setBackgroundTintList(android.content.res.ColorStateList.valueOf(MainActivity.uiColor(this,"primary",Color.rgb(33,78,67))));confirm.setTextColor(Color.WHITE);box.addView(confirm);
  Button cancel=new Button(this);cancel.setText("忽略这笔");cancel.setTextColor(MainActivity.uiColor(this,"primary",Color.rgb(33,78,67)));box.addView(cancel);
  confirm.setOnClickListener(v->openDraft());cancel.setOnClickListener(v->{prefs().edit().remove("draft").apply();dismiss();status("已忽略识别账单");});
  WindowManager.LayoutParams lp=new WindowManager.LayoutParams(-1,-2,WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,android.graphics.PixelFormat.TRANSLUCENT);lp.gravity=Gravity.BOTTOM;lp.y=pad*3;
  try{((WindowManager)getSystemService(WINDOW_SERVICE)).addView(box,lp);overlay=box;handler.postDelayed(()->{if(overlay==box)dismiss();},30000);}catch(Exception e){status("无法显示悬浮提示，草稿已保留，可打开简账核对");}
 }
 private void dismiss(){if(overlay!=null){try{((WindowManager)getSystemService(WINDOW_SERVICE)).removeView(overlay);}catch(Exception ignored){}overlay=null;}}
 public void onInterrupt(){dismiss();status("无障碍服务被中断");}
 public void onDestroy(){connected=false;prefs().unregisterOnSharedPreferenceChangeListener(changes);handler.removeCallbacksAndMessages(null);dismiss();super.onDestroy();}
}
