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
 private SQLiteDatabase db;
 private String pendingExport;
 private static final int EXPORT=41, IMPORT=42;
 public void onCreate(Bundle saved){
  super.onCreate(saved);cloud=new CloudTransport(this);
  db=openOrCreateDatabase("ledger.sqlite3",MODE_PRIVATE,null);
  db.enableWriteAheadLogging();
  db.execSQL("CREATE TABLE IF NOT EXISTS state(id INTEGER PRIMARY KEY CHECK(id=1),payload TEXT NOT NULL)");
  db.execSQL("CREATE TABLE IF NOT EXISTS entries(id TEXT PRIMARY KEY,type TEXT NOT NULL,amount INTEGER NOT NULL,category TEXT NOT NULL,account TEXT NOT NULL,book TEXT NOT NULL,date TEXT NOT NULL,note TEXT NOT NULL)");
  web=new WebView(this);web.setBackgroundColor(uiColor(this,"background",Color.rgb(245,246,243)));
  web.getSettings().setJavaScriptEnabled(true);web.getSettings().setDomStorageEnabled(true);
  web.getSettings().setAllowFileAccess(true);web.getSettings().setAllowContentAccess(false);
  web.getSettings().setAllowFileAccessFromFileURLs(false);web.getSettings().setAllowUniversalAccessFromFileURLs(false);
  web.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);
  web.addJavascriptInterface(new Store(),"Android");
  web.setWebViewClient(new WebViewClient(){public boolean shouldOverrideUrlLoading(WebView v,WebResourceRequest r){return true;}});
  web.setWebChromeClient(new WebChromeClient(){
   public boolean onConsoleMessage(ConsoleMessage m){android.util.Log.d("PlainLedger",m.message()+" @"+m.lineNumber());return true;}
   public boolean onJsConfirm(WebView v,String u,String message,JsResult result){new AlertDialog.Builder(MainActivity.this).setMessage(message).setPositiveButton("确认",(d,w)->result.confirm()).setNegativeButton("取消",(d,w)->result.cancel()).setOnCancelListener(d->result.cancel()).show();return true;}
   public boolean onJsAlert(WebView v,String u,String message,JsResult result){new AlertDialog.Builder(MainActivity.this).setMessage(message).setPositiveButton("好",(d,w)->result.confirm()).show();return true;}
  });
  android.widget.FrameLayout container=new android.widget.FrameLayout(this);
  container.addView(web,new android.widget.FrameLayout.LayoutParams(-1,-1));
  setContentView(container);
  if(Build.VERSION.SDK_INT>=30){getWindow().setDecorFitsSystemWindows(false);container.setOnApplyWindowInsetsListener((v,insets)->{android.graphics.Insets i=insets.getInsets(WindowInsets.Type.systemBars()|WindowInsets.Type.ime());v.setPadding(i.left,i.top,i.right,i.bottom);return insets;});}
  web.loadUrl("file:///android_asset/index.html");
 }
 private void deliverAutoDraft(){if(web!=null)web.postDelayed(()->{if(resumed)web.evaluateJavascript("if(window.receiveAutoDraft)window.receiveAutoDraft()",null);},200);}
 @Override protected void onResume(){super.onResume();resumed=true;if(web!=null){web.onResume();deliverAutoDraft();web.evaluateJavascript("window.Cloud&&window.Cloud.sync(false)",null);}}
 @Override protected void onPause(){resumed=false;if(web!=null)web.onPause();super.onPause();}
 @Override protected void onNewIntent(Intent intent){super.onNewIntent(intent);setIntent(intent);deliverAutoDraft();}
 public class Store {
  @JavascriptInterface public void cloudRequestAsync(String requestId,String method,String path,String body){if(!requestId.matches("[0-9]{1,24}"))return;cloudIO.execute(()->{String result=cloud.request(method,path,body);runOnUiThread(()->{if(!isFinishing()&&!isDestroyed()&&web!=null)web.evaluateJavascript("window.__cloudResult&&window.__cloudResult("+JSONObject.quote(requestId)+","+JSONObject.quote(result)+")",null);});});}
  @JavascriptInterface public String cloudEndpoint(){return cloud.endpoint();}
  @JavascriptInterface public String setCloudEndpoint(String address){return cloud.configure(address);}

  @JavascriptInterface public boolean isAppForeground(){return resumed;}
  @JavascriptInterface public String autoBillStatus(){try{boolean systemEnabled=false;android.view.accessibility.AccessibilityManager manager=(android.view.accessibility.AccessibilityManager)getSystemService(ACCESSIBILITY_SERVICE);for(android.accessibilityservice.AccessibilityServiceInfo info:manager.getEnabledAccessibilityServiceList(-1))if(info.getResolveInfo().serviceInfo.packageName.equals(getPackageName()))systemEnabled=true;android.content.SharedPreferences p=getSharedPreferences("auto_bill",0);JSONObject d=new JSONObject();d.put("enabled",p.getBoolean("enabled",false));d.put("systemEnabled",systemEnabled);d.put("connected",AutoBillService.isConnected());d.put("status",p.getString("status","尚未收到支付页面事件"));d.put("statusAt",p.getLong("status_at",0));return d.toString();}catch(Exception e){return "{}";}}
  @JavascriptInterface public void openAutoBillSystemSettings(){runOnUiThread(()->startActivity(new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,Uri.parse("package:"+getPackageName()))));}
  @JavascriptInterface public void testAutoBill(){if(!AutoBillService.isConnected()||!getSharedPreferences("auto_bill",0).getBoolean("enabled",false)){notice("请先开启应用内识别并连接系统无障碍服务");return;}getSharedPreferences("auto_bill",0).edit().putLong("test_request",System.currentTimeMillis()).apply();notice("3 秒后显示虚构金额测试窗口，不会自动保存账单");}

  @JavascriptInterface public void configureAutoBill(boolean enabled){if(!enabled)getSharedPreferences("auto_bill",0).edit().putBoolean("enabled",false).apply();if(enabled)runOnUiThread(()->new AlertDialog.Builder(MainActivity.this).setTitle("开启离线自动记账").setMessage("仅读取支付宝、微信、美团的页面文字来识别支付成功金额。不截图、不联网、不操作付款。识别后仍需核对并保存。请在下一页手动开启简账的无障碍服务。开启前请确认你同意上述读取范围。").setPositiveButton("同意，前往系统设置",(d,w)->{getSharedPreferences("auto_bill",0).edit().putBoolean("enabled",true).apply();startActivity(new Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS));}).setNegativeButton("取消",(d,w)->getSharedPreferences("auto_bill",0).edit().putBoolean("enabled",false).apply()).show());}
  @JavascriptInterface public String takeAutoDraft(){if(!resumed)return "null";return getSharedPreferences("auto_bill",0).getString("draft","null");}
  @JavascriptInterface public void ackAutoDraft(String id){android.content.SharedPreferences p=getSharedPreferences("auto_bill",0);try{JSONObject d=new JSONObject(p.getString("draft","null"));if(id.equals(d.optString("id",String.valueOf(d.optLong("created"))))){p.edit().remove("draft").putString("draft_opened",id).putString("status","核对窗口已打开，等待手动保存").putLong("status_at",System.currentTimeMillis()).apply();}}catch(Exception ignored){}}

  @JavascriptInterface public void rememberBook(String book){getSharedPreferences("auto_bill",0).edit().putString("book",book).apply();}
  @JavascriptInterface public String activeBook(){return getSharedPreferences("auto_bill",0).getString("book", "daily");}

  @JavascriptInterface public synchronized String load(){try(Cursor c=db.rawQuery("SELECT payload FROM state WHERE id=1",null)){return c.moveToFirst()?c.getString(0):"null";}}
  @JavascriptInterface public synchronized String save(String text){
   try{if(text.length()>10000000)throw new Exception("数据超过容量限制");JSONObject data=new JSONObject(text);JSONArray entries=data.getJSONArray("entries");if(data.getInt("version")!=1)throw new Exception("数据版本不支持");db.beginTransaction();try{db.execSQL("INSERT OR REPLACE INTO state(id,payload) VALUES(1,?)",new Object[]{text});db.delete("entries",null,null);for(int i=0;i<entries.length();i++){JSONObject e=entries.getJSONObject(i);long amount=e.getLong("amount");if(amount<=0)throw new Exception("金额无效");db.execSQL("INSERT INTO entries VALUES(?,?,?,?,?,?,?,?)",new Object[]{e.getString("id"),e.getString("type"),amount,e.getString("category"),e.getString("account"),e.getString("book"),e.getString("date"),e.getString("note")});}db.setTransactionSuccessful();}finally{db.endTransaction();}return "{\"ok\":true}";}catch(Exception e){return "{\"ok\":false,\"error\":"+JSONObject.quote("本地数据库保存失败："+e.getMessage())+"}";}
  }
  @JavascriptInterface public void exportFile(String name,String mime,String text){runOnUiThread(()->{pendingExport=text;Intent intent=new Intent(Intent.ACTION_CREATE_DOCUMENT);intent.addCategory(Intent.CATEGORY_OPENABLE);intent.setType(mime);intent.putExtra(Intent.EXTRA_TITLE,name);try{startActivityForResult(intent,EXPORT);}catch(Exception e){notice("此设备没有文件保存组件");}});}
  @JavascriptInterface public void importBackup(){runOnUiThread(()->{Intent intent=new Intent(Intent.ACTION_OPEN_DOCUMENT);intent.addCategory(Intent.CATEGORY_OPENABLE);intent.setType("*/*");try{startActivityForResult(intent,IMPORT);}catch(Exception e){notice("此设备没有文件选择组件");}});}
 }
 public static int uiColor(Context c,String name,int fallback){int id=c.getResources().getIdentifier("ledger_"+name,"color",c.getPackageName());return id==0?fallback:c.getResources().getColor(id,c.getTheme());}
 public static float uiRadius(Context c){int id=c.getResources().getIdentifier("ledger_radius","dimen",c.getPackageName());return id==0?20*c.getResources().getDisplayMetrics().density:c.getResources().getDimension(id);}
 private void notice(String s){Toast.makeText(this,s,Toast.LENGTH_LONG).show();}
 @Override protected void onActivityResult(int request,int result,Intent data){super.onActivityResult(request,result,data);if(result!=RESULT_OK||data==null||data.getData()==null)return;Uri uri=data.getData();try{if(request==EXPORT){try(OutputStream out=getContentResolver().openOutputStream(uri)){if(out==null)throw new IOException();out.write(pendingExport.getBytes(StandardCharsets.UTF_8));}pendingExport=null;notice("备份已保存");}else if(request==IMPORT){ByteArrayOutputStream buf=new ByteArrayOutputStream();try(InputStream in=getContentResolver().openInputStream(uri)){byte[] block=new byte[8192];int n;while((n=in.read(block))!=-1){if(buf.size()+n>10000000)throw new IOException("文件过大");buf.write(block,0,n);}}String text=buf.toString("UTF-8");web.evaluateJavascript("window.receiveBackup("+JSONObject.quote(text)+").catch(function(){})",null);}}catch(Exception e){notice("文件操作失败："+e.getMessage());}}

 @Override public void onBackPressed(){web.evaluateJavascript("window.handleAppBack ? window.handleAppBack() : false",r->{if(!"true".equals(r))finish();});}
 @Override protected void onDestroy(){cloudIO.shutdownNow();if(web!=null){web.removeJavascriptInterface("Android");web.destroy();}if(db!=null)db.close();super.onDestroy();}
}
