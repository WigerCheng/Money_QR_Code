package com.example.wiger.money_qr_code.Application;

import android.app.Application;
import android.graphics.Bitmap;
import android.media.ImageReader;
import android.media.projection.MediaProjection;

import com.example.wiger.money_qr_code.Service.ScreenShotService;

public class MyApplication extends Application{

    private String[] moneys = new String[]{"1"};
    private static MyApplication instance;
    private ScreenShotService screenShotService;

    public ScreenShotService getScreenShotService() {
        return screenShotService;
    }

    public void setScreenShotService(ScreenShotService screenShotService) {
        this.screenShotService = screenShotService;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }

    public static MyApplication getInstance() {
        return instance;
    }

    public String[] getMoneys() {
        return moneys;
    }

    public void setMoneys(String[] moneys) {
        this.moneys = moneys;
    }

}