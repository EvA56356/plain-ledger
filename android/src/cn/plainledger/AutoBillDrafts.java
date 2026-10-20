package cn.plainledger;
import android.content.*;
import org.json.JSONObject;
import java.util.UUID;

/** Common queue and cross-channel deduplication; no screen text or images are persisted. */
public final class AutoBillDrafts {
 public static final class Offer {public final JSONObject draft;public final boolean fresh;Offer(JSONObject d,boolean f){draft=d;fresh=f;}}
 public static synchronized Offer offer(Context c,long amount,String source,String channel,boolean test)throws Exception{
  if(amount<=0||amount>99999999999L)throw new IllegalArgumentException();
  SharedPreferences p=c.getSharedPreferences("auto_bill",0);long now=System.currentTimeMillis();
  String pending=p.getString("draft","null");
  if(!"null".equals(pending)){JSONObject old=new JSONObject(pending);if(now-old.optLong("created")<300000)return new Offer(old,false);}
  String key="recent_"+source+"_"+amount;
  long before=p.getLong(key,0);if(!test&&now>=before&&now-before<90000)return null;
  JSONObject d=new JSONObject();d.put("id",UUID.randomUUID().toString());d.put("amount",amount);d.put("source",source);d.put("channel",channel);d.put("test",test);d.put("created",now);
  SharedPreferences.Editor edit=p.edit().putString("draft",d.toString());
  // Bound the persisted fingerprint set. Tests never suppress real bills.
  for(String k:p.getAll().keySet())if(k.startsWith("recent_")&&now-p.getLong(k,0)>90000)edit.remove(k);
  if(!test)edit.putLong(key,now);
  if(!edit.commit())throw new IllegalStateException();return new Offer(d,true);
 }
 public static void status(Context c,String value){c.getSharedPreferences("auto_bill",0).edit().putString("status",value).putLong("status_at",System.currentTimeMillis()).apply();}
 public static void open(Context c){c.startActivity(new Intent(c,MainActivity.class).setAction(c.getPackageName()+".AUTO_BILL").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_SINGLE_TOP|Intent.FLAG_ACTIVITY_CLEAR_TOP));}
}
