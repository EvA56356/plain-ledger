package cn.plainledger;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import org.json.JSONObject;
import org.junit.*;
import org.junit.runner.RunWith;
import java.util.*;
import java.util.concurrent.*;
import static org.junit.Assert.*;

/** Synthetic amounts only: exercise persisted handoff independently of OCR/device payment apps. */
@RunWith(AndroidJUnit4.class)
public class AutoBillDraftQueueTest {
 private Context context;private SharedPreferences prefs;
 @Before public void clearBefore(){context=InstrumentationRegistry.getInstrumentation().getTargetContext();prefs=context.getSharedPreferences("auto_bill",0);assertTrue(prefs.edit().clear().commit());}
 @After public void clearAfter(){assertTrue(prefs.edit().clear().commit());}
 private JSONObject pending()throws Exception{return new JSONObject(AutoBillDrafts.peek(context));}

 @Test public void distinctPaymentsWaitInOrderWithoutReplacingHead()throws Exception{
  AutoBillDrafts.Offer first=AutoBillDrafts.offer(context,1234,"微信","页面文字",false);
  AutoBillDrafts.Offer second=AutoBillDrafts.offer(context,990,"支付宝","支付通知",false);
  assertTrue(first.fresh);assertFalse(second.fresh);assertTrue(second.queued);assertEquals(2,second.pendingCount);
  String firstId=first.draft.getString("id");assertEquals(firstId,pending().getString("id"));String peeked=AutoBillDrafts.peek(context);assertEquals(peeked,AutoBillDrafts.peek(context));
  assertFalse(AutoBillDrafts.acknowledge(context,"stale-id"));assertEquals(firstId,pending().getString("id"));
  assertTrue(AutoBillDrafts.acknowledge(context,firstId));assertEquals(firstId,pending().getString("id"));
  assertEquals(firstId,prefs.getString("draft_opened",""));
  assertTrue(AutoBillDrafts.complete(context,firstId,true));assertEquals(990,pending().getLong("amount"));
  assertEquals("",prefs.getString("draft_opened",""));
  assertTrue(AutoBillDrafts.discard(context,pending().getString("id")));assertEquals("null",AutoBillDrafts.peek(context));
 }

 @Test public void channelsDeduplicatePendingAndRecentlyDeliveredPayments()throws Exception{
  AutoBillDrafts.Offer first=AutoBillDrafts.offer(context,1234,"微信","页面文字",false);
  AutoBillDrafts.Offer duplicate=AutoBillDrafts.offer(context,1234,"微信","本地 OCR",false);
  assertFalse(duplicate.fresh);assertFalse(duplicate.queued);assertEquals(1,duplicate.pendingCount);assertEquals(first.draft.getString("id"),duplicate.draft.getString("id"));
  assertTrue(AutoBillDrafts.acknowledge(context,first.draft.getString("id")));
  assertEquals(first.draft.getString("id"),pending().getString("id"));
  assertTrue(AutoBillDrafts.complete(context,first.draft.getString("id"),true));
  assertNull(AutoBillDrafts.offer(context,1234,"微信","支付通知",false));
  assertNotNull(AutoBillDrafts.offer(context,1234,"支付宝","支付通知",false));
 }

 @Test public void waitingLegacyDraftDoesNotExpireWhenANewPaymentArrives()throws Exception{
  JSONObject waiting=new JSONObject().put("id","waiting-legacy").put("amount",1234).put("source","微信").put("created",System.currentTimeMillis()-600000);
  assertTrue(prefs.edit().putString("draft",waiting.toString()).commit());
  AutoBillDrafts.Offer next=AutoBillDrafts.offer(context,990,"支付宝","页面文字",false);
  assertTrue(next.queued);assertEquals("waiting-legacy",pending().getString("id"));
  assertTrue(AutoBillDrafts.acknowledge(context,"waiting-legacy"));assertEquals("waiting-legacy",pending().getString("id"));
  assertTrue(AutoBillDrafts.complete(context,"waiting-legacy",false));assertEquals(990,pending().getLong("amount"));
 }

 @Test public void sameAmountAfterNinetySecondsCanQueueWhileRecentChannelsMerge()throws Exception{
  AutoBillDrafts.Offer first=AutoBillDrafts.offer(context,1234,"微信","页面文字",false);String firstId=first.draft.getString("id");
  long before=System.currentTimeMillis()-91000;JSONObject waiting=new JSONObject(AutoBillDrafts.peek(context)).put("created",before);
  assertTrue(prefs.edit().putString("draft",waiting.toString()).putLong("recent_微信_1234",before).commit());
  AutoBillDrafts.Offer next=AutoBillDrafts.offer(context,1234,"微信","页面文字",false);assertTrue(next.queued);assertEquals(2,next.pendingCount);
  AutoBillDrafts.Offer crossChannel=AutoBillDrafts.offer(context,1234,"微信","支付通知",false);assertFalse(crossChannel.queued);assertEquals(2,crossChannel.pendingCount);
  assertTrue(AutoBillDrafts.acknowledge(context,firstId));assertTrue(AutoBillDrafts.complete(context,firstId,true));assertEquals(1234,pending().getLong("amount"));assertNotEquals(firstId,pending().getString("id"));
 }

 @Test public void fullQueueReportsCapacityAndPreservesEveryExistingDraft()throws Exception{
  for(int i=1;i<=20;i++)AutoBillDrafts.offer(context,i,"微信","页面文字",false);
  try{AutoBillDrafts.offer(context,21,"微信","支付通知",false);fail("A full queue must be explicit");}catch(IllegalStateException expected){assertTrue(expected.getMessage().contains("20"));}
  for(int i=1;i<=20;i++){JSONObject draft=pending();assertEquals(i,draft.getLong("amount"));assertTrue(AutoBillDrafts.acknowledge(context,draft.getString("id")));assertTrue(AutoBillDrafts.complete(context,draft.getString("id"),true));}
  assertEquals("null",AutoBillDrafts.peek(context));
 }

 @Test public void concurrentOffersAndAcknowledgementDoNotLosePayments()throws Exception{
  String firstId=AutoBillDrafts.offer(context,100,"微信","页面文字",false).draft.getString("id");
  ExecutorService workers=Executors.newFixedThreadPool(4);
  try{
   assertTrue(AutoBillDrafts.acknowledge(context,firstId));
   List<Callable<Boolean>> tasks=new ArrayList<>();tasks.add(()->AutoBillDrafts.complete(context,firstId,true));
   for(int i=1;i<=8;i++){final long amount=100+i;tasks.add(()->AutoBillDrafts.offer(context,amount,"微信","支付通知",false)!=null);}
   for(Future<Boolean> result:workers.invokeAll(tasks))assertTrue(result.get());
   Set<Long> amounts=new HashSet<>();while(!"null".equals(AutoBillDrafts.peek(context))){JSONObject draft=pending();assertTrue(amounts.add(draft.getLong("amount")));assertTrue(AutoBillDrafts.discard(context,draft.getString("id")));}
   assertEquals(8,amounts.size());for(long i=101;i<=108;i++)assertTrue(amounts.contains(i));
  }finally{workers.shutdownNow();}
 }

 @Test public void unopenedTestNeverBlocksRealPaymentButOpenTestEditorIsPreserved()throws Exception{
  String testId=AutoBillDrafts.offer(context,1234,"测试","测试",true).draft.getString("id");
  AutoBillDrafts.offer(context,990,"微信","页面文字",false);
  assertEquals(990,pending().getLong("amount"));assertFalse(AutoBillDrafts.contains(context,testId));
  AutoBillDrafts.Offer ignoredTest=AutoBillDrafts.offer(context,1234,"测试","测试",true);assertEquals(1,ignoredTest.pendingCount);assertFalse(ignoredTest.queued);
  assertTrue(AutoBillDrafts.discard(context,pending().getString("id")));
  testId=AutoBillDrafts.offer(context,1234,"测试","测试",true).draft.getString("id");
  assertTrue(AutoBillDrafts.acknowledge(context,testId));
  AutoBillDrafts.Offer real=AutoBillDrafts.offer(context,2500,"支付宝","支付通知",false);
  assertTrue(real.queued);assertEquals(testId,pending().getString("id"));assertEquals(2,real.pendingCount);
  assertTrue(AutoBillDrafts.discard(context,testId));assertEquals(2500,pending().getLong("amount"));
 }

 @Test public void displayedReviewSurvivesNewContextUntilSaveOrExplicitDiscard()throws Exception{
  String id=AutoBillDrafts.offer(context,1234,"微信","页面文字",false).draft.getString("id");
  assertTrue(AutoBillDrafts.acknowledge(context,id));
  Context recreated=context.createPackageContext(context.getPackageName(),0);
  assertEquals(id,new JSONObject(AutoBillDrafts.peek(recreated)).getString("id"));
  assertTrue(AutoBillDrafts.acknowledge(recreated,id));assertTrue(AutoBillDrafts.contains(recreated,id));
  assertTrue(AutoBillDrafts.discard(recreated,id));assertEquals("null",AutoBillDrafts.peek(context));
 }

 @Test public void committedStableEntryRecoversAnUnfinishedCompletionReceipt()throws Exception{
  String saved=AutoBillDrafts.offer(context,1234,"微信","页面文字",false).draft.getString("id");
  AutoBillDrafts.offer(context,990,"支付宝","支付通知",false);assertTrue(AutoBillDrafts.acknowledge(context,saved));
  AutoBillDrafts.reconcileSaved(context,new HashSet<>(Arrays.asList("auto-"+saved,"unrelated-manual-entry")));
  assertFalse(AutoBillDrafts.contains(context,saved));assertEquals(990,pending().getLong("amount"));assertEquals("",prefs.getString("draft_opened",""));
  AutoBillDrafts.reconcileSaved(context,Collections.singleton("auto-"+saved));assertEquals(990,pending().getLong("amount"));
 }
}
