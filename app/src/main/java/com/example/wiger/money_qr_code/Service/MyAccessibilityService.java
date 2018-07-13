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
    private Rect rect_set_money, rect_clear_money;
    private boolean isGetAlipayID = false;

    private static final long SLEEP_TIME = 1000;
    private static final int FLAG_FIRST_BOOT = 0;    //启动
    private static final int FLAG_MAINPAGE = 1;    //在主页面
    private static final int FLAG_OPEN_ME = 2;    //打开了我的（我）
    private static final int FLAG_OPEN_QR_UI = 3;    //进入了绿屏(回到首页)
    private static final int FLAG_OPEN_QR2_UI = 4;    //进入了黄屏(收钱)
    private static final int FLAG_SET_MONEY = 5;    //按下设置金额
    private static final int FLAG_SURE_MONEY = 6;    //设置金额，并返回黄屏
    private static final int FLAG_WALLET = 7;    //微信钱包
    private static final int FLAG_PERSONAL_INFORMATION = 8;//个人信息
    private static final int FLAG_PERSONAL_HOME = 9;//个人主页

    private int Flag = FLAG_FIRST_BOOT;

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
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
                if (Flag == FLAG_FIRST_BOOT) {
                    back_MainPage();
                    if (Flag == FLAG_MAINPAGE) {
                        clickByText("我", FLAG_OPEN_ME);
                        getWechatID(getNodeTextByText("微信号"));
                        sleep(SLEEP_TIME);
                        clickByText("钱包", FLAG_WALLET);
                    }
                } else if (Flag == FLAG_WALLET && event.getClassName().toString().equals("com.tencent.mm.plugin.mall.ui.MallIndexUI")) {
                    clickByText("收付款", FLAG_OPEN_QR_UI);
                } else if (Flag == FLAG_OPEN_QR_UI && event.getClassName().toString().equals("com.tencent.mm.plugin.offline.ui.WalletOfflineCoinPurseUI")) {
                    clickByText("二维码收款", FLAG_OPEN_QR2_UI);
                } else if (Flag == FLAG_OPEN_QR2_UI && event.getClassName().toString().equals("com.tencent.mm.plugin.collect.ui.CollectMainUI")) {
                    rect_set_money = getRect("设置金额");
                    clickByGesture(rect_set_money, FLAG_SET_MONEY);
                } else if (Flag == FLAG_SET_MONEY && event.getClassName().toString().equals("com.tencent.mm.plugin.collect.ui.CollectCreateQRCodeUI")) {
                    if (i != length) {
                        setMoney(myMoneys[i]);
                        screenShotService.setMoney(myMoneys[i]);
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
                if (Flag == FLAG_FIRST_BOOT) {
                    back_MainPage();
                    if (Flag == FLAG_MAINPAGE) {
                        if (!isGetAlipayID) {
                            clickAlipayMe();
                            sleep(SLEEP_TIME);
                            if (Flag == FLAG_OPEN_ME) {
                                clickByText("******", FLAG_PERSONAL_INFORMATION);
                            }
                        }
                    }
                } else if (Flag == FLAG_MAINPAGE) {
                    if (isGetAlipayID) {
                        sleep(1000);
                        clickByText("首页", FLAG_OPEN_QR_UI);
                        clickByText("收钱", FLAG_OPEN_QR2_UI);
                    }else {
                        clickAlipayMe();
                        sleep(SLEEP_TIME);
                        if (Flag == FLAG_OPEN_ME) {
                            clickByText("******", FLAG_PERSONAL_INFORMATION);
                        }
                    }
                } else if (Flag == FLAG_PERSONAL_INFORMATION && event.getClassName().toString().equals("com.alipay.mobile.security.personcenter.PersonCenterActivity")) {
                    clickByText("个人主页", FLAG_PERSONAL_HOME);
                } else if (Flag == FLAG_PERSONAL_HOME && event.getClassName().toString().equals("com.alipay.android.phone.wallet.profileapp.ui.ProfileActivity_")) {
                    getAlipayID();
                    back_MainPage();
                } else if (Flag == FLAG_PERSONAL_HOME) {
                    back_MainPage();
                }

            }
        } else if (eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {
            //Alipay
            if (packageName.equals("com.eg.android.AlipayGphone")) {
                if (event.getClassName().toString().equals("android.widget.FrameLayout") && Flag == FLAG_OPEN_QR2_UI) {
                    if (i != length) {
                        clickByText("设置金额", FLAG_SET_MONEY);
                    }
                    if (i == length) {
                        Util.open_App(getApplicationContext());
                        this.i = 0;
                        this.Flag = FLAG_FIRST_BOOT;
                    }
                } else if (event.getClassName().toString().equals("android.widget.FrameLayout") && Flag == FLAG_SET_MONEY) {
                    AccessibilityNodeInfo active_window = getRootInActiveWindow();
                    int childCount = active_window.getChildCount();
                    if (childCount == 9) {
                        return;
                    }
                    if (i != length) {
                        setMoney(myMoneys[i]);
                        screenShotService.setMoney(myMoneys[i]);
                    }
                    if (i < length) {
                        i++;
                    }
                } else if (Flag == FLAG_SURE_MONEY) {
                    screenShotService.startScreenShot();
                    clickByText("清除金额", FLAG_OPEN_QR2_UI);
                }
            }
//event.getClassName().toString().equals("android.widget.RelativeLayout") &&
            //Wechat
            else if (packageName.equals("com.tencent.mm")) {
                if (event.getClassName().toString().equals("android.widget.ListView") && Flag == FLAG_OPEN_QR2_UI) {
                    if (i != length) {
                        clickByGesture(rect_set_money, FLAG_SET_MONEY);
                    }
                    if (i == length) {
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
        AccessibilityNodeInfo node = getNodeByClassname("android.widget.EditText");
        Bundle arguments = new Bundle();
        arguments.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, money);
        node.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments);
        clickByText("确定",FLAG_SURE_MONEY);
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
            if (node != null) {
                node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
            }
        }
        this.Flag = Flag;
    }

    public void clickAlipayMe() {
        AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
        if (nodeInfo == null) {
            return;
        }
        List<AccessibilityNodeInfo> nodes_Text = nodeInfo.findAccessibilityNodeInfosByText("我的");
        AccessibilityNodeInfo node_Text;
        node_Text = get_click_nodes(nodes_Text.get(nodes_Text.size() - 1));
        node_Text.performAction(AccessibilityNodeInfo.ACTION_CLICK);
        this.Flag = FLAG_OPEN_ME;
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
            if (node != null) {
                node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
            }
        }
        this.Flag = Flag;
    }

    public AccessibilityNodeInfo getNodeByClassname(String className) {
        AccessibilityNodeInfo active_Window = getRootInActiveWindow();
        AccessibilityNodeInfo nodeInfo = null;
        List<AccessibilityNodeInfo> nodes = new ArrayList<>();
        recycleNode(active_Window, nodes);
        for (AccessibilityNodeInfo node : nodes) {
            if (className.equals(node.getClassName().toString())) {
                nodeInfo = node;
            }
        }
        return nodeInfo;
    }

    private void recycleNode(AccessibilityNodeInfo node, List<AccessibilityNodeInfo> nodes) {
        int childNum = node.getChildCount();
        if (childNum == 0) {
            nodes.add(node);
        } else {
            for (int i = 0; i < childNum; i++) {
                recycleNode(node.getChild(i), nodes);
            }
        }
    }

    public String getNodeTextByText(String text) {
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

    public void back_MainPage() {
        AccessibilityNodeInfo active_window = getRootInActiveWindow();
        if (active_window != null) {
            List<AccessibilityNodeInfo> nodes = active_window.findAccessibilityNodeInfosByText("返回");
            if (is_MainPage(nodes)) {
                Flag = FLAG_MAINPAGE;
            } else {
                AccessibilityNodeInfo node;
                for (int i = 0; i < nodes.size(); i++) {
                    if (!is_MainPage(nodes)) {
                        node = get_click_nodes(nodes.get(i));
                        node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                        back_MainPage();
                    }
                }
            }
        }
    }

    public boolean is_MainPage(List<AccessibilityNodeInfo> nodes) {
        boolean is_Main = true;
        for (AccessibilityNodeInfo node : nodes) {
            String desc = String.valueOf(node.getContentDescription());
            if ("返回".equals(desc)) {
                is_Main = false;
                break;
            }
        }
        return is_Main;
    }

    public AccessibilityNodeInfo get_click_nodes(AccessibilityNodeInfo node) {
        if (node.isClickable()) {
            return node;
        } else {
            AccessibilityNodeInfo nodeInfo = node.getParent();
            if (nodeInfo == null) {
                return null;
            } else {
                return get_click_nodes(nodeInfo);
            }
        }
    }

    private void getWechatID(String ID) {
        String wechatID = ID.substring(4);
        Intent intent = new Intent();
        intent.putExtra("WeChatID", wechatID);
        intent.setAction("com.example.wiger.money_qr_code");
        sendBroadcast(intent);
    }

    private void getAlipayID() {
        AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
        if (nodeInfo == null) {
            return;
        }
        List<AccessibilityNodeInfo> nodes = nodeInfo.findAccessibilityNodeInfosByText("支付宝账户");
        AccessibilityNodeInfo node, node_ID;
        node = nodes.get(0);
        node_ID = node.getParent().getChild(1);
        String AlipayID = node_ID.getText().toString();
        Intent intent = new Intent();
        intent.putExtra("AlipayID", AlipayID);
        intent.setAction("com.example.wiger.money_qr_code");
        sendBroadcast(intent);
        isGetAlipayID = true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        this.disableSelf();
    }

    public void sleep(long longs){
        try {
            Thread.sleep(longs);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}