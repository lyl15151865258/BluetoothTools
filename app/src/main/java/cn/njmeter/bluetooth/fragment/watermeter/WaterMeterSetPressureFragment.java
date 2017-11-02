package cn.njmeter.bluetooth.fragment.watermeter;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import cn.njmeter.bluetooth.R;
import cn.njmeter.bluetooth.activity.BluetoothToolsMainActivity;
import cn.njmeter.bluetooth.constant.Constant;
import cn.njmeter.bluetooth.constant.ProductType;
import cn.njmeter.bluetooth.fragment.BaseFragment;
import cn.njmeter.bluetooth.utils.AnalysisUtils;
import cn.njmeter.bluetooth.utils.CommonUtils;
import cn.njmeter.bluetooth.utils.MathUtils;

/**
 * 水表压力标定页面
 * Created by LiYuliang on 2017/11/01 0001.
 *
 * @author LiYuliang
 * @version 2017/11/01
 */

public class WaterMeterSetPressureFragment extends BaseFragment implements View.OnClickListener {

    private EditText etMeterId, etSetCommonPressure;
    private Context context;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_water_meter_set_pressure, container, false);
        context = getContext();
        initView(view);
        return view;
    }

    @Override
    public void initView(View view) {
        etMeterId = view.findViewById(R.id.et_meterId);
        etSetCommonPressure = view.findViewById(R.id.et_set_common_pressure);
        etSetCommonPressure.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //限制小数点后面最多两位
                if (s.toString().contains(Constant.POINT)) {
                    //限制小数位数不大于2个
                    if (s.length() - 1 - s.toString().indexOf(Constant.POINT) > 2) {
                        s = s.toString().subSequence(0, s.toString().indexOf(".") + 3);
                        etSetCommonPressure.setText(s);
                        //设置光标在末尾
                        etSetCommonPressure.setSelection(s.length());
                        CommonUtils.showToast(context, "最多两位小数");
                    }
                    //如果输入的值超过范围（0.00＜X≤10.00）弹出提示
                    if (s.length() - 1 - s.toString().indexOf(Constant.POINT) == 1 || s.length() - 1 - s.toString().indexOf(Constant.POINT) == 2) {
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
                if (s.toString().trim().equals(Constant.POINT)) {
                    s = "0" + s;
                    etSetCommonPressure.setText(s);
                    etSetCommonPressure.setSelection(2);
                }
                //除了小数开头不能为0，而且开头不允许连续出现0
                if (s.toString().startsWith("0") && s.toString().trim().length() > 1) {
                    if (!s.toString().substring(1, 2).equals(Constant.POINT)) {
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
        (view.findViewById(R.id.btn_read_pressure_calibration)).setOnClickListener(onClickListener);
        //零点标定
        (view.findViewById(R.id.btn_set_zero_point_calibration)).setOnClickListener(onClickListener);
        //常用点标定
        (view.findViewById(R.id.btn_set_normal_point_calibration)).setOnClickListener(onClickListener);
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String meterId = etMeterId.getText().toString().trim();
            if (TextUtils.isEmpty(meterId)) {
                showToast("请输入表号");
                return;
            } else if (meterId.length() != Constant.METER_ID_LENGTH) {
                showToast("请输入正确的8位表号");
                return;
            }
            String number, checkCode, command;
            switch (v.getId()) {
                case R.id.btn_read_pressure_calibration:
                    number = "68" + ProductType.WATER_METER + meterId + "0011113B03805500";
                    checkCode = AnalysisUtils.getCSSum(number, 0);
                    command = number + checkCode + "16";
                    BluetoothToolsMainActivity.writeData(command);
                    break;
                case R.id.btn_set_zero_point_calibration:
                    //零点标定
                    number = "68" + ProductType.WATER_METER + meterId + "0011113B07805400" + "00000000";
                    checkCode = AnalysisUtils.getCSSum(number, 0);
                    command = number + checkCode + "16";
                    BluetoothToolsMainActivity.writeData(command);
                    break;
                case R.id.btn_set_normal_point_calibration:
                    //常用点标定
                    if (TextUtils.isEmpty(etSetCommonPressure.getText())) {
                        CommonUtils.showToast(context, "请先输入设定值");
                        return;
                    } else if (etSetCommonPressure.getText().toString().contains(Constant.POINT)) {
                        if (etSetCommonPressure.getText().toString().length() - 1 - etSetCommonPressure.getText().toString().indexOf(Constant.POINT) != 2) {
                            CommonUtils.showToast(context, "设定值必须为两位小数");
                            return;
                        }
                    } else {
                        CommonUtils.showToast(context, "设定值必须为两位小数");
                        return;
                    }
                    int normalPointCalibration = Math.round(Float.valueOf(etSetCommonPressure.getText().toString()) * 100000);
                    number = "68" + ProductType.WATER_METER + meterId + "0011113B07805401" + MathUtils.addZeroForLeft(String.valueOf(Integer.toHexString(normalPointCalibration)), 8);
                    checkCode = AnalysisUtils.getCSSum(number, 0);
                    command = number + checkCode + "16";
                    BluetoothToolsMainActivity.writeData(command);
                    break;
                default:
                    break;
            }
        }
    };

}
