package cn.plainledger;
import android.graphics.*;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.util.concurrent.*;
import static org.junit.Assert.*;

/** Private screenshots are provisioned locally under build/, never included in source/release assets. */
@RunWith(AndroidJUnit4.class)
public class LocalOcrTest {
 @Test public void privateReceiptLayouts()throws Exception{
  org.junit.Assume.assumeTrue(java.util.Arrays.asList(InstrumentationRegistry.getInstrumentation().getContext().getAssets().list("")).contains("receipt1.jpg"));
  long[] expected={5000,1990,2130,1990};
  for(int i=0;i<4;i++){
   Bitmap bitmap;try(java.io.InputStream in=InstrumentationRegistry.getInstrumentation().getContext().getAssets().open("receipt"+(i+1)+".jpg")){bitmap=BitmapFactory.decodeStream(in);}
   CountDownLatch latch=new CountDownLatch(1);long[] actual={-1};String[] failure={null};
   LocalOcr.read(InstrumentationRegistry.getInstrumentation().getTargetContext(),bitmap,new LocalOcr.Callback(){
    public void done(String text,long elapsed){actual[0]=PaymentParser.inspectOcr(text).amount;android.util.Log.i("LedgerOcrTest","Local recognition completed in "+elapsed+" ms; raw text not logged");latch.countDown();}
    public void failed(String reason){failure[0]=reason;latch.countDown();}
   });
   assertTrue("OCR completion timeout",latch.await(60,TimeUnit.SECONDS));assertNull(failure[0]);assertEquals("Receipt layout "+(i+1),expected[i],actual[0]);
  }
 }
 @Test public void syntheticImageOcr()throws Exception{
  Bitmap image=Bitmap.createBitmap(800,1000,Bitmap.Config.ARGB_8888);Canvas canvas=new Canvas(image);canvas.drawColor(Color.WHITE);Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);paint.setColor(Color.BLACK);paint.setTextSize(48);canvas.drawText("支付成功",80,130,paint);paint.setTextSize(78);canvas.drawText("¥12.34",80,320,paint);paint.setTextSize(40);canvas.drawText("付款方式",80,500,paint);canvas.drawText("测试账户",80,600,paint);
  CountDownLatch latch=new CountDownLatch(1);long[] amount={-1};LocalOcr.read(InstrumentationRegistry.getInstrumentation().getTargetContext(),image,new LocalOcr.Callback(){public void done(String text,long elapsed){amount[0]=PaymentParser.inspectOcr(text).amount;latch.countDown();}public void failed(String reason){latch.countDown();}});assertTrue(latch.await(60,TimeUnit.SECONDS));assertEquals(1234,amount[0]);
 }
 @Test public void chatAndIncompletePaymentAreRejected(){
  assertEquals(0,PaymentParser.inspectOcr("示例聊天\n朋友：支付成功，19.90元\n按住说话").amount);
  assertEquals(0,PaymentParser.inspectOcr("确认付款\n¥19.90\n付款方式 测试账户").amount);
  assertEquals(0,PaymentParser.inspectOcr("支付成功\n¥19.90\n发送给朋友\n返回商家").amount);
 }
 @Test public void completedReceiptWithDiscountAndOnlyDoneButton()throws Exception{
  Bitmap image=Bitmap.createBitmap(800,1000,Bitmap.Config.ARGB_8888);Canvas canvas=new Canvas(image);canvas.drawColor(Color.WHITE);Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);paint.setColor(Color.BLACK);paint.setTextSize(60);canvas.drawText("支付成功",80,140,paint);paint.setTextSize(90);canvas.drawText("¥19.90",80,320,paint);paint.setTextSize(48);canvas.drawText("优惠 ¥1.00",80,500,paint);paint.setTextSize(60);canvas.drawText("完成",80,760,paint);
  CountDownLatch latch=new CountDownLatch(1);long[] amount={-1};LocalOcr.read(InstrumentationRegistry.getInstrumentation().getTargetContext(),image,new LocalOcr.Callback(){public void done(String text,long elapsed){amount[0]=PaymentParser.inspectOcr(text).amount;latch.countDown();}public void failed(String reason){latch.countDown();}});assertTrue(latch.await(30,TimeUnit.SECONDS));assertEquals(1990,amount[0]);
 }
 @Test public void channelsSharePendingDraftAndFingerprint()throws Exception{
  android.content.Context c=InstrumentationRegistry.getInstrumentation().getTargetContext();
  c.getSharedPreferences("auto_bill",0).edit().clear().commit();
  AutoBillDrafts.Offer first=AutoBillDrafts.offer(c,1234,"微信","页面文字",false);
  AutoBillDrafts.Offer second=AutoBillDrafts.offer(c,1234,"微信","本地 OCR",false);
  assertNotNull(first);assertFalse(second.fresh);assertEquals(first.draft.getString("id"),second.draft.getString("id"));
  c.getSharedPreferences("auto_bill",0).edit().remove("draft").commit();
  assertNull(AutoBillDrafts.offer(c,1234,"微信","支付通知",false));
  assertNotNull(AutoBillDrafts.offer(c,999,"微信","支付通知",false));
  c.getSharedPreferences("auto_bill",0).edit().clear().commit();
 }
}
