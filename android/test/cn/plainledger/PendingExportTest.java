package cn.plainledger;

import android.content.Context;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import org.junit.*;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class PendingExportTest {
 private File directory;
 private PendingExport pending;
 @Before public void before(){Context context=InstrumentationRegistry.getInstrumentation().getTargetContext();directory=new File(context.getCacheDir(),"export-test-"+UUID.randomUUID());pending=new PendingExport(directory);}
 @After public void after(){pending.clear();directory.delete();}

 @Test public void pickerPayloadAndDestinationRecoverWithoutActivityFields()throws Exception{
  String content="虚构账本，测试金额 12.34";pending.prepare(content);assertEquals("",new PendingExport(directory).resumableUri());
  new PendingExport(directory).destination("content://synthetic-test/document/export");
  PendingExport recreated=new PendingExport(directory);assertEquals("content://synthetic-test/document/export",recreated.resumableUri());
  ByteArrayOutputStream output=new ByteArrayOutputStream();recreated.copyTo(output);assertEquals(content,new String(output.toByteArray(),StandardCharsets.UTF_8));
  recreated.clear();assertEquals("",new PendingExport(directory).resumableUri());assertFalse(new File(directory,"payload").exists());
 }

 @Test public void utf8ByteLimitRejectsOversizedPayloadWithoutReplacingPendingExport()throws Exception{
  char[] characters=new char[PendingExport.LIMIT/3+1];java.util.Arrays.fill(characters,'测');
  try{pending.prepare(new String(characters));fail("UTF-8 payload must fit 10MB");}catch(IOException expected){assertTrue(expected.getMessage().contains("10MB"));}
  pending.prepare("first synthetic export");
  try{pending.prepare("replacement");fail("An active document picker must retain its original payload");}catch(IOException expected){}
  ByteArrayOutputStream output=new ByteArrayOutputStream();new PendingExport(directory).copyTo(output);assertEquals("first synthetic export",output.toString("UTF-8"));
 }

 @Test public void interruptedWriteCanRestartFromBeginningAndFailedPickerCanBeReplaced()throws Exception{
  pending.prepare("synthetic payload");pending.destination("content://synthetic-test/document/export");
  try{pending.copyTo(new OutputStream(){public void write(int value)throws IOException{throw new IOException("synthetic interruption");}});fail("Write must report interruption");}catch(IOException expected){}
  PendingExport recreated=new PendingExport(directory);assertFalse(recreated.resumableUri().isEmpty());
  ByteArrayOutputStream output=new ByteArrayOutputStream();recreated.copyTo(output);assertEquals("synthetic payload",output.toString("UTF-8"));
  recreated.failed();assertEquals("",recreated.resumableUri());recreated.prepare("new export after failure");
  output.reset();recreated.copyTo(output);assertEquals("new export after failure",output.toString("UTF-8"));
 }

 @Test public void onlyFreshActivityClearsOrphanedPickerWhileRestoredPickerAndWritingUriSurvive()throws Exception{
  pending.prepare("restored picker payload");
  PendingExport restored=new PendingExport(directory);restored.recoverActivity(true);
  ByteArrayOutputStream output=new ByteArrayOutputStream();restored.copyTo(output);assertEquals("restored picker payload",output.toString("UTF-8"));
  try{restored.prepare("must not replace active picker");fail("Saved Activity state keeps its pending picker");}catch(IOException expected){}
  PendingExport fresh=new PendingExport(directory);fresh.recoverActivity(false);assertFalse(new File(directory,"payload").exists());
  fresh.prepare("destination already selected");fresh.destination("content://synthetic-test/document/resume");
  PendingExport writing=new PendingExport(directory);writing.recoverActivity(false);assertEquals("content://synthetic-test/document/resume",writing.resumableUri());
  writing.recoverActivity(true);output.reset();writing.copyTo(output);assertEquals("destination already selected",output.toString("UTF-8"));
 }
}
