package cn.njmeter.bluetooth.interfaces;

import android.view.View;
import android.widget.AdapterView;

/**
 * Created by LiYuliang on 2017/7/26 0026.
 * 防止控件被重复点击的接口——对于GridView和ListView等
 */

public abstract class OnMultiItemClickListener implements AdapterView.OnItemClickListener {
    // 两次点击按钮之间的点击间隔不能少于1000毫秒
    private static final int MIN_CLICK_DELAY_TIME = 1000;
    private static long lastClickTime;

    public abstract void onMultiClick(AdapterView<?> parent, View view, int position, long id);

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        long curClickTime = System.currentTimeMillis();
        if ((curClickTime - lastClickTime) >= MIN_CLICK_DELAY_TIME) {
            // 超过点击间隔后再将lastClickTime重置为当前点击时间
            lastClickTime = curClickTime;
            onMultiClick(parent, view, position, id);
        }
    }
}
