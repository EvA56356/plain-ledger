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
 private final Handler handler=new Handler(Looper.getMainLooper());
 private View overlay;private String pendingPackage;private final Map<String,Long> recent=new HashMap<>();
 private final Runnable scan=()->scanPage(pendingPackage);
 private android.content.SharedPreferences prefs(){return getSharedPreferences("auto_bill",0);}
 public void onAccessibilityEvent(AccessibilityEvent event){
  if(!prefs().getBoolean("enabled",false)){dismiss();return;}
  String pkg=String.valueOf(event.getPackageName());
  if(!pkg.equals("com.eg.android.AlipayGphone")&&!pkg.equals("com.tencent.mm"))return;
  pendingPackage=pkg;handler.removeCallbacks(scan);handler.postDelayed(scan,350);
 }
 private void scanPage(String pkg){
  if(!prefs().getBoolean("enabled",false))return;
  long now=SystemClock.elapsedRealtime();
  AccessibilityNodeInfo root=getRootInActiveWindow();if(root==null)return;
  StringBuilder text=new StringBuilder();try{if(!pkg.contentEquals(root.getPackageName()))return;collect(root,text,0,new int[]{0});}finally{root.recycle();}
  long amount=PaymentParser.parse(text.toString());if(amount==0||overlay!=null)return;
  String key=pkg+":"+amount;Long prev=recent.get(key);if(prev!=null&&now-prev<90000)return;
  recent.entrySet().removeIf(e->now-e.getValue()>90000);recent.put(key,now);
  show(amount,pkg.equals("com.tencent.mm")?"微信":"支付宝");
 }
 private void collect(AccessibilityNodeInfo n,StringBuilder out,int depth,int[] count){
  if(depth>24||count[0]++>500||out.length()>20000)return;
  if(n.isVisibleToUser()&&!n.isPassword()){CharSequence t=n.getText();if(t!=null)out.append(t).append('\n');}
  for(int i=0;i<n.getChildCount();i++){AccessibilityNodeInfo c=n.getChild(i);if(c!=null)try{collect(c,out,depth+1,count);}finally{c.recycle();}}
 }
 private void show(long amount,String source){
  LinearLayout box=new LinearLayout(this);box.setOrientation(1);int pad=(int)(20*getResources().getDisplayMetrics().density);box.setPadding(pad,pad,pad,pad);
  android.graphics.drawable.GradientDrawable bg=new android.graphics.drawable.GradientDrawable();bg.setColor(Color.WHITE);bg.setCornerRadius(pad);box.setBackground(bg);box.setElevation(12);
  TextView title=new TextView(this);title.setText("识别到"+source+"支出 ¥ "+String.format(java.util.Locale.ROOT,"%.2f",amount/100.0)+"\n请核对后保存，尚未写入账本");title.setTextColor(Color.rgb(33,78,67));title.setTextSize(18);box.addView(title);
  Button confirm=new Button(this);confirm.setText("核对并记账");box.addView(confirm);Button cancel=new Button(this);cancel.setText("忽略");box.addView(cancel);
  confirm.setOnClickListener(v->{try{JSONObject draft=new JSONObject();draft.put("amount",amount);draft.put("source",source);draft.put("created",System.currentTimeMillis());prefs().edit().putString("draft",draft.toString()).commit();dismiss();startActivity(new Intent(this,MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_SINGLE_TOP));}catch(Exception ignored){dismiss();}});
  cancel.setOnClickListener(v->dismiss());
  WindowManager.LayoutParams lp=new WindowManager.LayoutParams(-1,-2,WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,android.graphics.PixelFormat.TRANSLUCENT);lp.gravity=Gravity.BOTTOM;lp.y=pad*3;
  try{((WindowManager)getSystemService(WINDOW_SERVICE)).addView(box,lp);overlay=box;handler.postDelayed(()->{if(overlay==box)dismiss();},20000);}catch(Exception ignored){}
 }
 private void dismiss(){if(overlay!=null){try{((WindowManager)getSystemService(WINDOW_SERVICE)).removeView(overlay);}catch(Exception ignored){}overlay=null;}}
 public void onInterrupt(){dismiss();}
 public void onDestroy(){handler.removeCallbacksAndMessages(null);dismiss();super.onDestroy();}
}
