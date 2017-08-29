package cn.njmeter.bluetooth.activity.bluetoothtools;

import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.IBinder;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.njmeter.bluetooth.BaseActivity;
import cn.njmeter.bluetooth.R;
import cn.njmeter.bluetooth.interfaces.OnMultiClickListener;
import cn.njmeter.bluetooth.utils.AnalysisUtils;
import cn.njmeter.bluetooth.utils.CommonUtils;
import cn.njmeter.bluetooth.utils.ScreenTools;

/**
 * Created by Li Yuliang on 2017/8/15.
 * 通用设置无线LoRa设置页面
 */

public class LoRaSettingActivity extends BaseActivity {

    @BindView(R.id.scrollView)
    ScrollView scrollView;

    @BindView(R.id.iv_search_bluetooth_device)
    ImageView iv_search_bluetooth_device;
    //读取参数
    @BindView(R.id.tv_wireless_state)
    TextView tv_wireless_state;
    @BindView(R.id.tv_power)
    TextView tv_power;
    @BindView(R.id.tv_channel)
    TextView tv_channel;
    @BindView(R.id.tv_datum_point)
    TextView tv_datum_point;
    @BindView(R.id.btn_read_parameters)
    Button btn_read_parameters;
    @BindView(R.id.btn_open_wireless)
    Button btn_open_wireless;
    @BindView(R.id.btn_close_wireless)
    Button btn_close_wireless;
    //修改信道
    @BindView(R.id.et_frequency_point_1)
    EditText et_frequency_point_1;
    @BindView(R.id.et_frequency_point_2)
    EditText et_frequency_point_2;
    @BindView(R.id.et_frequency_point_3)
    EditText et_frequency_point_3;
    @BindView(R.id.btn_set_channel)
    Button btn_set_channel;
    //修改基准频率
    @BindView(R.id.et_reference_frequency)
    EditText et_reference_frequency;
    @BindView(R.id.btn_set_datum_point)
    Button btn_set_datum_point;
    //修改功率
    @BindView(R.id.et_power)
    EditText et_power;
    @BindView(R.id.btn_set_power)
    Button btn_set_power;

    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lora_bluetooth);
        context = this;
        LinearLayout ll_root = (LinearLayout) findViewById(R.id.ll_root);
        ll_root.setPadding(0, ScreenTools.getStatusBarHeight(this), 0, 0);
        ButterKnife.bind(this);
        BluetoothToolsMainActivity.currentFragment = 0;
        initView();
    }

    //获得软键盘高度并滚动ScrollView
    private ViewTreeObserver.OnGlobalLayoutListener onGlobalLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
        @Override
        public void onGlobalLayout() {
            Rect rect = new Rect();
            getWindow().getDecorView().getWindowVisibleDisplayFrame(rect);
//            int screenHeight = getWindow().getDecorView().getRootView().getHeight();
            //计算软键盘占有的高度  = 屏幕高度 - 视图可见高度
            int heightDifference = screenHeight - rect.bottom;
            //针对在父控件中的View的参数获取
            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) scrollView.getLayoutParams();
            //设置ScrollView的marginBottom的值为软键盘占有的高度即可
            layoutParams.setMargins(0, 0, 0, heightDifference);
            scrollView.requestLayout();
        }
    };

    //初始化视图
    private void initView() {
        scrollView = (ScrollView) findViewById(R.id.scrollView);
        //监听软键盘，滚动ScrollView以适应输入框
        getWindow().getDecorView().getViewTreeObserver().addOnGlobalLayoutListener(onGlobalLayoutListener);
        int imageSrc;
        if (BluetoothToolsMainActivity.bluetoothSocket != null && BluetoothToolsMainActivity.bluetoothSocket.isConnected()) {
            imageSrc = R.drawable.bluetooth_connected;
        } else {
            imageSrc = R.drawable.bluetooth_disconnected;
        }
        iv_search_bluetooth_device.setImageResource(imageSrc);
        btn_read_parameters.setOnClickListener(onClickListener);
        btn_open_wireless.setOnClickListener(onClickListener);
        btn_close_wireless.setOnClickListener(onClickListener);
        btn_set_channel.setOnClickListener(onClickListener);
        btn_set_datum_point.setOnClickListener(onClickListener);
        btn_set_power.setOnClickListener(onClickListener);
        et_frequency_point_1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!TextUtils.isEmpty(s.toString())) {
                    if (Integer.valueOf(et_frequency_point_1.getText().toString()) > 50) {
                        s = s.toString().subSequence(0, s.length() - 1);
                        et_frequency_point_1.setText(s);
                        //设置光标在末尾
                        et_frequency_point_1.setSelection(s.length());
                        CommonUtils.showToast(context, "设定范围0≤X≤50");
                    }
                }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        et_frequency_point_2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!TextUtils.isEmpty(s.toString())) {
                    if (Integer.valueOf(et_frequency_point_2.getText().toString()) > 50) {
                        s = s.toString().subSequence(0, s.length() - 1);
                        et_frequency_point_2.setText(s);
                        //设置光标在末尾
                        et_frequency_point_2.setSelection(s.length());
                        CommonUtils.showToast(context, "设定范围0≤X≤50");
                    }
                }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        et_frequency_point_3.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!TextUtils.isEmpty(s.toString())) {
                    if (Integer.valueOf(et_frequency_point_3.getText().toString()) > 50) {
                        s = s.toString().subSequence(0, s.length() - 1);
                        et_frequency_point_3.setText(s);
                        //设置光标在末尾
                        et_frequency_point_3.setSelection(s.length());
                        CommonUtils.showToast(context, "设定范围0≤X≤50");
                    }
                }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        et_power.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!TextUtils.isEmpty(s.toString())) {
                    if (Integer.valueOf(et_power.getText().toString()) > 7) {
                        s = s.toString().subSequence(0, s.length() - 1);
                        et_power.setText(s);
                        //设置光标在末尾
                        et_power.setSelection(s.length());
                        CommonUtils.showToast(context, "设定范围0≤X≤7");
                    }
                    //开头不允许连续出现0
                    if (s.toString().startsWith("0") && s.toString().trim().length() > 1) {
                        et_power.setText(s.subSequence(0, 1));
                        et_power.setSelection(1);
                    }
                }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private View.OnClickListener onClickListener = new OnMultiClickListener() {
        @Override
        public void onMultiClick(View v) {
            String tx;
            switch (v.getId()) {
                case R.id.btn_read_parameters:
                    //读取参数
                    tx = "6854111111110011110803901E00DB16";
                    BluetoothToolsMainActivity.data = "";
                    BluetoothToolsMainActivity.writeData(tx);
                    break;
                case R.id.btn_open_wireless:
                    //启用无线
                    tx = "6854111111110011110503501A019516";
                    BluetoothToolsMainActivity.data = "";
                    BluetoothToolsMainActivity.writeData(tx);
                    break;
                case R.id.btn_close_wireless:
                    //停用无线
                    tx = "6854111111110011110503501A009416";
                    BluetoothToolsMainActivity.data = "";
                    BluetoothToolsMainActivity.writeData(tx);
                    break;
                case R.id.btn_set_channel:
                    //修改信道
                    String frequency_point_text_1 = et_frequency_point_1.getText().toString();
                    if (TextUtils.isEmpty(frequency_point_text_1)) {
                        //如果没输入则取默认值
                        frequency_point_text_1 = et_frequency_point_1.getHint().toString();
                    }
                    String frequency_point_text_2 = et_frequency_point_2.getText().toString();
                    if (TextUtils.isEmpty(frequency_point_text_2)) {
                        //如果没输入则取默认值
                        frequency_point_text_2 = et_frequency_point_2.getHint().toString();
                    }
                    String frequency_point_text_3 = et_frequency_point_3.getText().toString();
                    if (TextUtils.isEmpty(frequency_point_text_3)) {
                        //如果没输入则取默认值
                        frequency_point_text_3 = et_frequency_point_3.getHint().toString();
                    }
                    String frequency_point_1 = addZeroForNum(String.valueOf(Integer.toHexString(Integer.valueOf(frequency_point_text_1))), 2);
                    String frequency_point_2 = addZeroForNum(String.valueOf(Integer.toHexString(Integer.valueOf(frequency_point_text_2))), 2);
                    String frequency_point_3 = addZeroForNum(String.valueOf(Integer.toHexString(Integer.valueOf(frequency_point_text_3))), 2);
                    tx = "6854111111110011110705701C" + frequency_point_1 + frequency_point_2 + frequency_point_3;
                    tx = tx + AnalysisUtils.getCSSum(tx, 0) + "16";
                    BluetoothToolsMainActivity.data = "";
                    BluetoothToolsMainActivity.writeData(tx.toUpperCase());
                    break;
                case R.id.btn_set_datum_point:
                    //修改基准频率
                    String reference_frequency = et_reference_frequency.getText().toString();
                    if (TextUtils.isEmpty(reference_frequency)) {
                        //如果没输入则取默认值
                        reference_frequency = et_reference_frequency.getHint().toString();
                    } else {
                        //不满8位补全8位
                        reference_frequency = addZeroForNum(reference_frequency, 8);
                    }
                    tx = "6854111111110011110706801D" + reference_frequency;
                    tx = tx + AnalysisUtils.getCSSum(tx, 0) + "16";
                    BluetoothToolsMainActivity.data = "";
                    BluetoothToolsMainActivity.writeData(tx);
                    break;
                case R.id.btn_set_power:
                    //修改功率
                    String power = et_power.getText().toString();
                    if (TextUtils.isEmpty(power)) {
                        //如果没输入则取默认值
                        power = et_power.getHint().toString();
                    } else {
                        //补全2位
                        power = addZeroForNum(power, 2);
                    }
                    tx = "6854111111110011110603601B" + power;
                    tx = tx + AnalysisUtils.getCSSum(tx, 0) + "16";
                    BluetoothToolsMainActivity.data = "";
                    BluetoothToolsMainActivity.writeData(tx);
                    break;
                default:
                    break;
            }
        }
    };

    //位数不够补零
    public static String addZeroForNum(String str, int strLength) {
        int strLen = str.length();
        if (strLen < strLength) {
            while (strLen < strLength) {
                StringBuilder sb = new StringBuilder(str);
                sb.insert(0, "0");//左补0
//		    	sb.insert(strLen,"0");//右补0
                str = sb.toString();
                strLen = str.length();
            }
        }
        return str;
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

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            myFinish();
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //移除软键盘弹出收回监听（解决隐藏状态栏以及导航栏导致和软件盘冲突的解决所添加的监听）
        if (onGlobalLayoutListener != null) {
            getWindow().getDecorView().getViewTreeObserver().removeOnGlobalLayoutListener(onGlobalLayoutListener);
        }
    }
}

