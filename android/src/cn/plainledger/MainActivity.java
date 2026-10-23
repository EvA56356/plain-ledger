package cn.plainledger;

import android.app.*;
import android.os.*;
import android.content.*;
import android.database.*;
import android.database.sqlite.*;
import android.graphics.Color;
import android.net.Uri;
import android.view.*;
import android.webkit.*;
import android.widget.Toast;
import org.json.*;
import java.io.*;
import java.nio.charset.StandardCharsets;

public class MainActivity extends Activity {
 private WebView web;
 private volatile boolean resumed;
 private CloudTransport cloud;
 private final java.util.concurrent.ExecutorService cloudIO=java.util.concurrent.Executors.newSingleThreadExecutor();
 private boolean webReady;private int autoDraftAttempts;
 private final Handler autoDraftHandler=new Handler(Looper.getMainLooper());
 private final SharedPreferences.OnSharedPreferenceChangeListener draftChanges=(p,key)->{if("draft".equals(key)||"draft_queue".equals(key))deliverAutoDraft();};
 private final Runnable autoDraftDelivery=()->tryDeliverAutoDraft();
 private SQLiteDatabase db;
 private PendingExport pendingExport;private String receiptSource="截图";
 private static final Object EXPORT_LOCK=new Object();private static boolean exporting;
 private android.window.OnBackInvokedCallback backCallback;
 private static final int EXPORT=41, IMPORT=42;
 public void onCreate(Bundle saved){
  super.onCreate(saved);cloud=new CloudTransport(this);
  pendingExport=new PendingExport(this);
  pendingExport.recoverActivity(saved!=null);
  db=openOrCreateDatabase("ledger.sqlite3",MODE_PRIVATE,null);
  db.enableWriteAheadLogging();
  db.execSQL("CREATE TABLE IF NOT EXISTS state(id INTEGER PRIMARY KEY CHECK(id=1),payload TEXT NOT NULL)");
  db.execSQL("CREATE TABLE IF NOT EXISTS entries(id TEXT PRIMARY KEY,type TEXT NOT NULL,amount INTEGER NOT NULL,category TEXT NOT NULL,account TEXT NOT NULL,book TEXT NOT NULL,date TEXT NOT NULL,note TEXT NOT NULL)");
  web=new WebView(this);web.setBackgroundColor(uiColor(this,"background",Color.rgb(245,246,243)));
  web.getSettings().setJavaScriptEnabled(true);web.getSettings().setDomStorageEnabled(true);
  web.getSettings().setAllowFileAccess(false);web.getSettings().setAllowContentAccess(false);
  web.getSettings().setAllowFileAccessFromFileURLs(false);web.getSettings().setAllowUniversalAccessFromFileURLs(false);
  web.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);
  web.addJavascriptInterface(new Store(),"Android");
  web.setWebViewClient(new WebViewClient(){public boolean shouldOverrideUrlLoading(WebView v,WebResourceRequest r){return true;}public void onPageStarted(WebView v,String url,android.graphics.Bitmap icon){webReady=false;}public void onPageFinished(WebView v,String url){webReady=true;deliverAutoDraft();}});
  web.setWebChromeClient(new WebChromeClient(){
   public boolean onConsoleMessage(ConsoleMessage m){android.util.Log.d("PlainLedger",m.message()+" @"+m.lineNumber());return true;}
   public boolean onJsConfirm(WebView v,String u,String message,JsResult result){new AlertDialog.Builder(MainActivity.this).setMessage(message).setPositiveButton("确认",(d,w)->result.confirm()).setNegativeButton("取消",(d,w)->result.cancel()).setOnCancelListener(d->result.cancel()).show();return true;}
   public boolean onJsAlert(WebView v,String u,String message,JsResult result){new AlertDialog.Builder(MainActivity.this).setMessage(message).setPositiveButton("好",(d,w)->result.confirm()).show();return true;}
  });
  android.widget.FrameLayout container=new android.widget.FrameLayout(this);
  container.addView(web,new android.widget.FrameLayout.LayoutParams(-1,-1));
  setContentView(container);
  if(Build.VERSION.SDK_INT>=33){backCallback=this::handleBack;getOnBackInvokedDispatcher().registerOnBackInvokedCallback(android.window.OnBackInvokedDispatcher.PRIORITY_DEFAULT,backCallback);}
  if(Build.VERSION.SDK_INT>=30){getWindow().setDecorFitsSystemWindows(false);container.setOnApplyWindowInsetsListener((v,insets)->{android.graphics.Insets i=insets.getInsets(WindowInsets.Type.systemBars()|WindowInsets.Type.ime());v.setPadding(i.left,i.top,i.right,i.bottom);return insets;});}
  getSharedPreferences("auto_bill",0).registerOnSharedPreferenceChangeListener(draftChanges);
  web.loadUrl("file:///android_asset/index.html");AutoBillKeepAlive.update(this);
  String exportUri=pendingExport.resumableUri();if(!exportUri.isEmpty())writePendingExport(Uri.parse(exportUri));
 }
 private void deliverAutoDraft(){autoDraftHandler.post(()->{autoDraftHandler.removeCallbacks(autoDraftDelivery);autoDraftAttempts=0;if(web!=null&&resumed)autoDraftHandler.postDelayed(autoDraftDelivery,100);});}
 private void tryDeliverAutoDraft(){
  if(web==null||!resumed||!webReady)return;
  try{if("null".equals(AutoBillDrafts.peek(this)))return;}catch(Exception e){return;}
  web.evaluateJavascript("(function(){return !!(window.receiveAutoDraft && window.receiveAutoDraft())})()",result->{
   // A loaded page can still be initializing data or showing an existing editor.
   // Retry delivery without replacing it; the web timer also resumes after a dialog closes.
   if(web!=null&&resumed&&autoDraftAttempts++<20){try{if(!"null".equals(AutoBillDrafts.peek(this)))autoDraftHandler.postDelayed(autoDraftDelivery,500);}catch(Exception ignored){}}
  });
 }
 @Override protected void onResume(){super.onResume();resumed=true;ensureAutoBillRuntime();if(web!=null){web.onResume();deliverAutoDraft();web.evaluateJavascript("window.Cloud&&window.Cloud.sync(false)",null);}}
 @Override protected void onPause(){resumed=false;autoDraftHandler.removeCallbacks(autoDraftDelivery);if(web!=null)web.onPause();super.onPause();}
 @Override protected void onNewIntent(Intent intent){super.onNewIntent(intent);setIntent(intent);deliverAutoDraft();}
 public class Store {
  @JavascriptInterface public void cloudRequestAsync(String requestId,String method,String path,String body){if(!requestId.matches("[0-9]{1,24}"))return;cloudIO.execute(()->{String result=cloud.request(method,path,body);runOnUiThread(()->{if(!isFinishing()&&!isDestroyed()&&web!=null)web.evaluateJavascript("window.__cloudResult&&window.__cloudResult("+JSONObject.quote(requestId)+","+JSONObject.quote(result)+")",null);});});}
  @JavascriptInterface public String cloudEndpoint(){return cloud.endpoint();}
  @JavascriptInterface public String setCloudEndpoint(String address){return cloud.configure(address);}

  @JavascriptInterface public boolean isAppForeground(){return resumed;}
  @JavascriptInterface public String autoBillStatus(){try{boolean systemEnabled=false;android.view.accessibility.AccessibilityManager manager=(android.view.accessibility.AccessibilityManager)getSystemService(ACCESSIBILITY_SERVICE);for(android.accessibilityservice.AccessibilityServiceInfo info:manager.getEnabledAccessibilityServiceList(-1))if(info.getResolveInfo().serviceInfo.packageName.equals(getPackageName()))systemEnabled=true;android.content.SharedPreferences p=getSharedPreferences("auto_bill",0);JSONObject d=new JSONObject();d.put("enabled",p.getBoolean("enabled",false));d.put("systemEnabled",systemEnabled);d.put("connected",AutoBillService.isConnected());d.put("status",p.getString("status","尚未收到支付页面事件"));d.put("statusAt",p.getLong("status_at",0));d.put("ocr",p.getBoolean("ocr",false));d.put("notifications",p.getBoolean("notifications",false));d.put("notificationGranted",notificationGranted());d.put("diagnostic",p.getString("diagnostic",""));d.put("ocrMs",p.getLong("ocr_ms",0));d.put("capture",p.getString("capture_status","尚未截图"));d.put("sdk",Build.VERSION.SDK_INT);return d.toString();}catch(Exception e){return "{}";}}
  @JavascriptInterface public void openAutoBillSystemSettings(){runOnUiThread(()->startActivity(new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,Uri.parse("package:"+getPackageName()))));}
  @JavascriptInterface public void testAutoBill(){if(!AutoBillService.isConnected()||!getSharedPreferences("auto_bill",0).getBoolean("enabled",false)){notice("请先开启应用内识别并连接系统无障碍服务");return;}getSharedPreferences("auto_bill",0).edit().putLong("test_request",System.currentTimeMillis()).apply();notice("3 秒后显示虚构金额测试窗口，不会自动保存账单");}

  @JavascriptInterface public void configureAutoBill(boolean enabled){
   if(!enabled){getSharedPreferences("auto_bill",0).edit().putBoolean("enabled",false).apply();AutoBillKeepAlive.update(MainActivity.this);return;}
   runOnUiThread(()->new AlertDialog.Builder(MainActivity.this).setTitle("开启无感自动记账")
    .setMessage("一次开启后在后台监听支付宝、微信、美团与美团外卖。优先读取页面文字；文字为空或出现支付线索（含付款界面）时，在本机截图并用内置中文模型识别（Android 11 及以上），页面变化后会在有限时间内补扫。明确的聊天、分享、失败和退款转账页面会跳过；为判断是否支付页，这些应用的其他前台页面仍可能短暂参与本机识别。截图和原文仅在内存处理，不保存、不上传；不读取密码，不操作付款。识别成功尝试自动弹出核对窗口，并保留待核对草稿；手动保存后才入账。需要首次授权无障碍，随后不需要每笔付款操作开关。")
    .setPositiveButton("同意并开启",(d,w)->{getSharedPreferences("auto_bill",0).edit().putBoolean("enabled",true).putBoolean("ocr",true).apply();AutoBillKeepAlive.update(MainActivity.this);startActivity(new Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS));})
    .setNegativeButton("取消",null).show());
  }
  @JavascriptInterface public void copyAutoBillDiagnostic(){runOnUiThread(()->{String version="";try{version=getPackageManager().getPackageInfo(getPackageName(),0).versionName;}catch(Exception ignored){}String safe="简账 "+version+"\n"+autoBillStatus();((android.content.ClipboardManager)getSystemService(CLIPBOARD_SERVICE)).setPrimaryClip(ClipData.newPlainText("自动记账诊断（无截图或页面原文）",safe));notice("已复制诊断，不包含截图、商户或交易原文");});}
  @JavascriptInterface public void configurePaymentNotifications(boolean enabled){if(!enabled){getSharedPreferences("auto_bill",0).edit().putBoolean("notifications",false).apply();return;}runOnUiThread(()->new AlertDialog.Builder(MainActivity.this).setTitle("支付通知补充识别").setMessage("系统将授予通知读取权限。简账只处理支付宝、微信、美团及美团外卖的新通知，在本机提取明确支出金额；其他应用通知会直接忽略，原文不保存、不上传。与页面和 OCR 识别统一去重。这是可选补充，不影响自动 OCR。").setPositiveButton("同意，前往授权",(d,w)->{getSharedPreferences("auto_bill",0).edit().putBoolean("notifications",true).apply();startActivity(new Intent(android.provider.Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS));}).setNegativeButton("取消",null).show());}
  @JavascriptInterface public void readReceiptScreenshot(){runOnUiThread(()->new AlertDialog.Builder(MainActivity.this).setTitle("选择截图来源 · 本机识别").setItems(new String[]{"支付宝","微信","美团"},(dialog,index)->{receiptSource=new String[]{"支付宝","微信","美团"}[index];Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT).setType("image/*").addCategory(Intent.CATEGORY_OPENABLE);try{startActivityForResult(i,43);}catch(Exception e){notice("没有可用的图片选择器");}}).show());}
  @JavascriptInterface public String takeAutoDraft(){if(!resumed)return "null";try{return AutoBillDrafts.peek(MainActivity.this);}catch(Exception e){return "null";}}
  @JavascriptInterface public boolean ackAutoDraft(String id){try{return AutoBillDrafts.acknowledge(MainActivity.this,id);}catch(Exception e){return false;}}
  @JavascriptInterface public synchronized boolean finishAutoDraft(String id,boolean saved){
   try{
    if(saved)try(Cursor c=db.rawQuery("SELECT id FROM entries WHERE id=?",new String[]{"auto-"+id})){if(!c.moveToFirst())return false;}
    if(AutoBillDrafts.complete(MainActivity.this,id,saved))return true;
    // Completion is idempotent and never consumes a different queue item.
    return !AutoBillDrafts.contains(MainActivity.this,id);
   }catch(Exception e){return false;}
  }

  @JavascriptInterface public void rememberBook(String book){getSharedPreferences("auto_bill",0).edit().putString("book",book).apply();}
  @JavascriptInterface public String activeBook(){return getSharedPreferences("auto_bill",0).getString("book", "daily");}

  @JavascriptInterface public synchronized String load(){reconcileSavedDrafts();try(Cursor c=db.rawQuery("SELECT payload FROM state WHERE id=1",null)){return c.moveToFirst()?c.getString(0):"null";}}
  @JavascriptInterface public synchronized String save(String text){
   try{if(text.length()>10000000)throw new Exception("数据超过容量限制");JSONObject data=new JSONObject(text);JSONArray entries=data.getJSONArray("entries");if(data.getInt("version")!=1)throw new Exception("数据版本不支持");db.beginTransaction();try{db.execSQL("INSERT OR REPLACE INTO state(id,payload) VALUES(1,?)",new Object[]{text});db.delete("entries",null,null);for(int i=0;i<entries.length();i++){JSONObject e=entries.getJSONObject(i);long amount=e.getLong("amount");if(amount<=0)throw new Exception("金额无效");db.execSQL("INSERT INTO entries VALUES(?,?,?,?,?,?,?,?)",new Object[]{e.getString("id"),e.getString("type"),amount,e.getString("category"),e.getString("account"),e.getString("book"),e.getString("date"),e.getString("note")});}db.setTransactionSuccessful();}finally{db.endTransaction();}reconcileSavedDrafts();return "{\"ok\":true}";}catch(Exception e){return "{\"ok\":false,\"error\":"+JSONObject.quote("本地数据库保存失败："+e.getMessage())+"}";}
  }
  @JavascriptInterface public void exportFile(String name,String mime,String text){
   new Thread(()->{try{pendingExport.prepare(text);runOnUiThread(()->{
    Intent intent=new Intent(Intent.ACTION_CREATE_DOCUMENT).addCategory(Intent.CATEGORY_OPENABLE).setType(mime).putExtra(Intent.EXTRA_TITLE,name);
    intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION|Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
    try{startActivityForResult(intent,EXPORT);}catch(Exception e){pendingExport.clear();notice("此设备没有文件保存组件");}
   });}catch(Exception e){notice(e.getMessage()==null?"导出缓存准备失败，请重试":e.getMessage());}},"backup-prepare").start();
  }
  @JavascriptInterface public void importBackup(){runOnUiThread(()->{Intent intent=new Intent(Intent.ACTION_OPEN_DOCUMENT);intent.addCategory(Intent.CATEGORY_OPENABLE);intent.setType("*/*");try{startActivityForResult(intent,IMPORT);}catch(Exception e){notice("此设备没有文件选择组件");}});}
 }
 private void reconcileSavedDrafts(){try{
  java.util.Set<String> ids=new java.util.HashSet<>();try(Cursor c=db.rawQuery("SELECT id FROM entries WHERE id LIKE 'auto-%'",null)){while(c.moveToNext())ids.add(c.getString(0));}
  AutoBillDrafts.reconcileSaved(this,ids);
 }catch(Exception ignored){/* A committed database save remains successful if its review receipt needs retrying. */}}
 private boolean notificationGranted(){String value=android.provider.Settings.Secure.getString(getContentResolver(),"enabled_notification_listeners");if(value==null)return false;for(String item:value.split(":")){ComponentName c=ComponentName.unflattenFromString(item);if(c!=null&&c.getPackageName().equals(getPackageName())&&c.getClassName().equals("cn.plainledger.PaymentNotifications"))return true;}return false;}
 private void ensureAutoBillRuntime(){if(!getSharedPreferences("auto_bill",0).getBoolean("enabled",false))return;AutoBillKeepAlive.update(this);if(Build.VERSION.SDK_INT>=33&&checkSelfPermission("android.permission.POST_NOTIFICATIONS")!=android.content.pm.PackageManager.PERMISSION_GRANTED&&!getSharedPreferences("auto_bill",0).getBoolean("notification_permission_asked",false)){getSharedPreferences("auto_bill",0).edit().putBoolean("notification_permission_asked",true).apply();requestPermissions(new String[]{"android.permission.POST_NOTIFICATIONS"},81);}}
 private void recognizeReceipt(Uri uri){final String origin=receiptSource;notice("正在本机识别截图，不会自动保存账单");new Thread(()->{try{
  android.graphics.BitmapFactory.Options options=new android.graphics.BitmapFactory.Options();options.inJustDecodeBounds=true;try(InputStream in=getContentResolver().openInputStream(uri)){android.graphics.BitmapFactory.decodeStream(in,null,options);}if(options.outWidth<=0||options.outHeight<=0||((long)options.outWidth)*options.outHeight>40000000L)throw new IOException();options.inJustDecodeBounds=false;options.inSampleSize=Math.max(1,Math.max(options.outWidth,options.outHeight)/3000);
  android.graphics.Bitmap image;try(InputStream in=getContentResolver().openInputStream(uri)){image=android.graphics.BitmapFactory.decodeStream(in,null,options);}if(image==null)throw new IOException();
  LocalOcr.read(this,image,new LocalOcr.Callback(){public void done(String text,long elapsed){PaymentParser.Result result=PaymentParser.inspectOcr(text);if(result.amount==0){AutoBillDrafts.status(MainActivity.this,"截图识别："+result.status);notice("截图识别："+result.status);return;}try{AutoBillDrafts.Offer offer=AutoBillDrafts.offer(MainActivity.this,result.amount,origin,"选择截图 OCR",false);if(offer==null){notice("同一来源和金额已在 90 秒内提示，已去重");return;}AutoBillDrafts.status(MainActivity.this,"截图已在本机识别，等待核对");runOnUiThread(()->deliverAutoDraft());}catch(Exception e){notice("草稿准备失败，请重试");}}public void failed(String reason){notice(reason);}});
 }catch(Exception e){notice("图片无法读取或过大，请选择清晰的支付完成截图");}},"receipt-import").start();}
 public static int uiColor(Context c,String name,int fallback){int id=c.getResources().getIdentifier("ledger_"+name,"color",c.getPackageName());return id==0?fallback:c.getResources().getColor(id,c.getTheme());}
 public static float uiRadius(Context c){int id=c.getResources().getIdentifier("ledger_radius","dimen",c.getPackageName());return id==0?20*c.getResources().getDisplayMetrics().density:c.getResources().getDimension(id);}
 private void notice(String s){runOnUiThread(()->Toast.makeText(this,s,Toast.LENGTH_LONG).show());}
 @Override protected void onActivityResult(int request,int result,Intent data){
  super.onActivityResult(request,result,data);
  if(result!=RESULT_OK||data==null||data.getData()==null){if(request==EXPORT)pendingExport.clear();return;}
  Uri uri=data.getData();if(request==43){recognizeReceipt(uri);return;}
  try{if(request==EXPORT){
   try{getContentResolver().takePersistableUriPermission(uri,data.getFlags()&Intent.FLAG_GRANT_WRITE_URI_PERMISSION);}catch(SecurityException ignored){}
   pendingExport.destination(uri.toString());writePendingExport(uri);
  }else if(request==IMPORT){ByteArrayOutputStream buf=new ByteArrayOutputStream();try(InputStream in=getContentResolver().openInputStream(uri)){byte[] block=new byte[8192];int n;while((n=in.read(block))!=-1){if(buf.size()+n>10000000)throw new IOException("文件过大");buf.write(block,0,n);}}String text=buf.toString("UTF-8");web.evaluateJavascript("window.receiveBackup("+JSONObject.quote(text)+").catch(function(){})",null);}}catch(Exception e){if(request==EXPORT)pendingExport.failed();notice("文件操作失败："+e.getMessage());}
 }
 private void writePendingExport(Uri uri){
  synchronized(EXPORT_LOCK){if(exporting)return;exporting=true;}
  new Thread(()->{try{
   try(OutputStream out=getContentResolver().openOutputStream(uri,"wt")){pendingExport.copyTo(out);}
   pendingExport.clear();notice("备份已保存");
  }catch(Exception e){pendingExport.failed();notice("导出未完成，请重新导出："+(e.getMessage()==null?"目标文件不可用":e.getMessage()));}
  finally{try{getContentResolver().releasePersistableUriPermission(uri,Intent.FLAG_GRANT_WRITE_URI_PERMISSION);}catch(SecurityException ignored){}synchronized(EXPORT_LOCK){exporting=false;}}},"backup-write").start();
 }
 private void handleBack(){if(web==null){finish();return;}web.evaluateJavascript("window.handleAppBack ? window.handleAppBack() : false",r->{if(!"true".equals(r))finish();});}
 @Override public void onBackPressed(){handleBack();}
 @Override protected void onDestroy(){cloudIO.shutdownNow();if(Build.VERSION.SDK_INT>=33&&backCallback!=null)getOnBackInvokedDispatcher().unregisterOnBackInvokedCallback(backCallback);getSharedPreferences("auto_bill",0).unregisterOnSharedPreferenceChangeListener(draftChanges);autoDraftHandler.removeCallbacksAndMessages(null);if(web!=null){web.removeJavascriptInterface("Android");web.destroy();web=null;}if(db!=null)db.close();super.onDestroy();}
}
