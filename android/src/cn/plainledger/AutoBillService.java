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
 private static volatile boolean connected;private static AutoBillService live;
 public static boolean isConnected(){return connected;}
 private final Handler handler=new Handler(Looper.getMainLooper());
 private View overlay;private String pendingPackage="",eventText="";private long eventTextAt;private boolean queued;private int retries;
 private String foregroundPackage="";private String pendingClass="";private boolean ocrBusy;private long lastOcr;private int ocrAttempts;private long eventSequence,lastImageHash,imageHashAt;private String imageHashPackage="";
 private int[] statistics=new int[5];private int roots,readErrors;private final Set<Integer> seenWindows=new HashSet<>();
 private final Runnable scan=()->{queued=false;scanPage();};
 private SharedPreferences prefs(){return getSharedPreferences("auto_bill",0);}
 private boolean paymentApp(String p){return "com.eg.android.AlipayGphone".equals(p)||"com.tencent.mm".equals(p)||"com.sankuai.meituan".equals(p)||"com.sankuai.meituan.takeoutnew".equals(p);}
 private final SharedPreferences.OnSharedPreferenceChangeListener changes=(p,key)->{
  if("enabled".equals(key))AutoBillKeepAlive.update(this);
  if("enabled".equals(key)&&!p.getBoolean("enabled",false)){handler.removeCallbacks(scan);queued=false;eventText="";dismiss();status("应用内识别已暂停");}
  if("draft_opened".equals(key))dismiss();if("ocr".equals(key)){if(p.getBoolean("ocr",false))LocalOcr.warmUp(this);else dismiss();}
  if("test_request".equals(key))handler.postDelayed(()->{if(prefs().getBoolean("enabled",false))recognize(1234,"测试",true);},3000);
 };
 protected void onServiceConnected(){super.onServiceConnected();android.accessibilityservice.AccessibilityServiceInfo configuration=getServiceInfo();configuration.packageNames=null;setServiceInfo(configuration);connected=true;live=this;AutoBillKeepAlive.update(this);if(prefs().getBoolean("ocr",false))LocalOcr.warmUp(this);prefs().registerOnSharedPreferenceChangeListener(changes);status(prefs().getBoolean("enabled",false)?"服务已连接，等待支付宝、微信或美团页面":"系统服务已连接，但应用内识别已暂停");}
 private void status(String text){if(!text.equals(prefs().getString("status","")))prefs().edit().putString("status",text).putLong("status_at",System.currentTimeMillis()).apply();}
 public void onAccessibilityEvent(AccessibilityEvent event){
  if(!prefs().getBoolean("enabled",false))return;String pkg=String.valueOf(event.getPackageName());
  // Observe only package transitions outside the allowlist, never read their page nodes/text.
  if(event.getEventType()==AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED)foregroundPackage=pkg;
  if(!paymentApp(pkg))return;
  if(!pkg.equals(pendingPackage)){handler.removeCallbacks(scan);queued=false;eventText="";pendingPackage=pkg;}
  StringBuilder out=new StringBuilder();
  // Some payment views expose event text but no readable source/root node.
  if(!event.isPassword()){for(CharSequence t:event.getText())if(t!=null&&out.length()<30000)out.append(t).append('\n');CharSequence d=event.getContentDescription();if(d!=null&&out.length()<30000)out.append(d).append('\n');}
  AccessibilityNodeInfo source=event.getSource();if(source!=null)try{collect(source,out,0,new int[5]);}catch(Exception ignored){}finally{source.recycle();}
  eventText=out.toString();eventTextAt=SystemClock.elapsedRealtime();
  if(event.getEventType()==AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED){String next=String.valueOf(event.getClassName());if(!next.equals(pendingClass)){pendingClass=next;ocrAttempts=0;}}
  eventSequence++;retries=0;status("已收到"+sourceName(pkg)+"页面事件，正在读取");schedule(180);
 }
 // Do not restart this delay on every event: animated payment pages can starve a trailing debounce.
 private void schedule(long delay){if(!queued){queued=true;handler.postDelayed(scan,delay);}}
 private void scanPage(){
  if(!prefs().getBoolean("enabled",false)||!paymentApp(pendingPackage))return;
  List<String> snapshots=new ArrayList<>();statistics=new int[5];roots=0;readErrors=0;seenWindows.clear();
  try{AccessibilityNodeInfo root=getRootInActiveWindow();if(root!=null)try{snapshot(root,snapshots);}finally{root.recycle();}}catch(Exception ignored){readErrors++;}
  try{for(AccessibilityWindowInfo w:getWindows())try{AccessibilityNodeInfo n=w.getRoot();if(n!=null)try{snapshot(n,snapshots);}finally{n.recycle();}}catch(Exception ignored){readErrors++;}finally{w.recycle();}}catch(Exception ignored){readErrors++;}
  if(SystemClock.elapsedRealtime()-eventTextAt<2000&&!eventText.trim().isEmpty())snapshots.add(eventText);
  saveDiagnostic();
  if(snapshots.isEmpty()){status(readErrors>0?"窗口读取发生异常，请复制诊断信息":roots==0?"收到事件，但未取得支付应用窗口；请复制诊断信息":"已取得页面节点，但没有可读文字；可使用本地 OCR");if(prefs().getBoolean("ocr",false))offerOcr();else if(retries++<3)schedule(450);return;}
  // Keep window boundaries: an unrelated background window must not contaminate a payment sheet.
  PaymentParser.Result result=null;Set<Long> values=new HashSet<>();
  for(String text:snapshots){PaymentParser.Result candidate=PaymentParser.inspect(text);if(candidate.amount>0){values.add(candidate.amount);result=candidate;}else if(result==null)result=candidate;}
  if(values.size()>1){status("不同窗口提供了不同金额，请手动核对，未自动选择");if(retries++<4)schedule(450);return;}
  status(result.status);if(result.amount==0){if(prefs().getBoolean("ocr",false))offerOcr();else if(retries++<3)schedule(450);return;}
  handler.removeCallbacks(scan);queued=false;recognize(result.amount,sourceName(pendingPackage),false);
 }
 private String sourceName(String pkg){return pkg.equals("com.tencent.mm")?"微信":pkg.equals("com.eg.android.AlipayGphone")?"支付宝":"美团";}
 private void snapshot(AccessibilityNodeInfo n,List<String> snapshots){if(!pendingPackage.equals(String.valueOf(n.getPackageName())))return;if(!seenWindows.add(n.getWindowId()))return;roots++;StringBuilder text=new StringBuilder();int[] stats=new int[5];collect(n,text,0,stats);for(int i=0;i<5;i++)statistics[i]+=stats[i];if(!text.toString().trim().isEmpty())snapshots.add(text.toString());}
 private void collect(AccessibilityNodeInfo n,StringBuilder out,int depth,int[] count){
  if(depth>28||count[0]++>700||out.length()>30000)return;if(n.isPassword()){count[4]++;return;}
  if(n.isVisibleToUser()){count[1]++;CharSequence t=n.getText(),d=n.getContentDescription();if(t!=null)count[2]+=t.length();if(d!=null)count[3]+=d.length();if(t!=null)out.append(t).append('\n');if(d!=null&&!d.equals(t))out.append(d).append('\n');}
  for(int i=0;i<n.getChildCount();i++){AccessibilityNodeInfo c=n.getChild(i);if(c!=null)try{collect(c,out,depth+1,count);}finally{c.recycle();}}
 }
 private boolean recognize(long amount,String source,boolean test){return recognize(amount,source,test,"页面文字");}
 private boolean recognize(long amount,String source,boolean test,String channel){
  try{AutoBillDrafts.Offer offer=AutoBillDrafts.offer(this,amount,source,channel,test);
   if(offer==null){status("页面、OCR 或通知已在 90 秒内提示同一笔金额，已去重");return false;}
   JSONObject draft=offer.draft;show(draft.optLong("amount"),draft.optString("source"),draft.optBoolean("test"));openDraft();
   status(offer.fresh?"已通过"+channel+"识别，正在打开核对窗口":"已有待核对账单，未覆盖原草稿");return offer.fresh;
  }catch(Exception e){status("识别成功，但草稿准备失败");return false;}
 }
 public static void notification(Context c,long amount,String source){
  AutoBillService service=live;if(service!=null){service.handler.post(()->service.recognize(amount,source,false,"支付通知"));return;}
  try{AutoBillDrafts.Offer offer=AutoBillDrafts.offer(c,amount,source,"支付通知",false);if(offer==null)return;
   AutoBillDrafts.status(c,"通知已识别，账单待核对；打开简账查看");AutoBillDrafts.open(c);
  }catch(Exception ignored){AutoBillDrafts.status(c,"通知已识别，打开简账核对；后台启动可能受限");}
 }
 private void saveDiagnostic(){try{JSONObject d=new JSONObject();d.put("windows",roots);d.put("nodes",statistics[0]);d.put("visible",statistics[1]);d.put("textChars",statistics[2]);d.put("descriptionChars",statistics[3]);d.put("readErrors",readErrors);d.put("passwordNodesSkipped",statistics[4]);d.put("package",pendingPackage);d.put("activity",pendingClass.matches("[a-zA-Z0-9_.$]+")?pendingClass:"未知");d.put("capabilities",getServiceInfo().getCapabilities());d.put("sdk",Build.VERSION.SDK_INT);prefs().edit().putString("diagnostic",d.toString()).apply();}catch(Exception ignored){}}
 private boolean samePaymentForeground(String pkg){
  if(((android.app.KeyguardManager)getSystemService(KEYGUARD_SERVICE)).isKeyguardLocked()||!((PowerManager)getSystemService(POWER_SERVICE)).isInteractive())return false;
  try{AccessibilityNodeInfo n=getRootInActiveWindow();if(n!=null)try{String origin=String.valueOf(n.getPackageName());if(pkg.equals(origin))return true;if(!"null".equals(origin)&&!getPackageName().equals(origin))return false;}finally{n.recycle();}
   for(AccessibilityWindowInfo w:getWindows())try{if(w.getType()!=AccessibilityWindowInfo.TYPE_APPLICATION||!w.isActive())continue;AccessibilityNodeInfo root=w.getRoot();if(root!=null)try{String origin=String.valueOf(root.getPackageName());if(!"null".equals(origin))return pkg.equals(origin);}finally{root.recycle();}}finally{w.recycle();}
  }catch(Exception ignored){}return pkg.equals(foregroundPackage);
 }

 private void offerOcr(){
  if(!prefs().getBoolean("ocr",false)||Build.VERSION.SDK_INT<30||ocrBusy||!samePaymentForeground(pendingPackage))return;
  if(statistics[4]>0||pendingClass.toLowerCase(java.util.Locale.ROOT).matches(".*(login|password|verify|authentication|security).*")){status("登录、密码或验证界面已跳过自动 OCR");return;}
  try{for(AccessibilityWindowInfo w:getWindows())try{if(w.getType()==AccessibilityWindowInfo.TYPE_INPUT_METHOD)return;}finally{w.recycle();}}catch(Exception ignored){}
  capture();
 }
 private void capture(){
  if(Build.VERSION.SDK_INT<30||ocrBusy||!prefs().getBoolean("enabled",false)||!prefs().getBoolean("ocr",false))return;
  final String pkg=pendingPackage;if(!samePaymentForeground(pkg)){status("支付页面已离开前台，未截图");return;}
  long now=SystemClock.elapsedRealtime();if(now-lastOcr<1500){schedule(1500-(now-lastOcr));return;}final long sequence=eventSequence;lastOcr=now;ocrAttempts++;ocrBusy=true;dismiss();status("正在本机识别支付截图，图片不保存、不上传");
  try{TakeScreenshotCallback callback=new TakeScreenshotCallback(){
   public void onSuccess(ScreenshotResult result){
    android.graphics.Bitmap image=null;try{android.hardware.HardwareBuffer buffer=result.getHardwareBuffer();try{android.graphics.Bitmap hardware=android.graphics.Bitmap.wrapHardwareBuffer(buffer,result.getColorSpace());if(hardware!=null){image=hardware.copy(android.graphics.Bitmap.Config.ARGB_8888,false);hardware.recycle();}}finally{buffer.close();}
     if(image==null)throw new IllegalStateException();if(!prefs().getBoolean("enabled",false)||!prefs().getBoolean("ocr",false)||!samePaymentForeground(pkg)){image.recycle();ocrBusy=false;status("页面或授权已改变，已丢弃截图");return;}
     android.graphics.Bitmap sample=android.graphics.Bitmap.createScaledBitmap(image,64,128,true);long hash=0;boolean nonBlack=false;
     for(int y=7;y<118;y++)for(int x=0;x<64;x++){int pixel=sample.getPixel(x,y);hash=31*hash+(pixel&0x00F8F8F8);if(((pixel>>16)&255)>32||((pixel>>8)&255)>32||(pixel&255)>32)nonBlack=true;}sample.recycle();
     if(!nonBlack){image.recycle();ocrBusy=false;prefs().edit().putString("capture_status","系统返回空白画面").apply();status("系统截图返回空白画面，未识别；可能是系统或模拟器限制");if(ocrAttempts<3)schedule(1500);return;}
     prefs().edit().putString("capture_status","已取得非空白截图，仅内存处理").apply();
     if(pkg.equals(imageHashPackage)&&hash==lastImageHash&&SystemClock.elapsedRealtime()-imageHashAt<15000){image.recycle();ocrBusy=false;status("页面未变化，等待下一次支付页面更新");return;}lastImageHash=hash;imageHashAt=SystemClock.elapsedRealtime();imageHashPackage=pkg;
     LocalOcr.read(AutoBillService.this,image,new LocalOcr.Callback(){public void done(String text,long elapsed){ocrBusy=false;if(!prefs().getBoolean("enabled",false)||!prefs().getBoolean("ocr",false)||!samePaymentForeground(pkg)){status("页面或授权已改变，未使用识别结果");return;}PaymentParser.Result parsed=PaymentParser.inspectOcr(text);prefs().edit().putLong("ocr_ms",elapsed).apply();if(parsed.amount>0)recognize(parsed.amount,sourceName(pkg),false,"本地 OCR");else{status("本地 OCR 已完成："+parsed.status);if(eventSequence>sequence)schedule(180);}}public void failed(String reason){ocrBusy=false;status(reason);}});
    }catch(Exception error){if(image!=null&&!image.isRecycled())image.recycle();ocrBusy=false;status("截图转换失败，请使用选择截图识别");}
   }
   public void onFailure(int code){ocrBusy=false;if(code!=6&&ocrAttempts<3)schedule(1500);status(code==6?"当前窗口禁止截图，未尝试绕过；请手动记账":"系统截图失败（代码 "+code+"），可在设置选择截图识别");}
  };int window=-1;if(Build.VERSION.SDK_INT>=34){for(AccessibilityWindowInfo w:getWindows())try{if(w.isActive()&&w.getType()==AccessibilityWindowInfo.TYPE_APPLICATION){window=w.getId();break;}}finally{w.recycle();}}
  if(Build.VERSION.SDK_INT>=34&&window>=0)takeScreenshotOfWindow(window,getMainExecutor(),callback);else takeScreenshot(android.view.Display.DEFAULT_DISPLAY,getMainExecutor(),callback);
  }catch(Exception error){ocrBusy=false;status("未取得系统截图能力，请重新连接无障碍或选择截图识别");}
 }
 private void openDraft(){try{startActivity(new Intent(this,MainActivity.class).setAction(getPackageName()+".AUTO_BILL").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_SINGLE_TOP|Intent.FLAG_ACTIVITY_CLEAR_TOP));}catch(Exception e){status("后台打开被系统限制，请点击悬浮提示并检查应用后台弹出设置");}}
 private void show(long amount,String source,boolean test){
  dismiss();LinearLayout box=new LinearLayout(this);box.setOrientation(1);int pad=(int)(20*getResources().getDisplayMetrics().density);box.setPadding(pad,pad,pad,pad);
  android.graphics.drawable.GradientDrawable bg=new android.graphics.drawable.GradientDrawable();bg.setColor(MainActivity.uiColor(this,"paper",Color.WHITE));bg.setCornerRadius(MainActivity.uiRadius(this));box.setBackground(bg);box.setElevation(12);
  TextView title=new TextView(this);title.setText((test?"测试弹窗 · 虚构金额":"识别到"+source+"支出")+" ¥ "+String.format(java.util.Locale.ROOT,"%.2f",amount/100.0)+"\n核对后保存，尚未写入账本");title.setTextColor(MainActivity.uiColor(this,"ink",Color.rgb(33,78,67)));title.setTextSize(18);box.addView(title);
  Button confirm=new Button(this);confirm.setText("打开记账核对");confirm.setBackgroundTintList(android.content.res.ColorStateList.valueOf(MainActivity.uiColor(this,"primary",Color.rgb(33,78,67))));confirm.setTextColor(Color.WHITE);box.addView(confirm);
  Button cancel=new Button(this);cancel.setText("忽略这笔");cancel.setBackgroundTintList(android.content.res.ColorStateList.valueOf(MainActivity.uiColor(this,"soft",Color.LTGRAY)));cancel.setTextColor(MainActivity.uiColor(this,"primary",Color.rgb(33,78,67)));box.addView(cancel);
  confirm.setOnClickListener(v->openDraft());cancel.setOnClickListener(v->{prefs().edit().remove("draft").apply();dismiss();status("已忽略识别账单");});
  WindowManager.LayoutParams lp=new WindowManager.LayoutParams(-1,-2,WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,android.graphics.PixelFormat.TRANSLUCENT);lp.gravity=Gravity.BOTTOM;lp.y=pad*3;
  try{((WindowManager)getSystemService(WINDOW_SERVICE)).addView(box,lp);overlay=box;handler.postDelayed(()->{if(overlay==box)dismiss();},30000);}catch(Exception e){status("无法显示悬浮提示，草稿已保留，可打开简账核对");}
 }
 private void dismiss(){if(overlay!=null){try{((WindowManager)getSystemService(WINDOW_SERVICE)).removeView(overlay);}catch(Exception ignored){}overlay=null;}}
 public void onInterrupt(){dismiss();status("无障碍服务被中断");}
 public void onDestroy(){connected=false;live=null;prefs().unregisterOnSharedPreferenceChangeListener(changes);handler.removeCallbacksAndMessages(null);dismiss();super.onDestroy();}
}
