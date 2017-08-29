package cn.njmeter.bluetooth;


import android.app.Activity;

import java.lang.ref.WeakReference;

/**
 * Created by LiYuliang on 2017/8/9 0009.
 * Activity管理类，获取当前显示的Activity实例
 */

public class MyActivityManager {
    private static MyActivityManager sInstance = new MyActivityManager();
    // 采用弱引用持有 Activity ，避免造成 内存泄露
    private WeakReference<Activity> sCurrentActivityWeakRef;


    private MyActivityManager() {

    }

    public static MyActivityManager getInstance() {
        return sInstance;
    }

    public Activity getCurrentActivity() {
        Activity currentActivity = null;
        if (sCurrentActivityWeakRef != null) {
            currentActivity = sCurrentActivityWeakRef.get();
        }
        return currentActivity;
    }

    public void setCurrentActivity(Activity activity) {
        sCurrentActivityWeakRef = new WeakReference<Activity>(activity);
    }
}
