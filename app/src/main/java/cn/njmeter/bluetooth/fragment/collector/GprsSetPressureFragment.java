package cn.njmeter.bluetooth.fragment.collector;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.njmeter.bluetooth.R;
import cn.njmeter.bluetooth.activity.bluetoothtools.BluetoothToolsMainActivity;
import cn.njmeter.bluetooth.fragment.BaseFragment;
import cn.njmeter.bluetooth.interfaces.OnMultiClickListener;
import cn.njmeter.bluetooth.utils.AnalysisUtils;
import cn.njmeter.bluetooth.utils.CommonUtils;

public class GprsSetPressureFragment extends BaseFragment implements View.OnClickListener {

    @BindView(R.id.et_set_common_pressure)
    EditText etSetCommonPressure;
    @BindView(R.id.btn_read_pressure_calibration)
    Button btnReadPressureCalibration;
    @BindView(R.id.btn_set_zero_point_calibration)
    Button btnSetZeroPointCalibration;
    @BindView(R.id.btn_set_normal_point_calibration)
    Button btnSetNormalPointCalibration;

    private Context context;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_gprs_set_pressure, container, false);
        ButterKnife.bind(this, view);
        context = getContext();
        initView(view);
        initData();
        return view;
    }

    @Override
    public void initView(View view) {
        etSetCommonPressure.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //限制小数点后面最多两位
                if (s.toString().contains(".")) {
                    //限制小数位数不大于2个
                    if (s.length() - 1 - s.toString().indexOf(".") > 2) {
                        s = s.toString().subSequence(0, s.toString().indexOf(".") + 3);
                        etSetCommonPressure.setText(s);
                        //设置光标在末尾
                        etSetCommonPressure.setSelection(s.length());
                        CommonUtils.showToast(context, "最多两位小数");
                    }
                    //如果输入的值超过范围（0.00＜X≤10.00）弹出提示
                    if (s.length() - 1 - s.toString().indexOf(".") == 1 || s.length() - 1 - s.toString().indexOf(".") == 2) {
                        if (etSetCommonPressure.getText().toString().equals("0.00") || Float.valueOf(etSetCommonPressure.getText().toString()) > 10.00f) {
                            //去掉刚输入的数字
                            s = s.toString().subSequence(0, s.length() - 1);
                            etSetCommonPressure.setText(s);
                            //设置光标在末尾
                            etSetCommonPressure.setSelection(s.length());
                            CommonUtils.showToast(context, "设定范围0.00＜X≤10.00");
                        }
                    }
                } else if (!TextUtils.isEmpty(s.toString())) {
                    if (Integer.valueOf(etSetCommonPressure.getText().toString()) > 10) {
                        s = s.toString().subSequence(0, s.length() - 1);
                        etSetCommonPressure.setText(s);
                        //设置光标在末尾
                        etSetCommonPressure.setSelection(s.length());
                        CommonUtils.showToast(context, "设定范围0.00＜X≤10.00");
                    }
                }
                //如果直接输入小数点，前面自动补0
                if (s.toString().trim().equals(".")) {
                    s = "0" + s;
                    etSetCommonPressure.setText(s);
                    etSetCommonPressure.setSelection(2);
                }
                //除了小数开头不能为0，而且开头不允许连续出现0
                if (s.toString().startsWith("0") && s.toString().trim().length() > 1) {
                    if (!s.toString().substring(1, 2).equals(".")) {
                        etSetCommonPressure.setText(s.subSequence(0, 1));
                        etSetCommonPressure.setSelection(1);
                    }
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        //读取压力标定
        btnReadPressureCalibration.setOnClickListener(new OnMultiClickListener() {
            @Override
            public void onMultiClick(View v) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("7B8900203030303030303030303030");
                StringBuilder sb = new StringBuilder();
                sb.append("6848111111110011113B03805500");
                int checksum = getCheckSum(sb.toString());
                String cs = Integer.toHexString(checksum);
                sb.append(cs.substring(cs.length() - 2));
                sb.append("167B");
                stringBuilder.append(sb.toString());
                BluetoothToolsMainActivity.data = "";
                String tx = stringBuilder.toString().toUpperCase().replace(" ", "");
                BluetoothToolsMainActivity.writeData(tx);
            }
        });

        //零点标定
        btnSetZeroPointCalibration.setOnClickListener(new OnMultiClickListener() {
            @Override
            public void onMultiClick(View v) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("7B8900243030303030303030303030");
                StringBuilder sb = new StringBuilder();
                sb.append("6848111111110011113B07805400");
                sb.append("00000000");
                int checksum = getCheckSum(sb.toString());
                String cs = Integer.toHexString(checksum);
                sb.append(cs.substring(cs.length() - 2));
                sb.append("167B");
                stringBuilder.append(sb.toString());
                BluetoothToolsMainActivity.data = "";
                String tx = stringBuilder.toString().toUpperCase().replace(" ", "");
                BluetoothToolsMainActivity.writeData(tx);
            }
        });

        //常用点标定
        btnSetNormalPointCalibration.setOnClickListener(new OnMultiClickListener() {
            @Override
            public void onMultiClick(View v) {
                if (TextUtils.isEmpty(etSetCommonPressure.getText())) {
                    CommonUtils.showToast(context, "请先输入设定值");
                    return;
                } else if (etSetCommonPressure.getText().toString().contains(".")) {
                    if (etSetCommonPressure.getText().toString().length() - 1 - etSetCommonPressure.getText().toString().indexOf(".") != 2) {
                        CommonUtils.showToast(context, "设定值必须为两位小数");
                        return;
                    }
                } else {
                    CommonUtils.showToast(context, "设定值必须为两位小数");
                    return;
                }
                int normal_point_calibration = Math.round(Float.valueOf(etSetCommonPressure.getText().toString()) * 100000);
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("7B8900243030303030303030303030");
                StringBuilder sb = new StringBuilder();
                sb.append("6848111111110011113B07805401");
                sb.append(addZeroForNum(String.valueOf(Integer.toHexString(normal_point_calibration)), 8));
                int checksum = getCheckSum(sb.toString());
                String cs = Integer.toHexString(checksum);
                sb.append(cs.substring(cs.length() - 2));
                sb.append("167B");
                stringBuilder.append(sb.toString());
                BluetoothToolsMainActivity.data = "";
                String tx = stringBuilder.toString().toUpperCase().replace(" ", "");
                BluetoothToolsMainActivity.writeData(tx);
            }
        });

    }

    public int getCheckSum(String param) {
        int res = 0;
        for (int i = 0; i < param.length() / 2; i++) {
            res += AnalysisUtils.HexS2ToInt(param.substring(i * 2, i * 2 + 2));
        }
        return res;
    }

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

}