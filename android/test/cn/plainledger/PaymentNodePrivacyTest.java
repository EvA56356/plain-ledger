package cn.plainledger;

import android.view.accessibility.AccessibilityNodeInfo;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class PaymentNodePrivacyTest {
 @Test public void hiddenPasswordDoesNotBlockReceiptOrExposeItsText() {
  AccessibilityNodeInfo node = AccessibilityNodeInfo.obtain();
  try {
   node.setPassword(true); node.setVisibleToUser(false); node.setText("synthetic secret");
   int[] counts = new int[5]; StringBuilder text = new StringBuilder();
   AutoBillService.collect(node, text, 0, counts);
   assertEquals(0, counts[4]); assertEquals("", text.toString());
  } finally { node.recycle(); }
 }
 @Test public void visiblePasswordBlocksRecognitionWithoutExposingItsText() {
  AccessibilityNodeInfo node = AccessibilityNodeInfo.obtain();
  try {
   node.setPassword(true); node.setVisibleToUser(true); node.setText("synthetic secret");
   int[] counts = new int[5]; StringBuilder text = new StringBuilder();
   AutoBillService.collect(node, text, 0, counts);
   assertEquals(1, counts[4]); assertEquals("", text.toString());
  } finally { node.recycle(); }
 }
 @Test public void visibleContentDescriptionSuppliesAccessibleAmount() {
  AccessibilityNodeInfo node = AccessibilityNodeInfo.obtain();
  try {
   node.setVisibleToUser(true); node.setContentDescription("支付成功 ¥12.34");
   int[] counts = new int[5]; StringBuilder text = new StringBuilder();
   AutoBillService.collect(node, text, 0, counts);
   assertEquals(1234, PaymentParser.parse(text.toString())); assertEquals(0, counts[4]);
  } finally { node.recycle(); }
 }
}
