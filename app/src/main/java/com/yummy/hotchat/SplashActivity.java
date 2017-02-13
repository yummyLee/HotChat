package com.yummy.hotchat;

/**
 * 项目名称：HotChat2
 * 类描述：
 * 创建人：qq985
 * 创建时间：2016/11/26 20:23
 * 修改人：qq985
 * 修改时间：2016/11/26 20:23
 * 修改备注：
 */

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import com.yummy.hotchat.WifiTool.WifiTools;

public class SplashActivity extends Activity {

    private WifiTools wifiTools;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash);

        wifiTools=WifiTools.getInstance(this);
        wifiTools.openWifi();

        new DelayTime().start();

    }

    /**
     * 延迟时间
     */
    class DelayTime extends Thread{
        @Override
        public void run() {
            try {
                Thread.sleep(1500);
                Intent intent=new Intent(SplashActivity.this,MainActivity.class);
                startActivity(intent);
                finish();

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
