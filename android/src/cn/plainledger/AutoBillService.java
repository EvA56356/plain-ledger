package cn.plainledger;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.KeyguardManager;
import android.content.*;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.*;
import android.view.*;
import android.view.accessibility.*;
import android.widget.*;
import org.json.JSONObject;
import java.util.*;

public class AutoBillService extends AccessibilityService {
 private static volatile boolean connected;
 private static AutoBillService live;
 public static boolean isConnected() { return connected; }
 private final Handler handler = new Handler(Looper.getMainLooper());
 private final PaymentScanSession session = new PaymentScanSession();
 private final PaymentWindowOwnership ownership = new PaymentWindowOwnership();
 private View overlay;
 private boolean queued, ocrBusy;
 private long lastOcr, lastImageHash, imageHashAt, imageToken = -1, secureToken = -1, lastScan, scanAt;
 private String presentedId = "", overlayId = "";
 private int screenshotFailures;
 private int[] statistics = new int[5];
 private int roots, readErrors;
 private final Runnable scan = () -> { queued = false; scanPage(); };
 private SharedPreferences prefs() { return getSharedPreferences("auto_bill", 0); }
 private boolean paymentApp(String p) { return PaymentScanSession.supported(p); }
 private long now() { return SystemClock.elapsedRealtime(); }

 private final SharedPreferences.OnSharedPreferenceChangeListener changes = (p, key) -> {
  if ("enabled".equals(key)) {
   AutoBillKeepAlive.update(this);
   if (!p.getBoolean("enabled", false)) { stopWatching(); ownership.clear(); dismiss(); status("应用内识别已暂停"); }
   else { observeForeground(); syncPendingHint(); }
  }
  if ("draft_opened".equals(key) || "draft".equals(key) || "draft_queue".equals(key)) handler.post(this::syncPendingHint);
  if ("ocr".equals(key) && p.getBoolean("ocr", false)) LocalOcr.warmUp(this);
  if ("test_request".equals(key)) handler.postDelayed(() -> {
   if (prefs().getBoolean("enabled", false)) recognize(1234, "测试", true, "弹窗测试");
  }, 3000);
 };
 @Override protected void onServiceConnected() {
  super.onServiceConnected();
  AccessibilityServiceInfo configuration = getServiceInfo();
  configuration.packageNames = null; // Outside the allowlist observe window identity only.
  configuration.flags |= AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS
    | AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS
    | AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS;
  setServiceInfo(configuration);
  connected = true; live = this;
  prefs().registerOnSharedPreferenceChangeListener(changes);
  AutoBillKeepAlive.update(this);
  if (prefs().getBoolean("ocr", false)) LocalOcr.warmUp(this);
  status(prefs().getBoolean("enabled", false) ? "服务已连接，等待支付宝、微信或美团页面" : "系统服务已连接，但应用内识别已暂停");
  if (prefs().getBoolean("enabled", false)) observeForeground();
  syncPendingHint();
 }
 private void status(String text) {
  if (!text.equals(prefs().getString("status", "")))
   prefs().edit().putString("status", text).putLong("status_at", System.currentTimeMillis()).apply();
 }
 private void stopWatching() { handler.removeCallbacks(scan); queued = false; session.stop(); }
 private void observeForeground() {
  Foreground f = foreground();
  if (paymentApp(f.pkg)) { session.observe(f.pkg, "", now()); schedule(120); }
  else if (!f.pkg.isEmpty()) stopWatching();
 }
 @Override public void onAccessibilityEvent(AccessibilityEvent event) {
  if (!prefs().getBoolean("enabled", false)) return;
  String pkg = String.valueOf(event.getPackageName()); int type = event.getEventType();
  // Even outside the allowlist this records only an identity, never text or nodes.
  ownership.observe(event.getWindowId(), pkg, now());
  if (!paymentApp(pkg)) {
   // Windows-changed can have no package. System/IME/our overlay events are not app navigation.
   if (type == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED || type == AccessibilityEvent.TYPE_WINDOWS_CHANGED) {
    Foreground f = foreground();
    if (paymentApp(f.pkg)) {
     if (!session.pkg().equals(f.pkg) || !session.active(now())) session.observe(f.pkg, "", now());
     schedule(120);
    } else if (!f.pkg.isEmpty()) stopWatching();
   }
   return;
  }
  String activity = "", name = String.valueOf(event.getClassName());
  if (type == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED && name.matches("[a-zA-Z0-9_.$]+")
    && !name.startsWith("android.") && !name.startsWith("androidx.")
    && !name.contains(".widget.") && !name.contains(".view.")
    && (name.contains("Activity") || name.endsWith("UI") || name.contains(".Msp") || name.contains(".XRiver"))) activity = name;
  session.observe(pkg, activity, event.getWindowId(), now());
  StringBuilder text = new StringBuilder();
  if (!event.isPassword()) {
   // Event storms must not trigger a cross-process tree walk per child update.
   // Read current roots only in the paced scan; never retain AccessibilityNodeInfo.
   for (CharSequence t : event.getText()) appendBounded(text, t);
   appendBounded(text, event.getContentDescription());
  }
  if (session.remember(event.getWindowId(), text.toString(), now())) scheduleUrgent();
  else schedule(120); // Leading throttle: animated children cannot postpone a pending scan.
 }
 private void schedule(long delay) {
  if (!queued && session.active(now()) && prefs().getBoolean("enabled", false)) {
   long wait = Math.max(delay, 450 - (now() - lastScan));
   queued = true; scanAt = now() + wait; handler.postDelayed(scan, wait);
  }
 }
 private void scheduleUrgent() {
  long due = now() + Math.max(120, 450 - (now() - lastScan));
  if (queued && due < scanAt) { handler.removeCallbacks(scan); queued = false; }
  schedule(120);
 }
 private boolean interactive() {
  return ((PowerManager)getSystemService(POWER_SERVICE)).isInteractive()
    && !((KeyguardManager)getSystemService(KEYGUARD_SERVICE)).isKeyguardLocked();
 }
 private static final class Foreground { String pkg = ""; int window = -1; boolean ime, fromEvent; }
 private Foreground foreground() {
  Foreground f = new Foreground(); int priority = -1;
  try {
   for (AccessibilityWindowInfo w : getWindows()) try {
    if (w.getType() == AccessibilityWindowInfo.TYPE_INPUT_METHOD) { f.ime = true; continue; }
    if (w.getType() != AccessibilityWindowInfo.TYPE_APPLICATION || (!w.isFocused() && !w.isActive())) continue;
    int rank = w.isFocused() ? 2 : 1;
    String p = ""; boolean fromEvent = false;
    AccessibilityNodeInfo root = w.getRoot();
    if (root != null) try { p = String.valueOf(root.getPackageName()); } finally { root.recycle(); }
    if (p.isEmpty() || "null".equals(p)) { p = ownership.owner(w.getId(), now()); fromEvent = !p.isEmpty(); }
    // An unknown focused window must outrank an old active background window too.
    if (rank > priority) { priority = rank; f.pkg = p; f.window = w.getId(); f.fromEvent = fromEvent; }
   } finally { w.recycle(); }
   if (f.pkg.isEmpty()) {
    AccessibilityNodeInfo root = getRootInActiveWindow();
    if (root != null) try {
     String p = String.valueOf(root.getPackageName());
     if (!"null".equals(p) && (f.window < 0 || f.window == root.getWindowId())) { f.pkg = p; f.window = root.getWindowId(); f.fromEvent = false; }
    } finally { root.recycle(); }
   }
  } catch (Exception ignored) {}
  return f;
 }
 private boolean current(long token, String pkg) {
  return connected && prefs().getBoolean("enabled", false) && session.accepts(token, pkg, now())
    && interactive() && pkg.equals(foreground().pkg);
 }
 private void scanPage() {
  if (!prefs().getBoolean("enabled", false) || !session.active(now())) return;
  if (!interactive()) { stopWatching(); return; }
  Foreground f = foreground(); String pkg = session.pkg();
  if (!f.pkg.isEmpty() && !pkg.equals(f.pkg)) { stopWatching(); return; }
  if (f.pkg.isEmpty()) { ocrSkipped("暂未取得前台应用窗口，等待窗口就绪"); schedule(450); return; }
  session.window(f.window);
  lastScan = now();
  List<String> snapshots = readPage(f, pkg);
  List<String> recent = session.recent(f.window, now());
  session.confirmPaymentSurface(snapshots, recent, statistics[4] > 0);
  saveDiagnostic(f);
  if (statistics[4] > 0 || sensitiveActivity()) { status("登录、密码或验证界面已跳过识别"); schedule(900); return; }
  PaymentParser.Result parsed = PaymentScanSession.resolve(snapshots, recent);
  if (session.pageHandled(snapshots, recent)) { schedule(900); return; }
  if (parsed.amount > 0) {
   if (current(session.token(), pkg) && recognize(parsed.amount, sourceName(pkg), false, "页面文字")) session.markHandled(parsed.amount, snapshots);
   schedule(900); return;
  }
  status(snapshots.isEmpty() ? (readErrors > 0 ? "窗口读取异常，正在重试" : roots == 0 ? "支付窗口尚未就绪，正在重试" : "页面无可读文字，等待本地 OCR") : parsed.status);
  if (prefs().getBoolean("ocr", false) && PaymentScanSession.ocrAllowed(snapshots, recent, session.activity())) offerOcr(f, snapshots);
  schedule(900); // Late WebView/render completion can happen without another event.
 }
 private List<String> readPage(Foreground f, String pkg) {
  statistics = new int[5]; roots = 0; readErrors = 0;
  List<String> snapshots = new ArrayList<>(); Set<Integer> seen = new HashSet<>();
  try {
   AccessibilityNodeInfo root = getRootInActiveWindow();
   if (root != null) try { snapshot(root, pkg, f.window, seen, snapshots); } finally { root.recycle(); }
  } catch (Exception ignored) { readErrors++; }
  try {
   for (AccessibilityWindowInfo w : getWindows()) try {
    if (w.getType() != AccessibilityWindowInfo.TYPE_APPLICATION || (!w.isActive() && !w.isFocused())) continue;
    AccessibilityNodeInfo root = w.getRoot();
    if (root != null) try { snapshot(root, pkg, f.window, seen, snapshots); } finally { root.recycle(); }
   } catch (Exception ignored) { readErrors++; } finally { w.recycle(); }
  } catch (Exception ignored) { readErrors++; }
  return snapshots;
 }
 private void snapshot(AccessibilityNodeInfo n, String pkg, int window, Set<Integer> seen, List<String> snapshots) {
  if (!pkg.equals(String.valueOf(n.getPackageName())) || (window >= 0 && n.getWindowId() != window) || seen.contains(n.getWindowId())) return;
  roots++; StringBuilder text = new StringBuilder(); collect(n, text, 0, statistics);
  // An empty/stale active root must not hide a later readable root with the same window ID.
  if (!text.toString().trim().isEmpty()) { seen.add(n.getWindowId()); snapshots.add(text.toString()); }
 }
 static void collect(AccessibilityNodeInfo n, StringBuilder out, int depth, int[] count) {
  if (depth > 40 || count[0]++ >= 1600 || out.length() >= 30000) return;
  if (n.isPassword()) { if (n.isVisibleToUser()) count[4]++; return; }
  if (n.isVisibleToUser()) {
   count[1]++; CharSequence t = n.getText(), d = n.getContentDescription();
   if (t != null) { count[2] += t.length(); appendBounded(out, t); }
   if (d != null && !d.equals(t)) { count[3] += d.length(); appendBounded(out, d); }
  }
  for (int i = 0, children = n.getChildCount(); i < children && count[0] < 1600 && out.length() < 30000; i++) {
   AccessibilityNodeInfo child = n.getChild(i);
   if (child != null) try { collect(child, out, depth + 1, count); } finally { child.recycle(); }
  }
 }
 private static void appendBounded(StringBuilder text, CharSequence value) {
  if (value == null || text.length() >= 30000) return;
  int limit = Math.min(value.length(), 29999 - text.length());
  text.append(value, 0, limit).append('\n');
 }
 private String sourceName(String pkg) { return "com.tencent.mm".equals(pkg) ? "微信" : "com.eg.android.AlipayGphone".equals(pkg) ? "支付宝" : "美团"; }
 private void saveDiagnostic(Foreground f) {
  try {
   JSONObject d = new JSONObject(); d.put("windows", roots); d.put("nodes", statistics[0]); d.put("visible", statistics[1]);
   d.put("textChars", statistics[2]); d.put("descriptionChars", statistics[3]); d.put("readErrors", readErrors);
   d.put("passwordNodesSkipped", statistics[4]); d.put("package", session.pkg()); d.put("activity", session.activity());
   d.put("capabilities", getServiceInfo().getCapabilities()); d.put("serviceFlags", getServiceInfo().flags);
   d.put("sdk", Build.VERSION.SDK_INT); d.put("foregroundPackage", f.pkg); d.put("window", f.window); d.put("foregroundFromEvent", f.fromEvent); d.put("watching", session.active(now()));
   prefs().edit().putString("diagnostic", d.toString()).apply();
  } catch (Exception ignored) {}
 }
 private void ocrSkipped(String reason) { prefs().edit().putString("capture_status", reason).apply(); status(reason); }
 private void offerOcr(Foreground f, List<String> snapshots) {
  if (ocrBusy || secureToken == session.token() || !prefs().getBoolean("ocr", false)) return;
  if (Build.VERSION.SDK_INT < 30) { ocrSkipped("系统版本不支持无障碍截图"); return; }
  if (f.ime) { ocrSkipped("检测到输入法窗口，等待输入法关闭"); return; }
  if (statistics[4] > 0 || sensitiveActivity()) {
   ocrSkipped("登录、密码或验证界面已跳过自动 OCR"); return;
  }
  if (now() - lastOcr >= 1500) capture(f, false, snapshots);
 }
 private boolean sensitiveActivity() { return session.sensitiveActivity(); }
 private boolean captureCurrent(long token, String pkg, int window) {
  Foreground f = foreground();
  return current(token, pkg) && !f.ime && (window < 0 || f.window == window) && !sensitiveActivity();
 }
 private void capture(Foreground f, boolean displayFallback, List<String> snapshots) {
  final String pkg = session.pkg(); final long token = session.token();
  final List<String> captured = new ArrayList<>(snapshots);
  if (!captureCurrent(token, pkg, f.window) || !prefs().getBoolean("ocr", false)) return;
  ocrBusy = true; lastOcr = now(); status("正在本机识别支付截图，图片不保存、不上传");
  try {
   TakeScreenshotCallback callback = new TakeScreenshotCallback() {
    public void onSuccess(ScreenshotResult result) {
     Bitmap image = null;
     try {
      android.hardware.HardwareBuffer buffer = result.getHardwareBuffer();
      try { Bitmap hardware = Bitmap.wrapHardwareBuffer(buffer, result.getColorSpace());
       if (hardware != null) { image = hardware.copy(Bitmap.Config.ARGB_8888, false); hardware.recycle(); }
      } finally { buffer.close(); }
      if (image == null) throw new IllegalStateException();
      if (!captureCurrent(token, pkg, f.window) || !prefs().getBoolean("ocr", false)) { image.recycle(); finishCapture(); return; }
      Foreground latest = foreground(); List<String> latestText = readPage(latest, pkg);
      if (statistics[4] > 0 || !PaymentScanSession.ocrAllowed(latestText, session.recent(latest.window, now()), session.activity())) {
       image.recycle(); finishCapture(); return;
      }
      Bitmap sample = Bitmap.createScaledBitmap(image, 64, 128, true); long hash = 0; boolean nonBlack = false;
      for (int y = 7; y < 118; y++) for (int x = 0; x < 64; x++) {
       int pixel = sample.getPixel(x, y); hash = 31 * hash + (pixel & 0x00F8F8F8);
       if (((pixel >> 16) & 255) > 32 || ((pixel >> 8) & 255) > 32 || (pixel & 255) > 32) nonBlack = true;
      }
      sample.recycle();
      if (!nonBlack) { image.recycle(); ocrSkipped("系统截图返回空白画面，等待页面更新；请复制诊断信息"); finishCapture(); return; }
      screenshotFailures = 0; prefs().edit().putString("capture_status", "已取得非空白截图，仅内存处理").apply();
      if (token == imageToken && hash == lastImageHash && now() - imageHashAt < 3000) { image.recycle(); finishCapture(); return; }
      lastImageHash = hash; imageHashAt = now(); imageToken = token;
      LocalOcr.read(AutoBillService.this, image, new LocalOcr.Callback() {
       public void done(String text, long elapsed) {
        prefs().edit().putLong("ocr_ms", elapsed).apply();
        if (captureCurrent(token, pkg, f.window) && prefs().getBoolean("ocr", false)) {
         PaymentParser.Result parsed = PaymentParser.inspectOcr(text);
         Foreground latest = foreground(); List<String> currentText = readPage(latest, pkg);
         List<String> recent = session.recent(latest.window, now());
         if (statistics[4] > 0) status("密码或验证界面已跳过过期 OCR 结果");
         else if (session.pageHandled(currentText, recent)) { /* Another channel already delivered this page. */ }
         else if (parsed.amount > 0 && PaymentScanSession.acceptsOcr(parsed.amount, captured, currentText, recent, session.activity())) {
          if (recognize(parsed.amount, sourceName(pkg), false, "本地 OCR")) session.markHandled(parsed.amount, currentText);
         } else if (parsed.amount > 0) status("页面文字已变化，已丢弃过期 OCR 结果并等待重扫");
         else status("本地 OCR 已完成：" + parsed.status);
        }
        finishCapture();
       }
       public void failed(String reason) { imageToken = -1; if (current(token, pkg)) status(reason); finishCapture(); }
      });
     } catch (Exception error) {
      if (image != null && !image.isRecycled()) image.recycle(); if (current(token, pkg)) status("截图转换失败，正在等待重试"); finishCapture();
     }
    }
    public void onFailure(int code) {
     // Retry a stale window through the public display API; never bypass a secure window.
     if (!displayFallback && Build.VERSION.SDK_INT >= 34 && f.window >= 0 && code == ERROR_TAKE_SCREENSHOT_INVALID_WINDOW
       && captureCurrent(token, pkg, f.window)) { ocrBusy = false; capture(foreground(), true, captured); return; }
     if (!current(token, pkg)) { finishCapture(); return; }
     if (code == ERROR_TAKE_SCREENSHOT_SECURE_WINDOW) secureToken = token;
     screenshotFailures++; ocrSkipped(code == ERROR_TAKE_SCREENSHOT_SECURE_WINDOW ? "当前窗口禁止截图，请手动记账" : "系统截图失败（代码 " + code + "），正在等待重试"); finishCapture();
    }
   };
   if (Build.VERSION.SDK_INT >= 34 && f.window >= 0 && !displayFallback) takeScreenshotOfWindow(f.window, getMainExecutor(), callback);
   else takeScreenshot(Display.DEFAULT_DISPLAY, getMainExecutor(), callback);
  } catch (Exception error) { status("未取得系统截图能力，正在等待窗口就绪"); finishCapture(); }
 }
 private void finishCapture() { ocrBusy = false; schedule(screenshotFailures > 2 ? 3000 : 900); }
 private boolean recognize(long amount, String source, boolean test, String channel) {
  try {
   AutoBillDrafts.Offer offer = AutoBillDrafts.offer(this, amount, source, channel, test); if (offer == null) return true;
   String id = offer.draft.optString("id");
   boolean opened = id.equals(prefs().getString("draft_opened", ""));
   if (!opened && !id.equals(presentedId)) { presentedId = id; show(offer.draft); openDraft(); }
   status(offer.queued ? "新付款已进入待核对队列，共 " + offer.pendingCount + " 笔" : opened ? "核对窗口已打开，等待手动保存" : "已通过" + channel + "识别，等待核对窗口确认打开");
   return true;
  } catch (Exception e) { status(e instanceof IllegalStateException ? e.getMessage() : "识别成功，但草稿准备失败；请检查待核对队列"); return false; }
 }
 public static void notification(Context c, long amount, String source) {
  AutoBillService service = live;
  if (service != null) { service.handler.post(() -> service.recognize(amount, source, false, "支付通知")); return; }
  try {
   AutoBillDrafts.Offer offer = AutoBillDrafts.offer(c, amount, source, "支付通知", false);
   if (offer != null) { AutoBillDrafts.status(c, "通知已识别，账单待核对；打开简账查看"); AutoBillDrafts.open(c); }
  } catch (Exception ignored) { AutoBillDrafts.status(c, "通知已识别，打开简账核对；后台启动可能受限"); }
 }
 private void openDraft() {
  try {
   final String id = new JSONObject(AutoBillDrafts.peek(this)).optString("id");
   AutoBillDrafts.open(this);
   // startActivity can be silently denied. Only the UI receipt proves delivery.
   handler.postDelayed(() -> { try { if (!id.equals(prefs().getString("draft_opened", "")) && id.equals(new JSONObject(AutoBillDrafts.peek(this)).optString("id")))
    status("账单已识别但尚未打开核对窗口；可点击悬浮提示，并检查后台弹出权限"); } catch (Exception ignored) {} }, 2500);
  } catch (Exception e) { status("后台打开受限，请点击悬浮提示并检查应用后台弹出设置"); }
 }
 private void syncPendingHint() {
  if (!prefs().getBoolean("enabled", false)) { dismiss(); return; }
  try {
   JSONObject head = new JSONObject(AutoBillDrafts.peek(this)); String id = head.optString("id");
   if (id.isEmpty() || id.equals(prefs().getString("draft_opened", ""))) dismiss();
   else if (overlay == null || !id.equals(overlayId)) show(head);
  } catch (Exception ignored) { dismiss(); }
 }
 private void show(JSONObject draft) {
  dismiss(); long amount = draft.optLong("amount"); String source = draft.optString("source"), id = draft.optString("id"); boolean test = draft.optBoolean("test");
  LinearLayout box = new LinearLayout(this); box.setOrientation(1); int pad = (int)(20 * getResources().getDisplayMetrics().density); box.setPadding(pad, pad, pad, pad);
  android.graphics.drawable.GradientDrawable bg = new android.graphics.drawable.GradientDrawable();
  bg.setColor(MainActivity.uiColor(this, "paper", Color.WHITE)); bg.setCornerRadius(MainActivity.uiRadius(this)); box.setBackground(bg); box.setElevation(12);
  TextView title = new TextView(this); title.setText((test ? "测试弹窗 · 虚构金额" : "识别到" + source + "支出") + " ¥ " + String.format(Locale.ROOT, "%.2f", amount / 100.0) + "\n核对后保存，尚未写入账本");
  title.setTextColor(MainActivity.uiColor(this, "ink", Color.rgb(33, 78, 67))); title.setTextSize(18); box.addView(title);
  Button confirm = new Button(this); confirm.setText("打开记账核对"); confirm.setBackgroundTintList(android.content.res.ColorStateList.valueOf(MainActivity.uiColor(this, "primary", Color.rgb(33, 78, 67)))); confirm.setTextColor(Color.WHITE); box.addView(confirm);
  Button cancel = new Button(this); cancel.setText("忽略这笔"); cancel.setBackgroundTintList(android.content.res.ColorStateList.valueOf(MainActivity.uiColor(this, "soft", Color.LTGRAY))); cancel.setTextColor(MainActivity.uiColor(this, "primary", Color.rgb(33, 78, 67))); box.addView(cancel);
  confirm.setOnClickListener(v -> openDraft()); cancel.setOnClickListener(v -> { AutoBillDrafts.discard(this, id); syncPendingHint(); status("已忽略这笔识别账单，其余待核对账单已保留"); });
  WindowManager.LayoutParams lp = new WindowManager.LayoutParams(-1, -2, WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, android.graphics.PixelFormat.TRANSLUCENT);
  lp.gravity = Gravity.BOTTOM; lp.y = pad * 3;
  try { ((WindowManager)getSystemService(WINDOW_SERVICE)).addView(box, lp); overlay = box; overlayId = id; }
  catch (Exception e) { status("悬浮提示未显示，草稿已保留，可打开简账核对"); }
 }
 private void dismiss() { if (overlay != null) { try { ((WindowManager)getSystemService(WINDOW_SERVICE)).removeView(overlay); } catch (Exception ignored) {} overlay = null; } overlayId = ""; }
 @Override public void onInterrupt() { stopWatching(); dismiss(); status("无障碍服务被中断"); }
 @Override public void onDestroy() {
  connected = false; live = null; session.stop(); ownership.clear(); prefs().unregisterOnSharedPreferenceChangeListener(changes); handler.removeCallbacksAndMessages(null); dismiss(); super.onDestroy();
 }
}
