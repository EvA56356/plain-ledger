package cn.plainledger;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.SystemClock;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.chinese.ChineseTextRecognizerOptions;
import java.util.*;

/** Bundled on-device Chinese model. Neither images nor recognized text are retained here. */
public final class LocalOcr {
 public interface Callback { void done(String text,long elapsed); void failed(String reason); }
 private static TextRecognizer recognizer;private static boolean warmed;
 public static synchronized void warmUp(Context c){if(warmed)return;warmed=true;Bitmap b=Bitmap.createBitmap(64,64,Bitmap.Config.ARGB_8888);b.eraseColor(android.graphics.Color.WHITE);read(c,b,new Callback(){public void done(String t,long ms){}public void failed(String reason){}});}
 public static synchronized TextRecognizer client(){
  if(recognizer==null)recognizer=TextRecognition.getClient(new ChineseTextRecognizerOptions.Builder().build());
  return recognizer;
 }
 public static void read(Context context,Bitmap image,Callback callback){
  long started=SystemClock.elapsedRealtime();
  try{client().process(InputImage.fromBitmap(image,0))
   .addOnSuccessListener(result->{try{callback.done(ordered(result),SystemClock.elapsedRealtime()-started);}finally{image.recycle();}})
   .addOnFailureListener(error->{image.recycle();callback.failed("本地中文模型识别失败，请重试或手动填写");});
  }catch(Exception error){image.recycle();callback.failed("本地中文模型初始化失败，请重新打开应用");}
 }
 private static String ordered(Text text){
  List<Text.Line> lines=new ArrayList<>();
  for(Text.TextBlock b:text.getTextBlocks())lines.addAll(b.getLines());
  lines.sort(Comparator.comparingInt((Text.Line l)->l.getBoundingBox()==null?Integer.MAX_VALUE:l.getBoundingBox().top)
   .thenComparingInt(l->l.getBoundingBox()==null?0:l.getBoundingBox().left));
  StringBuilder out=new StringBuilder();
  // Preserve visual rows, e.g. "当前状态 支付成功" or the payment-success heading plus amount.
  List<Text.Line> row=new ArrayList<>();int top=0,height=0;
  for(Text.Line l:lines){android.graphics.Rect box=l.getBoundingBox();if(box==null)continue;
   if(!row.isEmpty()&&box.top-top>Math.max(8,height/2)){appendRow(out,row);row.clear();}
   if(row.isEmpty()){top=box.top;height=box.height();}row.add(l);
  }
  appendRow(out,row);return out.toString();
 }
 private static void appendRow(StringBuilder out,List<Text.Line> row){
  row.sort(Comparator.comparingInt(l->l.getBoundingBox().left));
  for(Text.Line l:row)if(out.length()<60000)out.append(l.getText()).append(' ');
  out.append('\n');
 }
}
