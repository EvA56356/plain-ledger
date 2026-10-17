package cn.plainledger;
import android.content.*;
import org.json.*;
import java.net.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
/** Native-only HTTPS transport; session cookies never enter JavaScript. */
public class CloudTransport {
 private final android.content.SharedPreferences prefs;
 public CloudTransport(Context context){prefs=context.getSharedPreferences("cloud_connection",0);}
 public String endpoint(){return prefs.getString("endpoint","");}
 public String configure(String address){try{
  URI u=new URI(address.trim());String host=u.getHost();boolean loopback="127.0.0.1".equals(host)||"localhost".equals(host);
  if(host==null||u.getUserInfo()!=null||u.getQuery()!=null||u.getFragment()!=null||!("".equals(u.getPath())||"/".equals(u.getPath()))||!("https".equals(u.getScheme())||loopback&&"http".equals(u.getScheme())))throw new Exception();
  String value=address.trim().replaceAll("/+$","");if(!value.equals(endpoint()))prefs.edit().putString("endpoint",value).remove("cookie").commit();return "{\"ok\":true}";
 }catch(Exception e){return "{\"ok\":false,\"error\":\"请输入不含路径的 HTTPS 服务器地址；HTTP 仅允许本机回环地址\"}";}}
 public synchronized String request(String method,String path,String body){HttpURLConnection conn=null;try{
  if(!path.matches("/api/[a-zA-Z0-9/_-]+")||!("GET".equals(method)||"POST".equals(method)||"PUT".equals(method))||endpoint().isEmpty())throw new Exception();
  conn=(HttpURLConnection)new URL(endpoint()+path).openConnection();conn.setInstanceFollowRedirects(false);conn.setRequestMethod(method);conn.setConnectTimeout(10000);conn.setReadTimeout(15000);conn.setRequestProperty("X-Requested-With","PlainLedger");conn.setRequestProperty("Content-Type","application/json");String cookie=prefs.getString("cookie","");if(!cookie.isEmpty())conn.setRequestProperty("Cookie",cookie);
  if(!"GET".equals(method)){byte[] bytes=body.getBytes(StandardCharsets.UTF_8);if(bytes.length>10000000)throw new Exception();conn.setDoOutput(true);try(OutputStream out=conn.getOutputStream()){out.write(bytes);}}
  int status=conn.getResponseCode();String set=conn.getHeaderField("Set-Cookie");if(set!=null&&set.startsWith("ledger_session="))prefs.edit().putString("cookie",set.split(";",2)[0]).commit();
  InputStream stream=status>=400?conn.getErrorStream():conn.getInputStream();if(stream==null)throw new Exception();ByteArrayOutputStream out=new ByteArrayOutputStream();try(InputStream in=stream){byte[] buffer=new byte[8192];int n;while((n=in.read(buffer))!=-1){if(out.size()+n>10000000)throw new Exception();out.write(buffer,0,n);}}
  JSONObject result=new JSONObject();result.put("status",status);result.put("data",new JSONObject(new String(out.toByteArray(),StandardCharsets.UTF_8)));return result.toString();
 }catch(Exception e){return "{\"status\":503,\"data\":{\"error\":\"无法连接服务器，请检查地址、网络及 HTTPS 证书\"}}";}finally{if(conn!=null)conn.disconnect();}}
}
