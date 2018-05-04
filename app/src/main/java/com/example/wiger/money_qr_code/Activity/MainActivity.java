package com.example.wiger.money_qr_code.Activity;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.provider.Settings;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.wiger.money_qr_code.Application.MyApplication;
import com.example.wiger.money_qr_code.R;
import com.example.wiger.money_qr_code.Service.ScreenShotService;
import com.example.wiger.money_qr_code.Util.Util;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends BaseActivity implements View.OnClickListener{

    private Button btn_open_Wechat,btn_sure,btn_open_Service,btn_open_Alipay;
    private EditText et_money;
    private TextView tv_showText;
    private MyApplication myApplication;
    private String[] moneys;
    private Intent intent;
    private MyReceiver receiver = null;
    public static final int REQUEST_MEDIA_PROJECTION = 2;
    private List<String> texts = new ArrayList<>();
    private String TAG = "MainActivityDebug";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG,"onCreate");
        setContentView(R.layout.activity_main);
        requestCapturePermission();
        initViews();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @SuppressLint("HandlerLeak")
    private void initViews()
    {
        btn_open_Wechat = findViewById(R.id.btn_open_Wechat);
        btn_sure = findViewById(R.id.btn_sure);
        btn_open_Service = findViewById(R.id.btn_open_Service);
        btn_open_Alipay = findViewById(R.id.btn_open_Alipay);
        et_money = findViewById(R.id.et_money);
        tv_showText = findViewById(R.id.tv_showText);

        btn_open_Wechat.setOnClickListener(this);
        btn_sure.setOnClickListener(this);
        btn_open_Service.setOnClickListener(this);
        btn_open_Alipay.setOnClickListener(this);

        myApplication = (MyApplication)this.getApplication();
        intent = new Intent(MainActivity.this, ScreenShotService.class);

        receiver = new MyReceiver();
        IntentFilter filter=new IntentFilter();
        filter.addAction("com.example.wiger.money_qr_code.Service.ScreenShotService");
        MainActivity.this.registerReceiver(receiver,filter);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.btn_open_Wechat:{
                Util.open_Wechat(getApplicationContext());
            }break;
            case R.id.btn_open_Alipay:{
                Util.open_Alipay(getApplicationContext());
            }break;
            case R.id.btn_sure:{
                getMoneys();
            }break;
            case R.id.btn_open_Service:{
                if (! Util.isStartAccessibilityService(getApplicationContext())) {
                    startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
                }
            }break;

        }
    }

    private void getMoneys()
    {
        String money = et_money.getText().toString();
        moneys = money.split("[,，]");
        myApplication.setMoneys(moneys);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG,"requestCode"+requestCode);
        switch (requestCode){
            case REQUEST_MEDIA_PROJECTION:{
                if (resultCode == RESULT_OK && data != null) {
                    ScreenShotService.setResultData(data);
                    startService(intent);
                }
            }break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopService(intent);
    }

    public void requestCapturePermission() {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            //5.0 之后才允许使用屏幕截图
            return;
        }

        MediaProjectionManager mediaProjectionManager = (MediaProjectionManager)
                getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        startActivityForResult(
                mediaProjectionManager.createScreenCaptureIntent(),
                REQUEST_MEDIA_PROJECTION);
    }

    /**
     * 获取广播数据
     * @author wiger
     */
    public class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle=intent.getExtras();
            String text = bundle.getString("Text");
            Log.d(TAG,text);
            texts.add(text+"\n");
            tv_showText.append(text+"\n");
        }
    }
}