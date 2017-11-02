package cn.njmeter.bluetooth.activity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import cn.njmeter.bluetooth.BaseActivity;
import cn.njmeter.bluetooth.R;
import cn.njmeter.bluetooth.utils.ApkUtils;

/**
 * Created by LiYuliang on 2017/2/17 0017.
 * Logo页面，每次打开程序都会运行
 *
 * @author LiYuliang
 * @version 2017/10/27
 */

public class LogoActivity extends BaseActivity {

    private TextView tvTime;
    private Boolean threadIsRun = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //去除状态栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_logo);
        ((TextView) findViewById(R.id.tv_version)).setText("版本号：" + ApkUtils.getVersionName(this));
        tvTime = findViewById(R.id.tv_time);
        (findViewById(R.id.rl_skip)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openActivity(BluetoothToolsMainActivity.class);
                finish();
            }
        });
        threeSeconds();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (threadIsRun) {
                    openActivity(BluetoothToolsMainActivity.class);
                    finish();
                }
            }
        }, 3000);
    }

    /**
     * 显示3秒倒计时
     */
    private void threeSeconds() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 3; i > 0; i--) {
                    Message msg = msgHandler.obtainMessage();
                    msg.arg1 = 1;
                    msg.obj = i;
                    msgHandler.sendMessage(msg);
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    private final Handler msgHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            tvTime.setText("跳过\n" + msg.obj + "s");
        }
    };

    //销毁Activity时将子线程标记设为false，否则用户点击跳过后会出现打开两次activity
    @Override
    protected void onDestroy() {
        threadIsRun = false;
        super.onDestroy();
    }

    //Logo页面不允许退出
    @Override
    public void onBackPressed() {
    }
}
