package com.secneo.apkwrapper;

import android.app.Application;
import android.content.pm.ApplicationInfo;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelFileDescriptor;
import android.os.SystemClock;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/* JADX INFO: loaded from: classes.dex */
public class H {
    public static Application sApp;
    public static ApplicationInfo sAppInfo;
    private static Boolean isLoaded = Boolean.FALSE;
    public static String PKGNAME = "com.youqi.miaomiao";
    public static String APPNAME = "com.lanniser.kittykeeping.KittyApplication";
    public static String ACFNAME = "androidx.core.app.CoreComponentFactory";
    public static String ISSOPHIX = "###SOPHIX###";
    public static String HAVEX86 = "###HAVEX86###";
    public static String HAVEX8664 = "###HAVEX8664###";
    public static String X86_LIBRARY = "DexHelper-x86";
    public static String ARM_LIBRARY = "DexHelper";
    public static String ORI_AW_NAME = "com.secneo.apkwrapper.AW";

    /* JADX WARN: Unreachable blocks removed: 2, instructions: 2 */
    public static int Iii1Iii1IIIi1(int i) throws Exception {
        try {
            int[] iArr = new int[0];
            try {
                BufferedReader bufferedReader = new BufferedReader(new FileReader("/proc/self/status"));
                Throwable th = null;
                while (true) {
                    try {
                        String line = bufferedReader.readLine();
                        if (line == null) {
                            bufferedReader.close();
                            return 0;
                        }
                        if (i == 0) {
                            if (line.startsWith("TracerPid:")) {
                                int i2 = Integer.parseInt(line.substring(10).trim());
                                bufferedReader.close();
                                return i2;
                            }
                        } else if (i == 1 && line.startsWith("PPid:")) {
                            int i3 = Integer.parseInt(line.substring(5).trim());
                            bufferedReader.close();
                            return i3;
                        }
                    } catch (Throwable th2) {
                        if (0 != 0) {
                            try {
                                bufferedReader.close();
                            } catch (Throwable th3) {
                                th.addSuppressed(th3);
                            }
                        } else {
                            bufferedReader.close();
                        }
                        throw th2;
                    }
                }
            } catch (IOException e) {
                Exception ex1 = new RuntimeException("read tracer", e);
                throw ex1;
            }
        } catch (Exception ex2) {
            throw ex2;
        }
    }

    /* JADX WARN: Unreachable blocks removed: 1, instructions: 1 */
    public static int Iii1Iii1IlIi1(int i) throws Exception {
        try {
            int[] iArr = new int[0];
            try {
                FileInputStream fileInputStream = new FileInputStream("/system/bin/app_process");
                Throwable th = null;
                try {
                    fileInputStream.read();
                    fileInputStream.close();
                } catch (Throwable th2) {
                    if (th != null) {
                        try {
                            fileInputStream.close();
                        } catch (Throwable th3) {
                            th.addSuppressed(th3);
                        }
                    } else {
                        fileInputStream.close();
                    }
                    throw th2;
                }
            } catch (IOException unused) {
            }
            SystemClock.sleep(2000L);
            return Iii1Iii1IIIi1(i);
        } catch (Exception ex1) {
            throw ex1;
        }
    }

    /* JADX WARN: Unreachable blocks removed: 1, instructions: 1 */
    public static int bytes2int(byte[] bArr) throws Exception {
        try {
            int[] iArr = new int[0];
            return bArr[3] | ((((((0 | bArr[0]) << 8) | bArr[1]) << 8) | bArr[2]) << 8);
        } catch (Exception ex1) {
            throw ex1;
        }
    }

    public static native String d(String str);

    /* JADX WARN: Unreachable blocks removed: 1, instructions: 1 */
    public static void g(Object obj) throws Exception {
        try {
            int[] iArr = new int[0];
        } catch (Exception ex1) {
            throw ex1;
        }
    }

    /* JADX WARN: Unreachable blocks removed: 1, instructions: 1 */
    public static Object getFieldValue(Class<?> cls, Object obj, String str) throws Exception {
        try {
            int[] iArr = new int[0];
            try {
                Field declaredField = cls.getDeclaredField(str);
                declaredField.setAccessible(true);
                return declaredField.get(obj);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                return null;
            } catch (IllegalArgumentException e2) {
                e2.printStackTrace();
                return null;
            } catch (NoSuchFieldException e3) {
                e3.printStackTrace();
                return null;
            }
        } catch (Exception ex1) {
            throw ex1;
        }
    }

    /* JADX WARN: Unreachable blocks removed: 1, instructions: 1 */
    public static Object getFieldValue(String str, Object obj, String str2) throws Exception {
        try {
            int[] iArr = new int[0];
            try {
                return getFieldValue(Class.forName(str), obj, str2);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                return null;
            } catch (IllegalArgumentException e2) {
                e2.printStackTrace();
                return null;
            }
        } catch (Exception ex1) {
            throw ex1;
        }
    }

    /* JADX WARN: Unreachable blocks removed: 1, instructions: 1 */
    private static int getRuntimeAbi() throws Exception {
        FileInputStream fileInputStream;
        try {
            int[] iArr = new int[0];
            byte[] bArr = new byte[20];
            int i = 0;
            try {
                fileInputStream = new FileInputStream("/proc/self/exe");
                try {
                    fileInputStream.read(bArr);
                    int iBytes2int = bytes2int(new byte[]{0, 0, bArr[19], bArr[18]});
                    if (iBytes2int == 3 || iBytes2int == 6 || iBytes2int == 7) {
                        i = 1;
                    } else if (iBytes2int == 62) {
                        i = 2;
                    }
                } catch (Exception unused) {
                    if (fileInputStream != null) {
                    }
                    return i;
                } catch (Throwable th) {
                    ex1 = th;
                    if (fileInputStream != null) {
                        try {
                            fileInputStream.close();
                        } catch (IOException unused2) {
                        }
                    }
                    throw ex1;
                }
            } catch (Exception unused3) {
                fileInputStream = null;
            } catch (Throwable th2) {
                ex1 = th2;
                fileInputStream = null;
            }
            try {
                fileInputStream.close();
            } catch (IOException unused4) {
            }
            return i;
        } catch (Exception ex1) {
            throw ex1;
        }
    }

    public static native int gha(String str);

    private static native void he(int i);

    /* JADX WARN: Unreachable blocks removed: 1, instructions: 1 */
    public static void init() throws Exception {
        try {
            int[] iArr = new int[0];
            Iii1Iii1IIIi1(0);
        } catch (Exception ex1) {
            throw ex1;
        }
    }

    /* JADX WARN: Unreachable blocks removed: 1, instructions: 1 */
    public static Object invokeMethod(Class<?> cls, Object obj, Object[] objArr, String str, Class<?>... clsArr) throws Exception {
        try {
            int[] iArr = new int[0];
            try {
                Method declaredMethod = cls.getDeclaredMethod(str, clsArr);
                declaredMethod.setAccessible(true);
                return declaredMethod.invoke(obj, objArr);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                return null;
            } catch (IllegalArgumentException e2) {
                e2.printStackTrace();
                return null;
            } catch (NoSuchMethodException e3) {
                e3.printStackTrace();
                return null;
            } catch (InvocationTargetException e4) {
                e4.printStackTrace();
                return null;
            }
        } catch (Exception ex1) {
            throw ex1;
        }
    }

    /* JADX WARN: Unreachable blocks removed: 1, instructions: 1 */
    public static boolean isNeedLoadX86() throws Exception {
        try {
            int[] iArr = new int[0];
            boolean zEqualsIgnoreCase = HAVEX86.equalsIgnoreCase("true");
            boolean zEqualsIgnoreCase2 = HAVEX8664.equalsIgnoreCase("true");
            int runtimeAbi = getRuntimeAbi();
            if (runtimeAbi != 1 || zEqualsIgnoreCase) {
                return runtimeAbi == 2 && !zEqualsIgnoreCase2;
            }
            return true;
        } catch (Exception ex1) {
            throw ex1;
        }
    }

    /* JADX WARN: Unreachable blocks removed: 2, instructions: 2 */
    public static void load() throws Exception {
        try {
            int[] iArr = new int[0];
            synchronized (isLoaded) {
                if (!isLoaded.booleanValue()) {
                    System.loadLibrary(isNeedLoadX86() ? X86_LIBRARY : ARM_LIBRARY);
                    isLoaded = Boolean.TRUE;
                }
            }
        } catch (Exception ex1) {
            throw ex1;
        }
    }

    /* JADX WARN: Unreachable blocks removed: 1, instructions: 1 */
    public static void main(String[] strArr) throws Exception {
        ParcelFileDescriptor parcelFileDescriptorAdoptFd;
        try {
            int[] iArr = new int[0];
            if (strArr.length != 4) {
                System.exit(1);
            }
            int i = 0;
            Throwable th = null;
            try {
                parcelFileDescriptorAdoptFd = ParcelFileDescriptor.adoptFd(Integer.parseInt(strArr[1]));
                try {
                    i = Integer.parseInt(strArr[3]);
                } catch (Exception unused) {
                    System.exit(1);
                }
            } catch (Exception unused2) {
                parcelFileDescriptorAdoptFd = null;
            }
            try {
                int iIii1Iii1IlIi1 = Iii1Iii1IlIi1(i);
                DataOutputStream dataOutputStream = new DataOutputStream(new ParcelFileDescriptor.AutoCloseOutputStream(parcelFileDescriptorAdoptFd));
                try {
                    dataOutputStream.writeUTF(Integer.toString(iIii1Iii1IlIi1));
                    dataOutputStream.close();
                } catch (Throwable th2) {
                    if (0 != 0) {
                        try {
                            dataOutputStream.close();
                        } catch (Throwable th3) {
                            th.addSuppressed(th3);
                        }
                    } else {
                        dataOutputStream.close();
                    }
                    throw th2;
                }
            } catch (Throwable unused3) {
                System.exit(1);
            }
        } catch (Exception ex1) {
            throw ex1;
        }
    }

    /* JADX WARN: Unreachable blocks removed: 1, instructions: 1 */
    public static boolean setFieldValue(Class<?> cls, Object obj, String str, Object obj2) throws Exception {
        try {
            int[] iArr = new int[0];
            try {
                Field declaredField = cls.getDeclaredField(str);
                declaredField.setAccessible(true);
                declaredField.set(obj, obj2);
                return true;
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                return false;
            } catch (IllegalArgumentException e2) {
                e2.printStackTrace();
                return false;
            } catch (NoSuchFieldException e3) {
                e3.printStackTrace();
                return false;
            }
        } catch (Exception ex1) {
            throw ex1;
        }
    }

    /* JADX WARN: Unreachable blocks removed: 1, instructions: 1 */
    public static boolean setFieldValue(String str, Object obj, String str2, Object obj2) throws Exception {
        try {
            int[] iArr = new int[0];
            try {
                setFieldValue(Class.forName(str), obj, str2, obj2);
                return true;
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                return false;
            } catch (IllegalArgumentException e2) {
                e2.printStackTrace();
                return false;
            }
        } catch (Exception ex1) {
            throw ex1;
        }
    }

    public static native void sha(String str, int i);

    /* JADX WARN: Unreachable blocks removed: 1, instructions: 1 */
    public static void showToast(String str, int i) throws Exception {
        try {
            int[] iArr = new int[0];
            Application application = sApp;
            if (application == null || application.getBaseContext() == null) {
                return;
            }
            new Handler(Looper.getMainLooper()).post(new a(str, i));
        } catch (Exception ex1) {
            throw ex1;
        }
    }

    public static native int sn(String str);
}
