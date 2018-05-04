package com.example.wiger.money_qr_code.Util;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.accessibility.AccessibilityManager;
import android.widget.LinearLayout;

import java.util.List;

public class Util {

    private static String service_name = "com.example.wiger.money_qr_code/.Service.MyAccessibilityService";
    private static String wechat_packageName = "com.tencent.mm";
    private static String alipay_packageName = "com.eg.android.AlipayGphone";
    private static String app_packageName = "com.example.wiger.money_qr_code";

    public static void open_Wechat(Context context){
        try {
            Intent intent = context.getPackageManager().getLaunchIntentForPackage(wechat_packageName);
            context.startActivity(intent);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void open_Alipay(Context context){
        try {
            Intent intent = context.getPackageManager().getLaunchIntentForPackage(alipay_packageName);
            context.startActivity(intent);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void open_App(Context context){
        try {
            Intent intent = context.getPackageManager().getLaunchIntentForPackage(app_packageName);
            context.startActivity(intent);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 判断AccessibilityService服务是否已经启动
     * @param context
     * @return
     */
    public static boolean isStartAccessibilityService(Context context){
        AccessibilityManager accessibilityManager = (AccessibilityManager) context.getSystemService(Context.ACCESSIBILITY_SERVICE);
        List<AccessibilityServiceInfo> serviceInfos = accessibilityManager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_GENERIC);
        for (AccessibilityServiceInfo info : serviceInfos) {
            String id = info.getId();
            if (id.contains(service_name)) {
                return true;
            }
        }
        return false;
    }

}