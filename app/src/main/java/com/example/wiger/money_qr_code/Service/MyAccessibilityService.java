package com.example.wiger.money_qr_code.Service;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.content.Intent;
import android.graphics.Path;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import com.example.wiger.money_qr_code.Application.MyApplication;
import com.example.wiger.money_qr_code.Util.Util;

import java.util.ArrayList;
import java.util.List;

public class MyAccessibilityService extends AccessibilityService {

    private static String TAG = "MyAccessibilityService";
    private MyApplication myApplication;
    private String packageName;
    private String[] myMoneys;
    private int length, i = 0;
    private ScreenShotService screenShotService;
    private Rect rect_set_money,
            rect_clear_money;
    private static int FLAG_FIRST_BOOT = 0;    //启动
    private static int FLAG_MAINPAGE = 1;    //在主页面
    private static int FLAG_OPEN_ME = 2;    //打开了Menu
    private static int FLAG_OPEN_QR_UI = 3;    //进入了绿屏
    private static int FLAG_OPEN_QR2_UI = 4;    //进入了黄屏
    private static int FLAG_SET_MONEY = 5;    //按下设置金额
    private static int FLAG_SURE_MONEY = 6;    //设置金额，并返回黄屏
    private static int FLAG_WALLET = 7;    //截屏
    private static int FLAG_SCREEN_CANCEL = 8;
    private int Flag = FLAG_FIRST_BOOT;

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        Log.d(TAG, "onServiceConnected");
        myApplication = (MyApplication) this.getApplication();
        screenShotService = myApplication.getScreenShotService();
        myMoneys = myApplication.getMoneys();
        length = myMoneys.length;
    }



    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        int eventType = event.getEventType();
        packageName = event.getPackageName().toString();
        if (eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            //Wechat
            if (packageName.equals("com.tencent.mm")) {
                Log.d(TAG, "I" + String.valueOf(i));
                Log.d(TAG, "WINDOWS_STATE_CHANGED");
                Log.d(TAG, "CLASSNAME" + event.getClassName().toString());
                Log.d(TAG, "FLAG" + String.valueOf(Flag));
                if (Flag == FLAG_FIRST_BOOT) {
                    back_WeChat_MainPage();
                    if (Flag == FLAG_MAINPAGE && event.getClassName().toString().equals("com.tencent.mm.ui.LauncherUI")) {
                        clickByText("我",FLAG_OPEN_ME);
                        getID(getNodeTextByText("微信号"));
                        clickByText("钱包",FLAG_WALLET);
                    }
                }
                else if (Flag == FLAG_MAINPAGE && event.getClassName().toString().equals("com.tencent.mm.ui.LauncherUI")) {
                    clickByText("我",FLAG_OPEN_ME);
                    String WechatID = getNodeTextByText("微信号");
                    Log.d(TAG,"微信号"+WechatID);
                    clickByText("钱包",FLAG_WALLET);
                }
                else if (Flag == FLAG_WALLET && event.getClassName().toString().equals("com.tencent.mm.plugin.mall.ui.MallIndexUI")) {
                    clickByText("收付款", FLAG_OPEN_QR_UI);
                } else if (Flag == FLAG_OPEN_QR_UI && event.getClassName().toString().equals("com.tencent.mm.plugin.offline.ui.WalletOfflineCoinPurseUI")) {
                    clickByText("二维码收款", FLAG_OPEN_QR2_UI);
                } else if (Flag == FLAG_OPEN_QR2_UI && event.getClassName().toString().equals("com.tencent.mm.plugin.collect.ui.CollectMainUI")) {
                    rect_set_money = getRect("设置金额");
                    clickByGesture(rect_set_money, FLAG_SET_MONEY);
                } else if (Flag == FLAG_SET_MONEY && event.getClassName().toString().equals("com.tencent.mm.plugin.collect.ui.CollectCreateQRCodeUI")) {
                    if (i != length) {
                        setMoney(myMoneys[i]);
                    }
                    if (i < length) {
                        i++;
                    }
                } else if (Flag == FLAG_SURE_MONEY && event.getClassName().toString().equals("com.tencent.mm.plugin.collect.ui.CollectMainUI"))//com.tencent.mm.plugin.collect.ui.CollectCreateQRCodeUI
                {
                    rect_clear_money = getRect("清除金额");
                    screenShotService.startScreenShot();
                    clickByGesture(rect_clear_money, FLAG_OPEN_QR2_UI);
                }
            }
            //Alipay
            else if (packageName.equals("com.eg.android.AlipayGphone")) {
                Log.d(TAG, "I" + String.valueOf(i));
                Log.d(TAG, "WINDOWS_STATE_CHANGED");
                Log.d(TAG, "CLASSNAME" + event.getClassName().toString());
                Log.d(TAG, "FLAG" + String.valueOf(Flag));
                if (event.getClassName().toString().equals("com.eg.android.AlipayGphone.AlipayLogin")|event.getClassName().toString().equals("android.view.View") && Flag == FLAG_FIRST_BOOT) {
                    clickByText("收钱", FLAG_OPEN_QR2_UI);
                }
            }
        } else if (eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {
//            Log.d(TAG, "I" + String.valueOf(i));
//            Log.d(TAG, "TYPE_WINDOW_CONTENT_CHANGED");
//            Log.d(TAG, "CLASSNAME" + event.getClassName().toString());
//            Log.d(TAG, "FLAG" + String.valueOf(Flag));
            //Alipay
            if (packageName.equals("com.eg.android.AlipayGphone")) {
                if (event.getClassName().toString().equals("android.widget.FrameLayout") && Flag == FLAG_OPEN_QR2_UI) {
                    if (i != length) {
                        clickByText("设置金额", FLAG_SET_MONEY);
                    }
                    if (i == length)
                    {
                        Util.open_App(getApplicationContext());
                        this.i = 0;
                        this.Flag = FLAG_FIRST_BOOT;
                    }
                }
                else if (event.getClassName().toString().equals("android.widget.FrameLayout") && Flag == FLAG_SET_MONEY)
                {
                    if (i != length) {
                        Log.d(TAG,"SETMONEY");
                        setMoney(myMoneys[i]);
                    }
                    if (i < length) {
                        i++;
                    }
                }
                else if (event.getClassName().toString().equals("android.widget.RelativeLayout") && Flag == FLAG_SURE_MONEY){
                    screenShotService.startScreenShot();
                    clickByText("清除金额", FLAG_OPEN_QR2_UI);
                }
            }
            //Wechat
            else if (packageName.equals("com.tencent.mm"))
            {
//                if (Flag == FLAG_FIRST_BOOT){
//                    back_WeChat_MainPage();
//                    if (Flag == FLAG_MAINPAGE && event.getClassName().toString().equals("com.tencent.mm.ui.LauncherUI")) {
//                        clickById("com.tencent.mm:id/c_x", FLAG_OPEN_MENU);
//                    }
//                }
//                else
                if (event.getClassName().toString().equals("android.widget.ListView") && Flag == FLAG_OPEN_QR2_UI){
                    if (i != length) {
                        clickByGesture(rect_set_money, FLAG_SET_MONEY);
                    }
                    if (i == length)
                    {
                        this.Flag = FLAG_FIRST_BOOT;
                        this.i = 0;
                        Util.open_App(getApplicationContext());
                    }
                }
            }
        }
    }


    public void clickByGesture(Rect rect, int flag) {
        Path path = new Path();
        path.moveTo(rect.centerX(), rect.centerY());
        GestureDescription.StrokeDescription strokeDescription = new GestureDescription.StrokeDescription(path, 10, 10);
        GestureDescription.Builder gestureDescription = new GestureDescription.Builder().addStroke(strokeDescription);
        this.dispatchGesture(gestureDescription.build(), null, null);
        this.Flag = flag;
    }

    public void setMoney(String money) {
        AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
        if (nodeInfo == null) {
            return;
        }
        List<AccessibilityNodeInfo> nodes = nodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/bt");
        nodes.addAll(nodeInfo.findAccessibilityNodeInfosByViewId("com.alipay.mobile.ui:id/content"));
        Log.d(TAG,"NODE:"+nodes.size());
        AccessibilityNodeInfo node;
        for (int i = 0; i < nodes.size(); i++) {
            node = get_click_nodes(nodes.get(i));
            Bundle arguments = new Bundle();
            arguments.putCharSequence(
                    AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, money);
            node.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments);
        }
        if (packageName.equals("com.tencent.mm")){
            clickById("com.tencent.mm:id/ak_", FLAG_SURE_MONEY);
        }
        else if (packageName.equals("com.eg.android.AlipayGphone")){
            clickById("com.alipay.mobile.payee:id/payee_NextBtn", FLAG_SURE_MONEY);
        }
    }

    @Override
    public void onInterrupt() {

    }

    public Rect getRect(String text) {
        Rect rect = new Rect();
        AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
        if (nodeInfo == null) {
            return null;
        }
        List<AccessibilityNodeInfo> nodes = nodeInfo.findAccessibilityNodeInfosByText(text);
        AccessibilityNodeInfo node;
        for (int i = 0; i < nodes.size(); i++) {
            node = get_click_nodes(nodes.get(i));
            node.getBoundsInScreen(rect);
        }

        return rect;
    }

    public void clickById(String id, int Flag) {
        AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
        if (nodeInfo == null) {
            return;
        }
        List<AccessibilityNodeInfo> nodes = nodeInfo.findAccessibilityNodeInfosByViewId(id);
        Log.d(TAG, id + String.valueOf(nodes.size()));
        AccessibilityNodeInfo node;
        for (int i = 0; i < nodes.size(); i++) {
            node = get_click_nodes(nodes.get(i));
            node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
        }
        this.Flag = Flag;
    }



    public void clickByText(String text, int Flag) {
        AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
        if (nodeInfo == null) {
            return;
        }
        List<AccessibilityNodeInfo> nodes = nodeInfo.findAccessibilityNodeInfosByText(text);
        Log.d(TAG, text + String.valueOf(nodes.size()));
        AccessibilityNodeInfo node;
        for (int i = 0; i < nodes.size(); i++) {
            node = get_click_nodes(nodes.get(i));
            node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
        }
        this.Flag = Flag;
    }

    public String getNodeTextByText(String text){
        String nodeText = null;
        AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
        if (nodeInfo == null) {
            return null;
        }
        List<AccessibilityNodeInfo> nodes = nodeInfo.findAccessibilityNodeInfosByText(text);
        Log.d(TAG, text + String.valueOf(nodes.size()));
        for (int i = 0; i < nodes.size(); i++) {
            nodeText = nodes.get(i).getText().toString();
        }
        return nodeText;
    }

    public boolean is_WeChat_MainPage(List<AccessibilityNodeInfo> nodes) {
        boolean is_Main = false;
        if (nodes.size() == 0) {
            Log.d(TAG, "NODES:主页面");
            is_Main = true;
        }
        return is_Main;
    }

    //通过有没有返回ID，判断是否在主页面
    public void back_WeChat_MainPage() {
        AccessibilityNodeInfo activewindow = getRootInActiveWindow();
        if (activewindow != null) {
            List<AccessibilityNodeInfo> nodes = new ArrayList<>();
            nodes.addAll(getRootInActiveWindow().findAccessibilityNodeInfosByViewId("com.tencent.mm:id/hx"));
            nodes.addAll(getRootInActiveWindow().findAccessibilityNodeInfosByViewId("com.tencent.mm:id/hi"));
            nodes.addAll(getRootInActiveWindow().findAccessibilityNodeInfosByViewId("com.tencent.mm:id/c29"));
            Log.d(TAG, String.valueOf(nodes.size()));
            if (is_WeChat_MainPage(nodes)) {
                Flag = FLAG_MAINPAGE;
            } else {
                Log.d(TAG, "NODES:不是主页面");
                AccessibilityNodeInfo node;
                for (int i = 0; i < nodes.size(); i++) {
                    if (!is_WeChat_MainPage(nodes)) {
                        node = get_click_nodes(nodes.get(i));
                        node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    }
                }
            }
        }
    }

    public AccessibilityNodeInfo get_click_nodes(AccessibilityNodeInfo node) {
        if (node.isClickable()) {
            return node;
        } else {
            return get_click_nodes(node.getParent());
        }
    }

    private void getID(String ID)
    {
        Intent intent=new Intent();
        intent.putExtra("WeChatID",ID);
        intent.setAction("com.example.wiger.money_qr_code");
        sendBroadcast(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        this.disableSelf();
    }
}