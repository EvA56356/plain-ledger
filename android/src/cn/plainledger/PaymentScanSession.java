package cn.plainledger;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.Set;
import java.util.Collections;
import java.util.Locale;
import java.util.regex.Pattern;

/** In-memory observation lifetime. Never persists screen text. */
final class PaymentScanSession {
 static final long WATCH_MS = 15000, EVENT_MS = 1500;
 private String pkg = "", activity = "";
 private long generation, deadline;
 private int window = -1, activityWindow = -1;
 private boolean awaitingAuthExit;
 private long handledToken = -1, handledAmount;
 private List<String> handledText = Collections.emptyList();
 private static final Pattern BLOCKED = Pattern.compile("退款成功|退款中|退款金额|退回金额|转账|收款成功|收钱到账|支付失败|付款失败|等待支付|待支付|按住说话|切换到按住说话|发送给朋友");
 private static final Pattern PAYMENT_HINT = Pattern.compile("支付|付款|交易成功|实付|收银|[¥￥]");
 private static final Pattern SUCCESS = Pattern.compile("支付成功|付款成功|交易成功");
 private static final Pattern RECEIPT_LAYOUT = Pattern.compile("付款方式|支付方式|返回商家|回首页|(?m)^\\s*完成\\s*$");
 private static final Pattern AUTH_PROMPT = Pattern.compile("请输入|输入密码|支付密码|验证码|验证身份|身份验证|请验证|正在验证|人脸识别|指纹验证|登录|口令|password|verification code|authenticate|sign in", Pattern.CASE_INSENSITIVE);
 private final ArrayDeque<EventText> events = new ArrayDeque<>();
 private final ArrayDeque<EventText> evidence = new ArrayDeque<>();
 private static final class EventText {
  final int window; final String text; final long time; final boolean blocked;
  EventText(int w, String t, long n, boolean b) { window = w; text = t; time = n; blocked = b; }
 }
 static boolean supported(String p) {
  return "com.tencent.mm".equals(p) || "com.eg.android.AlipayGphone".equals(p)
    || "com.sankuai.meituan".equals(p) || "com.sankuai.meituan.takeoutnew".equals(p);
 }
 void observe(String p, String nextActivity, long now) {
  observe(p, nextActivity, -1, now);
 }
 void observe(String p, String nextActivity, int observedWindow, long now) {
  if (!pkg.equals(p) || (!nextActivity.isEmpty() && !activity.equals(nextActivity))) {
   generation++; events.clear(); evidence.clear(); window = -1;
   handledToken = -1; handledText = Collections.emptyList();
   if (!pkg.equals(p)) { activity = ""; activityWindow = -1; awaitingAuthExit = false; }
  } else if (!active(now)) {
   // A later observation cannot revive a callback from an expired watch.
   generation++; events.clear(); evidence.clear();
   if (handledToken >= 0) handledToken = generation;
  }
  pkg = p;
  if (!nextActivity.isEmpty()) { activity = nextActivity; activityWindow = observedWindow; awaitingAuthExit = false; }
  deadline = now + WATCH_MS;
 }
 boolean remember(int window, String text, long now) {
  prune(now);
  if (text.trim().isEmpty()) return false;
  // Keep strong evidence apart from animated child fragments: an eight-event ring
  // can otherwise lose a receipt in under 300 ms during a 35 ms event storm.
  boolean blocked = BLOCKED.matcher(text).find();
  PaymentParser.Result parsed = SUCCESS.matcher(text).find() ? PaymentParser.inspect(text) : null;
  blocked |= parsed != null && parsed.status.contains("多个金额");
  if (blocked || parsed != null && parsed.amount > 0) {
   final boolean reject = blocked;
   if (blocked) {
    events.removeIf(e -> e.window == window);
    evidence.removeIf(e -> e.window == window);
   } else {
    evidence.removeIf(e -> e.window == window && (e.blocked || e.text.equals(text)));
    events.removeIf(e -> e.window == window && e.blocked);
   }
   while (evidence.size() >= 4) evidence.removeFirst();
   evidence.addLast(new EventText(window, text, now, reject)); return true;
  }
  for (EventText e : events) if (e.window == window && e.text.equals(text)) return false;
  while (events.size() >= 8) events.removeFirst();
  events.addLast(new EventText(window, text, now, false)); return false;
 }
 List<String> recent(int window, long now) {
  prune(now); List<String> out = new ArrayList<>();
  for (EventText e : events) if (window < 0 || e.window == window) out.add(e.text);
  for (EventText e : evidence) if (window < 0 || e.window == window) out.add(e.text);
  return out;
 }
 private void prune(long now) {
  while (!events.isEmpty() && now - events.peekFirst().time > EVENT_MS) events.removeFirst();
  while (!evidence.isEmpty() && now - evidence.peekFirst().time > EVENT_MS) evidence.removeFirst();
 }
 void window(int value) {
  boolean replaced = window >= 0 && value >= 0 && window != value;
  if (replaced) {
   generation++;
   handledToken = -1; handledText = Collections.emptyList();
   // The window event may arrive before this scan; keep only the new window's evidence.
   events.removeIf(e -> e.window != value);
   evidence.removeIf(e -> e.window != value);
  }
  if (value >= 0 && !activity.isEmpty() && (activityWindow >= 0 ? activityWindow != value : replaced)) {
   // An Activity hint belongs to its own window. A vanished authentication
   // window is not the new payment window, but unknown auth surfaces stay gated.
   awaitingAuthExit |= sensitiveName(activity); activity = ""; activityWindow = -1;
  }
  window = value;
 }
 void stop() { generation++; pkg = ""; activity = ""; window = -1; activityWindow = -1; awaitingAuthExit = false; deadline = 0; events.clear(); evidence.clear(); handledToken = -1; handledText = Collections.emptyList(); }
 boolean active(long now) { return !pkg.isEmpty() && now <= deadline; }
 boolean accepts(long token, String p, long now) { return generation == token && pkg.equals(p) && active(now); }
 long token() { return generation; }
 String pkg() { return pkg; }
 String activity() { return activity; }
 boolean sensitiveActivity() { return awaitingAuthExit || sensitiveName(activity); }
 void confirmPaymentSurface(List<String> live, List<String> recent, boolean passwordVisible) {
  if (!awaitingAuthExit || passwordVisible) return;
  List<String> current = live.isEmpty() ? recent : live;
  if (blocking(current)) return;
  boolean layout = false;
  for (String text : current) {
   if (AUTH_PROMPT.matcher(text).find()) return;
   layout |= RECEIPT_LAYOUT.matcher(text).find();
  }
  // A blank new window alone never removes the authentication boundary. A
  // completed receipt in that window can, even if it omitted an Activity event.
  if (choose(current).amount > 0 || layout && success(current)) awaitingAuthExit = false;
 }
 private static boolean sensitiveName(String name) { return name.toLowerCase(Locale.ROOT).matches(".*(login|password|verify|authentication|security|fingerprint|faceid).*"); }
 void markHandled(long amount, List<String> live) {
  handledToken = generation; handledAmount = amount; handledText = new ArrayList<>(live);
 }
 boolean pageHandled(List<String> live, List<String> recent) {
  if (handledToken != generation) return false;
  PaymentParser.Result current = resolve(live, recent);
  if (current.amount > 0) return current.amount == handledAmount;
  if (blocking(live.isEmpty() ? recent : live)) { handledToken = -1; handledText = Collections.emptyList(); return false; }
  if (live.isEmpty() && !blocking(recent)) return true;
  if (live.equals(handledText) || success(live)) return true;
  handledToken = -1; handledText = Collections.emptyList(); return false;
 }
 static boolean ocrAllowed(List<String> live, List<String> recent, String activity) {
  List<String> evidence = live.isEmpty() ? recent : live;
  if (blocking(evidence)) return false;
  if (evidence.isEmpty()) return true;
  for (String text : evidence) if (PAYMENT_HINT.matcher(text).find()) return true;
  return activity.toLowerCase(Locale.ROOT).matches(".*(pay|wallet|cashier|msp|xriver).*");
 }
 static boolean acceptsOcr(long amount, List<String> captured, List<String> live, List<String> recent, String activity) {
  if (amount <= 0 || !ocrAllowed(live, recent, activity)) return false;
  PaymentParser.Result current = resolve(live, recent);
  if (current.amount > 0) return current.amount == amount;
  if (blocking(recent)) return false;
  // In the same WebView/window, navigation need not produce a new Activity identity.
  // Changed readable text needs a current matching amount; otherwise take a fresh image.
  return live.isEmpty() || live.equals(captured);
 }
 static PaymentParser.Result resolve(List<String> live, List<String> recent) {
  PaymentParser.Result result = choose(live);
  // A recent success must never override a readable failure, chat, different amount or navigation.
  if (live.isEmpty() || result.status.equals("支付成功，但未找到可确认的金额")) {
   PaymentParser.Result fallback = choose(recent);
   if (fallback.amount > 0) return fallback;
  }
  return result;
 }
 private static PaymentParser.Result choose(List<String> snapshots) {
  PaymentParser.Result result = PaymentParser.inspect(""); Set<Long> values = new HashSet<>();
  for (String text : snapshots) {
   PaymentParser.Result candidate = PaymentParser.inspect(text);
   if (BLOCKED.matcher(text).find() || candidate.status.contains("多个金额")) return new PaymentParser.Result(0, candidate.status);
   if (candidate.amount > 0) { values.add(candidate.amount); result = candidate; }
   else if (result.amount == 0) result = candidate;
  }
  return values.size() > 1 ? new PaymentParser.Result(0, "不同窗口提供多个金额，未自动选择") : result;
 }
 private static boolean blocking(List<String> snapshots) {
  for (String text : snapshots) if (BLOCKED.matcher(text).find() || PaymentParser.inspect(text).status.contains("多个金额")) return true;
  return choose(snapshots).status.contains("多个金额");
 }
 private static boolean success(List<String> snapshots) {
  for (String text : snapshots) if (SUCCESS.matcher(text).find()) return true;
  return false;
 }
}
