package cn.njmeter.bluetooth.activity;

import android.content.Context;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.njmeter.bluetooth.BaseActivity;
import cn.njmeter.bluetooth.R;
import cn.njmeter.bluetooth.fragment.watermeter.WaterMeterAdjustFragment;
import cn.njmeter.bluetooth.fragment.watermeter.WaterMeterDataFragment;
import cn.njmeter.bluetooth.fragment.watermeter.WaterMeterLcdFragment;
import cn.njmeter.bluetooth.fragment.watermeter.WaterMeterParameterFragment;
import cn.njmeter.bluetooth.fragment.watermeter.WaterMeterSetPressureFragment;
import cn.njmeter.bluetooth.utils.ScreenTools;
import cn.njmeter.bluetooth.widget.NoScrollViewPager;

/**
 * Created by Li Yuliang on 2017/8/8.
 * 水表蓝牙设置页面，包含4个Fragment
 */

public class WaterMeterSettingActivity extends BaseActivity {
    @BindViews({R.id.ll_a, R.id.ll_b, R.id.ll_c, R.id.ll_d, R.id.ll_e})
    LinearLayout[] menus;
    @BindView(R.id.viewpager)
    NoScrollViewPager viewPager;
    @BindView(R.id.iv_search_bluetooth_device)
    ImageView iv_search_bluetooth_device;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_watermeter_bluetooth);
        LinearLayout ll_root = (LinearLayout) findViewById(R.id.ll_root);
        ll_root.setPadding(0, ScreenTools.getStatusBarHeight(this), 0, 0);
        ButterKnife.bind(this);
        BluetoothToolsMainActivity.currentFragment = 0;
        initView();
    }

    //初始化视图
    private void initView() {
        int imageSrc;
        if (BluetoothToolsMainActivity.bluetoothSocket != null && BluetoothToolsMainActivity.bluetoothSocket.isConnected()) {
            imageSrc = R.drawable.bluetooth_connected;
        } else {
            imageSrc = R.drawable.bluetooth_disconnected;
        }
        iv_search_bluetooth_device.setImageResource(imageSrc);
        viewPager.setAdapter(viewPagerAdapter);
        //设置Fragment预加载，非常重要,可以保存每个页面fragment已有的信息,防止切换后原页面信息丢失
        viewPager.setOffscreenPageLimit(5);
        //刚进来默认选择第一个
        menus[0].setSelected(true);
        //viewPager添加滑动监听，用于控制TextView的展示
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                for (LinearLayout menu : menus) {
                    menu.setSelected(false);
                }
                menus[position].setSelected(true);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    //点击除了EditText以外的所有地方都可以隐藏软键盘
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (isShouldHideKeyboard(v, ev)) {
                hideKeyboard(v.getWindowToken());
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    //根据EditText所在坐标和用户点击的坐标相对比，来判断是否隐藏键盘，因为当用户点击EditText时则不能隐藏
    private boolean isShouldHideKeyboard(View v, MotionEvent event) {
        if (v != null && (v instanceof EditText)) {
            int[] l = {0, 0};
            v.getLocationInWindow(l);
            int left = l[0],
                    top = l[1],
                    bottom = top + v.getHeight(),
                    right = left + v.getWidth();
            return !(event.getX() > left && event.getX() < right && event.getY() > top && event.getY() < bottom);
        }
        // 如果焦点不是EditText则忽略，这个发生在视图刚绘制完，第一个焦点不在EditText上，和用户用轨迹球选择其他的焦点
        return false;
    }

    //获取InputMethodManager，隐藏软键盘
    private void hideKeyboard(IBinder token) {
        if (token != null) {
            InputMethodManager im = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            im.hideSoftInputFromWindow(token, InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    //ViewPager适配器
    private FragmentStatePagerAdapter viewPagerAdapter = new FragmentStatePagerAdapter(getSupportFragmentManager()) {
        @Override
        public int getCount() {
            return 5;
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return new WaterMeterDataFragment();
                case 1:
                    return new WaterMeterLcdFragment();
                case 2:
                    return new WaterMeterParameterFragment();
                case 3:
                    return new WaterMeterAdjustFragment();
                case 4:
                    return new WaterMeterSetPressureFragment();
                default:
                    break;
            }
            return null;
        }
    };

    //TextView点击事件
    @OnClick({R.id.ll_a, R.id.ll_b, R.id.ll_c, R.id.ll_d, R.id.ll_e})
    public void onClick(LinearLayout linearLayout) {
        for (int i = 0; i < menus.length; i++) {
            menus[i].setSelected(false);
            menus[i].setTag(i);
        }
        //设置选择效果
        linearLayout.setSelected(true);
        //参数false代表瞬间切换，true表示平滑过渡
        BluetoothToolsMainActivity.currentFragment = (Integer) linearLayout.getTag();
        viewPager.setCurrentItem(BluetoothToolsMainActivity.currentFragment, false);
    }
}

