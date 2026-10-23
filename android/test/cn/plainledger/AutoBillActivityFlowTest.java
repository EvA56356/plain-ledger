package cn.plainledger;

import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.webkit.WebView;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import org.json.JSONObject;
import org.junit.*;
import org.junit.runner.RunWith;
import java.lang.reflect.Field;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import static org.junit.Assert.*;

/** Exercise the real packaged WebView, Java bridge and SQLite commit, with isolated synthetic data. */
@RunWith(AndroidJUnit4.class)
public class AutoBillActivityFlowTest {
 private Instrumentation instrumentation;
 private Context context;
 private MainActivity activity;
 @Before public void prepare() {
  instrumentation = InstrumentationRegistry.getInstrumentation();
  context = instrumentation.getTargetContext();
  assertTrue("Never run on a production ledger", context.getPackageName().endsWith(".ocrtest"));
  assertTrue(context.getSharedPreferences("auto_bill",0).edit().clear().commit());
 }
 @After public void finish() {
  if (activity != null) instrumentation.runOnMainSync(() -> activity.finish());
  instrumentation.waitForIdleSync();
  context.getSharedPreferences("auto_bill",0).edit().clear().commit();
 }
 private void start() {
  activity = (MainActivity) instrumentation.startActivitySync(new Intent(context,MainActivity.class)
    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK));
 }
 private WebView web() throws Exception {
  Field field = MainActivity.class.getDeclaredField("web"); field.setAccessible(true);
  return (WebView)field.get(activity);
 }
 private String eval(String script) throws Exception {
  String[] result = {null}; CountDownLatch completed = new CountDownLatch(1);
  WebView view = web();
  instrumentation.runOnMainSync(() -> view.evaluateJavascript(script,value -> { result[0] = value; completed.countDown(); }));
  assertTrue("WebView evaluation timed out",completed.await(5,TimeUnit.SECONDS)); return result[0];
 }
 private void await(String expression) throws Exception {
  long end = android.os.SystemClock.elapsedRealtime()+10000;
  while(android.os.SystemClock.elapsedRealtime()<end) {
   if("true".equals(eval("(function(){return !!("+expression+")})()"))) return;
   Thread.sleep(100);
  }
  fail("Packaged UI did not complete the requested synthetic flow");
 }
 private String offer(long amount) throws Exception {
  return AutoBillDrafts.offer(context,amount,"微信","测试页面",false).draft.getString("id");
 }
 @Test public void displayedDraftSurvivesActivityRestartAndSavesOnce() throws Exception {
  String id = offer(1237), entryId = "auto-"+id;
  start(); await("document.querySelector('#entryDialog[open]') && document.querySelector('#entryTitle').textContent==='核对识别账单'");
  boolean[] fileAccess = {true}; WebView loaded = web();
  instrumentation.runOnMainSync(() -> fileAccess[0] = loaded.getSettings().getAllowFileAccess());
  assertFalse(fileAccess[0]);
  assertEquals(id,new JSONObject(AutoBillDrafts.peek(context)).getString("id"));
  instrumentation.runOnMainSync(() -> activity.finish()); instrumentation.waitForIdleSync();
  start(); await("document.querySelector('#entryDialog[open]') && document.querySelector('#entryForm').elements.amount.value==='12.37'");
  eval("document.querySelector('#entryForm').dispatchEvent(new Event('submit',{bubbles:true,cancelable:true}));");
  await("!document.querySelector('#entryDialog[open]') && state.entries.filter(e=>e.id==="+JSONObject.quote(entryId)+").length===1");
  assertEquals("null",AutoBillDrafts.peek(context));
  instrumentation.runOnMainSync(() -> activity.finish()); instrumentation.waitForIdleSync();
  start(); await("typeof state!=='undefined' && state.entries.filter(e=>e.id==="+JSONObject.quote(entryId)+").length===1");
  assertEquals("null",AutoBillDrafts.peek(context));
 }
 @Test public void explicitCancelCompletesDraftWithoutWritingAnEntry() throws Exception {
  String id = offer(1238), entryId = "auto-"+id;
  start(); await("document.querySelector('#entryDialog[open]') && document.querySelector('#entryForm').elements.amount.value==='12.38'");
  eval("document.querySelector('#entryDialog').close();");
  await("!document.querySelector('#entryDialog[open]') && Android.takeAutoDraft()==='null'");
  assertEquals("true",eval("state.entries.every(e=>e.id!=="+JSONObject.quote(entryId)+")"));
  assertEquals("null",AutoBillDrafts.peek(context));
 }
}
