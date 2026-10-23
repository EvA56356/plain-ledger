package com.secneo.apkwrapper;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AppComponentFactory;
import android.app.Application;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentProvider;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.os.Build;
import android.text.TextUtils;

/* JADX INFO: loaded from: classes.dex */
@TargetApi(28)
public final class AP extends AppComponentFactory {
    private AppComponentFactory a = null;

    /* JADX WARN: Unreachable blocks removed: 1, instructions: 1 */
    private synchronized AppComponentFactory a(ClassLoader classLoader) throws Exception {
        AppComponentFactory appComponentFactory;
        try {
            int[] iArr = new int[0];
            synchronized (this) {
                if (this.a == null && !TextUtils.isEmpty(H.ACFNAME)) {
                    try {
                        this.a = (AppComponentFactory) classLoader.loadClass(H.ACFNAME).newInstance();
                    } catch (Exception unused) {
                    }
                }
                appComponentFactory = this.a;
            }
            return appComponentFactory;
        } catch (Exception ex1) {
            throw ex1;
        }
    }

    /* JADX WARN: Unreachable blocks removed: 1, instructions: 1 */
    private static boolean a() throws Exception {
        try {
            int[] iArr = new int[0];
            return (!Boolean.parseBoolean(AW.validACF) || "androidx.core.app.CoreComponentFactory".equals(H.ACFNAME) || "android.app.AppComponentFactory".equals(H.ACFNAME) || "android.support.v4.app.CoreComponentFactory".equals(H.ACFNAME)) ? false : true;
        } catch (Exception ex1) {
            throw ex1;
        }
    }

    /* JADX WARN: Unreachable blocks removed: 1, instructions: 1 */
    public synchronized void a(AppComponentFactory appComponentFactory) throws Exception {
        try {
            int[] iArr = new int[0];
            synchronized (this) {
                this.a = appComponentFactory;
            }
        } catch (Exception ex1) {
            throw ex1;
        }
    }

    /* JADX WARN: Unreachable blocks removed: 1, instructions: 1 */
    @Override // android.app.AppComponentFactory
    public Activity instantiateActivity(ClassLoader classLoader, String str, Intent intent) throws Exception {
        try {
            int[] iArr = new int[0];
            if (a()) {
                AppComponentFactory appComponentFactoryA = a(classLoader);
                a(appComponentFactoryA);
                if (appComponentFactoryA != null) {
                    return appComponentFactoryA.instantiateActivity(classLoader, str, intent);
                }
            }
            return super.instantiateActivity(classLoader, str, intent);
        } catch (Exception ex1) {
            throw ex1;
        }
    }

    /* JADX WARN: Unreachable blocks removed: 1, instructions: 1 */
    @Override // android.app.AppComponentFactory
    public Application instantiateApplication(ClassLoader classLoader, String str) throws Exception {
        try {
            int[] iArr = new int[0];
            if (Build.VERSION.SDK_INT >= 29 && !Boolean.parseBoolean(AW.gameCenter) && !TextUtils.isEmpty(H.APPNAME)) {
                str = H.APPNAME;
            }
            if (a()) {
                AppComponentFactory appComponentFactoryA = a(classLoader);
                a(appComponentFactoryA);
                if (appComponentFactoryA != null) {
                    H.sApp = appComponentFactoryA.instantiateApplication(classLoader, str);
                }
            }
            if (H.sApp == null) {
                H.sApp = super.instantiateApplication(classLoader, str);
            }
            return H.sApp;
        } catch (Exception ex1) {
            throw ex1;
        }
    }

    /* JADX WARN: Unreachable blocks removed: 1, instructions: 1 */
    @Override // android.app.AppComponentFactory
    @TargetApi(29)
    public ClassLoader instantiateClassLoader(ClassLoader classLoader, ApplicationInfo applicationInfo) throws Exception {
        try {
            int[] iArr = new int[0];
            H.sAppInfo = applicationInfo;
            H.load();
            if (a()) {
                AppComponentFactory appComponentFactoryA = a(classLoader);
                a(appComponentFactoryA);
                if (appComponentFactoryA != null) {
                    return appComponentFactoryA.instantiateClassLoader(classLoader, applicationInfo);
                }
            }
            return super.instantiateClassLoader(classLoader, applicationInfo);
        } catch (Exception ex1) {
            throw ex1;
        }
    }

    /* JADX WARN: Unreachable blocks removed: 1, instructions: 1 */
    @Override // android.app.AppComponentFactory
    public ContentProvider instantiateProvider(ClassLoader classLoader, String str) throws Exception {
        try {
            int[] iArr = new int[0];
            if (a()) {
                AppComponentFactory appComponentFactoryA = a(classLoader);
                a(appComponentFactoryA);
                if (appComponentFactoryA != null) {
                    return appComponentFactoryA.instantiateProvider(classLoader, str);
                }
            }
            return super.instantiateProvider(classLoader, str);
        } catch (Exception ex1) {
            throw ex1;
        }
    }

    /* JADX WARN: Unreachable blocks removed: 1, instructions: 1 */
    @Override // android.app.AppComponentFactory
    public BroadcastReceiver instantiateReceiver(ClassLoader classLoader, String str, Intent intent) throws Exception {
        try {
            int[] iArr = new int[0];
            if (a()) {
                AppComponentFactory appComponentFactoryA = a(classLoader);
                a(appComponentFactoryA);
                if (appComponentFactoryA != null) {
                    return appComponentFactoryA.instantiateReceiver(classLoader, str, intent);
                }
            }
            return super.instantiateReceiver(classLoader, str, intent);
        } catch (Exception ex1) {
            throw ex1;
        }
    }

    /* JADX WARN: Unreachable blocks removed: 1, instructions: 1 */
    @Override // android.app.AppComponentFactory
    public Service instantiateService(ClassLoader classLoader, String str, Intent intent) throws Exception {
        try {
            int[] iArr = new int[0];
            if (a()) {
                AppComponentFactory appComponentFactoryA = a(classLoader);
                a(appComponentFactoryA);
                if (appComponentFactoryA != null) {
                    return appComponentFactoryA.instantiateService(classLoader, str, intent);
                }
            }
            return super.instantiateService(classLoader, str, intent);
        } catch (Exception ex1) {
            throw ex1;
        }
    }
}
