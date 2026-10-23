package cn.plainledger;

import android.content.Context;
import android.util.AtomicFile;
import java.io.*;
import java.nio.charset.StandardCharsets;
import org.json.JSONObject;

/** Private, disposable cache state survives Activity/process recreation while a picker is open. */
final class PendingExport {
 static final int LIMIT=10000000;
 private final File directory;
 private final AtomicFile payload,state;
 PendingExport(Context context){this(new File(context.getCacheDir(),"pending-export"));}
 PendingExport(File directory){this.directory=directory;payload=new AtomicFile(new File(directory,"payload"));state=new AtomicFile(new File(directory,"state.json"));}
 /** Called only from onCreate: a fresh Activity cannot receive an abandoned old picker result. */
 synchronized void recoverActivity(boolean restoringActivity){
  if(restoringActivity)return;
  JSONObject value=readState();
  if(value!=null&&"waiting".equals(value.optString("status"))&&value.optString("uri","").isEmpty())clear();
 }
 synchronized void prepare(String text)throws Exception{
  JSONObject previous=readState();
  if(previous!=null&&!"failed".equals(previous.optString("status")))throw new IOException("已有导出正在等待保存，请先完成或取消文件选择");
  if(text==null||text.length()>LIMIT)throw new IOException("导出文件超过 10MB");
  byte[] bytes=text.getBytes(StandardCharsets.UTF_8);if(bytes.length>LIMIT)throw new IOException("导出文件超过 10MB");
  if(!directory.isDirectory()&&!directory.mkdirs())throw new IOException("无法创建导出缓存");
  write(payload,bytes);
  try{writeState(new JSONObject().put("status","waiting").put("uri",""));}catch(Exception error){payload.delete();throw error;}
 }
 synchronized void destination(String uri)throws Exception{
  if(readState()==null||!payload.getBaseFile().isFile())throw new IOException("导出缓存已不可用，请重新导出");
  writeState(new JSONObject().put("status","writing").put("uri",uri));
 }
 synchronized String resumableUri(){JSONObject value=readState();return value!=null&&"writing".equals(value.optString("status"))?value.optString("uri",""):"";}
 synchronized void copyTo(OutputStream out)throws IOException{
  if(out==null)throw new IOException("无法打开目标文件");
  try(InputStream in=payload.openRead()){
   byte[] block=new byte[8192];int count,total=0;
   while((count=in.read(block))!=-1){total+=count;if(total>LIMIT)throw new IOException("导出文件超过 10MB");out.write(block,0,count);}
  }
  out.flush();
 }
 synchronized void failed(){JSONObject value=readState();if(value!=null)try{value.put("status","failed");writeState(value);}catch(Exception ignored){} }
 synchronized void clear(){state.delete();payload.delete();}
 private JSONObject readState(){try(InputStream in=state.openRead()){
  ByteArrayOutputStream bytes=new ByteArrayOutputStream();byte[] block=new byte[1024];int count;
  while((count=in.read(block))!=-1){if(bytes.size()+count>8192)throw new IOException();bytes.write(block,0,count);}
  return new JSONObject(new String(bytes.toByteArray(),StandardCharsets.UTF_8));
 }catch(Exception missing){return null;}}
 private void writeState(JSONObject value)throws IOException{write(state,value.toString().getBytes(StandardCharsets.UTF_8));}
 private static void write(AtomicFile target,byte[] bytes)throws IOException{
  FileOutputStream out=null;try{out=target.startWrite();out.write(bytes);target.finishWrite(out);}catch(IOException error){if(out!=null)target.failWrite(out);throw error;}
 }
}
