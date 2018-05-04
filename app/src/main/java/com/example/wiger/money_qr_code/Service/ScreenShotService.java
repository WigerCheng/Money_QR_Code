package com.example.wiger.money_qr_code.Service;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;

import com.example.wiger.money_qr_code.Application.MyApplication;
import com.example.wiger.money_qr_code.Util.QRCodeUtil;

import java.nio.ByteBuffer;
import java.util.ArrayList;


@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class ScreenShotService extends Service {

    private int mScreenWidth;
    private int mScreenHeight;
    private int mScreenDensity;
    private WindowManager mWindowManager;

    private MediaProjection mMediaProjection;
    private VirtualDisplay mVirtualDisplay;
    private static Intent mResultData = null;
    private ImageReader mImageReader;

    private MyApplication myApplication;

    private String TAG = "ScreenShotServiceDebug";



    @Override
    public void onCreate() {
        super.onCreate();
        getScreen();
        createImageReader();
        Log.d(TAG,"Create");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG,"Start");
        myApplication = (MyApplication)this.getApplication();
        myApplication.setScreenShotService(ScreenShotService.this);
        return super.onStartCommand(intent, flags, startId);
    }

    private void getScreen(){
        //GET SCREEN
        Log.d(TAG,"getScreen");
        mWindowManager = (WindowManager) MyApplication.getInstance().getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics metrics = new DisplayMetrics();
        mWindowManager.getDefaultDisplay().getMetrics(metrics);
        mScreenDensity = metrics.densityDpi;
        mScreenWidth = metrics.widthPixels;
        mScreenHeight = metrics.heightPixels;
    }

    public static void setResultData(Intent mResultData) {
        ScreenShotService.mResultData = mResultData;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void startScreenShot() {
        Handler handler1 = new Handler();
        handler1.postDelayed(new Runnable() {
            public void run() {
                Log.d(TAG,"start virtual");
                //start virtual
                startVirtual();
            }
        }, 5);

        handler1.postDelayed(new Runnable() {
            public void run() {
                //capture the screen
                Log.d(TAG,"capture the screen");
                startCapture();
            }
        }, 30);
    }


    private void createImageReader() {
        Log.d(TAG,"createImageReader");
        mImageReader = ImageReader.newInstance(mScreenWidth,mScreenHeight,PixelFormat.RGBA_8888,1);
    }

    public void startVirtual() {
        Log.d(TAG,"startVirtual");
        if (mMediaProjection!=null){
            virtualDisplay();
        } else {
            setUpMediaProjection();
            virtualDisplay();
        }
    }

    public void setUpMediaProjection() {
        Log.d(TAG,"setUpMediaProjection");
        if (mResultData == null) {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            startActivity(intent);
        } else {
            mMediaProjection = getMediaProjectionManager().getMediaProjection(Activity.RESULT_OK, mResultData);
        }
    }

    private MediaProjectionManager getMediaProjectionManager() {
        Log.d(TAG,"getUpMediaProjection");
        return (MediaProjectionManager) MyApplication.getInstance().getSystemService(Context.MEDIA_PROJECTION_SERVICE);
    }

    private void virtualDisplay() {
        Log.d(TAG,"virtualDisplay");
        mVirtualDisplay = mMediaProjection.createVirtualDisplay("screen-mirror",
                mScreenWidth,mScreenHeight,mScreenDensity, DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                mImageReader.getSurface(),null,null);
    }

    private void startCapture() {
        Log.d(TAG,"startCapture");
        Image image = mImageReader.acquireLatestImage();
        if (image == null) {
            startScreenShot();
        } else {
            SaveTask mSaveTask = new SaveTask();
            mSaveTask.execute(image);
        }
    }


    @SuppressLint("StaticFieldLeak")
    public class SaveTask extends AsyncTask<Image, Void, Bitmap> {

        @Override
        protected Bitmap doInBackground(Image... params) {

            if (params == null || params.length < 1 || params[0] == null) {

                return null;
            }

            Image image = params[0];

            int width = image.getWidth();
            int height = image.getHeight();
            final Image.Plane[] planes = image.getPlanes();
            final ByteBuffer buffer = planes[0].getBuffer();
            //每个像素的间距
            int pixelStride = planes[0].getPixelStride();
            //总的间距
            int rowStride = planes[0].getRowStride();
            int rowPadding = rowStride - pixelStride * width;
            Bitmap bitmap = Bitmap.createBitmap(width + rowPadding / pixelStride, height, Bitmap.Config.ARGB_8888);
            bitmap.copyPixelsFromBuffer(buffer);
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height);
            image.close();
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            if (bitmap != null) {
                String QR_Text = QRCodeUtil.readQRImage(bitmap);
                Log.d(TAG,"QR_TEXT"+QR_Text);
                Intent intent=new Intent();
                intent.putExtra("Text", QR_Text);
                intent.setAction("com.example.wiger.money_qr_code.Service.ScreenShotService");
                sendBroadcast(intent);
            }
        }
    }

    private void tearDownMediaProjection() {
        if (mMediaProjection != null) {
            mMediaProjection.stop();
            mMediaProjection = null;
        }
    }

    private void stopVirtual() {
        if (mVirtualDisplay == null) {
            return;
        }
        mVirtualDisplay.release();
        mVirtualDisplay = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopVirtual();
        tearDownMediaProjection();
    }
}
