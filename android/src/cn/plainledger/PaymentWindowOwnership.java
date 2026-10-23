package cn.plainledger;

import java.util.LinkedHashMap;
import java.util.Map;

/** Package/window identities only. A surface without a root can still have a focused window ID. */
final class PaymentWindowOwnership {
 private static final class Identity {
  final String pkg; final long at;
  Identity(String p, long n) { pkg = p; at = n; }
 }
 private final Map<Integer,Identity> windows = new LinkedHashMap<>();
 void observe(int id, String pkg, long now) {
  if (id < 0 || pkg == null || pkg.isEmpty() || "null".equals(pkg)) return;
  windows.remove(id); windows.put(id, new Identity(pkg, now));
  while (windows.size() > 16) windows.remove(windows.keySet().iterator().next());
 }
 String owner(int id, long now) {
  Identity identity = windows.get(id);
  return identity != null && now >= identity.at && now - identity.at <= PaymentScanSession.WATCH_MS ? identity.pkg : "";
 }
 void clear() { windows.clear(); }
}
