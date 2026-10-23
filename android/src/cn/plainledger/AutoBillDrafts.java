package cn.plainledger;
import android.content.*;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.*;

/** Common bounded queue and cross-channel deduplication; receipt text/images are never persisted. */
public final class AutoBillDrafts {
 private static final int CAPACITY=20;
 private static final long DEDUP_MS=90000;
 public static final class Offer {
  public final JSONObject draft;public final boolean fresh,queued;public final int pendingCount;
  Offer(JSONObject d,boolean f,boolean q,int count){draft=d;fresh=f;queued=q;pendingCount=count;}
 }
 public static synchronized Offer offer(Context c,long amount,String source,String channel,boolean test)throws Exception{
  if(amount<=0||amount>99999999999L)throw new IllegalArgumentException();
  SharedPreferences p=c.getSharedPreferences("auto_bill",0);long now=System.currentTimeMillis();List<JSONObject> drafts=read(p);
  // An unopened synthetic test must never occupy a real payment's place or capacity.
  String opened=p.getString("draft_opened","");
  if(test){for(JSONObject old:drafts)if(!old.optBoolean("test"))return new Offer(drafts.get(0),false,false,drafts.size());}
  else if(drafts.removeIf(d->d.optBoolean("test")&&!identity(d).equals(opened))){commit(write(p,drafts));notifyPending(c,drafts);}
  for(JSONObject old:drafts)if(old.optLong("amount")==amount&&Objects.equals(source,old.optString("source"))&&test==old.optBoolean("test")&&now>=old.optLong("created")&&now-old.optLong("created")<DEDUP_MS)return new Offer(drafts.get(0),false,false,drafts.size());
  String key="recent_"+source+"_"+amount;long before=p.getLong(key,0);
  if(!test&&now>=before&&now-before<DEDUP_MS)return null;
  if(drafts.size()>=CAPACITY)throw new IllegalStateException("待核对账单已达 20 笔，请先核对或忽略已有账单");
  JSONObject d=new JSONObject();d.put("id",UUID.randomUUID().toString());d.put("amount",amount);d.put("source",source);d.put("channel",channel);d.put("test",test);d.put("created",now);
  boolean fresh=drafts.isEmpty();drafts.add(d);SharedPreferences.Editor edit=write(p,drafts);
  // Bound the persisted fingerprint set. Tests never suppress real bills.
  for(Map.Entry<String,?> entry:p.getAll().entrySet())if(entry.getKey().startsWith("recent_")&&entry.getValue() instanceof Long&&now-((Long)entry.getValue())>DEDUP_MS)edit.remove(entry.getKey());
  if(!test)edit.putLong(key,now);commit(edit);notifyPending(c,drafts);return new Offer(drafts.get(0),fresh,!fresh,drafts.size());
 }
 /** Peeking never consumes a draft. Waiting for an editor must not expire a pending review. */
 public static synchronized String peek(Context c){
  SharedPreferences p=c.getSharedPreferences("auto_bill",0);List<JSONObject> drafts=read(p);if(drafts.isEmpty())return "null";
  // Also recover a queued head left by an older component removing the legacy draft key.
  if(!drafts.get(0).toString().equals(p.getString("draft","null")))commit(write(p,drafts));return drafts.get(0).toString();
 }
 /** A presentation receipt never consumes the payment. Unsaved reviews survive process death. */
 public static synchronized boolean acknowledge(Context c,String id){
  SharedPreferences p=c.getSharedPreferences("auto_bill",0);List<JSONObject> drafts=read(p);
  for(int i=0;i<drafts.size();i++)if(identity(drafts.get(i)).equals(id)){
   if(i==0&&id.equals(p.getString("draft_opened","")))return true;
   // A real offer can race the test editor's first receipt; preserve that already open editor.
   JSONObject shown=drafts.remove(i);drafts.add(0,shown);
   commit(write(p,drafts).putString("draft_opened",id).putString("status","核对窗口已打开，等待手动保存").putLong("status_at",System.currentTimeMillis()));
   return true;
  }
  return false;
 }
 public static synchronized boolean complete(Context c,String id,boolean saved){return remove(c,id,saved);}
 public static synchronized boolean contains(Context c,String id){for(JSONObject d:read(c.getSharedPreferences("auto_bill",0)))if(identity(d).equals(id))return true;return false;}
 public static synchronized boolean discard(Context c,String id){return remove(c,id,false);}
 private static boolean remove(Context c,String id,boolean saved){
  SharedPreferences p=c.getSharedPreferences("auto_bill",0);List<JSONObject> drafts=read(p);
  if(id==null)return false;
  boolean removed=drafts.removeIf(d->id.equals(identity(d)));if(!removed)return false;
  SharedPreferences.Editor edit=write(p,drafts).putString("status",saved?"识别账单已保存，其余待核对账单已保留":"已忽略识别账单，其余待核对账单已保留").putLong("status_at",System.currentTimeMillis());
  if(id.equals(p.getString("draft_opened","")))edit.remove("draft_opened");commit(edit);notifyPending(c,drafts);return true;
 }
 /** Stable entry IDs let a committed save recover from a crash before its preference receipt. */
 public static synchronized void reconcileSaved(Context c,Set<String> entryIds){
  SharedPreferences p=c.getSharedPreferences("auto_bill",0);List<JSONObject> drafts=read(p);
  Set<String> removed=new HashSet<>();for(JSONObject d:drafts)if(entryIds.contains("auto-"+identity(d)))removed.add(identity(d));
  if(removed.isEmpty()){notifyPending(c,drafts);return;}
  drafts.removeIf(d->removed.contains(identity(d)));SharedPreferences.Editor edit=write(p,drafts);
  if(removed.contains(p.getString("draft_opened","")))edit.remove("draft_opened");commit(edit);notifyPending(c,drafts);
 }
 private static void notifyPending(Context c,List<JSONObject> drafts){int count=0;for(JSONObject d:drafts)if(!d.optBoolean("test"))count++;AutoBillReviewNotification.update(c,count);}
 private static String identity(JSONObject draft){return draft.optString("id",String.valueOf(draft.optLong("created")));}
 private static List<JSONObject> read(SharedPreferences p){
  List<JSONObject> drafts=new ArrayList<>();Set<String> ids=new HashSet<>();
  try{add(drafts,ids,new JSONObject(p.getString("draft","null")));}catch(Exception ignored){}
  try{JSONArray queue=new JSONArray(p.getString("draft_queue","[]"));for(int i=0;i<queue.length()&&drafts.size()<CAPACITY;i++)add(drafts,ids,queue.optJSONObject(i));}catch(Exception ignored){}
  return drafts;
 }
 private static void add(List<JSONObject> drafts,Set<String> ids,JSONObject d){if(d!=null&&d.optLong("amount")>0&&d.optLong("amount")<=99999999999L&&ids.add(identity(d)))drafts.add(d);}
 private static SharedPreferences.Editor write(SharedPreferences p,List<JSONObject> drafts){
  SharedPreferences.Editor edit=p.edit();if(drafts.isEmpty())edit.remove("draft");else edit.putString("draft",drafts.get(0).toString());
  JSONArray queue=new JSONArray();for(int i=1;i<drafts.size();i++)queue.put(drafts.get(i));return edit.putString("draft_queue",queue.toString());
 }
 private static void commit(SharedPreferences.Editor edit){if(!edit.commit())throw new IllegalStateException("识别草稿无法写入本机存储");}
 public static void status(Context c,String value){c.getSharedPreferences("auto_bill",0).edit().putString("status",value).putLong("status_at",System.currentTimeMillis()).apply();}
 public static void open(Context c){c.startActivity(new Intent(c,MainActivity.class).setAction(c.getPackageName()+".AUTO_BILL").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_SINGLE_TOP|Intent.FLAG_ACTIVITY_CLEAR_TOP));}
}
