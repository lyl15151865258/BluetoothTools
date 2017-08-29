package cn.njmeter.bluetooth;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;

import java.util.LinkedList;
import java.util.List;

import cn.njmeter.bluetooth.utils.SharedPreferencesUtils;

/**
 * Created by Li Yuliang on 2017/03/01.
 * 初始化
 */

public class BluetoothApplication extends Application {
    private static BluetoothApplication instance;
    private static Context context;
    private List<Activity> activityList = new LinkedList<>();

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
        //初始化SharedPreferences工具
        SharedPreferencesUtils.init(getApplicationContext()); //注册当前最前的Activity
        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

            }

            @Override
            public void onActivityStarted(Activity activity) {

            }

            @Override
            public void onActivityResumed(Activity activity) {
                MyActivityManager.getInstance().setCurrentActivity(activity);
            }

            @Override
            public void onActivityPaused(Activity activity) {

            }

            @Override
            public void onActivityStopped(Activity activity) {

            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

            }

            @Override
            public void onActivityDestroyed(Activity activity) {

            }
        });
    }

    //设置字体不随系统字体大小改变
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        if (newConfig.fontScale != 1)//非默认值
            getResources();
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public Resources getResources() {
        Resources res = super.getResources();
        if (res.getConfiguration().fontScale != 1) {//非默认值
            Configuration newConfig = new Configuration();
            newConfig.setToDefaults();//设置默认
            res.updateConfiguration(newConfig, res.getDisplayMetrics());
        }
        return res;
    }

    //单例模式中获取唯一的MyApplication实例
    public static BluetoothApplication getInstance() {
        if (instance == null) {
            instance = new BluetoothApplication();
        }
        return instance;
    }

    //单例模式中获取唯一的MyApplication实例
    public static Context getContext() {
        return context;
    }

    //添加Activity到容器中
    public void addActivity(Activity activity) {
        activityList.add(activity);
    }

    //遍历所有Activity并finish
    public void exit() {
        for (Activity activity : activityList) {
            activity.finish();
        }
//            android.os.Process.killProcess(android.os.Process.myPid());
//            System.exit(0);
    }
}
